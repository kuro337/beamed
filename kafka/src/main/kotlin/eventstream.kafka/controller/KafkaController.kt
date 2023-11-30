package eventstream.kafka.client


import eventstream.kafka.config.KafkaConfig
import eventstream.kafka.consumer.KafkaConsumerWrapper
import eventstream.kafka.controller.KafkaControllerLogging
import eventstream.kafka.producer.KafkaMessage
import eventstream.kafka.producer.KafkaProducerWrapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.admin.ConsumerGroupDescription
import org.apache.kafka.common.TopicPartition
import java.time.Duration


class KafkaController(private val config: KafkaConfig) {
    private val kafkaClient: KafkaClient = KafkaClient(config)
    private val kafkaProducer: KafkaProducerWrapper = KafkaProducerWrapper(config)

    companion object {

        private val logger = KotlinLogging.logger {}

        fun getDefaultKafkaController(): KafkaController {

            logger.info { "Getting Default Kafka Controller for Event Streaming MonoRepo." }

            val password = System.getenv("KAFKA_PASSWORD")
            if (password.isNullOrBlank()) {
                logger.error { KafkaControllerLogging.runtimeClientSecretMissingError() }
            }

            val kafkaConfig = KafkaConfig(
                bootstrapServers = "kafka.default.svc.cluster.local:9092",
                securityProtocol = "SASL_PLAINTEXT",
                saslMechanism = "SCRAM-SHA-256",
                saslJaasConfig = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"$password\";"
            )

            return KafkaController(kafkaConfig)
        }
    }

    init {
        logger.info { "Initializing KafkaController" }
    }

    fun close() {
        kafkaClient.close()
        kafkaProducer.close()
    }

    fun getTopics(print: Boolean = true): Set<String> {
        return kafkaClient.getTopics().also {
            if (print) logger.info { "Topics for Broker :$it" }
        }
    }


    fun printTopicMetadata(topicName: String): String {
        val topicMetadata = kafkaClient.getTopicInformation(topicName)
        logger.info { "<Metadata> for topic $topicName: $topicMetadata" }
        return topicMetadata
    }

    fun createTopic(topicName: String, partitions: Int, replicationFactor: Short) {
        kafkaClient.createTopic(topicName, partitions, replicationFactor)
        logger.info { "Successfully created Topic: $topicName" }
    }

    fun deleteTopic(topicName: String) {
        kafkaClient.deleteTopic(topicName)
    }

    fun sendMessage(topicName: String, key: String, message: String) {
        kafkaProducer.sendMessage(topicName, key, message)
        logger.info { "Successfully sent Event to: $topicName with Key $key" }
    }

    fun sendMessages(messages: List<KafkaMessage>) {
        messages.forEach { message ->
            kafkaProducer.sendMessage(message.topic, message.key, message.message)
            logger.info { "Successfully sent Event to: ${message.topic} with Key ${message.key}" }
        }
    }

    /**
     * #### `ConsumerRead`
     * - Example Usage
     * ```kotlin
     * val kafkaController = KafkaController.getDefaultKafkaController()
     * // Read Events for 5s from the beginning of the Topic
     * kafkaController.readMessages("my-topic", 5000, "earliest")
     * // Read Events for 10s that arrive after the Consumer has been launched
     * kafkaController.readMessages("my-topic2", 10000, "earliest") // Poll for 10 seconds - only events after c
     * ```
     * @constructor KafkaController
     * @param topicName Name of the Topic
     * @param timeoutMillis Duration in ms
     * @param offSet "latest" or "earliest"
     * @return Unit
     * @throws IOException If an input/output error occurs.
     * @author kuro337
     * @constructor KafkaController
     * @sample eventstream.kafka.client.KafkaController.readMessages
     */
    fun readMessages(
        topicName: String,
        groupId: String,
        timeoutMillis: Long,
        offSet: String = "latest",
        print: Boolean = true
    ): List<String> {
        val kafkaConsumerWrapper = KafkaConsumerWrapper(config, groupId, offSet)
        kafkaConsumerWrapper.subscribe(listOf(topicName))
        val messages = mutableListOf<String>()

        try {
            val records = kafkaConsumerWrapper.poll(Duration.ofMillis(timeoutMillis))
            records.forEach { record ->
                record.timestampType()
                messages.add(record.value()).also {
                    if (print) logger.info {
                        """
                    Topic:${record.topic()}:Key:${record.key()}:Value:${record.value()}
                    Timestamp: ${record.timestamp()}
                    Timestamp: ${record.timestampType()}
                    """.trimIndent()
                    }
                }
            }
        } finally {
            kafkaConsumerWrapper.close()
        }
        return messages
    }

    /*
    * Consumer Groups and Offsets
    * Consumer Group is created implicitly for each Consumer for a Topic Partition
    * Can define a Consumer Group Explicitly when Consuming Messages as well for more granularity
    *
     */

    // Expose KafkaClient's consumer group functionalities
    fun listConsumerGroups(): List<String> {
        return kafkaClient.listConsumerGroups()
    }

    fun describeConsumerGroup(groupId: String): ConsumerGroupDescription {
        return kafkaClient.describeConsumerGroup(groupId)
    }

    fun getConsumerLag(groupId: String): Map<TopicPartition, Long> {
        return kafkaClient.getConsumerLag(groupId)
    }


}



