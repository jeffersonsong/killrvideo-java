package com.killrvideo.discovery

import com.evanlennick.retry4j.CallExecutor
import com.evanlennick.retry4j.Status
import com.evanlennick.retry4j.config.RetryConfigBuilder
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.discovery.ServiceDiscoveryDaoEtcd
import com.xqbase.etcd4j.EtcdClient
import com.xqbase.etcd4j.EtcdClientException
import com.xqbase.etcd4j.EtcdNode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URI
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import javax.annotation.PostConstruct

/**
 * Hanle operation arount ETCD (connection, read, write).
 *
 * @author DataStax Developer Advocates Team
 */
@Component("killrvideo.discovery.etcd")
@Profile(KillrVideoConfiguration.PROFILE_DISCOVERY_ETCD)
class ServiceDiscoveryDaoEtcd : ServiceDiscoveryDao {
    @Value("\${killrvideo.discovery.etcd.host: 10.0.75.1}")
    private val etcdServerHost: String? = null

    @Value("\${killrvideo.discovery.etcd.port: 2379}")
    private val etcdServerPort = 0

    @Value("\${killrvideo.discovery.etcd.maxNumberOfTries: 10}")
    private val maxNumberOfTriesEtcd = 0

    @Value("\${killrvideo.discovery.etcd.delayBetweenTries: 2}")
    private val delayBetweenTriesEtcd = 0

    /** Native client.  */
    private var etcdClient: EtcdClient? = null
    @PostConstruct
    fun connect() {
        val etcdUrl = String.format("http://%s:%d", etcdServerHost, etcdServerPort)
        LOGGER.info("Initialize connection to ETCD Server:")
        LOGGER.info(" + Connecting to '{}'", etcdUrl)
        etcdClient = EtcdClient(URI.create(etcdUrl))
        waitForEtcd()
        LOGGER.info(" + [OK] Connection established.")
    }

    /**
     * Read from ETCD using a retry mecanism.
     *
     * @return
     */
    private fun waitForEtcd() {
        val atomicCount = AtomicInteger(1)
        val getKeyFromEtcd = Callable<List<EtcdNode>> {
            try {
                val nodes = etcdClient!!.listDir("/")
                check(!(nodes == null || nodes.isEmpty())) { "/ is required in ETCD but not yet present" }
                nodes
            } catch (e: EtcdClientException) {
                throw IllegalStateException("Cannot Access ETCD Server : " + e.message)
            }
        }
        val etcdRetryConfig = RetryConfigBuilder()
            .retryOnAnyException()
            .withMaxNumberOfTries(maxNumberOfTriesEtcd)
            .withDelayBetweenTries(delayBetweenTriesEtcd.toLong(), ChronoUnit.SECONDS)
            .withFixedBackoff()
            .build()
        CallExecutor<List<EtcdNode>>(etcdRetryConfig)
            .afterFailedTry { s: Status<*>? ->
                LOGGER.info(
                    "Attempt #{}/{} : ETCD is not ready (retry in {}s)",
                    atomicCount.getAndIncrement(), maxNumberOfTriesEtcd, delayBetweenTriesEtcd
                )
            }
            .onFailure { s: Status<*>? ->
                LOGGER.error("ETCD is not ready after {} attempts, exiting", maxNumberOfTriesEtcd)
                System.err.println("ETCD is not ready after $maxNumberOfTriesEtcd attempts, exiting now.")
                System.exit(500)
            }
            .execute(getKeyFromEtcd).result
            .stream().map { node: EtcdNode -> node.value }
            .collect(Collectors.toList())
    }

    /**
     * Give a service name like 'CommentServices' look for Directory at namespace killrvideo
     * and list value (keys are generated)
     *
     * @param serviceName
     * unique service name
     * @return
     * list of values
     */
    override fun lookup(serviceName: String): List<String> {
        var endPointList: List<String> = ArrayList()
        val serviceDirectoryKey = KILLRVIDEO_SERVICE_NAMESPACE + serviceName + "/"
        LOGGER.info(" List endpoints for key '{}':", serviceDirectoryKey)
        try {
            val existingNodes = etcdClient!!.listDir(serviceDirectoryKey)
            if (existingNodes != null) {
                endPointList = existingNodes
                    .stream()
                    .map { node: EtcdNode -> node.value }
                    .collect(Collectors.toList())
            }
        } catch (e: EtcdClientException) {
        }
        LOGGER.info(" + [OK] Endpoints retrieved '{}':", endPointList)
        return endPointList
    }

    /**
     * Given a servicename and a host give the latest port if exist. This will be used for
     *
     * @param serviceName
     * target service
     * @return
     */
    @Synchronized
    fun lookupServicePorts(serviceName: String, hostName: String): Optional<Int> {
        var targetPort = -1
        LOGGER.info("Accessing last port for endpoint with same host")
        for (endpoint in lookup(serviceName)) {
            val endpointChunks = endpoint.split(":").toTypedArray()
            val endPointPort = endpointChunks[1].toInt()
            val endPointHost = endpointChunks[0]
            if (hostName.equals(endPointHost, ignoreCase = true)) {
                if (endPointPort > targetPort) {
                    targetPort = endPointPort
                    LOGGER.info(" + Found {}", targetPort)
                }
            }
        }
        return if (targetPort == -1) Optional.empty() else Optional.of(targetPort)
    }

    /** {@inheritDoc}  */
    override fun register(serviceName: String, hostName: String, portNumber: Int): String {
        val serviceDirectoryKey = KILLRVIDEO_SERVICE_NAMESPACE + serviceName.trim { it <= ' ' } + "/"
        val endPoint = "$hostName:$portNumber"
        return try {
            try {
                LOGGER.info("Register endpoint '{}' for key '{}':", endPoint, serviceDirectoryKey)
                etcdClient!!.createDir(serviceDirectoryKey, null, false)
                LOGGER.info(" + Dir '{}' has been created", serviceDirectoryKey)
            } catch (e: EtcdClientException) {
                LOGGER.info(" + Dir '{}' already exist", serviceDirectoryKey)
            }
            val existingNodes = etcdClient!!.listDir(serviceDirectoryKey)
            if (existingNodes != null) {
                val existingEndpoint = existingNodes
                    .stream().filter { p: EtcdNode -> p.value.equals(endPoint, ignoreCase = true) }
                    .findFirst()
                // Return existing key
                if (existingEndpoint.isPresent) {
                    LOGGER.info(" + [OK] Endpoint '{}' already exist", endPoint)
                    return existingEndpoint.get().key
                }
            }
            // Create new Key
            val serviceKey = serviceDirectoryKey + UUID.randomUUID()
            etcdClient!![serviceKey] = endPoint
            LOGGER.info(" + [OK] Endpoint registered with key '{}'", serviceKey)
            serviceKey
        } catch (e: EtcdClientException) {
            throw IllegalStateException("Cannot register services into ETCD", e)
        }
    }

    /** {@inheritDoc}  */
    override fun unregisterEndpoint(serviceName: String, hostName: String, portNumber: Int) {
        val serviceDirectoryKey = KILLRVIDEO_SERVICE_NAMESPACE + serviceName + "/"
        val endPoint = "$hostName:$portNumber"
        try {
            LOGGER.info("Unregister endpoint '{}' for key '{}':", endPoint, serviceDirectoryKey)
            val existingNodes = etcdClient!!.listDir(serviceDirectoryKey)
            var existingEndpoint = Optional.empty<EtcdNode>()
            if (existingNodes != null) {
                existingEndpoint = existingNodes
                    .stream().filter { p: EtcdNode -> p.value.equals(endPoint, ignoreCase = true) }
                    .findFirst()
            }
            if (existingEndpoint.isPresent) {
                etcdClient!!.delete(existingEndpoint.get().key)
                LOGGER.info(" + [OK] Endpoint has been deleted (key={})", existingEndpoint.get().key)
            } else {
                LOGGER.info(" + [OK] This endpoint does not exist")
            }
        } catch (e: EtcdClientException) {
            throw IllegalStateException("Cannot register services into ETCD", e)
        }
    }

    /** {@inheritDoc}  */
    override fun unregister(serviceName: String) {
        val serviceDirectoryKey = KILLRVIDEO_SERVICE_NAMESPACE + serviceName + "/"
        try {
            LOGGER.info("Delete dir  '{}'", serviceDirectoryKey)
            etcdClient!!.deleteDir("/killrvideo/services/$serviceName", true)
            LOGGER.info(" + [OK] Directory has been deleted")
        } catch (e: EtcdClientException) {
            LOGGER.info(" + [OK] Directory did not exist")
        }
    }

    companion object {
        /** Initialize dedicated connection to ETCD system.  */
        private val LOGGER = LoggerFactory.getLogger(ServiceDiscoveryDaoEtcd::class.java)

        /** Namespace.  */
        const val KILLRVIDEO_SERVICE_NAMESPACE = "/killrvideo/services/"
    }
}
