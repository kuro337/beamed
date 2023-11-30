package eventstream.beam


import eventstream.beam.functional.pcollections.filterByNumericField
import eventstream.beam.functional.pcollections.filterByStringField
import eventstream.beam.functional.pipeline.*
import eventstream.beam.logger.BeamLogger
import eventstream.beam.models.FredSeries
import org.apache.beam.sdk.Pipeline

fun main() {

    /* @Usage of Beam Library using Functional Utilities */

    /* Read Lines from CSV , Serialize, Transform to Generic Record, and Write as Parquet Files */

    Pipeline.create().apply {
        readCSVConvertToEntity<FredSeries>(
            listOf("data/input/simple_data_noheaders.csv"),
            FredSeries::serializeFromCsvLine
        ).apply {
            toGenericRecords<FredSeries>().apply {
                logElements("Logged Element: ")
            }.apply {
                // writeToParquet<FredSeries>("data/output/beam/parquet/")
            }
        }
    }.run().waitUntilFinish()

    /* Reading the transformed Parquet Files , Serializing , and Logging */


    Pipeline.create().apply {
        readAllParquetFromDirectory<FredSeries>(
            "data/output/beam/parquet/"
        ).apply {
            convertToBeamEntity<FredSeries>()
        }.apply {
            logElements().also { BeamLogger.logger.info { "Successfully Serialized from Parquet Files." } }
        }
    }.run().waitUntilFinish()


    /* Distributed Categorical Analysis */

    val pipeline = Pipeline.create()

    val genericRecords = pipeline.readAllParquetFromDirectory<FredSeries>("data/output/beam/parquet/")
    val converted = genericRecords.convertToBeamEntity<FredSeries>()

    val filteredByPopularity = converted.filterByNumericField(FredSeries::popularity) { it > 5 }
    val filteredByTimeline = converted.filterByStringField(FredSeries::lastUpdated) { it.contains("2023") }

    filteredByTimeline.logElements("Updated this year : ")
    filteredByPopularity.logElements("Popularity > 5 : ")

    pipeline.run().waitUntilFinish()


}




