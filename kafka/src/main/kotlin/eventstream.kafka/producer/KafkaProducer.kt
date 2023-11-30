package eventstream.kafka.producer

import eventstream.kafka.config.KafkaConfig

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

data class KafkaMessage(val topic: String, val key: String, val message: String)

class KafkaProducerWrapper(config: KafkaConfig) {
    private val producer = KafkaProducer<String, String>(config.toProperties())

    fun sendMessage(topic: String, key: String, message: String) {
        val record = ProducerRecord<String, String>(topic, key, message)
        producer.send(record)
    }

    fun sendMessages(messages: List<KafkaMessage>) {
        messages.forEach { message ->
            sendMessage(message.topic, message.key, message.message)
        }
    }

    fun close() {
        producer.close()
    }
}