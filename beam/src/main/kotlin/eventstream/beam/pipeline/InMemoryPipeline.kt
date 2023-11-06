package eventstream.beam.pipeline

import eventstream.beam.pipeline.analyze.analyzeGenericColsFredSeries
import eventstream.beam.pipeline.decorators.PipelineType
import eventstream.beam.pipeline.transform.csv.CSVSchema
import eventstream.beam.pipeline.transform.csv.CsvRowMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SimpleFunction
import org.apache.beam.sdk.values.TypeDescriptor


@PipelineType("InMemory")
class InMemoryPipeline() {

    class StringUpperCaseFunction : SimpleFunction<String, String>() {
        override fun apply(input: String): String {
            return input.uppercase()
        }
    }

    companion object {
        fun run(data: List<String>, output: String, writeFiles: Boolean = false): Unit {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            logger.debug { "Creating Pipeline" }

            val pipeline = Pipeline.create().also {
                logger.debug { "Successfully Created Pipeline" }
            }

            data.forEach { file ->
                pipeline
                    .apply("Read Data", TextIO.read().from(file))
                    .apply(
                        "Process Rows", MapElements
                            .into(TypeDescriptor.of(String::class.java))
                            .via(StringUpperCaseFunction())
                    ).let {
                        if (writeFiles) it.apply(
                            "Write Output", TextIO.write()
                                .to(output).withSuffix(".txt")
                        )
                    }.also {
                        logger.debug { "Transformed $it" }
                    }

                pipeline.run().waitUntilFinish()
            }
            logger.info { "Successfully Executed In-Memory Pipeline with ${data.size} files." }

        }


        fun runCSVRowMapperPipeline(data: List<String>, output: String) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            data.forEach { file ->
                val pipeline = Pipeline.create()
                CsvRowMapper.applyCSVProcessing(pipeline, file, output)
                pipeline.run().waitUntilFinish()
            }

            logger.info { "Successfully Executed In-Memory Pipeline with ${data.size} files." }
        }


        fun runCsvFedSeriesPipeline(data: String) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()

            CSVSchema.applySerializationFromCsvFile(pipeline, data)

            pipeline.run().waitUntilFinish()

            logger.info { "Successfully executed In-Memory Pipeline for file: $data" }
        }

        fun runFredSeriesCategoricalAnalysis(data: String) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()

            val fredCollection = CSVSchema.applySerializationFromCsvFile(pipeline, data)

            analyzeGenericColsFredSeries(fredCollection, listOf("title", "units", "popularity"))

            pipeline.run().waitUntilFinish()

            logger.info { "Successfully executed In-Memory Pipeline for file: $data" }
        }

        fun runCSVSchema(data: String) {
            val logger = KotlinLogging.logger {}
            logger.info { "Running In-Memory Pipeline" }

            val pipeline = Pipeline.create()

            CSVSchema.applyCsvClassSerialization(pipeline)

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


