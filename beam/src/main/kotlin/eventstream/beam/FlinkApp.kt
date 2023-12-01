package eventstream.beam


import eventstream.beam.functional.pipeline.kafka.readFromKafka
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.functional.pipeline.readCSVConvertToEntity
import eventstream.beam.models.FredSeries
import eventstream.beam.pipelines.createFlinkPipeline
import io.github.oshai.kotlinlogging.KotlinLogging

fun main() {


    System.setProperty("logback.configurationFile", "logback.xml")

    val logger = KotlinLogging.logger {}


    logger.info { "Logger Active" }

    logger.info { "Starting Flink App" }


    /* Kick off Job to Flink Cluster - Check k8/flink/deploy_job.md */

    val pipeline = createFlinkPipeline(jobName = "MyFlinkJob")

    val fileLines =
        pipeline.readCSVConvertToEntity(listOf("s3://beam-kuro/fred_series.csv"), FredSeries::serializeFromCsvLine)

    // Use the printLines function
    fileLines.logElements("Serialized from S3: ")

    pipeline.run().waitUntilFinish()

    val flinkPipeline = createFlinkPipeline(jobName = "MyFlinkJob")

    val kafkaMessages = flinkPipeline.readFromKafka(
        topic = "my-topic",
        groupId = "my-topic-group",
        readFromEarliest = true
    )

    kafkaMessages.logElements("KafkaMsg: ")



    pipeline.run().waitUntilFinish()


}


