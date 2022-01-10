package com.killrvideo

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import killrvideo.comments.CommentsServiceGrpc
import killrvideo.comments.CommentsServiceGrpc.CommentsServiceBlockingStub
import killrvideo.ratings.RatingsServiceGrpc
import killrvideo.ratings.RatingsServiceGrpc.RatingsServiceBlockingStub
import killrvideo.search.SearchServiceGrpc
import killrvideo.search.SearchServiceGrpc.SearchServiceBlockingStub
import killrvideo.statistics.StatisticsServiceGrpc
import killrvideo.statistics.StatisticsServiceGrpc.StatisticsServiceBlockingStub
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc
import killrvideo.suggested_videos.SuggestedVideoServiceGrpc.SuggestedVideoServiceBlockingStub
import killrvideo.user_management.UserManagementServiceGrpc
import killrvideo.user_management.UserManagementServiceGrpc.UserManagementServiceBlockingStub
import killrvideo.video_catalog.VideoCatalogServiceGrpc
import killrvideo.video_catalog.VideoCatalogServiceGrpc.VideoCatalogServiceBlockingStub
import org.springframework.util.Assert

/**
 * As Unit Test or cany consumer you may want USE the runing GRPC API.
 *
 * @author DataStax advocates Team
 */
class KillrvideoServicesGrpcClient(
    /** Grpc Endpoint  */
    private val grpcEndPoint: ManagedChannel
) {
    /** Clients for different services in GRPC.  */
    lateinit var commentServiceGrpcClient: CommentsServiceBlockingStub
    lateinit var ratingServiceGrpcClient: RatingsServiceBlockingStub
    lateinit var searchServiceGrpcClient: SearchServiceBlockingStub
    lateinit var statisticServiceGrpcClient: StatisticsServiceBlockingStub
    lateinit var suggestedVideoServiceGrpcClient: SuggestedVideoServiceBlockingStub
    lateinit var userServiceGrpcClient: UserManagementServiceBlockingStub
    lateinit var videoCatalogServiceGrpcClient: VideoCatalogServiceBlockingStub

    /**
     * Connection to GRPC Server.
     *
     * @param grpcServer
     * current grpc hostname
     * @param grpcPort
     * current grpc portnumber
     */
    constructor(grpcServer: String?, grpcPort: Int) : this(
        ManagedChannelBuilder.forAddress(grpcServer, grpcPort).usePlaintext().build()
    )

    /**
     * Extension point for your own GRPC channel.
     *
     * @param grpcEnpoint
     * current GRPC Channe
     */
    init {
        initServiceClients()
    }

    /**
     * Init item
     */
    fun initServiceClients() {
        Assert.notNull(grpcEndPoint, "GrpcEnpoint must be setup")
        commentServiceGrpcClient = CommentsServiceGrpc.newBlockingStub(grpcEndPoint)
        ratingServiceGrpcClient = RatingsServiceGrpc.newBlockingStub(grpcEndPoint)
        searchServiceGrpcClient = SearchServiceGrpc.newBlockingStub(grpcEndPoint)
        statisticServiceGrpcClient = StatisticsServiceGrpc.newBlockingStub(grpcEndPoint)
        suggestedVideoServiceGrpcClient = SuggestedVideoServiceGrpc.newBlockingStub(grpcEndPoint)
        userServiceGrpcClient = UserManagementServiceGrpc.newBlockingStub(grpcEndPoint)
        videoCatalogServiceGrpcClient = VideoCatalogServiceGrpc.newBlockingStub(grpcEndPoint)
    }
}
