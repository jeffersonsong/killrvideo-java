import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
}

allprojects {
    group = "com.datastax"
    version = "3.0.8"
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        // plugin("com.google.protobuf")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

    dependencies {
        testImplementation(Deps.Junit.jupiter)
        testImplementation(Deps.Junit.jupiterEngine)
        //api(kotlin("stdlib"))
    }
}
