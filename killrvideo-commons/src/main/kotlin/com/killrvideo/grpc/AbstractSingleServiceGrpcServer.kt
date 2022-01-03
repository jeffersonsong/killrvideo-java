package com.killrvideo.grpc

import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.discovery.ServiceDiscoveryDaoEtcd
import com.killrvideo.grpc.AbstractSingleServiceGrpcServer
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject

/**
 * Support class to build GRPC Server per service.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
abstract class AbstractSingleServiceGrpcServer {
    /** Global Configuration. s */
    @Inject
    protected var killrVideoConfig: KillrVideoConfiguration? = null

    /** Connectivity to ETCD Service discovery.  */
    @Inject
    protected var serviceDiscoveryDao: ServiceDiscoveryDaoEtcd? = null

    /** GRPC Server to start.  */
    protected var grpcServer: Server? = null

    /** Service Name.  */
    protected abstract val serviceName: String?

    /** Lock target port.  */
    protected abstract val defaultPort: Int

    /** Service Definition.  */
    protected abstract val service: ServerServiceDefinition?

    /**
     * Start
     */
    @PostConstruct
    @Throws(Exception::class)
    fun startGrpcServer() {
        LOGGER.info("Initializing Comment Service")
        grpcServerPort = defaultPort
        val maxUsedPort = serviceDiscoveryDao!!.lookupServicePorts(
            serviceName!!,
            killrVideoConfig!!.applicationHost
        )
        maxUsedPort.ifPresent { integer: Int -> grpcServerPort = integer + 1 }
        grpcServer = ServerBuilder.forPort(grpcServerPort)
            .addService(service)
            .build()
        Runtime.getRuntime().addShutdownHook(Thread { stopGrpcServer() })
        grpcServer!!.start()
        LOGGER.info("[OK] Grpc Server started on port: '{}'", grpcServerPort)
        serviceDiscoveryDao!!.register(
            serviceName!!,
            killrVideoConfig!!.applicationHost, grpcServerPort
        )
    }

    @PreDestroy
    fun stopGrpcServer() {
        LOGGER.info("Calling shutdown for GrpcServer")
        serviceDiscoveryDao!!.register(
            serviceName!!,
            killrVideoConfig!!.applicationHost, grpcServerPort
        )
        grpcServer!!.shutdown()
    }

    companion object {
        /** Some logger.  */
        private val LOGGER = LoggerFactory.getLogger(AbstractSingleServiceGrpcServer::class.java)

        /** Port to be allocated dynamically based on ETCD.  */
        protected var grpcServerPort = 0
    }
}
