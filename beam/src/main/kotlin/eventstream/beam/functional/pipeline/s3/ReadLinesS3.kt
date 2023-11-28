package eventstream.beam.functional.pipeline.s3

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.PCollection
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

/**
 * #### `Read from S3 - Pipeline.readLinesFromS3AndProcess(bucketUri: String, processFn: DoFn<String, Void>) `
 * - Example Usage
 * ```kotlin
 * fun main() {
 *     val pipeline = createFlinkPipeline(jobName = "MyFlinkJob")
 *
 *     val fileLines = pipeline.getLinesFromS3("s3://beam-kuro/fred_series.csv")
 *
 *     fileLines.printLines("Read Line: ")
 *
 *     pipeline.run().waitUntilFinish()
 * }
 * ```
 */
fun Pipeline.getLinesFromS3(bucketUri: String): PCollection<String> {
    return this.apply(
        "ReadFromFile",
        TextIO.read().from(bucketUri)
    )
}


class S3ReadDoFn(private val awsAccessKey: String, private val awsSecretKey: String, private val region: String) :
    DoFn<String, String>() {
    lateinit var s3Client: S3Client

    @Setup
    fun setup() {
        val credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
        )

        s3Client = S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(Region.of(region))
            .build()
    }

    @ProcessElement
    fun processElement(@Element input: String, receiver: OutputReceiver<String>) {
        // Use s3Client to perform operations on S3
    }

    @Teardown
    fun teardown() {
        s3Client.close()
    }
}