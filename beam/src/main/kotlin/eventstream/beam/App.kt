package eventstream.beam

import eventstream.beam.models.FredSeries
import eventstream.beam.pipeline.InMemoryPipeline


fun main() {

//    println("Hello World")
    val options = InMemoryPipeline.createOptions(
        files = listOf("data/input/simple_data_noheaders.csv"),
        output = "data/output/beam/csv/",
        beamEntityClass = FredSeries::class.java
    )


    val inMemoryPipeline = InMemoryPipeline(options)

//    inMemoryPipeline.run(PIPELINE.CSV_TO_ENTITY)

    inMemoryPipeline.run(PIPELINE.ENTITY_TO_CSV)

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

