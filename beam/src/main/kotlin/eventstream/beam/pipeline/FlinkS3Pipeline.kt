package eventstream.beam.pipeline

import eventstream.beam.pipeline.decorators.PipelineType
import eventstream.beam.pipeline.options.attachAWSCredsToFlinkPipelineOptions
import eventstream.beam.pipeline.options.createFlinkPipelineOptions
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection

@PipelineType("Flink S3 Pipeline")
object FlinkS3Pipeline {
    @JvmStatic
    fun run() {
        /*
        @FlinkRunner
        @DistributedPipeline

        @Beam @Flink

        - Executes the Beam Job on a Flink Runner
        - Flink is deployed to accept any number of Jobs
        - Flink running on Kubernetes

        @Considerations
        - Provide S3 Credentials Directly

        */

        /*      @SubmitJob      */

        /* @JobOptions */
        val options = createFlinkPipelineOptions("localhost:8081", "s3BeamJob")
            .also { flinkOptions ->
                attachAWSCredsToFlinkPipelineOptions(
                    flinkOptions,
                    "*********************",
                    "********************************",
                    "us-east-1"
                )
            }

        val pipeline = Pipeline.create(options)

        /* @Read  */

        val fileLines: PCollection<String> = pipeline
            .apply("ReadFromFile", TextIO.read().from("s3://beam-kuro/fred_series.csv"))

        /* @Transform  */

        fileLines.apply("PrintLines", ParDo.of(object : DoFn<String, Void>() {
            @ProcessElement
            fun processElement(@Element line: String, receiver: OutputReceiver<Void>) {
                println(line)
            }
        }))

        /*  @Run  */

        pipeline.run().waitUntilFinish()
    }
}

/*
        val credentials: AwsCredentials =
        AwsBasicCredentials.create("******", "***************")

        options.`as`(AwsOptions::class.java).awsCredentialsProvider = StaticCredentialsProvider.create(credentials)
        options.`as`(AwsOptions::class.java).awsRegion = Region.US_EAST_1

        val pipeline = Pipeline.create(options)
 */
