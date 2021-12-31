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

    implementation(Deps.Grpc.all) {
        exclude(group = "io.grpc", module = "grpc-testing");
        exclude(group = "junit", module = "junit");
    }

    implementation(Deps.commonsCodec)

    implementation(Deps.Apache.Commons.collections4)
    testImplementation(Deps.Junit.jupiter)
    testImplementation(Deps.Mockito.core)
    compileOnly(Deps.lombok)

    implementation(Deps.Spring.context)
    implementation(Deps.Datastax.mapperRuntime)
    implementation(Deps.Apache.Commons.lang3)
    implementation(Deps.Javax.annotation)

    annotationProcessor(Deps.lombok)
    annotationProcessor(Deps.Datastax.mapperProcessor)
}

description = "+ killrvideo-service-ratings"

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
