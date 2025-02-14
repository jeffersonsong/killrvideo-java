package com.killrvideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages="com.killrvideo")
@EnableAutoConfiguration(exclude = {
        CassandraAutoConfiguration.class
})
public class KillrVideoServices {

    /**
     * As SpringBoot application, this is the "main" class
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(KillrVideoServices.class);
        app.run(args);
    }
    
}