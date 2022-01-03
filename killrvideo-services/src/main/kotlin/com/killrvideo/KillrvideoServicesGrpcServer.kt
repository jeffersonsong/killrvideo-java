package com.killrvideo

import com.killrvideo.KillrvideoServicesGrpcServer
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.discovery.ServiceDiscoveryDao
import com.killrvideo.service.comment.grpc.CommentsServiceGrpc
import com.killrvideo.service.rating.grpc.RatingsServiceGrpc
import com.killrvideo.service.search.grpc.SearchServiceGrpc
import com.killrvideo.service.statistic.grpc.StatisticsServiceGrpc
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpc
import com.killrvideo.service.user.grpc.UserManagementServiceGrpc
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpc
import io.grpc.Server
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject

/**
 * Startup a GRPC server on expected port and register all services.
 *
 * @author DataStax advocates team.
 */
@Component
class KillrvideoServicesGrpcServer {
    /** Listening Port for GRPC.  */
    @Value("\${killrvideo.grpc-server.port: 50101}")
    private val grpcPort = 0

    /** Connectivity to ETCD Service discovery.  */
    @Inject
    private val config: KillrVideoConfiguration? = null

    /** Connectivity to ETCD Service discovery.  */
    @Inject
    private val serviceDiscoveryDao: ServiceDiscoveryDao? = null

    @Inject
    private val commentService: CommentsServiceGrpc? = null

    @Value("\${killrvideo.services.comment: true}")
    private val commentServiceEnabled = true

    @Inject
    private val ratingService: RatingsServiceGrpc? = null

    @Value("\${killrvideo.services.rating: true}")
    private val ratingServiceEnabled = true

    @Inject
    private val searchService: SearchServiceGrpc? = null

    @Value("\${killrvideo.services.search: true}")
    private val searchServiceEnabled = true

    @Inject
    private val statisticsService: StatisticsServiceGrpc? = null

    @Value("\${killrvideo.services.statistic: true}")
    private val statisticServiceEnabled = true

    @Inject
    private val videoCatalogService: VideoCatalogServiceGrpc? = null

    @Value("\${killrvideo.services.videoCatalog: true}")
    private val videoCatalogServiceEnabled = true

    @Inject
    private val userService: UserManagementServiceGrpc? = null

    @Value("\${killrvideo.services.user: true}")
    private val userServiceEnabled = true

    @Inject
    private val suggestedVideosService: SuggestedVideosServiceGrpc? = null

    @Value("\${killrvideo.services.suggestedVideo: true}")
    private val suggestedVideoServiceEnabled = true

    /**
     * GRPC Server to set up.
     */
    private lateinit var grpcServer: Server

    @PostConstruct
    @Throws(Exception::class)
    fun start() {
        LOGGER.info("Initializing Grpc Server...")

        // Create GRPC server referencing only enabled services
        val builder = ServerBuilder.forPort(grpcPort)
        if (commentServiceEnabled) {
            builder.addService(commentService!!.bindService())
        }
        if (ratingServiceEnabled) {
            builder.addService(ratingService!!.bindService())
        }
        if (searchServiceEnabled) {
            builder.addService(searchService!!.bindService())
        }
        if (statisticServiceEnabled) {
            builder.addService(statisticsService!!.bindService())
        }
        if (videoCatalogServiceEnabled) {
            builder.addService(videoCatalogService!!.bindService())
        }
        if (suggestedVideoServiceEnabled) {
            builder.addService(suggestedVideosService!!.bindService())
        }
        if (userServiceEnabled) {
            builder.addService(userService!!.bindService())
        }
        grpcServer = builder.build()

        // Declare a shutdown hook otherwise JVM is listening on  a port forever
        Runtime.getRuntime().addShutdownHook(Thread { stopGrpcServer() })

        // Start Grpc listener
        grpcServer.start()
        LOGGER.info("[OK] Grpc Server started on port: '{}'", grpcPort)
        registerServices()
    }

    @PreDestroy
    fun stopGrpcServer() {
        LOGGER.info("Calling shutdown for GrpcServer")
        grpcServer!!.shutdown()
        unRegisterServices()
    }

    /**
     * Registering Services to Service Discovery : - ETCD if needed - do nothing with Kubernetes
     */
    private fun registerServices() {
        if (commentServiceEnabled) {
            serviceDiscoveryDao!!.register(
                commentService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (ratingServiceEnabled) {
            serviceDiscoveryDao!!.register(
                ratingService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (searchServiceEnabled) {
            serviceDiscoveryDao!!.register(
                searchService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (statisticServiceEnabled) {
            serviceDiscoveryDao!!.register(
                statisticsService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (videoCatalogServiceEnabled) {
            serviceDiscoveryDao!!.register(
                videoCatalogService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (suggestedVideoServiceEnabled) {
            serviceDiscoveryDao!!.register(
                suggestedVideosService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (userServiceEnabled) {
            serviceDiscoveryDao!!.register(
                userService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
    }

    private fun unRegisterServices() {
        if (commentServiceEnabled) {
            serviceDiscoveryDao!!.unregisterEndpoint(
                commentService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (ratingServiceEnabled) {
            serviceDiscoveryDao!!.unregisterEndpoint(
                ratingService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (searchServiceEnabled) {
            serviceDiscoveryDao!!.unregisterEndpoint(
                searchService!!.serviceKey,
                config!!.applicationHost!!, grpcPort
            )
        }
        if (statisticServiceEnabled) {
            serviceDiscoveryDao!!.unregisterEndpoint(
                statisticsService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (videoCatalogServiceEnabled) {
            serviceDiscoveryDao!!.unregisterEndpoint(
                videoCatalogService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (suggestedVideoServiceEnabled) {
            serviceDiscoveryDao!!.unregisterEndpoint(
                suggestedVideosService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
        if (userServiceEnabled) {
            serviceDiscoveryDao!!.unregisterEndpoint(
                userService!!.serviceKey,
                config!!.applicationHost!!,
                grpcPort
            )
        }
    }

    companion object {
        /** Some logger.  */
        private val LOGGER = LoggerFactory.getLogger(KillrvideoServicesGrpcServer::class.java)
    }
}
