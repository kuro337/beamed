package eventstream.kafka

import eventstream.kafka.client.KafkaController
import eventstream.kafka.producer.KafkaMessage
import io.github.oshai.kotlinlogging.KotlinLogging

fun main() {
    val logger = KotlinLogging.logger("Kafka.EntryPoint")
    logger.info { "Event Stream Kafka App Running." }

    val kafkaController = KafkaController.getDefaultKafkaController()

    try {

        kafkaController.createTopic("my-topic", 3, 1)
        kafkaController.sendMessage("my-topic", "key1", "Hello Kafka!")

        val messages = listOf(
            KafkaMessage("my-topic", "key1", "Hello Kafka 1"),
            KafkaMessage("my-topic", "key2", "Hello Kafka 2"),
            KafkaMessage("my-topic", "key3", "Hello Kafka 3"),
            KafkaMessage("my-topic", "key3", "Msg 4"),
            KafkaMessage("my-topic", "key3", "Msg 5"),
            KafkaMessage("my-topic", "key3", "Msg 6"),
            KafkaMessage("my-topic", "key3", "Msg 7"),
        )

        kafkaController.sendMessages(messages)

        kafkaController.createTopic("new-topic", 3, 1)
        kafkaController.sendMessage("new-topic", "key1", "Hello Kafka11!")
        kafkaController.sendMessage("new-topic", "key2", "Hello Kafka1112222!")
        kafkaController.sendMessage("new-topic", "key3", "Hello Kafka3333!")
        kafkaController.sendMessage("new-topic", "key4", "Hello Kafka4444!")

        kafkaController.readMessages("my-topic", "my-consumer-group", 5000, "earliest") // Poll for 5 seconds

        val consumerGroups = kafkaController.listConsumerGroups()
        logger.info { "Consumer Groups: $consumerGroups" }

        kafkaController.describeConsumerGroup("my-consumer-group")

        val consumerLag = kafkaController.getConsumerLag("my-consumer-group")
        consumerLag.forEach { (partition, lag) ->
            logger.info { "Partition: $partition, Lag: $lag" }
        }

        logger.info { "Testing Topic Creation" }
        kafkaController.createTopic("temp-topic", 3, 1)

        logger.info { "Testing Topic Deletion" }
        kafkaController.deleteTopic("temp-topic")
        logger.info { "Topic Deletion Successful" }


        kafkaController.printTopicMetadata("my-topic")


    } catch (e: Exception) {
        logger.error(e) { "An error occurred in the Kafka operations." }
    } finally {
        kafkaController.close()
    }

    logger.info { "Event Stream Kafka App Ending." }


}