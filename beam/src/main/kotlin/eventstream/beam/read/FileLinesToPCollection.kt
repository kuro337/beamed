/*
*
* **** Read Text File into a PCollection for Beam ****
*
* @Input
* - Pass the Pipeline , List of File Paths
*
* @Output
* -> Returns a PCollection<String> holding each Line
*
* @Usage
* val pipeline = Pipeline.create()
* val readParams = CSVReadParameters(options.inputs)
* val lines = CSVSource.read(pipeline, readParams)
*/


package eventstream.beam.read

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.Flatten
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PCollectionList


class CSVReadParameters(inputFiles: List<String>) : ReadParameters(inputFiles)

class S3ReadParameters(inputDescriptors: List<String>, val bucketName: String) : ReadParameters(inputDescriptors)


object CSVSource : BeamReader<CSVReadParameters> {
    /**
     * #### `Read Text File into a PCollection for Beam`
     *
     * - Usage
     *
     * ```kotlin
     * val pipeline = Pipeline.create()
     *
     * val readParams = CSVReadParameters(options.inputs)
     *
     * val lines = CSVSource.read(pipeline, readParams)
     * ```
     * @constructor KafkaController
     * @param pipeline Name of the Topic
     * @param params Duration in ms
     * @return PCollection<String>
     * @throws IOException If an input/output error occurs.
     * @author kuro337
     * @constructor CSVSource
     * @sample eventstream.beam.read
     */
    override fun read(pipeline: Pipeline, params: CSVReadParameters): PCollection<String> {

        params as? CSVReadParameters
            ?: throw IllegalArgumentException("CSVSource requires CSVReadParameters for reading.")

        val inputFiles = params.inputDescriptors

        if (inputFiles.size == 1) {
            return pipeline.apply("Read CSV File", TextIO.read().from(inputFiles.first()))
        }

        val filePCollections = inputFiles.map { pattern ->
            pipeline.apply("Read CSV File Pattern $pattern", TextIO.read().from(pattern))
        }

        println("Lines Read from CSV")

        return PCollectionList.of(filePCollections)
            .apply("Flatten PCollectionList into one PCollection", Flatten.pCollections())

    }
}