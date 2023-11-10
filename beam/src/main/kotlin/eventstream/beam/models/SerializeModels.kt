package eventstream.beam.models

import eventstream.utilities.io.csv.printCsvHeaders
import eventstream.utilities.io.csv.readCsvLines
import io.github.oshai.kotlinlogging.KotlinLogging


object SerializeModels {
    private var logger = KotlinLogging.logger("Serialize Models")
    fun serializeAndReturnSingleModel(): FredSeries? {
        val testLine =
            "PCU113310113310,Producer Price Index by Industry: Logging,1981-12-01,2023-09-01,Monthly,Index Dec 1981=100,Not Seasonally Adjusted,2023-10-11 08:05:34-05,1,13,"

        val parsedObject = FredSeries.serializeFromCSVLine(testLine)
        logger.info { parsedObject }

        return parsedObject
    }

    fun serializeGetFredSeriesModSingleton(): FredSeriesMod? {
        val testLine =
            "PCU113310113310,Producer Price Index by Industry: Logging,1981-12-01,2023-09-01,Monthly,Index Dec 1981=100,Not Seasonally Adjusted,2023-10-11 08:05:34-05,1,13,"

        val parsedObject = FredSeriesMod.serializeFromCsvLine(testLine)
        logger.info { parsedObject }

        return parsedObject
    }

    fun serializeFedSeries() {
        printCsvHeaders("data/input/simple_data.csv")
        val lines = readCsvLines("data/input/simple_data.csv").drop(1) // drop header

        var successfulCount = 0
        var failedCount = 0

        val fredSeriesList = lines.mapNotNull { line ->
            FredSeries.Companion.serializeFromCSVLine(line)?.also {
                successfulCount++
            } ?: run {
                failedCount++
                null
            }
        }

        fredSeriesList.forEach { println(it) }

        logger.info { "Number of successful serializations: $successfulCount" }
        logger.info { "Number of failed serializations: $failedCount" }


        logger.info { "Testing Single Serialize." }

        val testLine =
            "PCU113310113310,Producer Price Index by Industry: Logging,1981-12-01,2023-09-01,Monthly,Index Dec 1981=100,Not Seasonally Adjusted,2023-10-11 08:05:34-05,1,13,"

        val parsedObject = FredSeries.serializeFromCSVLine(testLine)
        logger.info { parsedObject }
    }

    fun serializeFedSeriesMod() {
        printCsvHeaders("data/input/simple_data.csv")
        val lines = readCsvLines("data/input/simple_data.csv").drop(1) // drop header

        var successfulCount = 0
        var failedCount = 0

        val fredSeriesList = lines.mapNotNull { line ->
            FredSeriesMod.Companion.serializeFromCsvLine(line)?.also {
                successfulCount++
            } ?: run {
                failedCount++
                null
            }
        }

        fredSeriesList.forEach { println(it) }

        logger.info { "Number of successful serializations: $successfulCount" }
        logger.info { "Number of failed serializations: $failedCount" }

        logger.info { "Testing Single Serialize." }

        val testLine =
            "PCU113310113310,Producer Price Index by Industry: Logging,1981-12-01,2023-09-01,Monthly,Index Dec 1981=100,Not Seasonally Adjusted,2023-10-11 08:05:34-05,1,13,"

        val parsedObject = FredSeriesMod.serializeFromCsvLine(testLine)
        logger.info { parsedObject }
    }
}