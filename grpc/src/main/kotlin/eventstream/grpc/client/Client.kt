package eventstream.grpc.client


import eventstream.proto.FooOuterClass.Foo
import eventstream.proto.FooServiceGrpcKt
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main(): Unit = runBlocking {
    // Set up a channel to the server
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    // Create a stub (client proxy) for making calls to the server
    val stub = FooServiceGrpcKt.FooServiceCoroutineStub(channel)

    // Variable to hold Foo object
    lateinit var foo: Foo

    // Measure serialization time
    val serializationTime = measureTimeMillis {
        foo = Foo.newBuilder()
            .setName("John Doe")
            .setAge(30)
            .setGender("Male")
            .setIncome(50000.0)
            .addAllSomeList(listOf("Reading", "Cycling"))
            .build()
    }

    println("Serialization time: $serializationTime ms")

    // Make an RPC call to the server with the same Foo object
    val response = stub.processFoo(foo)
    println("Received response: $response")

    channel.shutdown()
}