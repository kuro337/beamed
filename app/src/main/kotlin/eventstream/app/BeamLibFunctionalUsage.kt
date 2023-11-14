//package eventstream.app
//
//import eventstream.beam.functional.pcollections.writeToParquet
//import eventstream.beam.functional.pipeline.logElements
//import eventstream.beam.functional.pipeline.readCSVConvertToEntity
//import eventstream.beam.functional.pipeline.toGenericRecords
//import eventstream.beam.logger.BeamLogger
//import eventstream.beam.models.FredSeries
//import org.apache.beam.sdk.Pipeline
//
//
//fun main() {
//
//    /* @Usage of Beam Library using Functional Utilities */
//
//    /* Read Lines from CSV , Serialize, Transform to Generic Record, and Write as Parquet Files */
//
//    Pipeline.create().apply {
//        readCSVConvertToEntity<FredSeries>(
//            listOf("data/input/simple_data_noheaders.csv"),
//            FredSeries::serializeFromCsvLine
//        ).apply {
//            toGenericRecords<FredSeries>().apply {
//                logElements("Logged Element: ")
//            }.apply {
//                writeToParquet<FredSeries>("data/output/beam/parquet/")
//            }
//        }
//    }.run().waitUntilFinish()
//
//    /* Reading the transformed Parquet Files */
//
//    Pipeline.create().apply {
//        readParquetToGenericRecord<FredSeries>(
//            listOf(
//                "data/output/beam/parquet/output-00000-of-00004.parquet",
//                "data/output/beam/parquet/output-00001-of-00004.parquet",
//                "data/output/beam/parquet/output-00002-of-00004.parquet",
//                "data/output/beam/parquet/output-00003-of-00004.parquet",
//            )
//        ).apply {
//            convertToBeamEntity<FredSeries>()
//        }.apply {
//            logElements().also { BeamLogger.logger.info { "Successfully Serialized from Parquet Files." } }
//        }
//    }.run().waitUntilFinish()
//
//
//}
//
//
//
//
