package eventstream.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.values.PCollection


enum class TRANSFORMATION {
    CAPITALIZE_LINE, CSV_SERIALIZE_ROWS, SERIALIZE_ENTITY, CATEGORICAL_ANALYSIS, CSV_SCHEMA, SIMPLE_SCHEMA
}

// Define the interface
abstract class BeamTransformation<PARAMS, INPUT, OUTPUT> {
    abstract val transformationType: TRANSFORMATION
    abstract fun apply(input: INPUT, params: PARAMS): OUTPUT
}


interface PCollectionTransformation<T, U> {
    fun apply(input: PCollection<T>, pipeline: Pipeline): PCollection<U>
}

interface PCollectionSink<T> {
    fun apply(input: PCollection<T>, pipeline: Pipeline)
}

