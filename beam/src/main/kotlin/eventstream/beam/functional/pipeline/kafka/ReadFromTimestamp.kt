package eventstream.beam.functional.pipeline.kafka

import eventstream.beam.logger.BeamLogger.logger
import eventstream.beam.pipelines.options.KafkaPipelineOptions
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.joda.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * #### Read Messages from a Kafka Topic - `Pipeline.readFromKafkaFromTimestamp`
 * - Example Usage
 * ```kotlin
 * val pipeline = Pipeline.create(/* your pipeline options */)
 *
 * /* Read from a Particular Timestamp */
 * val kafkaMessages = pipeline.readFromKafkaByTimestamp(
 *     bootstrapServers = "kafka-broker:9092",
 *     topic = "your-topic",
 *     dateTimeStr = "2023-01-31 15:30:00"
 * )
 *
 * /* Read from Current Timestamp */
 *
 * val kafkaMessages = pipeline.readFromKafkaByTimestamp(
 *     bootstrapServers = "kafka-broker:9092",
 *     topic = "your-topic"
 * )
 *
 * ```
 * @constructor KafkaController
 * @param bootstrapServers Kafka Broker URLs
 * @param topic Topic Name
 * @param dateTimeStr Timestamp to Begin Reading by yyyy-MM-dd HH:mm:ss
 * @param dateFormat "latest" or "earliest"
 * @return PCollection<KV<String, String>>
 * @throws IOException If an input/output error occurs.
 * @author kuro337
 */
fun Pipeline.readFromKafkaByTimestamp(
    topic: String,
    // groupId: String?,
    dateTimeStr: String? = null,
    dateFormat: String = "yyyy-MM-dd HH:mm:ss"
): PCollection<KV<String, String>> {

    // set group id logic too - for optional group id read. Consumer Group is created for a Consumer to maintain offsets.
    val kafkaOptions = this.options.`as`(KafkaPipelineOptions::class.java)
    val consumerConfig = mapOf(
        //if (groupId) "group.id" to groupId,
        "security.protocol" to kafkaOptions.getSecurityProtocol(),
        "sasl.mechanism" to kafkaOptions.getSaslMechanism(),
        SaslConfigs.SASL_JAAS_CONFIG to kafkaOptions.getSaslJaasConfig()
    )

    // Calculate start timestamp
    val startTimestampMillis = if (dateTimeStr != null) {
        val zoneId = ZoneId.of("UTC")
        val formatter = DateTimeFormatter.ofPattern(dateFormat)
        val specificDateTime = LocalDateTime.parse(dateTimeStr, formatter)
        specificDateTime.atZone(zoneId).toInstant().toEpochMilli()
    } else {
        System.currentTimeMillis()
    }
    logger.info { "Reading from Kafka topic $topic starting from timestamp: $startTimestampMillis" }

    // Create Kafka read transform
    val kafkaRead = KafkaIO.read<String, String>()
        .withBootstrapServers(kafkaOptions.getBootstrapServers())
        .withTopic(topic)
        .withKeyDeserializer(StringDeserializer::class.java)
        .withValueDeserializer(StringDeserializer::class.java)
        .withStartReadTime(Instant(startTimestampMillis))
        .withConsumerConfigUpdates(consumerConfig)
        .withoutMetadata() /* Only gets K/V from Message */

    return this.apply("Read from Kafka topic $topic from timestamp", kafkaRead)
}