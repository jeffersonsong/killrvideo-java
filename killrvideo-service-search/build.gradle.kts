import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.datastax.java-conventions")
    id("com.google.protobuf")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":killrvideo-commons"))
    testImplementation(Deps.Junit.jupiter)
    testImplementation(Deps.Mockito.core)
    compileOnly(Deps.lombok)

    implementation(Deps.Google.protobuf)
    implementation(Deps.Grpc.all) {
        exclude(group = "io.grpc", module = "grpc-testing");
        exclude(group = "junit", module = "junit");
    }

    implementation(Deps.Spring.context)

    implementation(Deps.Logback.core)
    runtimeOnly(Deps.Logback.classic)

    implementation(Deps.Javax.annotation)
    implementation(Deps.Apache.Commons.lang3)

    implementation(Deps.Datastax.core)

    annotationProcessor(Deps.lombok)
    annotationProcessor(Deps.Datastax.mapperProcessor)
}

description = "+ killrvideo-service-search"

protobuf {
    protoc {
        artifact = Deps.Google.protobuf
    }
    plugins {
        id("grpc") {
            artifact = Deps.Grpc.protocGen
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}
