package eventstream.beam.functional.pipeline.kafka

import eventstream.beam.pipelines.options.createKafkaPipelineOptions
import org.apache.beam.sdk.Pipeline


/**
 * Extension function to create a Beam Pipeline with KafkaPipelineOptions.
 *
 * Usage example:
 * ```
 * val args = arrayOf("--bootstrapServers=kafka.server:9092", "--saslJaasConfig=jaasConfig", ...)
 * val pipeline = Pipeline.createWithKafkaOptions(args)
 * ```
 *
 * @param args Command-line arguments.
 * @return A Pipeline object configured with KafkaPipelineOptions.
 */
fun Pipeline.createWithKafkaOptions(args: Array<String>): Pipeline {
    val kafkaOptions = createKafkaPipelineOptions(args)
    return Pipeline.create(kafkaOptions)
}
