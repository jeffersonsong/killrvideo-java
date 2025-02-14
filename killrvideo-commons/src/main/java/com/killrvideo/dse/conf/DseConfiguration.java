package com.killrvideo.dse.conf;

import com.datastax.dse.driver.api.core.graph.DseGraph;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.killrvideo.discovery.ServiceDiscoveryDao;
import com.killrvideo.dse.graph.KillrVideoTraversalSource;
import com.killrvideo.model.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Configuration
public class DseConfiguration {

    /**
     * Internal logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DseConfiguration.class);

    @Value("${killrvideo.discovery.service.cassandra: cassandra}")
    private String cassandraServiceName;

    @Value("${killrvideo.cassandra.clustername: 'killrvideo'}")
    public String dseClustOerName;

    @Value("${killrvideo.graph.timeout: 30000}")
    public Integer graphTimeout;

    @Value("${killrvideo.graph.recommendation.name: 'killrvideo_video_recommendations'}")
    public String graphRecommendationName;

    @Value("#{environment.KILLRVIDEO_DSE_USERNAME}")
    public Optional<String> dseUsername;

    @Value("#{environment.KILLRVIDEO_DSE_PASSWORD}")
    public Optional<String> dsePassword;

    @Value("${killrvideo.cassandra.maxNumberOfTries: 50}")
    private Integer maxNumberOfTries;

    @Value("#{environment.KILLRVIDEO_MAX_NUMBER_RETRY}")
    private Optional<Integer> maxNumberOfTriesFromEnvVar;

    @Value("${killrvideo.cassandra.delayBetweenTries: 3}")
    private Integer delayBetweenTries;

    @Value("#{environment.KILLRVIDEO_DELAY_BETWEEN_RETRY}")
    private Optional<Integer> delayBetweenTriesFromEnvVar;

    @Value("${killrvideo.ssl.CACertFileLocation: cassandra.cert}")
    private String sslCACertFileLocation;

    @Value("#{environment.KILLRVIDEO_SSL_CERTIFICATE}")
    public Optional<String> sslCACertFileLocationEnvVar;

    @Value("${killrvideo.ssl.enable: false}")
    public boolean dseEnableSSL = false;

    @Value("#{environment.KILLRVIDEO_ENABLE_SSL}")
    public Optional<Boolean> dseEnableSSLEnvVar;

    @Value("${killrvideo.etcd.enabled : true}")
    private final boolean etcdLookup = false;

    @Inject
    private ServiceDiscoveryDao discoveryDao;

    @Bean
    public CqlSession initializeCassandra() {
        long top = System.currentTimeMillis();
        LOGGER.info("Initializing connection to Cassandra...");

        CqlSessionBuilder clusterConfig = CqlSession.builder();
        populateContactPoints(clusterConfig);
        populateAuthentication(clusterConfig);
        populateSSL(clusterConfig);

        final AtomicInteger atomicCount = new AtomicInteger(1);
        Callable<CqlSession> connectionToCassandra = () -> clusterConfig.withKeyspace(CommonConstants.KILLRVIDEO_KEYSPACE).build();

        maxNumberOfTriesFromEnvVar.ifPresent(integer -> maxNumberOfTries = integer);

        delayBetweenTriesFromEnvVar.ifPresent(integer -> delayBetweenTries = integer);

        // Connecting to DSE with a retry mechanism :
        // In docker deployments we may have to wait until all components are up and running.
        RetryConfig config = new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(maxNumberOfTries)
                .withDelayBetweenTries(delayBetweenTries, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        return new CallExecutor<CqlSession>(config)
                .afterFailedTry(s -> LOGGER.info("Attempt #{}/{} failed.. trying in {} seconds, waiting for Cassandra to start...", atomicCount.getAndIncrement(),
                        maxNumberOfTries, delayBetweenTries))
                .onFailure(s -> {
                    LOGGER.error("Cannot connection to Cassandra after {} attempts, exiting", maxNumberOfTries);
                    System.err.println("Can not connect to Cassandra after " + maxNumberOfTries + " attempts, exiting");
                    System.exit(500);
                })
                .onSuccess(s -> {
                    long timeElapsed = System.currentTimeMillis() - top;
                    LOGGER.info("[OK] Connection established to Cassandra Cluster in {} millis.", timeElapsed);
                })
                .execute(connectionToCassandra).getResult();
    }

    /**
     * Graph Traversal for suggested videos.
     *
     * @return traversal
     */
    @Bean
    public KillrVideoTraversalSource initializeGraphTraversalSource() {
        return DseGraph.g.getGraph().traversal(KillrVideoTraversalSource.class);
    }

    /**
     * Retrieve cluster nodes adresses (eg:node1:9042) from ETCD and initialize the `contact points`,
     * endpoints of Cassandra cluster nodes.
     *
     * @param clusterConfig current configuration
     * @note [Initializing Contact Points with Java Driver]
     * <p>
     * (1) The default port is 9042. If you keep using the default port you
     * do not need to use or `withPort()` or `addContactPointsWithPorts()`, only `addContactPoint()`.
     * <p>
     * (2) Best practice is to use the SAME PORT for each node and to  setup the port through `withPort()`.
     * <p>
     * (3) Never, ever use `addContactPointsWithPorts` in clusters : it will ony set port FOR THE FIRST NODE.
     * DON'T USE, EVEN IF ALL NODE USE SAME PORT. It purpose is only for tests and standalone servers.
     * <p>
     * (4) What if I have a cluster and nodes do not use the same port (eg: node1:9043, node2:9044, node3:9045) ?
     * You need to use {@link AddressTranslator} as defined below and reference with `withAddressTranslator(translator);`
     *
     * <code>
     * public class MyClusterAddressTranslator implements AddressTranslator {
     * @Override public void init(Cluster cluster) {}
     * @Override public void close() {}
     * @Override public InetSocketAddress translate(InetSocketAddress incoming) {
     * // Given the configuration
     * Map<String, Integer> clusterNodes = new HashMap<String, Integer>() { {
     * put("node1", 9043);put("node2", 9044);put("node3", 9045);
     * }};
     * String targetHostName = incoming.getHostName();
     * if (clusterNodes.containsKey(targetHostName)) {
     * return new InetSocketAddress(targetHostName, clusterNodes.get(targetHostName));
     * }
     * throw new IllegalArgumentException("Cannot translate URL " + incoming + " hostName not found");
     * }
     * }
     * </code>
     */
    private void populateContactPoints(CqlSessionBuilder clusterConfig) {
        discoveryDao.lookup(cassandraServiceName).stream()
                .map(this::asSocketInetAdress)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(clusterConfig::addContactPoint);
    }

    /**
     * Convert information in ETCD as real adress {@link InetSocketAddress} if possible.
     *
     * @param contactPoint network node adress information like hostname:port
     * @return java formatted inet adress
     */
    private Optional<InetSocketAddress> asSocketInetAdress(String contactPoint) {
        Optional<InetSocketAddress> target = Optional.empty();
        try {
            if (contactPoint != null && contactPoint.length() > 0) {
                String[] chunks = contactPoint.split(":");
                if (chunks.length == 2) {
                    LOGGER.info(" + Adding node '{}' to the Cassandra cluster definition", contactPoint);
                    return Optional.of(new InetSocketAddress(InetAddress.getByName(chunks[0]), Integer.parseInt(chunks[1])));
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.warn(" + Cannot read contactPoint - "
                    + "Invalid Port Numer, entry '" + contactPoint + "' will be ignored", e);
        } catch (UnknownHostException e) {
            LOGGER.warn(" + Cannot read contactPoint - "
                    + "Invalid Hostname, entry '" + contactPoint + "' will be ignored", e);
        }
        return target;
    }

    /**
     * Check to see if we have username and password from the environment
     * This is here because we have a dual use scenario.  One for developers and others
     * who download KillrVideo and run within a local Docker container and the other
     * who might need (like us for example) to connect KillrVideo up to an external
     * cluster that requires authentication.
     */
    private void populateAuthentication(CqlSessionBuilder clusterConfig) {
        if (dseUsername.isPresent() && dsePassword.isPresent()
                && dseUsername.get().length() > 0) {
            clusterConfig.withAuthCredentials(dseUsername.get(), dsePassword.get());
            String obfuscatedPassword = new String(new char[dsePassword.get().length()]).replace("\0", "*");
            LOGGER.info(" + Using supplied DSE username: '{}' and password: '{}' from environment variables",
                    dseUsername.get(), obfuscatedPassword);
        } else {
            LOGGER.info(" + Connection is not authenticated (no username/password)");
        }
    }

    /**
     * If SSL is enabled use the supplied CA cert file to create
     * an SSL context and use to configure our cluster.
     *
     * @param clusterConfig current configuration
     */
    private void populateSSL(CqlSessionBuilder clusterConfig) {

        // Reading Environment Variables to eventually override default config
        if (dseEnableSSLEnvVar.isPresent()) {
            dseEnableSSL = dseEnableSSLEnvVar.get();
            sslCACertFileLocationEnvVar.ifPresent(s -> sslCACertFileLocation = s);
        }

        if (dseEnableSSL) {
            LOGGER.info(" + SSL is enabled, using supplied SSL certificate: '{}'", sslCACertFileLocation);
            try {
                // X509 Certificate
                FileInputStream fis = new FileInputStream(sslCACertFileLocation);
                X509Certificate caCert = (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(new BufferedInputStream(fis));

                // KeyStore
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, null);
                ks.setCertificateEntry(Integer.toString(1), caCert);

                // TrustStore
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                clusterConfig.withSslContext(sslContext);

            } catch (FileNotFoundException fne) {
                String errMsg = "SSL cert file not found. You must provide a valid certification file when using SSL encryption option.";
                LOGGER.error(errMsg, fne);
                throw new IllegalArgumentException(errMsg, fne);

            } catch (CertificateException ce) {
                String errCert = "Your CA certificate looks invalid. You must provide a valid certification file when using SSL encryption option.";
                LOGGER.error(errCert, ce);

                throw new IllegalArgumentException(errCert, ce);
            } catch (Exception e) {
                String errSsl = "Exception in SSL configuration: ";
                LOGGER.error(errSsl, e);
                throw new IllegalArgumentException(errSsl, e);
            }
        } else {
            LOGGER.info(" + SSL encryption is not enabled)");
        }
    }
}
