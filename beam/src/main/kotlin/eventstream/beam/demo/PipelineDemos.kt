package eventstream.beam.demo

import eventstream.beam.functional.pipeline.convertToBeamEntity
import eventstream.beam.functional.pipeline.kafka.readFromKafka
import eventstream.beam.functional.pipeline.kafka.readFromKafkaByTimestamp
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.functional.pipeline.readAllParquetFromDirectory
import eventstream.beam.functional.pipeline.readCSVConvertToEntity
import eventstream.beam.logger.BeamLogger
import eventstream.beam.models.FredSeries
import eventstream.beam.pipelines.createFlinkPipeline
import eventstream.beam.pipelines.factory.PipelineFactory
import eventstream.beam.pipelines.options.createKafkaPipelineOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.KV
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {

}

fun runFlinkPipelines() {


    val logger = KotlinLogging.logger {}


    logger.info { "Testing Logs" }

    logger.info { "Starting Kafka Reader App" }

    /* Read from S3 and Serialize CSV */

    PipelineFactory.createWithAwsCredsFromEnv().apply {
        readCSVConvertToEntity(listOf("s3://beam-kuro/fred_series.csv"), FredSeries::serializeFromCsvLine).apply {
            logElements("Serialized from S3 : ")
        }
    }.run().waitUntilFinish()

    Pipeline.create().apply {
        readAllParquetFromDirectory<FredSeries>(
            "data/output/beam/parquet/"
        ).apply {
            convertToBeamEntity<FredSeries>()
        }.apply {
            logElements().also { BeamLogger.logger.info { "Successfully Serialized from Parquet Files." } }
        }
    }.run().waitUntilFinish()


    /* Kick off Job to Flink Cluster - Check k8/flink/deploy_job.md */

    val pipeline = createFlinkPipeline(jobName = "MyFlinkJob")

    val fileLines =
        pipeline.readCSVConvertToEntity(listOf("s3://beam-kuro/fred_series.csv"), FredSeries::serializeFromCsvLine)

    // Use the printLines function
    fileLines.logElements("Serialized from S3: ")

    pipeline.run().waitUntilFinish()


    // val pipeline = Pipeline.create(/* your pipeline options */)

    println("Testing new log")
    val kafkaMessages = pipeline.readFromKafka(
        bootstrapServers = "kafka.default.svc.cluster.local:9092",
        topic = "my-topic"
    )

    BeamLogger.logger.info { "Read from Kafka Topic my-topic Success - checking Events" }

    // Further processing, like logging the messages
    kafkaMessages.apply("Log Kafka Messages", ParDo.of(object : DoFn<KV<String, String>, Void>() {
        @ProcessElement
        fun processElement(c: ProcessContext) {
            logger.info { "BeamKafka Read : Key: ${c.element().key}, Value: ${c.element().value}" }
        }
    }))

    pipeline.run().waitUntilFinish()


}

fun runKafkaPipelines() {
    val dateTimeStr = "2023-01-31 15:30:00"
    val dateFormat = "yyyy-MM-dd HH:mm:ss"

    val formatter = DateTimeFormatter.ofPattern(dateFormat)
    val specificDateTime = LocalDateTime.parse(dateTimeStr, formatter)
    val startTimestampMillis = specificDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    println("Converted timestamp: $startTimestampMillis")

    // Initialize logger
    val logger = KotlinLogging.logger {}



    logger.info { "Starting Kafka Reader App" }

    val pipeline = Pipeline.create(createKafkaPipelineOptions())

    val kafkaMessagesOg = pipeline.readFromKafka(
        bootstrapServers = "kafka.default.svc.cluster.local:9092",
        topic = "my-topic",
    )
    kafkaMessagesOg.logElements("KafkaMsg: ")

    val kafkaMessages = pipeline.readFromKafka(
        topic = "my-topic",
        groupId = "my-topic-group",
        readFromEarliest = true
    )

    kafkaMessages.logElements("KafkaMsg: ")

    val kafkaMessagesFromTimeStamp = pipeline.readFromKafkaByTimestamp(
        topic = "new-topic",
        dateTimeStr = "2023-11-15 15:30:00" // Example date
    )

    kafkaMessagesFromTimeStamp.logElements("KafkaMsgFromTimestamp: ")


    BeamLogger.logger.info { "Pipelines Created and Transforms Applied." }


    pipeline.run().waitUntilFinish()
}