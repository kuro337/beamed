# Proto

```
Scalars: Such as int32, int64, uint32, uint64, double, float, bool, string, and bytes.
Enums: User-defined enumerations.
Messages: Complex types, which are essentially other message definitions.
Repeated Fields: For arrays or lists, using the repeated keyword.
Maps: Represent key-value pairs, defined as map<key_type, value_type>.
```

- `build.gradle.kts` for App with protos

```kotlin
import com.google.protobuf.gradle.id

plugins {
    id("eventstream.kotlin-application-conventions")
    id("com.google.protobuf") version "0.9.4"

    application
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-protobuf:1.59.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
    implementation("io.grpc:grpc-netty:1.59.0") /* Required for gRPC Server */

}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4" // Replace with actual version
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.59.0" // Replace with actual version
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar" // Replace with actual version
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


```

- Put .proto file in grpc/src/main/proto/foo.proto

```protobuf
syntax = "proto3";

package eventstream.proto;

message Foo {
    string name = 1;
    int32 age = 2;
    string gender = 3;
    double income = 4;
    repeated string some_list = 5;  // List of strings
}

service FooService {
    rpc ProcessFoo (Foo) returns (FooResponse) {}
}

message FooResponse {
    // Define the structure of your response
}

```

- Then we generate Kotlin Code Stubs

```bash
./gradlew :grpc:build

# Generates Kotlin classes for Foo, FooService, and FooResponse 
# grpc/build/generated/source/proto/main directory.
# These classes will be based on your .proto file.
```

- Now we can define our Server - by convention if our .proto file was `foo.proto` - our `Foo` `FooResponse` will be
  in `FooOuterClass`

```kotlin
package eventstream.grpc.server

import eventstream.proto.FooOuterClass.Foo
import eventstream.proto.FooOuterClass.FooResponse
import eventstream.proto.FooServiceGrpcKt
import io.grpc.Server
import io.grpc.ServerBuilder

class FooServiceImpl : FooServiceGrpcKt.FooServiceCoroutineImplBase() {
    override suspend fun processFoo(request: Foo): FooResponse {
        println("Received request: $request")
        val response = FooResponse.newBuilder()
            // Set fields in the response
            .build()
        return response
    }
}

fun startServer() {
    val server: Server = ServerBuilder
        .forPort(50051)
        .addService(FooServiceImpl())
        .build()
        .start()

    println("Server started, listening on 50051")
    server.awaitTermination()
}

fun main() {
    startServer()
}


```

- `Client.kt` - Client for Grpc

```kotlin
// Include Couroutines
// implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

package eventstream.grpc.client


import eventstream.proto.FooOuterClass.Foo
import eventstream.proto.FooServiceGrpcKt
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    // Set up a channel to the server
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    // Create a stub (client proxy) for making calls to the server
    val stub = FooServiceGrpcKt.FooServiceCoroutineStub(channel)

    // Create a Foo message
    val foo = Foo.newBuilder()
        .setName("John Doe")
        .setAge(30)
        .setGender("Male")
        .setIncome(50000.0)
        .addAllSomeList(listOf("Reading", "Cycling"))
        .build()

    // Make an RPC call to the server
    val response = stub.processFoo(foo)
    println("Received response: $response")

    channel.shutdown()
}


```

- Now run `Server` and `Client` to test sending and recieving the Message