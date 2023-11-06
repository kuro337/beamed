package eventstream.beam.pipeline.transform.csv

import eventstream.beam.pipeline.InMemoryPipeline
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.values.TypeDescriptor

class CsvRowMapper {
    companion object {
        private val logger = KotlinLogging.logger {}

        fun applyCSVProcessing(pipeline: Pipeline, file: String, output: String) {
            logger.info { "Applying CSV processing for file: $file" }

            pipeline.apply("Read Data", TextIO.read().from(file))
                .apply(
                    "Process Rows", MapElements.into(TypeDescriptor.of(String::class.java))
                        .via(InMemoryPipeline.StringUpperCaseFunction())
                )
                .apply("Write Output", TextIO.write().to(output).withSuffix(".txt"))
        }
    }
}