object Deps {
    const val coroutinesVersion = "1.5.2"
    const val kotlinLogging= "io.github.microutils:kotlin-logging:2.1.21"

    object Datastax {
        private const val datastaxDriverVersion = "4.13.0"
        val core = "com.datastax.oss:java-driver-core:$datastaxDriverVersion"
        val mapperRuntime = "com.datastax.oss:java-driver-mapper-runtime:$datastaxDriverVersion"
        val mapperProcessor = "com.datastax.oss:java-driver-mapper-processor:$datastaxDriverVersion"
    }

    object Logback {
        private const val logbackVersion = "1.2.10"
        val core = "ch.qos.logback:logback-core:$logbackVersion"
        val classic = "ch.qos.logback:logback-classic:$logbackVersion"
    }

    object Spring {
        private const val contextVersion = "5.3.14"
        val context = "org.springframework:spring-context:$contextVersion"

        object Boot {
            private const val starterVersion = "2.6.2"
            val starter = "org.springframework.boot:spring-boot-starter:$starterVersion"
        }
    }

    object Javax {
        private const val annotationVersion = "1.3.2"
        private const val injectVersion = "1"
        private const val validationVersion = "2.0.1.Final"
        private const val elVersion = "3.0.0"

        val inject = "javax.inject:javax.inject:$injectVersion"
        val annotation = "javax.annotation:javax.annotation-api:$annotationVersion"
        val validation = "javax.validation:validation-api:$validationVersion"
        val el = "javax.el:javax.el-api:$elVersion"
        val glassfishEl = "org.glassfish:javax.el:$elVersion"
    }

    object Google {
        private const val protobufVersion = "3.19.1"
        private const val guavaVersion = "31.0.1-jre"
        val protobuf = "com.google.protobuf:protoc:$protobufVersion"
        val protobufJavaUtils = "com.google.protobuf:protobuf-java-util:3.19.0-rc-2"
        val protobufKotlin = "com.google.protobuf:protobuf-kotlin:$protobufVersion"
        val guava = "com.google.guava:guava:$guavaVersion"
    }

    object Grpc {
        private const val grpcVersion = "1.43.1"
        private const val grpcKotlinVersion = "1.2.0"

        val protocGenJava = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        val protocGenKotlin = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        val all = "io.grpc:grpc-all:$grpcVersion"
        val protobuf = "io.grpc:grpc-protobuf:$grpcVersion"
        val kotlinStub = "io.grpc:grpc-kotlin-stub:$grpcKotlinVersion"
    }

    const val etcd4j = "com.xqbase:etcd4j:1.2"
    const val retry4j = "com.evanlennick:retry4j:0.14.0"

    const val commonsCodec = "commons-codec:commons-codec:1.15"
    const val hamcrest = "org.hamcrest:hamcrest:2.2"
    const val mockk = "io.mockk:mockk:1.12.2"

    object Apache {
        object Commons {
            private const val lang3Version = "3.12.0"
            private const val collections4Version = "4.4"
            val lang3 = "org.apache.commons:commons-lang3:$lang3Version"
            val collections4 = "org.apache.commons:commons-collections4:$collections4Version"
        }

        object Tinkerpop {
            const val tinkerpopVersion = "3.4.12"
            val tinkergraph = "org.apache.tinkerpop:tinkergraph-gremlin:$tinkerpopVersion"
        }

        object Kafka {
            private const val kafkaVersion = "3.0.0"
            val connectJson = "org.apache.kafka:connect-json:$kafkaVersion"
            val connectApi = "org.apache.kafka:connect-api:$kafkaVersion"
        }
    }

    object Hibernate {
        private const val validatorVersion = "6.2.0.Final"
        val validator = "org.hibernate.validator:hibernate-validator:$validatorVersion"
    }

    object Junit {
        private const val jupiterVersion = "5.8.2"
        val jupiter = "org.junit.jupiter:junit-jupiter-api:$jupiterVersion"
        val jupiterEngine = "org.junit.jupiter:junit-jupiter-engine:$jupiterVersion"
    }

    object Mockito {
        const val mockitoVersion = "4.0.0"
        val core = "org.mockito:mockito-core:$mockitoVersion"
    }

    object JetBrian {
        object Kotlinx {
            val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
            val coroutinesJdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion"
        }
    }
}
