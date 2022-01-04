package com.killrvideo

import com.google.common.collect.ImmutableList
import com.killrvideo.conf.KillrVideoConfiguration
import com.killrvideo.discovery.ServiceDiscoveryDao
import com.killrvideo.service.comment.grpc.CommentsServiceGrpc
import com.killrvideo.service.utils.GlobalGrpcExceptionHandler
import com.killrvideo.service.rating.grpc.RatingsServiceGrpc
import com.killrvideo.service.search.grpc.SearchServiceGrpc
import com.killrvideo.service.statistic.grpc.StatisticsServiceGrpc
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpc
import com.killrvideo.service.user.grpc.UserManagementServiceGrpc
import com.killrvideo.service.utils.TraceServiceCallInterceptor
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpc
import io.grpc.*
import mu.KotlinLogging
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
    private val logger = KotlinLogging.logger {}

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
        logger.info("Initializing Grpc Server...")

        val interceptors = listOf(
            GlobalGrpcExceptionHandler(),
            TraceServiceCallInterceptor()
        )
        val serviceList = buildServiceList()

        // Create GRPC server referencing only enabled services
        val builder = ServerBuilder.forPort(grpcPort)
        serviceList.forEach {service ->
            builder.addService(
                ServerInterceptors.intercept(service, interceptors)
            )
        }

        grpcServer = builder.build()

        // Declare a shutdown hook otherwise JVM is listening on  a port forever
        Runtime.getRuntime().addShutdownHook(Thread { stopGrpcServer() })

        // Start Grpc listener
        grpcServer.start()
        logger.info("[OK] Grpc Server started on port: '{}'", grpcPort)
        registerServices()
    }

    private fun buildServiceList(): ImmutableList<BindableService> {
        val serviecListBuilder = ImmutableList.Builder<BindableService>();
        if (commentServiceEnabled) {
            serviecListBuilder.add(commentService)
        }
        if (ratingServiceEnabled) {
            serviecListBuilder.add(ratingService)
        }
        if (searchServiceEnabled) {
            serviecListBuilder.add(searchService)
        }
        if (statisticServiceEnabled) {
            serviecListBuilder.add(statisticsService)
        }
        if (videoCatalogServiceEnabled) {
            serviecListBuilder.add(videoCatalogService)
        }
        if (suggestedVideoServiceEnabled) {
            serviecListBuilder.add(suggestedVideosService)
        }
        if (userServiceEnabled) {
            serviecListBuilder.add(userService)
        }

        return serviecListBuilder.build()
    }

    @PreDestroy
    fun stopGrpcServer() {
        logger.info("Calling shutdown for GrpcServer")
        grpcServer.shutdown()
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
}
