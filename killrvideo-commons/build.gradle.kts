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
    // Spring, Inversion Of Control
    implementation("org.springframework:spring-context:${Spring.context}")
    implementation("javax.inject:javax.inject:${Javax.inject}")

    // Logging
    implementation("ch.qos.logback:logback-core:$logback")
    runtimeOnly("ch.qos.logback:logback-classic:$logback")

    // Service Discovery
    implementation("com.xqbase:etcd4j:$etcd4j")

    // Retry until ETCD and DSE are ready.
    implementation("com.evanlennick:retry4j:$retry4j")

    // Annotation
    implementation("javax.annotation:javax.annotation-api:${Javax.annotation}")

    // Validation
    implementation("javax.validation:validation-api:${Javax.validation}")
    runtimeOnly("org.hibernate.validator:hibernate-validator:${Hibernate.validator}")

    // Expression language
    runtimeOnly("javax.el:javax.el-api:${Javax.el}")
    runtimeOnly("org.glassfish:javax.el:${Javax.el}")

    // GRPC
    implementation("io.grpc:grpc-all:$grpcVersion") {
        exclude(group = "io.grpc", module = "grpc-testing");
        exclude(group = "junit", module = "junit");
    }

    // Kafka
    implementation("org.apache.kafka:connect-json:${Apache.kafka}")

    // Java driver for DSE
    implementation("com.datastax.oss:java-driver-mapper-processor:$datastaxDriver")
    implementation("org.apache.tinkerpop:tinkergraph-gremlin:${Apache.tinkerpop}")

    // Transport Guava Bus (In Memory)
    implementation("com.google.guava:guava:${Google.guava}")

    // Java Bean
    compileOnly("org.projectlombok:lombok:$lombok")

    // Junit5 + Spring
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Junit.jupiter}")
    testImplementation("org.mockito:mockito-core:$mockito")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok:$lombok")
    annotationProcessor("com.datastax.oss:java-driver-mapper-processor:$datastaxDriver")
    annotationProcessor("org.apache.tinkerpop:tinkergraph-gremlin:${Apache.tinkerpop}")
}

description = "+ killrvideo-commons"

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
