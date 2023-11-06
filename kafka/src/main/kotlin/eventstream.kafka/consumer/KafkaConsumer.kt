package eventstream.kafka.consumer


import eventstream.kafka.config.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

class KafkaConsumerWrapper(config: KafkaConfig, offset: String) {
    private val consumer: KafkaConsumer<String, String>

    init {
        /*
            latest (reads msgs after consumer starts)
            earliest (reads from the beginning)
        */

        val props = Properties()

        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = offset
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = config.bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "kafka-consumer-group"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props["security.protocol"] = config.securityProtocol
        props["sasl.mechanism"] = config.saslMechanism
        props["sasl.jaas.config"] = config.saslJaasConfig
        /* Additional Consumer Settings such as Offset as Required */

        consumer = KafkaConsumer(props)
    }

    fun subscribe(topics: List<String>) {
        consumer.subscribe(topics)
    }

    fun poll(duration: Duration): List<ConsumerRecord<String, String>> {
        val records = consumer.poll(duration)
        return records.toList()
    }

    fun close() {
        consumer.close()
    }
}
