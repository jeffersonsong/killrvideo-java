import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.datastax.java-conventions")
    id("com.google.protobuf")
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))
    api(Deps.JetBrian.Kotlinx.coroutinesCore)
    api(Deps.Grpc.protobuf)
    api(Deps.Google.protobufJavaUtils)
    api(Deps.Google.protobufKotlin)
    api(Deps.Grpc.kotlinStub)

    implementation(project(":killrvideo-commons"))
    testImplementation(project(":killrvideo-test-utils"))

    implementation(Deps.JetBrian.Kotlinx.coroutinesJdk8)
    implementation(Deps.kotlinLogging)

    testImplementation(Deps.Junit.jupiter)
    testImplementation(Deps.Mockito.core)
    testImplementation(Deps.mockk)

    implementation(Deps.Spring.context)
    implementation(Deps.Datastax.mapperRuntime)

    implementation(Deps.Grpc.all) {
        exclude(group = "io.grpc", module = "grpc-testing")
        exclude(group = "junit", module = "junit")
    }

    implementation(Deps.commonsCodec)

    implementation(Deps.Apache.Commons.lang3)
    implementation(Deps.Apache.Commons.collections4)
    implementation(Deps.Javax.annotation)

    kapt(Deps.Datastax.mapperProcessor)
}

description = "+ killrvideo-service-ratings"

protobuf {
    protoc {
        artifact = Deps.Google.protobuf
    }
    plugins {
        id("grpc") {
            artifact = Deps.Grpc.protocGenJava
        }
        id("grpckt") {
            artifact = Deps.Grpc.protocGenKotlin
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

sourceSets.getByName("main") {
    java.srcDir("build/generated/source/kaptKotlin/main")
    java.srcDir("build/generated/source/proto/main/java")
    java.srcDir("build/generated/source/proto/main/kotlin")
    java.srcDir("build/generated/source/proto/main/grpc")
    java.srcDir("build/generated/source/proto/main/grpckt")
}
