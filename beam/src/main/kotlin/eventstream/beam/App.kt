package eventstream.beam


fun main() {


//    InMemoryPipeline.runCSVSchema("data/input/simple_data_noheaders.csv")
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

}

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

