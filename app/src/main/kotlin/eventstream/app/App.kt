//package eventstream.app
//
//import eventstream.beam.functional.pipeline.kafka.readFromKafka
//import eventstream.beam.functional.pipeline.logElements
//import eventstream.beam.logger.BeamLogger
//import eventstream.beam.pipelines.options.createKafkaPipelineOptions
//import io.github.oshai.kotlinlogging.KotlinLogging
//import org.apache.beam.sdk.Pipeline
//
//fun main(args: Array<String>) {
//    /*
//
//    Staging Application to use Functionality from Library.
//
//     */
//
//
//    // Set Logback configuration file
//    System.setProperty("logback.configurationFile", "logback.xml")
//
//    // Initialize logger
//    val logger = KotlinLogging.logger {}
//
//
//    logger.info { "Starting Kafka Read App" }
//
//
//
//
//    logger.info { "Starting Kafka Reader App" }
//    val pipeline = Pipeline.create(createKafkaPipelineOptions(args))
//
//    val kafkaMessages = pipeline.readFromKafka(
//        bootstrapServers = "kafka.default.svc.cluster.local:9092",
//        topic = "my-topic"
//    )
//
//    kafkaMessages.logElements("KafkaMsg: ")
//
//    BeamLogger.logger.info { "Read from Kafka Topic my-topic Success - checking Events" }
//
//
//
//    pipeline.run().waitUntilFinish()
//
//}
//
