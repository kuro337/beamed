package eventstream.beam.demo

import eventstream.beam.models.FredSeries
import eventstream.beam.models.SimpleSchema
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor


object CSVSchema {
    private val logger = KotlinLogging.logger {}

    fun fredSeriesSingletonPipeline(pipeline: Pipeline): PCollection<FredSeries> {
        val fredSeriesForTesting = FredSeries()
        
        logger.info { "Object being Used for Pipeline $fredSeriesForTesting" }

        pipeline.getSchemaRegistry().registerPOJO(FredSeries::class.java)

        val singleFredSeriesPCollection: PCollection<FredSeries> = pipeline.apply(
            Create.of(fredSeriesForTesting)
                .withCoder(AvroCoder.of(FredSeries::class.java))
        )

        // Apply a simple transformation to log the FredSeries objects
        singleFredSeriesPCollection.apply(
            "Print Row", ParDo.of(object : DoFn<FredSeries, Void>() {
                @ProcessElement
                fun processElement(c: ProcessContext) {
                    logger.info { c.element().toString() } // Log the FredSeries object
                }
            })
        )

        return singleFredSeriesPCollection
    }

    fun applySerializationFromCsvFile(lines: PCollection<String>): PCollection<FredSeries> {


//        val lines: PCollection<String> = pipeline.apply("Create File Patterns", Create.of(inputFiles))

        val fredSeriesModPCollection: PCollection<FredSeries?> = lines
            .apply(
                "Convert Lines to FredSeries", MapElements
                    .into(TypeDescriptor.of(FredSeries::class.java))
                    .via(SerializableFunction<String, FredSeries?> { line ->
                        try {
                            FredSeries.serializeFromCsvLine(line)
                        } catch (e: Exception) {
                            null // or log/handle the error appropriately
                        }
                    })
            ).setCoder(AvroCoder.of(FredSeries::class.java))


        // @ Filter Failed Serializations
        val nonNullFredSeriesPCollection: PCollection<FredSeries> = fredSeriesModPCollection
            .apply("Filter Nulls", ParDo.of(object : DoFn<FredSeries?, FredSeries>() {
                @ProcessElement
                fun processElement(@Element fredSeriesMod: FredSeries, out: OutputReceiver<FredSeries>) {
                    fredSeriesMod?.let { out.output(it) }
                }

            })).setCoder(AvroCoder.of(FredSeries::class.java))

        nonNullFredSeriesPCollection.apply(
            "Print FredSeries", ParDo.of(object : DoFn<FredSeries, Void>() {
                @ProcessElement
                fun processElement(@Element fredSeriesMod: FredSeries, context: ProcessContext) {
                    logger.info { fredSeriesMod }
                }
            })
        )

        return nonNullFredSeriesPCollection
    }


    fun applySimpleSerialization(pipeline: Pipeline) {
        pipeline.getSchemaRegistry().registerPOJO(SimpleSchema::class.java)
        val simpleObj = SimpleSchema("John", 25.4)
        logger.info { "Obj being used : ${simpleObj.toString()} " }

        // Create a PCollection with a single SimpleSchema object
        val simpleSchemaPCollection: PCollection<SimpleSchema> = pipeline.apply(
            Create.of(simpleObj).withCoder(AvroCoder.of(SimpleSchema::class.java))
        )
        // Apply a simple transformation to log the SimpleSchema objects
        simpleSchemaPCollection.apply(
            ParDo.of(object : DoFn<SimpleSchema, Void>() {
                @ProcessElement
                fun processElement(c: ProcessContext) {
                    logger.info { c.element().toString() }
                }
            })
        )
    }
}

/*



*/