package eventstream.app

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


}
