//package eventstream.beam.demo
//
//
//import eventstream.beam.*
//import eventstream.beam.interfaces.entity.BeamEntity
//import eventstream.beam.interfaces.pipeline.BeamPipeline
//import eventstream.beam.interfaces.pipeline.BeamPipelineOptions
//import eventstream.beam.interfaces.pipeline.PIPELINE
//import eventstream.beam.pipeline.decorators.PipelineType
//import eventstream.beam.pipelines.decorators.PipelineType
//import eventstream.beam.read.CSVReadParameters
//import eventstream.beam.read.CSVSource
//import eventstream.beam.source.CSVReadParameters
//import eventstream.beam.source.CSVSource
//import eventstream.beam.transformations.csv.*
//import eventstream.beam.transformations.parquet.CollectionToGenRecordParams
//import eventstream.beam.transformations.parquet.CollectionToGenericRecord
//import io.github.oshai.kotlinlogging.KotlinLogging
//import org.apache.beam.sdk.Pipeline
//
//
//class DirectPipelineDemoOptions(
//    val inputs: List<String>,
//    val output: String? = null,
//    val writeFiles: Boolean? = false,
//    val serializer: StaticBeamEntity<out BeamEntity>? = null /* Covariant Type */
//) :
//    BeamPipelineOptions {
//    override fun requiredOptions(): Unit {
//        println(
//            """
//            Required Options for In-Memory Pipeline:
//
//            val inputs: List<String>,
//            val output: String? = null,
//            val writeFiles: Boolean? = false,
//            val serializer: SerializableEntity<out BeamEntity>? = null /* Covariant Type */
//
//            val directPipeline = DirectPipelineDemoOptions(
//                inputs = listOf("data/input/fred_series.csv", "data/input/fred_series2.csv"),
//                output = "data/output/beam",
//                writeFiles = false,
//                /*
//                serializer should be a static class that
//                implements the SerializableEntity interface
//                */
//                serializer = YourModel.Companion
//            )
//            """.trimIndent()
//        )
//    }
//}
//
//
//@PipelineType("DirectPipelineDemo")
//class DirectPipeline(private val options: DirectPipelineDemoOptions) : BeamPipeline {
//    private val logger = KotlinLogging.logger { "DirectPipeline.Init" }
//
//    private val pipelines = listOf(PIPELINE.CAPITALIZE_LINE, PIPELINE.CSV_SERIALIZE_ROWS, PIPELINE.CATEGORICAL_ANALYSIS)
//    override fun getPipelines(): List<PIPELINE> {
//        return pipelines
//    }
//
//    override fun getOptions(): BeamPipelineOptions = options
//
//    override fun run(pipelineType: PIPELINE) {
//        logger.info { "Running Direct Pipeline - $pipelineType with Options Set" }
//        executePipeline(pipelineType, options)
//
//    }
//
//    /* @Override for Explicit Options */
//    fun run(pipelineType: PIPELINE, options: DirectPipelineDemoOptions) {
//        logger.info { "Running Direct Pipeline - $pipelineType with Options Override" }
//        executePipeline(pipelineType, options)
//    }
//
//
//    private fun executePipeline(pipelineType: PIPELINE, options: DirectPipelineDemoOptions): Unit {
//        when (pipelineType) {
//            PIPELINE.CAPITALIZE_LINE -> csvCapitalizePipeline(options)
//            PIPELINE.CSV_SERIALIZE_ROWS -> runCsvFedSeriesPipeline(options)
//            PIPELINE.CATEGORICAL_ANALYSIS -> runFredSeriesCategoricalAnalysis(options)
//            PIPELINE.CSV_TO_ENTITY -> csvLinesToBeamModelSerialization(options)
//            else -> throw IllegalArgumentException("Pipeline type not supported by this class")
//        }
//    }
//
//    companion object {
//        fun createOptions(
//            data: List<String>,
//            output: String? = null,
//            writeFiles: Boolean? = false,
//            serializer: StaticBeamEntity<out BeamEntity>? = null
//        ): DirectPipelineDemoOptions {
//            return DirectPipelineDemoOptions(data, output, writeFiles, serializer)
//        }
//
//        fun csvCapitalizePipeline(options: DirectPipelineDemoOptions) {
//            val pipeline = Pipeline.create()
//            val readParams = CSVReadParameters(options.inputs)
//            val lines = CSVSource.read(pipeline, readParams)
//            val upperCaseLines = CsvTransformation.ConvertLinesUppercase.apply(
//                lines,
//                UppercaseTransformParams(options.output, options.writeFiles)
//            )
//
//            pipeline.run().waitUntilFinish()
//        }
//
//        fun csvLinesToBeamModelSerialization(entityOptions: DirectPipelineDemoOptions) {
//            val pipeline = Pipeline.create()
//            val readParams = CSVReadParameters(entityOptions.inputs)
//            val lines = CSVSource.read(pipeline, readParams)
//
//            /* 1. Convert PCollection<String> to PCollection<BeamEntity> */
//            entityOptions.serializer?.also { serializer ->
//                val serializationParams = SerializationParams(serializer)
//                val entityCollection = BeamSerialization.SerializeEntityFromCSVLines
//                    .apply(lines, serializationParams)
//
//                /* 2. Count Frequencies of any Entity's fields */
//                analyzeGenericCols(
//                    entityCollection,
//                    listOf("title", "units", "popularity"),
//                )
//
//                /* 3. Write PCollection<BeamEntity> to Parquet Files
//                *   - Converts:
//                *     PCollection<BeamEntity> -> PCollection<GenericRecord>
//                *     (Required for Parquet)
//                * */
//
//                val genericRecordPCollection = CollectionToGenericRecord.apply(
//                    entityCollection,
//                    params = CollectionToGenRecordParams(
//                        serializer.getAvroSchema(),
//                        // writeParquetPath = "data/output/beam" // Uncomment to Write Parquet Output files
//                    )
//                )
//
//                pipeline.run().waitUntilFinish()
//            } ?: throw IllegalArgumentException("No serializer provided for entity transformation.")
//        }
//
//        fun categoricalEntityPipeline(
//            options: DirectPipelineDemoOptions,
//        ) {
//            val logger = KotlinLogging.logger {}
//            logger.info { "Running In-Memory Pipeline" }
//
//            val pipeline = Pipeline.create()
//            val readParams = CSVReadParameters(options.inputs)
//            val lines = CSVSource.read(pipeline, readParams)
//            val fredCollection = CSVSchema.applySerializationFromCsvFile(lines)
//
//            analyzeGenericColsFredSeries(fredCollection, listOf("title", "units", "popularity"))
//
//            pipeline.run().waitUntilFinish()
//
//            logger.info { "Successfully executed In-Memory Pipeline for files." }
//        }
//
//        fun runCsvFedSeriesPipeline(
//            options: DirectPipelineDemoOptions,
//        ) {
//            val logger = KotlinLogging.logger {}
//            logger.info { "Running In-Memory Pipeline" }
//
//            val pipeline = Pipeline.create()
//            val readParams = CSVReadParameters(options.inputs)
//            val lines = CSVSource.read(pipeline, readParams)
//            CSVSchema.applySerializationFromCsvFile(lines)
//
//            pipeline.run().waitUntilFinish()
//
//            logger.info { "Successfully executed In-Memory Pipeline for files." }
//        }
//
//        fun runFredSeriesCategoricalAnalysis(
//            options: DirectPipelineDemoOptions,
//        ) {
//            val logger = KotlinLogging.logger {}
//            logger.info { "Running In-Memory Pipeline" }
//
//            val pipeline = Pipeline.create()
//            val readParams = CSVReadParameters(options.inputs)
//            val lines = CSVSource.read(pipeline, readParams)
//            val fredCollection = CSVSchema.applySerializationFromCsvFile(lines)
//
//            analyzeGenericColsFredSeries(fredCollection, listOf("title", "units", "popularity"))
//
//            pipeline.run().waitUntilFinish()
//
//            logger.info { "Successfully executed In-Memory Pipeline for files." }
//        }
//
//        fun runCSVSchema(data: String) {
//            val logger = KotlinLogging.logger {}
//            logger.info { "Running In-Memory Pipeline" }
//
//            val pipeline = Pipeline.create()
//
//            CSVSchema.fredSeriesSingletonPipeline(pipeline)
//
//            pipeline.run().waitUntilFinish()
//
//            logger.info { "Successfully executed In-Memory Pipeline for file: $data" }
//        }
//
//        fun runSimpleSchema() {
//            val logger = KotlinLogging.logger {}
//            logger.info { "Running In-Memory Pipeline" }
//
//            val pipeline = Pipeline.create()
//
//            CSVSchema.applySimpleSerialization(pipeline)
//
//            pipeline.run().waitUntilFinish()
//
//            logger.info { "Successfully executed Simple In-Memory Pipeline." }
//        }
//    }
//}
//
//
