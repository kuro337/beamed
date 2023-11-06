package eventstream.kafka.producer

import eventstream.kafka.config.KafkaConfig

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class KafkaProducerWrapper(config: KafkaConfig) {
    private val producer = KafkaProducer<String, String>(config.toProperties())

    fun sendMessage(topic: String, key: String, message: String) {
        val record = ProducerRecord<String, String>(topic, key, message)
        producer.send(record)
    }

    fun close() {
        producer.close()
    }
}