package eventstream.beam

import eventstream.beam.models.FredSeriesMod
import eventstream.beam.pipeline.InMemoryPipeline
import eventstream.beam.pipeline.InMemoryPipelineOptions


fun main() {

    println("Hello World")
    val files = listOf("data/input/simple_data_noheaders.csv")
    val options = InMemoryPipeline.createOptions(files)

    val inMemoryPipeline = InMemoryPipeline(options)

    // Transform CSV Rows to be all Uppercase
    //inMemoryPipeline.run(PIPELINE.CAPITALIZE_LINE)

    // Explicitly Pass New Options on a Pipeline to Process Different Data
//    inMemoryPipeline.run(
//        PIPELINE.CAPITALIZE_LINE, InMemoryPipelineOptions(
//            inputs = listOf("data/input/fred_series.csv", "data/input/fred_series2.csv"),
//            output = "data/output/beam",
//            writeFiles = false
//        )
//    )

//    inMemoryPipeline.run(
//        PIPELINE.CSV_SERIALIZE_ROWS,
//        InMemoryPipelineOptions(inputs = listOf("data/input/simple_data_noheaders.csv"))
//    )

    inMemoryPipeline.run(
        PIPELINE.SERIALIZE_ENTITY_FROM_CSV,
        InMemoryPipelineOptions(
            serializer = FredSeriesMod.Companion,
            inputs = listOf("data/input/simple_data_noheaders.csv")
        )
    )


    /* Test Serialization for Beam in Isolation */
//    val entity: SerializableEntity<FredSeriesMod> = FredSeriesMod.Companion
//    val coder: Coder<FredSeriesMod?> = NullableCoder.of(AvroCoder.of(FredSeriesMod::class.java))
//    val doFn = ConvertCsvToEntityFn(entity, coder)
//    SerializableUtils.ensureSerializable(doFn)


}


// Run CSV_SERIALIZE_ROWS pipeline process
// inMemoryPipeline.run(PIPELINE.CSV_SERIALIZE_ROWS)//    InMemoryPipeline.runCSVSchema("data/input/simple_data_noheaders.csv")

//
//    InMemoryPipeline.runCsvFedSeriesPipeline("data/input/simple_data_noheaders.csv")
//
//    InMemoryPipeline.runFredSeriesCategoricalAnalysis("data/input/simple_data_noheaders.csv")
//
//    InMemoryPipeline.runSimpleSchema()
//
//    SerializeModels.serializeFedSeries()
//
//    SerializeModels.serializeFedSeriesMod()


/*
@Pipelines

- @InMemory -> Process and Transform Files from Disk
- @S3Memory -> Pull Data from Object Storage and Process in Memory
- @FlinkS3  -> Submit a job to an external Flink Cluster that pulls data from s3 & processes it

@Usage InMemory
InMemoryPipeline.runCSVRowMapperPipeline(listOf("data/input/fred_series.csv","data/input/fred_series2.csv"),"data/output/beam")

@Usage S3Memory
S3MemoryPipeline.run()

@Usage FlinkS3
FlinkS3Pipeline.run()

 */

