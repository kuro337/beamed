package eventstream.beam.ingest

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.Flatten
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PCollectionList

// Source interface for reading data into the pipeline
open class ReadParameters(val inputDescriptors: List<String>)

// Specialized parameters
class S3ReadParameters(inputDescriptors: List<String>, val bucketName: String) : ReadParameters(inputDescriptors)
class CSVReadParameters(inputFiles: List<String>) : ReadParameters(inputFiles)

interface PCollectionSource<T> {
    fun read(pipeline: Pipeline, params: ReadParameters): PCollection<T>
}

object CSVSource : PCollectionSource<String> {
    override fun read(pipeline: Pipeline, params: ReadParameters): PCollection<String> {

        params as? CSVReadParameters
            ?: throw IllegalArgumentException("CSVSource requires CSVReadParameters for reading.")

        val inputFiles = params.inputDescriptors

        if (inputFiles.size == 1) {
            return pipeline.apply("Read CSV File", TextIO.read().from(inputFiles.first()))
        }

        // If there are multiple patterns, create a PCollection for each and flatten them into one.
        val filePCollections = inputFiles.map { pattern ->
            pipeline.apply("Read CSV File Pattern $pattern", TextIO.read().from(pattern))
        }
        return PCollectionList.of(filePCollections)
            .apply("Flatten PCollectionList into one PCollection", Flatten.pCollections())

    }
}