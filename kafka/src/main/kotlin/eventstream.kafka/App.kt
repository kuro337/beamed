package eventstream.kafka

import eventstream.kafka.client.KafkaController
import io.github.oshai.kotlinlogging.KotlinLogging

fun main() {
    val logger = KotlinLogging.logger("Kafka.EntryPoint")
    logger.info { "Event Stream Kafka App Running." }

    val kafkaController = KafkaController.getDefaultKafkaController()

    try {

        kafkaController.createTopic("my-topic", 3, 1)
        kafkaController.sendMessage("my-topic", "key1", "Hello Kafka!")

        kafkaController.createTopic("my-topic2", 3, 1)
        kafkaController.sendMessage("my-topic2", "key1", "Hello Kafka!")

        kafkaController.readMessages("my-topic", 5000, "earliest") // Poll for 5 seconds
        kafkaController.readMessages("my-topic2", 10000, "earliest") // Poll for 10 seconds

    } catch (e: Exception) {
        logger.error(e) { "An error occurred in the Kafka operations." }
    } finally {
        kafkaController.close()
    }

    logger.info { "Event Stream Kafka App Ending." }


}