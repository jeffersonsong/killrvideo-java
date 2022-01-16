import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.time.Instant

plugins {
    application
    id("com.datastax.java-conventions")
    id("org.springframework.boot")
    id("com.google.cloud.tools.jib")
    kotlin("jvm")
}

application {
    mainClass.set("com.killrvideo.KillrVideoServices")
}

dependencies {
    api(kotlin("stdlib"))

    implementation(project(":killrvideo-commons"))
    implementation(project(":killrvideo-service-ratings"))
    implementation(project(":killrvideo-service-comments"))
    implementation(project(":killrvideo-service-search"))
    implementation(project(":killrvideo-service-statistics"))
    implementation(project(":killrvideo-service-suggestedvideo"))
    implementation(project(":killrvideo-service-users"))
    implementation(project(":killrvideo-service-videocatalog"))

    implementation(Deps.Spring.Boot.starter)
    implementation(Deps.Grpc.all) {
        exclude(group = "io.grpc", module = "grpc-testing");
        exclude(group = "junit", module = "junit");
    }
    implementation(Deps.Javax.inject)
    implementation(Deps.kotlinLogging)
}

tasks.getByName<BootJar>("bootJar") {
    launchScript()
}

description = "+ killrvideo-services"

jib {
    from {
        image = "openjdk:11"
    }
    to {
        image = "killrvideo-java-local:${version}"
    }
    container {
        ports = listOf("50101")
        mainClass = "com.killrvideo.KillrVideoServices"
        jvmFlags = listOf("-Djava.security.egd=file:/dev/./urandom")
        creationTime = Instant.now().toString()
    }
}
