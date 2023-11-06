package eventstream.kafka.config

import java.util.*


data class KafkaConfig(
    val bootstrapServers: String,
    val securityProtocol: String,
    val saslMechanism: String,
    val saslJaasConfig: String
) {
    fun toProperties(): Properties {
        return Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("security.protocol", securityProtocol)
            put("sasl.mechanism", saslMechanism)
            put("sasl.jaas.config", saslJaasConfig)
            put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        }
    }
}