package eventstream.beam


import eventstream.beam.functional.pipeline.convertToBeamEntity
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.functional.pipeline.readAllParquetFromDirectory
import eventstream.beam.logger.BeamLogger
import eventstream.beam.models.FredSeries
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline

fun main() {


    System.setProperty("logback.configurationFile", "logback.xml")

    val logger = KotlinLogging.logger {}


    logger.info { "Testing Logs" }

    logger.info { "Starting Kafka Reader App" }

    /* Read from S3 and Serialize CSV */

//    PipelineFactory.createWithAwsCredsFromEnv().apply {
//        readCSVConvertToEntity(listOf("s3://beam-kuro/fred_series.csv"), FredSeries::serializeFromCsvLine).apply {
//            logElements("Serialized from S3 : ")
//        }
//    }.run().waitUntilFinish()

    Pipeline.create().apply {
        readAllParquetFromDirectory<FredSeries>(
            "data/output/beam/parquet/"
        ).apply {
            convertToBeamEntity<FredSeries>()
        }.apply {
            logElements().also { BeamLogger.logger.info { "Successfully Serialized from Parquet Files." } }
        }
    }.run().waitUntilFinish()


//    /* Kick off Job to Flink Cluster - Check k8/flink/deploy_job.md */

//    val pipeline = createFlinkPipeline(jobName = "MyFlinkJob")
//
//    val fileLines =
//        pipeline.readCSVConvertToEntity(listOf("s3://beam-kuro/fred_series.csv"), FredSeries::serializeFromCsvLine)
//
//    // Use the printLines function
//    fileLines.logElements("Serialized from S3: ")
//
//    pipeline.run().waitUntilFinish()


//
//    val pipeline = Pipeline.create(/* your pipeline options */)
//
//    println("Testing new log")
//    val kafkaMessages = pipeline.readFromKafka(
//        bootstrapServers = "kafka.default.svc.cluster.local:9092",
//        topic = "my-topic"
//    )
//
//    BeamLogger.logger.info { "Read from Kafka Topic my-topic Success - checking Events" }
//
//    // Further processing, like logging the messages
//    kafkaMessages.apply("Log Kafka Messages", ParDo.of(object : DoFn<KV<String, String>, Void>() {
//        @ProcessElement
//        fun processElement(c: ProcessContext) {
//            logger.info { "BeamKafka Read : Key: ${c.element().key}, Value: ${c.element().value}" }
//        }
//    }))
//
//    pipeline.run().waitUntilFinish()


}


