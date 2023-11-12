package eventstream.beam.pipeline

import eventstream.beam.*
import eventstream.beam.effects.WriteCollection
import eventstream.beam.models.FredSeries
import eventstream.beam.pipeline.decorators.PipelineType
import eventstream.beam.source.CSVReadParameters
import eventstream.beam.source.CSVSource
import eventstream.beam.transformations.csv.*
import eventstream.beam.transformations.csv.TransformationParams
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.values.PCollection


class InMemoryPipelineOptions(
    val files: List<String>,
    val output: String? = null,
    val writeFiles: Boolean? = false,
    val beamEntityClass: Class<out BeamEntity>? = null
) :
    BeamPipelineOptions {
    override fun requiredOptions(): Unit {
        println(
            """
            Required Options for In-Memory Pipeline:
            
            val inputs: List<String>,
            val output: String? = null,
            val writeFiles: Boolean? = false,
            val serializer: BeamEntityStatic<out BeamEntity>? = null /* Covariant Type */
            
            val directPipeline = InMemoryPipelineOptions(
                inputs = listOf("data/input/fred_series.csv", "data/input/fred_series2.csv"),
                output = "data/output/beam",
                writeFiles = false,
                /* 
                serializer should be a static class that 
                implements the BeamEntityStatic interface 
                */
                serializer = YourModel.Companion
            )
            """.trimIndent()
        )
    }
}


@PipelineType("InMemory")
class InMemoryPipeline(private val options: InMemoryPipelineOptions) : BeamPipeline {
    private val logger = KotlinLogging.logger { "InMemoryPipeline.Init" }

    private val pipelines = listOf(
        PIPELINE.CAPITALIZE_LINE,
        PIPELINE.CSV_SERIALIZE_ROWS,
        PIPELINE.CATEGORICAL_ANALYSIS,
        PIPELINE.CSV_TO_ENTITY_COUNT_FIELDS,
        PIPELINE.CSV_TO_ENTITY_TO_PARQUET,
        PIPELINE.CSV_TO_ENTITY,
        PIPELINE.PARQUET_TO_ENTITY,
        PIPELINE.ENTITY_TO_CSV
    )

    override fun getPipelines(): List<PIPELINE> {
        return pipelines
    }

    override fun getOptions(): BeamPipelineOptions = options

    override fun run(pipelineType: PIPELINE) {
        logger.info { "Running Direct Pipeline - $pipelineType with Options Set" }
        executePipeline(pipelineType, options)

    }

    /* @Override for Explicit Options */
    fun run(pipelineType: PIPELINE, options: InMemoryPipelineOptions) {
        logger.info { "Running Direct Pipeline - $pipelineType with Options Override" }
        executePipeline(pipelineType, options)
    }


    private fun executePipeline(pipelineType: PIPELINE, options: InMemoryPipelineOptions): Unit {
        when (pipelineType) {

            PIPELINE.CAPITALIZE_LINE -> csvCapitalizePipeline(options)

            PIPELINE.CSV_TO_ENTITY -> csvLinesToBeamEntitySerialization(options)

            PIPELINE.ENTITY_TO_CSV -> entityToCSVWrite(options)

            PIPELINE.CATEGORICAL_ANALYSIS,
            PIPELINE.CSV_TO_ENTITY_COUNT_FIELDS -> serializeFromCSVAnalyzeEntity(options)


            PIPELINE.CSV_TO_ENTITY_TO_PARQUET -> serializeEntityFromFileAndTransformToParquet(options)

            PIPELINE.PARQUET_TO_ENTITY -> serializeParquetFilesFromDisk(options)

            else -> throw IllegalArgumentException("Pipeline type not supported by this class")
        }
    }

    /*
    *  Predefined Pipelines that showcase using Library Functionality
    *
    * - These Pipelines can be used with ANY Model that abides by the Interfaces Defined
    *
    * - To leverage functionality from the library -
    * - Simply define a Model that satisfies these interfaces
    *
    * @Class             : interface BeamEntity
    * @Companion Object  : Abstract Class BeamEntityStatic
    *
    * -> class AnyGenericModel : BeamEntityStatic<out BeamEntity>
    *
    * - Transform to and from Normal UTF String Files
    * - Transform to and from CSV
    * - Transform to and from Parquet
    * - Analyze Metadata for Entities
    *
    * */
    companion object {
        fun createOptions(
            files: List<String>,
            output: String? = null,
            writeFiles: Boolean? = false,
            beamEntityClass: Class<out BeamEntity>? = null
        ): InMemoryPipelineOptions {
            return InMemoryPipelineOptions(files, output, writeFiles, beamEntityClass)
        }

        fun csvLinesToBeamEntitySerialization(options: InMemoryPipelineOptions) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)

            println("Lines Read")

            // Define the SerializationParams with FredSeries class
            val serializationParams = SerializationParams(options.beamEntityClass as Class<FredSeries>)

            // Apply the SerializeEntityFromCSVLines transformation
            val fredSeriesCollection = SerializeEntityFromCSVLines<FredSeries>()
                .apply(lines, serializationParams)

            val loggedCollection = logEntityCollection(fredSeriesCollection)

            pipeline.run().waitUntilFinish()

        }

        fun entityToCSVWrite(options: InMemoryPipelineOptions) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)


            val serializationParams = SerializationParams(options.beamEntityClass as Class<FredSeries>)

            /* CSV -> Entity */
            val fredSeriesCollection = SerializeEntityFromCSVLines<FredSeries>()
                .apply(lines, serializationParams)

            /* @LogEntity  */
            val loggedCollection = logEntityCollection(fredSeriesCollection)

            /* PCollection<T> -> PCollection<String>  */
            val csvLines: PCollection<String> =
                EntityToCsv<FredSeries>().apply(fredSeriesCollection, TransformationParams())

            options.output?.also {
                WriteCollection.outputCollections(csvLines, options.output, "csv")
            }

            pipeline.run().waitUntilFinish()

        }


        fun serializeParquetFilesFromDisk(options: InMemoryPipelineOptions) {
            val pipeline = Pipeline.create()

//            options.serializer?.also {
//                //val genericRecords = readAndLogParquetFiles(options.inputs[0], pipeline, options.serializer.getStaticSchema())
//
//                println("Serializer Passed Running Func")
//                val readParams = ParquetReadParameters(options.inputs, options.serializer.getStaticSchema())
//
//                val genericRecordsFromDisk = ParquetFileToGenericRecordCollection.read(pipeline, readParams)
//
//                GenericRecordToBeamEntity.printGenericRecordFields(genericRecordsFromDisk)
//
//                println("Applied Pipeline")
//            }


            pipeline.run().waitUntilFinish()
            println("Finished Pipeline")

        }

        fun csvCapitalizePipeline(options: InMemoryPipelineOptions) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)
            val upperCaseLines = CsvTransformation.ConvertLinesUppercase.apply(
                lines,
                UppercaseTransformParams(options.output, options.writeFiles)
            )

            pipeline.run().waitUntilFinish()
        }


        fun serializeFromCSVAnalyzeEntity(
            options: InMemoryPipelineOptions,
        ) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)

//            /* 1. Convert PCollection<String> to PCollection<BeamEntity> */
//            options.serializer?.also { serializer ->
//                val serializationParams = SerializationParams(serializer)
//                val entityCollection = BeamSerialization.SerializeEntityFromCSVLines
//                    .apply(lines, serializationParams)
//
//                /* 2. Count Frequencies of any Entity's fields */
//                analyzeGenericCols(
//                    entityCollection,
//                    listOf("title", "units", "popularity"),
//                )

//                pipeline.run().waitUntilFinish()
//            } ?: throw IllegalArgumentException("No serializer provided for entity transformation.")
        }

        fun serializeEntityFromFileAndTransformToParquet(
            options: InMemoryPipelineOptions,
        ) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)

//            /* 1. Convert PCollection<String> to PCollection<BeamEntity> */
//            options.serializer?.also { serializer ->
//                val serializationParams = SerializationParams(serializer)
//                val entityCollection = BeamSerialization.SerializeEntityFromCSVLines
//                    .apply(lines, serializationParams)


            /* 2. Write PCollection<BeamEntity> to Parquet Files
            *   - Converts:
            *     PCollection<BeamEntity> -> PCollection<GenericRecord>
            *     (Required for Parquet)
            * */

//                val genericRecordPCollection = CollectionToGenericRecord.apply(
//                    entityCollection,
//                    params = CollectionToGenRecordParams(
//                        serializer.getStaticSchema(),
//                        writeParquetPath = options.output // Uncomment to Write Parquet Output files
//                    )
//                )
//
//                pipeline.run().waitUntilFinish()
//            } ?: throw IllegalArgumentException("No serializer provided for entity transformation.")
        }

    }
}


