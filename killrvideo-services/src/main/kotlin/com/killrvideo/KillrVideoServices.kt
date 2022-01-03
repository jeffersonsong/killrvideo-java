package com.killrvideo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = ["com.killrvideo"])
@EnableAutoConfiguration(exclude = [CassandraAutoConfiguration::class])
object KillrVideoServices {
    /**
     * As SpringBoot application, this is the "main" class
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val app = SpringApplication(KillrVideoServices::class.java)
        app.run(*args)
    }
}
