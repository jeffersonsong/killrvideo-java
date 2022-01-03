plugins {
    id("com.datastax.java-conventions")
    kotlin("jvm")
}

dependencies {
    api(kotlin("stdlib"))

    implementation(project(":killrvideo-commons"))

    implementation(Deps.Junit.jupiter)
    implementation(Deps.mockk)
    implementation(Deps.Datastax.core)
}

description = "killrvideo-test-utils"
