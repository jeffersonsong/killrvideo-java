package com.killrvideo.conf

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.validation.Validation
import javax.validation.Validator

/**
 * Configuration for KillrVideo application leveraging on DSE, ETCD and any external source.
 *
 * @author DataStax Developer Advocates team.
 */
@Configuration
open class KillrVideoConfiguration {
    /**
     * Getter for attribute 'applicationHost'.
     *
     * @return
     * current value of 'applicationHost'
     */
    @Value("#{environment.KILLRVIDEO_HOST_IP ?: '10.0.75.1'}")
    val applicationHost: String? = null

    /**
     * Getter for attribute 'applicationName'.
     *
     * @return
     * current value of 'applicationName'
     */
    @Value("\${application.name: KillrVideo}")
    val applicationName: String? = null

    @get:Bean
    open val beanValidator: Validator
        get() = Validation.buildDefaultValidatorFactory().validator

    companion object {
        /** Use Spring profile to adapt behaviours.  */
        const val PROFILE_MESSAGING_KAFKA = "messaging_kafka"
        const val PROFILE_MESSAGING_MEMORY = "messaging_memory"
        const val PROFILE_DISCOVERY_ETCD = "discovery_etcd"
        const val PROFILE_DISCOVERY_STATIC = "discovery_static"
    }
}
