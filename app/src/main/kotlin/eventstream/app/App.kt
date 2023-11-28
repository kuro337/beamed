package eventstream.app

import eventstream.beam.functional.pipeline.convertToBeamEntity
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.functional.pipeline.readAllParquetFromDirectory
import eventstream.beam.logger.BeamLogger
import eventstream.beam.models.FredSeries
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline

fun main() {
    /*

    Staging Application to use Functionality from Library.

     */


    // Set Logback configuration file
    System.setProperty("logback.configurationFile", "logback.xml")

    // Initialize logger
    val logger = KotlinLogging.logger {}


    logger.info { "Test Log New " }

    logger.info { "Starting Kafka Reader App" }

    Pipeline.create().apply {
        readAllParquetFromDirectory<FredSeries>(
            "data/output/beam/parquet/"
        ).apply {
            convertToBeamEntity<FredSeries>()
        }.apply {
            logElements().also { BeamLogger.logger.info { "Successfully Serialized from Parquet Files." } }
        }
    }.run().waitUntilFinish()


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

