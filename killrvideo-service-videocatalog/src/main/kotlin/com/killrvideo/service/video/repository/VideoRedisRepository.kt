package com.killrvideo.service.video.repository

import com.killrvideo.service.video.dto.Video
import kotlinx.coroutines.future.await
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class VideoRedisRepository(
    private val redisson: RedissonClient
) {
    suspend fun insertVideoAsync(video: Video) {
        if (video.videoid == null) {
            throw IllegalArgumentException("videoid missing")
        }

        val buket: RBucket<Video> = redisson.getBucket(redisKey(video.videoid!!))
        val future = buket.setAsync(video)
        future.toCompletableFuture().await()
    }

    suspend fun getVideoById(videioid: UUID): Video? {
        val bucket: RBucket<Video> = redisson.getBucket(redisKey(videioid))
        return if (bucket.isExists) {
            bucket.async.toCompletableFuture().await()
        } else {
            null
        }
    }

    suspend fun getVideoPreview(listOfVideoIds: List<UUID>): List<Video> {
        val buckets = redisson.getBuckets()
        val keys: Array<String> = listOfVideoIds.stream().map { redisKey(it) }.toArray { n -> Array(n) { "it = $it" } }
        val future = buckets.getAsync<Video>(*keys)

        return future.toCompletableFuture()
            .thenApply { it.values.toList() }
            .await()
    }

    private fun redisKey(videioid: UUID): String =
        "video/${videioid}"
}
