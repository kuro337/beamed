package eventstream.beam.pipelines.options

import eventstream.beam.logger.BeamLogger.logger
import org.apache.beam.sdk.options.Description
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.PipelineOptionsFactory

interface KafkaPipelineOptions : PipelineOptions {

    @Description("Kafka bootstrap servers")
    fun getBootstrapServers(): String
    fun setBootstrapServers(value: String)

    @Description("Kafka SASL JAAS config")
    fun getSaslJaasConfig(): String
    fun setSaslJaasConfig(value: String)

    @Description("Kafka security protocol")
    fun getSecurityProtocol(): String
    fun setSecurityProtocol(value: String)

    @Description("Kafka SASL mechanism")
    fun getSaslMechanism(): String
    fun setSaslMechanism(value: String)


}

/*

val kafkaOptions = createKafkaPipelineOptions(args, groupId = "my-consumer-group")

val pipeline = Pipeline.create(kafkaOptions)

*/

/**
 * Creates KafkaPipelineOptions using provided command-line arguments.
 * Default values are used for any missing configurations.
 *
 * Usage example:
 * ```
 * val args = arrayOf("--bootstrapServers=kafka.server:9092", "--saslJaasConfig=jaasConfig", ...)
 * val kafkaOptions = createKafkaPipelineOptions(args)
 * val pipeline = Pipeline.create(kafkaOptions)
 * ```
 *
 * @param args Command-line arguments passed to the application.
 * @return Configured KafkaPipelineOptions.
 */
fun createKafkaPipelineOptions(args: Array<String>): KafkaPipelineOptions {

    val password: String = System.getenv("KAFKA_PASSWORD") ?: ""

    logger.info { "Passowrd is $password " }


    return PipelineOptionsFactory.fromArgs(*args)
        .withValidation()
        .`as`(KafkaPipelineOptions::class.java).apply {
            // Set default values if not provided in args
            if (getBootstrapServers().isNullOrBlank()) {
                setBootstrapServers("kafka.default.svc.cluster.local:9092")
            }
            if (getSaslJaasConfig().isNullOrBlank()) {
                setSaslJaasConfig(
                    "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"${password}\";"
                )
            }
            if (getSecurityProtocol().isNullOrBlank()) {
                setSecurityProtocol("SASL_PLAINTEXT")
            }
            if (getSaslMechanism().isNullOrBlank()) {
                setSaslMechanism("SCRAM-SHA-256")
            }
        }
}

/*

object KafkaKubeOptions {
    private val password: String = System.getenv("KAFKA_PASSWORD") ?: ""
    val saslJaasConfig: String =
        "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"$password\";"
    const val securityProtocol: String = "SASL_PLAINTEXT"
    const val saslMechanism: String = "SCRAM-SHA-256"
}
 */