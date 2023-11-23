package eventstream.grpc.client

import eventstream.grpc.entities.Foo
import eventstream.proto.Embedding
import eventstream.proto.Embedding.StringRequest
import eventstream.proto.EmbeddingServiceGrpcKt
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking


private val logger = KotlinLogging.logger { }

fun main(): Unit = runBlocking {

    val foo = Foo(
        age = 25,
        gender = "Male",
        name = "Johns",
        income = 200000.00,
        hobbies = listOf("Fishing", "Kayaking", "Swimming")
    )

    val foos = listOf(
        Foo(
            age = 20,
            gender = "Male",
            name = "Johns",
            income = 200000.00,
            hobbies = listOf("Hunting", "Kayaking", "Swimming")
        ),
        Foo(
            age = 21,
            gender = "Male",
            name = "Jake",
            income = 180000.00,
            hobbies = listOf("Fishing", "Kayaking", "Swimming")
        ),
        Foo(
            age = 30,
            gender = "Female",
            name = "Micahela",
            income = 170000.00,
            hobbies = listOf("Driving", "Photography", "Marine Biology")
        ),
        Foo(
            age = 30,
            gender = "Female",
            name = "Jiyan",
            income = 88000.00,
            hobbies = listOf("Climbing", "Rock Climbing", "Jumping")
        ),
        Foo(
            age = 30,
            gender = "Female",
            name = "Salie",
            income = 250000.00,
            hobbies = listOf("Crossfit", "Cardio", "Snowboarding")
        )
    )

    logger.info { foo.toString() }

    /* Set up a channel to the server */
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    logger.info { "Created Channel Successfully " }

    /* Create a stub (client proxy) for making calls to the server*/

    val embedStub = EmbeddingServiceGrpcKt.EmbeddingServiceCoroutineStub(channel)

    /* Single String -> Embedding RPC */
    invokerGenerateStringEmbeddings(embedStub, foo.toString())


    /* Batched List<String> -> List<Embedding> RPC */
    invokerGenerateBatchStringEmbeddings(embedStub, foos.map { it.toString() })

    /* Concurrent List<List<String>> -> List<List<Embedding>> RPC Call */
    invokerGenerateBatchStringEmbeddingsConcurrent(
        embedStub,
        listOf(listOf("Message 1", "Message 2"), listOf("Message 3", "Message 4"))
    )

    /* Create a list of queries and data */
    val queries = listOf("Query 1", "Query 2")
    val data = listOf("Data 1", "Data 2", "Data 3")

    /* Call the invokerCalculateSimilarities function */
    invokerCalculateSimilarities(embedStub, queries, data)

    channel.shutdown()
}

suspend fun invokerGenerateStringEmbeddings(
    embedStub: EmbeddingServiceGrpcKt.EmbeddingServiceCoroutineStub,
    inputStr: String
) {

    /* @Request: rpc GenerateStringEmbeddings (StringRequest) -> StringRequest Creation */
    val stringRequest = StringRequest.newBuilder()
        .setInput(inputStr)
        .build()

    /* @Invoke: rpc GenerateStringEmbeddings(..) -> stub.generateStringEmbeddings(StringRequest)   */
    val response = embedStub.generateStringEmbeddings(stringRequest)

    logger.info { "RPC Call for GenerateStringEmbeddings($inputStr) Successful: ${response.embeddingList}" }

}

suspend fun invokerGenerateBatchStringEmbeddings(
    embedStub: EmbeddingServiceGrpcKt.EmbeddingServiceCoroutineStub,
    embeddingsToConvert: List<String>
) {
    /* @Request: rpc GenerateBatchStringEmbeddings (BatchStringRequest) -> BatchStringRequest Creation  */
    val batchStringRequest = Embedding.BatchStringRequest.newBuilder().let { req ->
        req.addAllInputs(embeddingsToConvert)
    }.build()

    /* @Invoke: rpc GenerateBatchStringEmbeddings(..) -> stub.generateBatchStringEmbeddings(StringRequest)   */
    val response = embedStub.generateBatchStringEmbeddings(batchStringRequest)
    logger.info { "RPC Call GenerateBatchStringEmbeddings($embeddingsToConvert) Successful. Response:" }

    response.embeddingListsList.forEachIndexed { i, embeddingsListsList ->
        logger.info { "Embeddings for '${embeddingsToConvert[i]}': ${embeddingsListsList.embeddingsList}" }
    }
}

suspend fun invokerGenerateBatchStringEmbeddingsConcurrent(
    embedStub: EmbeddingServiceGrpcKt.EmbeddingServiceCoroutineStub,
    batchEmbeddings: List<List<String>>
) {
    coroutineScope {
        val deferredResponses = batchEmbeddings.map { embeddings ->
            async {
                val request = Embedding.BatchStringRequest.newBuilder()
                    .addAllInputs(embeddings)
                    .build()
                embedStub.generateBatchStringEmbeddings(request)
            }
        }
        val responses = deferredResponses.awaitAll()

        logger.info { "Batch RPC Call GenerateBatchStringEmbeddingsConcurrent Successful. Results:" }
        responses.forEachIndexed { i, response ->
            logger.info { "Embedding Response ${i + 1} for ${batchEmbeddings[i]}" }
            response.embeddingListsList.forEachIndexed { j, embeddingList ->
                logger.info { "Embeddings for '${batchEmbeddings[i][j]}': ${embeddingList.embeddingsList}" }
            }
        }
    }
}


suspend fun invokerCalculateSimilarities(
    embedStub: EmbeddingServiceGrpcKt.EmbeddingServiceCoroutineStub,
    queries: List<String>,
    data: List<String>
) {
    /* @Request: Create a SimilarityRequest with queries and data */
    val similarityRequest = Embedding.SimilarityRequest.newBuilder()
        .addAllQueries(queries)
        .addAllData(data)
        .build()

    /* @Invoke: Call the CalculateSimilarities RPC method */
    val response = embedStub.calculateSimilarities(similarityRequest)

    logger.info { "RPC Call for CalculateSimilarities Successful. Similarities:" }
    logger.info { "Comparing Queries against Data : $data" }

    response.similaritiesMap.forEach { (query, scores) ->
        logger.info { "Similarities for '$query': ${scores.scoresList}" }
    }
}
