package com.killrvideo.dse.conf

import com.datastax.dse.driver.api.core.graph.DseGraph
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.CqlSessionBuilder
import com.evanlennick.retry4j.CallExecutor
import com.evanlennick.retry4j.Status
import com.evanlennick.retry4j.config.RetryConfigBuilder
import com.killrvideo.discovery.ServiceDiscoveryDao
import com.killrvideo.dse.graph.KillrVideoTraversalSource
import com.killrvideo.model.CommonConstants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@Configuration
open class DseConfiguration {
    @Value("\${killrvideo.discovery.service.cassandra: cassandra}")
    private val cassandraServiceName: String? = null

    @Value("\${killrvideo.cassandra.clustername: 'killrvideo'}")
    var dseClustOerName: String? = null

    @Value("\${killrvideo.graph.timeout: 30000}")
    var graphTimeout: Int? = null

    @Value("\${killrvideo.graph.recommendation.name: 'killrvideo_video_recommendations'}")
    var graphRecommendationName: String? = null

    @Value("#{environment.KILLRVIDEO_DSE_USERNAME}")
    var dseUsername: Optional<String>? = null

    @Value("#{environment.KILLRVIDEO_DSE_PASSWORD}")
    var dsePassword: Optional<String>? = null

    @Value("\${killrvideo.cassandra.maxNumberOfTries: 50}")
    private var maxNumberOfTries: Int? = null

    @Value("#{environment.KILLRVIDEO_MAX_NUMBER_RETRY}")
    private val maxNumberOfTriesFromEnvVar: Optional<Int>? = null

    @Value("\${killrvideo.cassandra.delayBetweenTries: 3}")
    private var delayBetweenTries: Int? = null

    @Value("#{environment.KILLRVIDEO_DELAY_BETWEEN_RETRY}")
    private val delayBetweenTriesFromEnvVar: Optional<Int>? = null

    @Value("\${killrvideo.ssl.CACertFileLocation: cassandra.cert}")
    private var sslCACertFileLocation: String? = null

    @Value("#{environment.KILLRVIDEO_SSL_CERTIFICATE}")
    var sslCACertFileLocationEnvVar: Optional<String>? = null

    @Value("\${killrvideo.ssl.enable: false}")
    var dseEnableSSL = false

    @Value("#{environment.KILLRVIDEO_ENABLE_SSL}")
    var dseEnableSSLEnvVar: Optional<Boolean>? = null

    @Value("\${killrvideo.etcd.enabled : true}")
    private val etcdLookup = false

    @Inject
    private val discoveryDao: ServiceDiscoveryDao? = null
    @Bean
    open fun initializeCassandra(): CqlSession {
        val top = System.currentTimeMillis()
        LOGGER.info("Initializing connection to Cassandra...")
        val clusterConfig = CqlSession.builder()
        populateContactPoints(clusterConfig)
        populateAuthentication(clusterConfig)
        populateSSL(clusterConfig)
        val atomicCount = AtomicInteger(1)
        val connectionToCassandra = Callable { clusterConfig.withKeyspace(CommonConstants.KILLRVIDEO_KEYSPACE).build() }
        maxNumberOfTriesFromEnvVar!!.ifPresent { integer: Int? -> maxNumberOfTries = integer }
        delayBetweenTriesFromEnvVar!!.ifPresent { integer: Int? -> delayBetweenTries = integer }

        // Connecting to DSE with a retry mechanism :
        // In docker deployments we may have to wait until all components are up and running.
        val config = RetryConfigBuilder()
            .retryOnAnyException()
            .withMaxNumberOfTries(maxNumberOfTries!!)
            .withDelayBetweenTries(delayBetweenTries!!.toLong(), ChronoUnit.SECONDS)
            .withFixedBackoff()
            .build()
        return CallExecutor<CqlSession>(config)
            .afterFailedTry { s: Status<*>? ->
                LOGGER.info(
                    "Attempt #{}/{} failed.. trying in {} seconds, waiting for Cassandra to start...",
                    atomicCount.getAndIncrement(),
                    maxNumberOfTries,
                    delayBetweenTries
                )
            }
            .onFailure { s: Status<*>? ->
                LOGGER.error("Cannot connection to Cassandra after {} attempts, exiting", maxNumberOfTries)
                System.err.println("Can not connect to Cassandra after $maxNumberOfTries attempts, exiting")
                System.exit(500)
            }
            .onSuccess { s: Status<*>? ->
                val timeElapsed = System.currentTimeMillis() - top
                LOGGER.info("[OK] Connection established to Cassandra Cluster in {} millis.", timeElapsed)
            }
            .execute(connectionToCassandra).result
    }

    /**
     * Graph Traversal for suggested videos.
     *
     * @return traversal
     */
    @Bean
    open fun initializeGraphTraversalSource(): KillrVideoTraversalSource {
        return DseGraph.g.graph.traversal(KillrVideoTraversalSource::class.java)
    }

    /**
     * Retrieve cluster nodes adresses (eg:node1:9042) from ETCD and initialize the `contact points`,
     * endpoints of Cassandra cluster nodes.
     *
     * @param clusterConfig current configuration
     * @note [Initializing Contact Points with Java Driver]
     *
     *
     * (1) The default port is 9042. If you keep using the default port you
     * do not need to use or `withPort()` or `addContactPointsWithPorts()`, only `addContactPoint()`.
     *
     *
     * (2) Best practice is to use the SAME PORT for each node and to  setup the port through `withPort()`.
     *
     *
     * (3) Never, ever use `addContactPointsWithPorts` in clusters : it will ony set port FOR THE FIRST NODE.
     * DON'T USE, EVEN IF ALL NODE USE SAME PORT. It purpose is only for tests and standalone servers.
     *
     *
     * (4) What if I have a cluster and nodes do not use the same port (eg: node1:9043, node2:9044, node3:9045) ?
     * You need to use [AddressTranslator] as defined below and reference with `withAddressTranslator(translator);`
     *
     * `
     * public class MyClusterAddressTranslator implements AddressTranslator {
     * @Override public void init(Cluster cluster) {}
     * @Override public void close() {}
     * @Override public InetSocketAddress translate(InetSocketAddress incoming) {
     * // Given the configuration
     * Map<String></String>, Integer> clusterNodes = new HashMap<String></String>, Integer>() { {
     * put("node1", 9043);put("node2", 9044);put("node3", 9045);
     * }};
     * String targetHostName = incoming.getHostName();
     * if (clusterNodes.containsKey(targetHostName)) {
     * return new InetSocketAddress(targetHostName, clusterNodes.get(targetHostName));
     * }
     * throw new IllegalArgumentException("Cannot translate URL " + incoming + " hostName not found");
     * }
     * }
    ` *
     */
    private fun populateContactPoints(clusterConfig: CqlSessionBuilder) {
        discoveryDao!!.lookup(cassandraServiceName!!).stream()
            .map { contactPoint: String? -> asSocketInetAdress(contactPoint) }
            .filter { obj: Optional<InetSocketAddress> -> obj.isPresent }
            .map { obj: Optional<InetSocketAddress> -> obj.get() }
            .forEach { contactPoint: InetSocketAddress? ->
                clusterConfig.addContactPoint(
                    contactPoint!!
                )
            }
    }

    /**
     * Convert information in ETCD as real adress [InetSocketAddress] if possible.
     *
     * @param contactPoint network node adress information like hostname:port
     * @return java formatted inet adress
     */
    private fun asSocketInetAdress(contactPoint: String?): Optional<InetSocketAddress> {
        val target: Optional<InetSocketAddress> = Optional.empty()
        try {
            if (contactPoint != null && contactPoint.isNotEmpty()) {
                val chunks = contactPoint.split(":").toTypedArray()
                if (chunks.size == 2) {
                    LOGGER.info(" + Adding node '{}' to the Cassandra cluster definition", contactPoint)
                    return Optional.of(InetSocketAddress(InetAddress.getByName(chunks[0]), chunks[1].toInt()))
                }
            }
        } catch (e: NumberFormatException) {
            LOGGER.warn(
                " + Cannot read contactPoint - "
                        + "Invalid Port Numer, entry '" + contactPoint + "' will be ignored", e
            )
        } catch (e: UnknownHostException) {
            LOGGER.warn(
                " + Cannot read contactPoint - "
                        + "Invalid Hostname, entry '" + contactPoint + "' will be ignored", e
            )
        }
        return target
    }

    /**
     * Check to see if we have username and password from the environment
     * This is here because we have a dual use scenario.  One for developers and others
     * who download KillrVideo and run within a local Docker container and the other
     * who might need (like us for example) to connect KillrVideo up to an external
     * cluster that requires authentication.
     */
    private fun populateAuthentication(clusterConfig: CqlSessionBuilder) {
        if (dseUsername!!.isPresent && dsePassword!!.isPresent
            && dseUsername!!.get().isNotEmpty()
        ) {
            clusterConfig.withAuthCredentials(dseUsername!!.get(), dsePassword!!.get())
            val obfuscatedPassword = String(CharArray(dsePassword!!.get().length)).replace("\u0000", "*")
            LOGGER.info(
                " + Using supplied DSE username: '{}' and password: '{}' from environment variables",
                dseUsername!!.get(), obfuscatedPassword
            )
        } else {
            LOGGER.info(" + Connection is not authenticated (no username/password)")
        }
    }

    /**
     * If SSL is enabled use the supplied CA cert file to create
     * an SSL context and use to configure our cluster.
     *
     * @param clusterConfig current configuration
     */
    private fun populateSSL(clusterConfig: CqlSessionBuilder) {

        // Reading Environment Variables to eventually override default config
        if (dseEnableSSLEnvVar!!.isPresent) {
            dseEnableSSL = dseEnableSSLEnvVar!!.get()
            sslCACertFileLocationEnvVar!!.ifPresent { s: String? -> sslCACertFileLocation = s }
        }
        if (dseEnableSSL) {
            LOGGER.info(" + SSL is enabled, using supplied SSL certificate: '{}'", sslCACertFileLocation)
            try {
                // X509 Certificate
                val fis = FileInputStream(sslCACertFileLocation)
                val caCert = CertificateFactory.getInstance("X.509")
                    .generateCertificate(BufferedInputStream(fis)) as X509Certificate

                // KeyStore
                val ks = KeyStore.getInstance(KeyStore.getDefaultType())
                ks.load(null, null)
                ks.setCertificateEntry(Integer.toString(1), caCert)

                // TrustStore
                val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                tmf.init(ks)
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, tmf.trustManagers, null)
                clusterConfig.withSslContext(sslContext)
            } catch (fne: FileNotFoundException) {
                val errMsg =
                    "SSL cert file not found. You must provide a valid certification file when using SSL encryption option."
                LOGGER.error(errMsg, fne)
                throw IllegalArgumentException(errMsg, fne)
            } catch (ce: CertificateException) {
                val errCert =
                    "Your CA certificate looks invalid. You must provide a valid certification file when using SSL encryption option."
                LOGGER.error(errCert, ce)
                throw IllegalArgumentException(errCert, ce)
            } catch (e: Exception) {
                val errSsl = "Exception in SSL configuration: "
                LOGGER.error(errSsl, e)
                throw IllegalArgumentException(errSsl, e)
            }
        } else {
            LOGGER.info(" + SSL encryption is not enabled)")
        }
    }

    companion object {
        /**
         * Internal logger.
         */
        private val LOGGER = LoggerFactory.getLogger(DseConfiguration::class.java)
    }
}
