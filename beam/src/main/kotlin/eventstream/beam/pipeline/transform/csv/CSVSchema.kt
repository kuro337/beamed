package eventstream.beam.pipeline.transform.csv

import eventstream.beam.models.FredSeriesMod
import eventstream.beam.models.SerializeModels.serializeGetFredSeriesModSingleton
import eventstream.beam.models.SimpleSchema
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor


object CSVSchema {
    private val logger = KotlinLogging.logger {}

    fun applyCsvClassSerialization(pipeline: Pipeline) {
        val fredSeriesForTesting = serializeGetFredSeriesModSingleton()
        logger.info { "Object being Used for Pipeline $fredSeriesForTesting" }

        pipeline.getSchemaRegistry().registerPOJO(FredSeriesMod::class.java)

        val singleFredSeriesPCollection: PCollection<FredSeriesMod> = pipeline.apply(
            Create.of(fredSeriesForTesting)
                .withCoder(AvroCoder.of(FredSeriesMod::class.java))
        )

        // Apply a simple transformation to log the FredSeriesMod objects
        singleFredSeriesPCollection.apply(
            "Print Row", ParDo.of(object : DoFn<FredSeriesMod, Void>() {
                @ProcessElement
                fun processElement(c: ProcessContext) {
                    logger.info { c.element().toString() } // Log the FredSeriesMod object
                }
            })
        )
    }

    fun applySerializationFromCsvFile(pipeline: Pipeline, file: String): PCollection<FredSeriesMod> {
        val fredSeriesModPCollection: PCollection<FredSeriesMod?> = pipeline
            .apply("Read Data", TextIO.read().from(file))
            .apply(
                "Convert Lines to FredSeriesMod", MapElements
                    .into(TypeDescriptor.of(FredSeriesMod::class.java))
                    .via(SerializableFunction<String, FredSeriesMod?> { line ->
                        FredSeriesMod.serializeFromCSVLine(line)
                    })
            ).setCoder(AvroCoder.of(FredSeriesMod::class.java))

        // @ Filter Failed Serializations
        val nonNullFredSeriesModPCollection: PCollection<FredSeriesMod> = fredSeriesModPCollection
            .apply("Filter Nulls", ParDo.of(object : DoFn<FredSeriesMod?, FredSeriesMod>() {
                @ProcessElement
                fun processElement(@Element fredSeriesMod: FredSeriesMod, out: OutputReceiver<FredSeriesMod>) {
                    fredSeriesMod?.let { out.output(it) }
                }

            })).setCoder(AvroCoder.of(FredSeriesMod::class.java))

        nonNullFredSeriesModPCollection.apply(
            "Print FredSeriesMod", ParDo.of(object : DoFn<FredSeriesMod, Void>() {
                @ProcessElement
                fun processElement(@Element fredSeriesMod: FredSeriesMod, context: ProcessContext) {
                    logger.info { fredSeriesMod }
                }
            })
        )

        return nonNullFredSeriesModPCollection
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