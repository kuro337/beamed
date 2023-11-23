package eventstream.grpc.server

import eventstream.proto.FooOuterClass.Foo
import eventstream.proto.FooOuterClass.FooResponse
import eventstream.proto.FooServiceGrpcKt
import io.grpc.Server
import io.grpc.ServerBuilder

/* Listens for Remote Calls made to processFoo() */
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
