package eventstream.beam.pipelines

import eventstream.beam.pipelines.decorators.PipelineType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.io.aws2.options.AwsOptions
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection
import software.amazon.awssdk.regions.Region

@PipelineType("S3 Memory Pipeline")
object S3MemoryPipeline {
    @Transient
    val logger = KotlinLogging.logger {}

    @JvmStatic
    fun run() {
        /*

        @DirectRunner
        @InMemoryPipeline

        @Beam @Flink
            - Currently this class runs Beam Pipeline In Memory on Current Machine

         */

        logger.info { "S3 Read Fed Data Pipeline" }

        /*      @Options      */
        val options: PipelineOptions = PipelineOptionsFactory.fromArgs("--runner=DirectRunner").create()
            .apply {
                `as`(AwsOptions::class.java).awsRegion = Region.US_EAST_1
            }

        val pipeline = Pipeline.create(options)

        /*      @Read           */
        val fileLines: PCollection<String> = pipeline
            .apply("ReadFromFile", TextIO.read().from("s3://beam-kuro/fred_series.csv"))

        /*      @Transform      */
        fileLines.apply("PrintLines", ParDo.of(object : DoFn<String, Void>() {
            @ProcessElement
            fun processElement(@Element line: String, receiver: OutputReceiver<Void>) {
                logger.info { "$line" }
            }
        }))

        /*          @Run              */
        pipeline.run().waitUntilFinish()
    }
}

/*
Currently this Inherits Credentials from System Runtime

We can also pass Credentials Directly as Such -

Using Explicit Credentials-

val credentials: AwsCredentials = AwsBasicCredentials.create("******", "***************")
val options: PipelineOptions = PipelineOptionsFactory.fromArgs("--runner=DirectRunner").create()
options.`as`(AwsOptions::class.java).awsCredentialsProvider = StaticCredentialsProvider.create(credentials)
options.`as`(AwsOptions::class.java).setAwsRegion(Region.US_EAST_1)

val pipeline = Pipeline.create(options)


*/