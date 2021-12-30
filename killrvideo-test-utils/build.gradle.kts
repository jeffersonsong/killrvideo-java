/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("com.datastax.java-conventions")
}

dependencies {
    implementation(project(":killrvideo-commons"))
    implementation("org.junit.jupiter:junit-jupiter-api:${Junit.jupiter}")

    implementation("org.mockito:mockito-core:$mockito")
    implementation("com.datastax.oss:java-driver-core:$datastaxDriver")
}

description = "killrvideo-test-utils"
