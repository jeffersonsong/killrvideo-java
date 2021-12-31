buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://repo.spring.io/snapshot")
        maven(url = "https://repo.spring.io/milestone")
    }
}

plugins {
    id("org.springframework.boot") version "2.6.2" apply false
    id("com.google.protobuf") version "0.8.15" apply false
    id("java")
}

allprojects {
    group = "com.datastax"
    version = "3.0.8"
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply {
        plugin("java")
        plugin("com.google.protobuf")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }

    tasks.withType<Test>() {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation(Deps.Junit.jupiter)
        testImplementation(Deps.Junit.jupiterEngine)
    }
}
