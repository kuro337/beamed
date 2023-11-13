package eventstream.beam.pipelines

import eventstream.beam.interfaces.pipeline.BeamPipeline
import eventstream.beam.interfaces.pipeline.BeamPipelineOptions
import eventstream.beam.interfaces.pipeline.PIPELINE
import eventstream.beam.pipelines.decorators.PipelineType
import eventstream.beam.pipelines.options.attachAWSCredsToFlinkPipelineOptions
import eventstream.beam.pipelines.options.createFlinkPipelineOptions
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection

@PipelineType("Flink S3 Pipeline")
class FlinkS3Pipeline(private val options: BeamPipelineOptions) : BeamPipeline {
    private val pipelines = listOf(PIPELINE.FLINK_S3)

    override fun getOptions(): BeamPipelineOptions = options
    override fun run(pipelineType: PIPELINE) {
        when (pipelineType) {
            PIPELINE.FLINK_S3 -> objectStorePipeline()
            // Handle other cases if necessary
            else -> throw IllegalArgumentException("Pipeline type not supported by this class")
        }
    }

    override fun getPipelines(): List<PIPELINE> {
        return listOf(PIPELINE.FLINK_S3)
    }

    private fun objectStorePipeline() {
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
