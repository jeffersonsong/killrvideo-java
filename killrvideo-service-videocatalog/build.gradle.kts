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
    testImplementation(project(":killrvideo-test-utils"))
    implementation("org.apache.commons:commons-collections4:${Apache.Commons.collections4}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Junit.jupiter}")
    testImplementation("org.mockito:mockito-core:$mockito")
    compileOnly("org.projectlombok:lombok:$lombok")

    implementation("com.google.protobuf:protobuf-java:${Google.protobuf}")
    implementation("io.grpc:grpc-all:$grpcVersion") {
        exclude(group = "io.grpc", module = "grpc-testing");
        exclude(group = "junit", module = "junit");
    }

    implementation("com.google.guava:guava:${Google.guava}")
    implementation("org.springframework:spring-context:${Spring.context}")
    implementation("com.datastax.oss:java-driver-mapper-runtime:$datastaxDriver")
    implementation("javax.annotation:javax.annotation-api:${Javax.annotation}")
    implementation("org.apache.commons:commons-lang3:${Apache.Commons.lang3}")
    testImplementation("org.hamcrest:hamcrest:$hamcrest")

    annotationProcessor("org.projectlombok:lombok:$lombok")
    annotationProcessor("com.datastax.oss:java-driver-mapper-processor:$datastaxDriver")
}

description = "+ killrvideo-service-videocatalog"

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Google.protobuf}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
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
