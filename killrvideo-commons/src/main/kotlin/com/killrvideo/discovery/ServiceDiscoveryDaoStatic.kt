package com.killrvideo.discovery

import com.killrvideo.conf.KillrVideoConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*

/**
 * There is no explicit access to
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Component("killrvideo.discovery.network")
@Profile(KillrVideoConfiguration.PROFILE_DISCOVERY_STATIC)
class ServiceDiscoveryDaoStatic : ServiceDiscoveryDao {
    @Value("\${killrvideo.discovery.service.kafka: kafka}")
    private val kafkaServiceName: String? = null

    @Value("\${killrvideo.discovery.static.kafka.port: 8082}")
    private val kafkaPort = 0

    @Value("\${killrvideo.discovery.static.kafka.brokers}")
    private val kafkaBrokers: String? = null

    @Value("#{environment.KILLRVIDEO_KAFKA_BROKERS}")
    private val kafkaBrokersEnvVar: Optional<String>? = null

    @Value("\${killrvideo.discovery.service.cassandra: cassandra}")
    private val cassandraServiceName: String? = null

    @Value("\${killrvideo.discovery.static.cassandra.port: 9042}")
    private val cassandraPort = 0

    @Value("\${killrvideo.discovery.static.cassandra.contactPoints}")
    private var cassandraContactPoints: String? = null

    @Value("#{environment.KILLRVIDEO_DSE_CONTACT_POINTS}")
    private val cassandraContactPointsEnvVar: Optional<String>? = null

    /** {@inheritDoc}  */
    override fun lookup(serviceName: String): List<String> {
        val endPointList: MutableList<String> = ArrayList()
        LOGGER.info(" + Lookup for key '{}':", serviceName)
        if (kafkaServiceName.equals(serviceName, ignoreCase = true)) {
            if (kafkaBrokersEnvVar!!.isPresent && !kafkaBrokersEnvVar.get().isBlank()) {
                cassandraContactPoints = kafkaBrokersEnvVar.get()
                LOGGER.info(" + Reading broker from KILLRVIDEO_KAFKA_BROKERS")
            }
            Arrays.stream(kafkaBrokers!!.split(",").toTypedArray())
                .forEach { ip: String -> endPointList.add("$ip:$kafkaPort") }
        } else if (cassandraServiceName.equals(serviceName, ignoreCase = true)) {
            // Explicit overwriting of contact points from env var
            // Better than default spring : simpler
            if (cassandraContactPointsEnvVar!!.isPresent && !cassandraContactPointsEnvVar.get().isBlank()) {
                cassandraContactPoints = cassandraContactPointsEnvVar.get()
                LOGGER.info(" + Reading contactPoints from KILLRVIDEO_DSE_CONTACT_POINTS")
            }
            Arrays.stream(cassandraContactPoints!!.split(",").toTypedArray())
                .forEach { ip: String -> endPointList.add("$ip:$cassandraPort") }
        }
        LOGGER.info(" + Endpoints retrieved '{}':", endPointList)
        return endPointList
    }

    /** {@inheritDoc}  */
    override fun register(serviceName: String, hostName: String, portNumber: Int): String {
        // Do nothing in k8s service are registered through DNS
        return serviceName
    }

    /** {@inheritDoc}  */
    override fun unregister(serviceName: String) {}

    /** {@inheritDoc}  */
    override fun unregisterEndpoint(serviceName: String, hostName: String, portNumber: Int) {}

    companion object {
        /** Initialize dedicated connection to ETCD system.  */
        private val LOGGER = LoggerFactory.getLogger(ServiceDiscoveryDaoStatic::class.java)
    }
}
