package eventstream.beam.pipeline

import eventstream.beam.*
import eventstream.beam.ingest.CSVReadParameters
import eventstream.beam.ingest.CSVSource
import eventstream.beam.models.FredSeriesMod
import eventstream.beam.pipeline.analyze.analyzeGenericColsFredSeries
import eventstream.beam.pipeline.decorators.PipelineType
import eventstream.beam.pipeline.transform.csv.BeamParquet
import eventstream.beam.pipeline.transform.csv.CSVSchema
import eventstream.beam.transformations.BeamSerialization
import eventstream.beam.transformations.CsvTransformation
import eventstream.beam.transformations.SerializationParams
import eventstream.beam.transformations.UppercaseTransformParams
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.values.PCollection


class InMemoryPipelineOptions(
    val inputs: List<String>,
    val output: String? = null,
    val writeFiles: Boolean? = false,
    val serializer: SerializableEntity<out BeamEntity>? = null // out-projected type for flexibility

) :
    BeamPipelineOptions {
    override fun requiredOptions(): Unit {
        println("Required Options")
    }

}


// Since the EntityOptions now expects a serializer that works on any subtype of T where T is a BeamEntity,
// you should be able to pass FredSeriesMod.Companion as the serializer to EntityOptions<FredSeriesMod> directly.


@PipelineType("InMemory")
class InMemoryPipeline(private val options: InMemoryPipelineOptions) : BeamPipeline {
    private val logger = KotlinLogging.logger { "InMemoryPipeline.Init" }

    private val pipelines = listOf(PIPELINE.CAPITALIZE_LINE, PIPELINE.CSV_SERIALIZE_ROWS, PIPELINE.CATEGORICAL_ANALYSIS)
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
            PIPELINE.CSV_SERIALIZE_ROWS -> runCsvFedSeriesPipeline(options)
            PIPELINE.CATEGORICAL_ANALYSIS -> runFredSeriesCategoricalAnalysis(options)
            PIPELINE.SERIALIZE_ENTITY_FROM_CSV -> csvLinesToBeamModelSerialization(options)
            else -> throw IllegalArgumentException("Pipeline type not supported by this class")
        }
    }

    companion object {
        fun createOptions(
            data: List<String>,
            output: String? = null,
            writeFiles: Boolean? = false,
            serializer: SerializableEntity<out BeamEntity>? = null
        ): InMemoryPipelineOptions {
            return InMemoryPipelineOptions(data, output, writeFiles, serializer)
        }

        fun csvCapitalizePipeline(options: InMemoryPipelineOptions) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.inputs)
            val lines = CSVSource.read(pipeline, readParams)
            val upperCaseLines = CsvTransformation.ConvertLinesUppercase.apply(
                lines,
                UppercaseTransformParams(options.output, options.writeFiles)
            )

            pipeline.run().waitUntilFinish()
        }

        fun csvLinesToBeamModelSerialization(entityOptions: InMemoryPipelineOptions) {
            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(entityOptions.inputs)
            val lines = CSVSource.read(pipeline, readParams)

            entityOptions.serializer?.also { serializer ->
                // Use the SerializationParams to wrap your serializer before calling apply.

                val serializationParams = SerializationParams(serializer as SerializableEntity<BeamEntity>)

//                val serializationParams = SerializationParams(serializer)

                val entityCollection = BeamSerialization.SerializeEntityFromCSVLines
                    .apply(lines, serializationParams)


                BeamSerialization.SerializeEntityFromCSVLines.analyzeGenericCols(
                    entityCollection,
                    listOf("title", "units", "popularity"),
                )

                // data/output/beam

                BeamParquet.writeFredSeriesModToParquet(
                    entityCollection as PCollection<FredSeriesMod>,
                    "data/output/beam"
                )


                pipeline.run().waitUntilFinish()
            } ?: throw IllegalArgumentException("No serializer provided for entity transformation.")
        }

        fun categoricalEntityPipeline(
            options: InMemoryPipelineOptions,
        ) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.inputs)
            val lines = CSVSource.read(pipeline, readParams)
            val fredCollection = CSVSchema.applySerializationFromCsvFile(lines)

            analyzeGenericColsFredSeries(fredCollection, listOf("title", "units", "popularity"))

            pipeline.run().waitUntilFinish()

            logger.info { "Successfully executed In-Memory Pipeline for files." }
        }

        fun runCsvFedSeriesPipeline(
            options: InMemoryPipelineOptions,
        ) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.inputs)
            val lines = CSVSource.read(pipeline, readParams)
            CSVSchema.applySerializationFromCsvFile(lines)

            pipeline.run().waitUntilFinish()

            logger.info { "Successfully executed In-Memory Pipeline for files." }
        }

        fun runFredSeriesCategoricalAnalysis(
            options: InMemoryPipelineOptions,
        ) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()
            val readParams = CSVReadParameters(options.inputs)
            val lines = CSVSource.read(pipeline, readParams)
            val fredCollection = CSVSchema.applySerializationFromCsvFile(lines)

            analyzeGenericColsFredSeries(fredCollection, listOf("title", "units", "popularity"))

            pipeline.run().waitUntilFinish()

            logger.info { "Successfully executed In-Memory Pipeline for files." }
        }

        fun runCSVSchema(data: String) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()

            CSVSchema.fredSeriesSingletonPipeline(pipeline)

            pipeline.run().waitUntilFinish()

            logger.info { "Successfully executed In-Memory Pipeline for file: $data" }
        }

        fun runSimpleSchema() {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()

            CSVSchema.applySimpleSerialization(pipeline)

            pipeline.run().waitUntilFinish()

            logger.info { "Successfully executed Simple In-Memory Pipeline." }
        }
    }
}


