import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.datastax.java-conventions")
    id("com.google.protobuf")
    kotlin("jvm")
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
    testImplementation(Deps.Junit.jupiter)
    testImplementation(Deps.Mockito.core)
    compileOnly(Deps.lombok)

    implementation(Deps.Spring.context)
    implementation(Deps.Datastax.mapperRuntime)

    implementation(Deps.Grpc.all) {
        exclude(group = "io.grpc", module = "grpc-testing")
        exclude(group = "junit", module = "junit")
    }

    implementation(Deps.Datastax.core)

    implementation(Deps.Apache.Commons.lang3)
    implementation(Deps.Javax.validation)
    implementation(Deps.Javax.annotation)
    implementation(Deps.Javax.inject)
    implementation(Deps.Apache.Kafka.connectApi)
    implementation(Deps.Apache.Tinkerpop.tinkergraph)

    annotationProcessor(Deps.lombok)
    annotationProcessor(Deps.Datastax.mapperProcessor)
}

description = "+ killrvideo-service-suggestedvideo"

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
