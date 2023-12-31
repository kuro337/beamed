import com.google.protobuf.gradle.id

plugins {
    id("eventstream.kotlin-application-conventions")
    id("com.google.protobuf") version "0.9.4"

    application
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.11")
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-protobuf:1.59.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
    implementation("io.grpc:grpc-netty:1.59.0") /* Required for gRPC Server */
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4" // Newer 3.24.4
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.59.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
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

application {
    mainClass.set("eventstream.grpc.AppKt")
}
