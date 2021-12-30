import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    application
    id("com.datastax.java-conventions")
    id("org.springframework.boot")
}

application {
    mainClass.set("com.killrvideo.KillrVideoServices")
}

dependencies {
    implementation(project(":killrvideo-commons"))
    implementation(project(":killrvideo-service-ratings"))
    implementation(project(":killrvideo-service-comments"))
    implementation(project(":killrvideo-service-search"))
    implementation(project(":killrvideo-service-statistics"))
    implementation(project(":killrvideo-service-suggestedvideo"))
    implementation(project(":killrvideo-service-users"))
    implementation(project(":killrvideo-service-videocatalog"))

    implementation("org.springframework.boot:spring-boot-starter:${Spring.Boot.starter}")
    implementation("io.grpc:grpc-all:$grpcVersion") {
        exclude(group = "io.grpc", module = "grpc-testing");
        exclude(group = "junit", module = "junit");
    }
    implementation("javax.inject:javax.inject:${Javax.inject}")
}

tasks.getByName<BootJar>("bootJar") {
    launchScript()
}

description = "+ killrvideo-services"
