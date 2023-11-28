package eventstream.beam.functional.pipeline.kafka

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer


/* Read from Kafka Topic and Return PCollection<KeyString,ValueString> */
object KafkaKubeOptions {
    private val password: String = System.getenv("KAFKA_PASSWORD") ?: ""
    val saslJaasConfig: String =
        "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"$password\";"
    const val securityProtocol: String = "SASL_PLAINTEXT"
    const val saslMechanism: String = "SCRAM-SHA-256"
}

/**
 * #### Read Messages from a Kafka Topic - `Pipeline.readFromKafka`
 *
 * - **Reading from Kafa with Default Config**
 * ```kotlin
 *
 *  /* Default Config */
 *  val kafkaMessages = pipeline.readFromKafka(
 *     bootstrapServers = "kafka.default.svc.cluster.local:9092",
 *     topic = "my-topic"
 * )
 * ```
 *
 * - **Reading with Custom Config**
 *
 * ```kotlin
 * /* Custom Config */
 * val kafkaMessages = pipeline.readFromKafka(
 *     bootstrapServers = "kafka.default.svc.cluster.local:9092",
 *     topic = "my-topic",
 *     saslJaasConfig = "custom-jaas-config",
 *     securityProtocol = "custom-protocol",
 *     saslMechanism = "custom-mechanism"
 * )
 * ```
 * @constructor KafkaController
 * @param bootstrapServers Kafka Broker URLs
 * @param topic Topic Name
 * @param offSet "latest" or "earliest"
 * @return PCollection<KV<String, String>>
 * @throws IOException If an input/output error occurs.
 * @author kuro337
 */
fun Pipeline.readFromKafka(
    bootstrapServers: String,
    topic: String,
    saslJaasConfig: String = KafkaKubeOptions.saslJaasConfig,
    securityProtocol: String = KafkaKubeOptions.securityProtocol,
    saslMechanism: String = KafkaKubeOptions.saslMechanism
): PCollection<KV<String, String>> {
    val kafkaRead = KafkaIO.read<String, String>()
        .withBootstrapServers(bootstrapServers)
        .withTopic(topic)
        .withKeyDeserializer(StringDeserializer::class.java)
        .withValueDeserializer(StringDeserializer::class.java)
        .withConsumerConfigUpdates(
            mapOf(
                "security.protocol" to securityProtocol,
                "sasl.mechanism" to saslMechanism,
                SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig
            )
        )
        .withoutMetadata()

    return this.apply("Read from Kafka topic $topic", kafkaRead)
}