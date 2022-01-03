package com.killrvideo;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.killrvideo.conf.KillrVideoConfiguration;
import com.killrvideo.discovery.ServiceDiscoveryDao;
import com.killrvideo.service.comment.grpc.CommentsServiceGrpc;
import com.killrvideo.service.rating.grpc.RatingsServiceGrpc;
import com.killrvideo.service.search.grpc.SearchServiceGrpc;
import com.killrvideo.service.statistic.grpc.StatisticsServiceGrpc;
import com.killrvideo.service.suggestedvideo.grpc.SuggestedVideosServiceGrpc;
import com.killrvideo.service.user.grpc.UserManagementServiceGrpc;
import com.killrvideo.service.video.grpc.VideoCatalogServiceGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Startup a GRPC server on expected port and register all services.
 *
 * @author DataStax advocates team.
 */
@Component
public class KillrvideoServicesGrpcServer {

    /** Some logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(KillrvideoServicesGrpcServer.class);
    
    /** Listening Port for GRPC. */
    @Value("${killrvideo.grpc-server.port: 50101}")
    private int grpcPort;
    
    /** Connectivity to ETCD Service discovery. */
    @Inject
    private KillrVideoConfiguration config;
    
    /** Connectivity to ETCD Service discovery. */
    @Inject
    private ServiceDiscoveryDao serviceDiscoveryDao;
    
    @Inject
    private CommentsServiceGrpc commentService;
    
    @Value("${killrvideo.services.comment: true}")
    private final boolean commentServiceEnabled = true;
    
    @Inject
    private RatingsServiceGrpc ratingService;
    
    @Value("${killrvideo.services.rating: true}")
    private final boolean ratingServiceEnabled = true;
    
    @Inject
    private SearchServiceGrpc searchService;
    
    @Value("${killrvideo.services.search: true}")
    private final boolean searchServiceEnabled = true;
    
    @Inject
    private StatisticsServiceGrpc statisticsService;
    
    @Value("${killrvideo.services.statistic: true}")
    private final boolean statisticServiceEnabled = true;
    
    @Inject
    private VideoCatalogServiceGrpc videoCatalogService;
 
    @Value("${killrvideo.services.videoCatalog: true}")
    private final boolean videoCatalogServiceEnabled = true;
    
    @Inject
    private UserManagementServiceGrpc userService;
    
    @Value("${killrvideo.services.user: true}")
    private final boolean userServiceEnabled = true;
    
    @Inject
    private SuggestedVideosServiceGrpc suggestedVideosService;
    
    @Value("${killrvideo.services.suggestedVideo: true}")
    private final boolean suggestedVideoServiceEnabled = true;
  
    /**
     * GRPC Server to set up.
     */
    private Server grpcServer;
    
    @PostConstruct
    public void start() throws Exception {
        LOGGER.info("Initializing Grpc Server...");
        
        // Create GRPC server referencing only enabled services
        ServerBuilder<?> builder = ServerBuilder.forPort(grpcPort);
        if (commentServiceEnabled) {
            builder.addService(this.commentService.bindService());
        }
        if (ratingServiceEnabled) {
            builder.addService(this.ratingService.bindService());
        }
        if (searchServiceEnabled) {
            builder.addService(this.searchService.bindService());
        }
        if (statisticServiceEnabled) {
            builder.addService(this.statisticsService.bindService());
        }
        if (videoCatalogServiceEnabled) {
            builder.addService(this.videoCatalogService.bindService());
        }
        if (suggestedVideoServiceEnabled) {
            builder.addService(this.suggestedVideosService.bindService());
        }
        if (userServiceEnabled) {
            builder.addService(this.userService.bindService());
        }
        grpcServer = builder.build();
        
        // Declare a shutdown hook otherwise JVM is listening on  a port forever
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopGrpcServer));

        // Start Grpc listener
        grpcServer.start();
        LOGGER.info("[OK] Grpc Server started on port: '{}'", grpcPort);
        registerServices();
    }
    
    @PreDestroy
    public void stopGrpcServer() {
        LOGGER.info("Calling shutdown for GrpcServer");
        grpcServer.shutdown();
        unRegisterServices();
    }
    
    /**
     * Registering Services to Service Discovery : - ETCD if needed - do nothing with Kubernetes
     */
    private void registerServices() {
        if (commentServiceEnabled) {
            serviceDiscoveryDao.register(
                    this.commentService.getServiceKey(), 
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (ratingServiceEnabled) {
            serviceDiscoveryDao.register(
                    this.ratingService.getServiceKey(), 
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (searchServiceEnabled) {
            serviceDiscoveryDao.register(
                    this.searchService.getServiceKey(), 
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (statisticServiceEnabled) {
            serviceDiscoveryDao.register(
                    this.statisticsService.getServiceKey(),
                    config.getApplicationHost(),
                    grpcPort);
        }
        if (videoCatalogServiceEnabled) {
            serviceDiscoveryDao.register(
                    this.videoCatalogService.getServiceKey(),
                    config.getApplicationHost(),
                    grpcPort);
        }
        if (suggestedVideoServiceEnabled) {
            serviceDiscoveryDao.register(
                    this.suggestedVideosService.getServiceKey(),
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (userServiceEnabled) {
            serviceDiscoveryDao.register(
                    this.userService.getServiceKey(),
                    config.getApplicationHost(),
                    grpcPort);
        }
    }
    
    private void unRegisterServices() {
        if (commentServiceEnabled) {
            serviceDiscoveryDao.unregisterEndpoint(
                    this.commentService.getServiceKey(),
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (ratingServiceEnabled) {
            serviceDiscoveryDao.unregisterEndpoint(
                    this.ratingService.getServiceKey(),
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (searchServiceEnabled) {
            serviceDiscoveryDao.unregisterEndpoint(
                    this.searchService.getServiceKey(),
                    config.getApplicationHost(), grpcPort);
        }
        if (statisticServiceEnabled) {
            serviceDiscoveryDao.unregisterEndpoint(
                    this.statisticsService.getServiceKey(),
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (videoCatalogServiceEnabled) {
            serviceDiscoveryDao.unregisterEndpoint(
                    this.videoCatalogService.getServiceKey(), 
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (suggestedVideoServiceEnabled) {
            serviceDiscoveryDao.unregisterEndpoint(
                    this.suggestedVideosService.getServiceKey(),
                    config.getApplicationHost(), 
                    grpcPort);
        }
        if (userServiceEnabled) {
            serviceDiscoveryDao.unregisterEndpoint(
                    this.userService.getServiceKey(),
                    config.getApplicationHost(), 
                    grpcPort);
        }
    }
    
}
