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
    api(Deps.Google.protobufJavaUtils)
    api(Deps.Google.protobufKotlin)

    implementation(Deps.JetBrian.Kotlinx.coroutinesJdk8)
    implementation(Deps.kotlinLogging)

    runtimeOnly(Deps.Google.protobufJavaUtils)

    // Spring, Inversion Of Control
    implementation(Deps.Spring.context)
    implementation(Deps.Javax.inject)

    // Logging
    implementation(Deps.Logback.core)
    runtimeOnly(Deps.Logback.classic)
    implementation(Deps.kotlinLogging)

    // Service Discovery
    implementation(Deps.etcd4j)

    // Retry until ETCD and DSE are ready.
    implementation(Deps.retry4j)

    // Annotation
    implementation(Deps.Javax.annotation)

    // Validation
    implementation(Deps.Javax.validation)
    runtimeOnly(Deps.Hibernate.validator)

    // Expression language
    runtimeOnly(Deps.Javax.el)
    runtimeOnly(Deps.Javax.glassfishEl)

    // GRPC
    implementation(Deps.Grpc.all) {
        exclude(group = "io.grpc", module = "grpc-testing")
        exclude(group = "junit", module = "junit")
    }

    // Kafka
    implementation(Deps.Apache.Kafka.connectJson)

    // Java driver for DSE
    implementation(Deps.Datastax.mapperProcessor)
    implementation(Deps.Apache.Tinkerpop.tinkergraph)

    // Transport Guava Bus (In Memory)
    implementation(Deps.Google.guava)

    // Java Bean
    compileOnly(Deps.lombok)

    // Junit5 + Spring
    testImplementation(Deps.Junit.jupiter)
    testImplementation(Deps.Mockito.core)

    // Annotation processors
    kapt(Deps.Apache.Tinkerpop.tinkergraph)
}

description = "+ killrvideo-commons"

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
