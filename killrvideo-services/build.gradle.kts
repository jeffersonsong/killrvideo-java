import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    application
    id("com.datastax.java-conventions")
    id("org.springframework.boot")
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
}

tasks.getByName<BootJar>("bootJar") {
    launchScript()
}

description = "+ killrvideo-services"
