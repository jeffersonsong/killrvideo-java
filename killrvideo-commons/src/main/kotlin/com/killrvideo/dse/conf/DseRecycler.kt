package com.killrvideo.dse.conf

import com.datastax.oss.driver.api.core.CqlSession
import mu.KotlinLogging
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy
import javax.inject.Inject

@Component
class DseRecycler {
    private val logger = KotlinLogging.logger {  }
    @Inject
    private lateinit var dseSession: CqlSession

    @PreDestroy
    fun onDestroy() {
        if (!dseSession.isClosed) {
            logger.info("Closing DSE Cluster (clean up at shutdown)")
            dseSession.close()
            logger.info(" + DSE Cluster is now closed.")
        }
    }
}
