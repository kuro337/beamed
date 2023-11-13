package eventstream.beam.read

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.values.PCollection

/**
 * `Read Interface`
 * @author kuro337
 * @sample eventstream.beam.read.BeamReader
 */
interface BeamReader<T : ReadParameters> {
    fun read(pipeline: Pipeline, params: T): PCollection<*>
}

open class ReadParameters(val inputDescriptors: List<String>)

