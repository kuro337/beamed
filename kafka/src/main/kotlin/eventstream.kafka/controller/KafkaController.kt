package eventstream.kafka.client


import eventstream.kafka.config.KafkaConfig
import eventstream.kafka.consumer.KafkaConsumerWrapper
import eventstream.kafka.controller.KafkaControllerLogging
import eventstream.kafka.producer.KafkaProducerWrapper
import io.github.oshai.kotlinlogging.KotlinLogging
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

    fun createTopic(topicName: String, partitions: Int, replicationFactor: Short) {
        kafkaClient.createTopic(topicName, partitions, replicationFactor)
        logger.info { "Successfully created Topic: $topicName" }
    }

    fun sendMessage(topicName: String, key: String, message: String) {
        kafkaProducer.sendMessage(topicName, key, message)
        logger.info { "Successfully sent Event to: $topicName with Key $key" }

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
    fun readMessages(topicName: String, timeoutMillis: Long, offSet: String = "latest") {
        val kafkaConsumerWrapper = KafkaConsumerWrapper(config, offSet)
        kafkaConsumerWrapper.subscribe(listOf(topicName))

        try {
            val records = kafkaConsumerWrapper.poll(Duration.ofMillis(timeoutMillis))
            for (record in records) {
                logger.info { "Read Event from Offset record from topic ${record.topic()} with key ${record.key()}: ${record.value()}" }
            }
        } finally {
            kafkaConsumerWrapper.close()
        }
    }

    /*

    @KafkaConsumer

    fun readMessages(topic: String) {
        // Implement Kafka Consumer
        val consumer = KafkaConsumer<String, String>(config.toProperties())
    }

    */


}



