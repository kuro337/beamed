package eventstream.beam


import eventstream.beam.functional.pipeline.kafka.readFromKafka
import eventstream.beam.functional.pipeline.kafka.readFromKafkaByTimestamp
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.logger.BeamLogger
import eventstream.beam.pipelines.options.createKafkaPipelineOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {

    val dateTimeStr = "2023-01-31 15:30:00"
    val dateFormat = "yyyy-MM-dd HH:mm:ss"

    val formatter = DateTimeFormatter.ofPattern(dateFormat)
    val specificDateTime = LocalDateTime.parse(dateTimeStr, formatter)
    val startTimestampMillis = specificDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    println("Converted timestamp: $startTimestampMillis")

    // Initialize logger
    val logger = KotlinLogging.logger {}



    logger.info { "Starting Kafka Reader App" }

    val pipeline = Pipeline.create(createKafkaPipelineOptions(args))

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


