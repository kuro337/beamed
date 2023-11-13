/*
package eventstream.beam

import eventstream.beam.interfaces.pipeline.PIPELINE
import eventstream.beam.models.FredSeries
import eventstream.beam.pipelines.InMemoryPipeline


fun main() {

    val options = InMemoryPipeline.createOptions(
        files = listOf("data/input/simple_data_noheaders.csv"),
        output = "data/output/beam/parquet/",
        writeFiles = false,
        beamEntityClass = FredSeries::class.java
    )


    val inMemoryPipeline = InMemoryPipeline(options)

    /* Transform CSV Data */
    inMemoryPipeline.run(PIPELINE.CSV_TO_ENTITY)

    /* Transform CSV Files into Parquet */
    inMemoryPipeline.run(PIPELINE.CSV_TO_ENTITY_TO_PARQUET)

    /* Write CSV */
    // inMemoryPipeline.run(PIPELINE.ENTITY_TO_CSV)

}

*/

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

