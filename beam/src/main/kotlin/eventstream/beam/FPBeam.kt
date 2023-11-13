/*
package eventstream.beam

import eventstream.beam.functional.pcollections.writeToParquet
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.functional.pipeline.readCSVConvertToEntity
import eventstream.beam.functional.pipeline.toGenericRecords
import eventstream.beam.models.FredSeries
import org.apache.beam.sdk.Pipeline

fun main() {

    /* Read Lines from CSV , Serialize, Transform to Generic Record, and Write as Parquet Files */

    Pipeline.create().apply {
        readCSVConvertToEntity<FredSeries>(
            listOf("data/input/simple_data_noheaders.csv"),
            FredSeries::serializeFromCsvLine
        ).apply {
            toGenericRecords<FredSeries>().apply {
                logElements("Logged Element: ")
            }.apply {
                writeToParquet<FredSeries>("data/output/beam/parquet/")
            }
        }
    }.run().waitUntilFinish()


}

*/