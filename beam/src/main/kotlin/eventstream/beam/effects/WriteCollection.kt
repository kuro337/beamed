package eventstream.beam.effects

import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PDone

/**
 * #### `ConsumerRead`
 * - Example Usage
 * ```kotlin
 *  upperCaseLines.apply("Write Output", TextIO.write().to(outputPath).withSuffix(".txt"))
 *
 *
 *
 * ```
 * @constructor WriteCollection
 * @param topicName Name of the Topic
 * @param timeoutMillis Duration in ms
 * @param offSet "latest" or "earliest"
 * @return Unit
 * @throws IOException If an input/output error occurs.
 * @author kuro337
 * @constructor KafkaController
 * @sample eventstream.kafka.client.KafkaController.readMessages
 */
class WriteCollection {
    fun outputCollections(input: PCollection<String>, outputPath: String, suffix: String): PDone {
        val output = input.apply(
            "Write Output", TextIO.write().to(outputPath).withSuffix(suffix)
        )
        return output
    }
}