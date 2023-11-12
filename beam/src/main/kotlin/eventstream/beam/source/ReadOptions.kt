package eventstream.beam.source

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.values.PCollection


interface PCollectionSource<T> {
    fun read(pipeline: Pipeline, params: ReadParameters): PCollection<T>
}

open class ReadParameters(val inputDescriptors: List<String>)
