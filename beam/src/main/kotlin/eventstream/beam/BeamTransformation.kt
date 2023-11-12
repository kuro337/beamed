package eventstream.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.values.PCollection


enum class TRANSFORMATION {
    FLINK_S3,
    CAPITALIZE_LINE,
    ENTITY_TO_CSV,
    CSV_SERIALIZE_ROWS,
    CATEGORICAL_ANALYSIS,
    CSV_TO_ENTITY,
    CSV_TO_ENTITY_COUNT_FIELDS,
    CSV_TO_ENTITY_TO_PARQUET,
    PARQUET_TO_ENTITY,
    ENTITY_TO_GENERIC_RECORD
}

/**
 * #### `ConsumerRead`
 * - Example Usage
 * ```kotlin
 *
 * data class TransformationParams(val input : List<String> , val data : DataType)
 *
 *
 *
 * object FooBarBeamTransformation :
 *   BeamTransformation <
 *      TransformationParams,
 *      PCollection<String>,
 *      PCollection<DataType>>() {
 *
 *         override val transformationType = TRANSFORMATION.SENSIBLE_ENUM_IDENTIFIER
 *
 *         override fun apply(input: PCollection<String>, params: TransformationParams):  PCollection<DataType> {
 *             return functionThatPerformsTransformation(input, params)
 *         }
 *
 *
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
abstract class BeamTransformation<PARAMS, INPUT, OUTPUT> {
    abstract val transformationType: TRANSFORMATION
    abstract fun apply(input: INPUT, params: PARAMS): OUTPUT
}

sealed class TransformationParams {
    object NoParams : TransformationParams()
    data class SomeParams(val data: String) : TransformationParams()
    // ... other parameter types
}

abstract class FlexibleBeamTransformationContract<INPUT, OUTPUT> {
    abstract val transformationType: TRANSFORMATION
    abstract fun apply(input: INPUT, params: TransformationParams): OUTPUT
}


interface PCollectionTransformation<T, U> {
    fun apply(input: PCollection<T>, pipeline: Pipeline): PCollection<U>
}

interface PCollectionSink<T> {
    fun apply(input: PCollection<T>, pipeline: Pipeline)
}

