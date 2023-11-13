package eventstream.beam.pipelines

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.getParquetSchemaNoNamespace
import eventstream.beam.interfaces.entity.logEntityCollection
import eventstream.beam.interfaces.pipeline.BeamPipeline
import eventstream.beam.interfaces.pipeline.BeamPipelineOptions
import eventstream.beam.interfaces.pipeline.PIPELINE
import eventstream.beam.models.FredSeries
import eventstream.beam.pipelines.decorators.PipelineType
import eventstream.beam.read.CSVReadParameters
import eventstream.beam.read.CSVSource
import eventstream.beam.read.ParquetFileToGenericRecordCollection
import eventstream.beam.read.ParquetReadParameters
import eventstream.beam.transformations.csv.*
import eventstream.beam.transformations.helpers.logGenericRecords
import eventstream.beam.transformations.parquet.CollectionToGenericRecord
import eventstream.beam.transformations.parquet.EntityToGenericRecordParams
import eventstream.beam.transformations.parquet.GenericRecordToBeamEntity
import eventstream.beam.write.WriteCollection
import eventstream.beam.write.WriteParquetCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.avro.generic.GenericRecord
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
        Logger.logger.info {
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
        }
    }
}

object Logger {
    val logger = KotlinLogging.logger { "eventstream.beam.pipelines.InMemoryPipeline" }
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

            PIPELINE.PARQUET_TO_ENTITY -> serializeParquetFilesFromDisk(options)

            PIPELINE.CSV_TO_ENTITY_TO_PARQUET -> readCSVToEntityToGenericRecordToParquetWrite(options)

            PIPELINE.CATEGORICAL_ANALYSIS,
            PIPELINE.CSV_TO_ENTITY_COUNT_FIELDS -> serializeFromCSVAnalyzeEntity(options)


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

            Logger.logger.info { "Lines Read" }

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

            /* List<String(Filenames)> -> PCollection<String> */
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)


            /* CSV -> Entity */
            val serializationParams = SerializationParams(options.beamEntityClass as Class<BeamEntity>)
            val fredSeriesCollection = SerializeEntityFromCSVLines<BeamEntity>()
                .apply(lines, serializationParams)

            /* @LogEntity  */
            val loggedCollection = logEntityCollection(fredSeriesCollection)

            /* PCollection<T> -> PCollection<String>  */
            val csvLines: PCollection<String> =
                EntityToCsv<BeamEntity>().apply(fredSeriesCollection, TransformationParams())

            options.output?.also {
                WriteCollection.outputCollections(csvLines, options.output, "csv")
            }

            pipeline.run().waitUntilFinish()

        }


        fun serializeParquetFilesFromDisk(options: InMemoryPipelineOptions) {
            val pipeline = Pipeline.create()

            val readParquetLines = ParquetReadParameters(options.files, options.beamEntityClass as Class<BeamEntity>)

            val genericRecordsFromParquet = ParquetFileToGenericRecordCollection<BeamEntity>().read(
                pipeline,
                ParquetReadParameters(options.files, options.beamEntityClass)
            )

            val convertedRecords = GenericRecordToBeamEntity<BeamEntity>().apply(
                genericRecordsFromParquet,
                SerializationParams(options.beamEntityClass)
            )

            val loggedCollection = logEntityCollection(convertedRecords)


            pipeline.run().waitUntilFinish()
            Logger.logger.info { "Finished Pipeline" }

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


        fun readCSVToEntityToGenericRecordToParquetWrite(options: InMemoryPipelineOptions) {

            val pipeline = Pipeline.create()

            /* List<String(Filenames)> -> PCollection<String> */
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)

            /* CSV -> Entity */
            val serializationParams = SerializationParams(options.beamEntityClass as Class<BeamEntity>)
            val entityCollection = SerializeEntityFromCSVLines<BeamEntity>()
                .apply(lines, serializationParams)


            /* @LogEntity PCollection<BeamEntity> */
            logEntityCollection(entityCollection)

            /* PCollection<BeamEntity> -> PCollection<GenericRecord> */
            val genericRecords: PCollection<GenericRecord> = CollectionToGenericRecord<BeamEntity>().apply(
                entityCollection,
                EntityToGenericRecordParams(options.beamEntityClass)
            )

            logGenericRecords(genericRecords)

            /* @Write PCollection<GenericRecord> to Parquet File on Disk */
            if (options.writeFiles == true && options.output != null) {
                WriteParquetCollection.writeParquetCollectionToDisk(
                    genericRecords,
                    options.beamEntityClass.getParquetSchemaNoNamespace(),
                    options.output
                )
            }

            pipeline.run().waitUntilFinish()
            Logger.logger.info { "Finished Pipeline" }

        }

        fun serializeFromCSVAnalyzeEntity(
            options: InMemoryPipelineOptions,
        ) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)


        }

        fun serializeEntityFromFileAndTransformToParquet(
            options: InMemoryPipelineOptions,
        ) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.files)
            val lines = CSVSource.read(pipeline, readParams)


        }

    }
}



