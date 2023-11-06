package eventstream.app

import eventstream.beam.models.FredSeries
import eventstream.utilities.io.csv.readCsvByClassPath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FredSeriesTest {
    companion object {
        private const val VALID_CSV_LINE =
            "PCU113310113310,\"Producer Price Index by Industry: Logging\",1981-12-01,2023-09-01,Monthly,Index Dec 1981=100,Not Seasonally Adjusted,2023-10-11 08:05:34-05,1,13,"
        private const val INVALID_CSV_LINE = "INVALID_DATA"
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[XXX][X]")
    }

    @Test
    fun testSerializeFromCSVLineSuccess() {
        val fredSeries = FredSeries.Companion.serializeFromCSVLine(VALID_CSV_LINE)
        assertNotNull(fredSeries, "Serialization should succeed for valid CSV line")
        fredSeries?.let {
            assertEquals("PCU113310113310", it.id)
            assertEquals("Producer Price Index by Industry: Logging", it.title)
            assertEquals(LocalDate.parse("1981-12-01", dateFormatter), it.observationStart)
            assertEquals(LocalDate.parse("2023-09-01", dateFormatter), it.observationEnd)
            assertEquals(LocalDateTime.parse("2023-10-11 08:05:34-05", dateTimeFormatter), it.lastUpdated)
            assertEquals(1, it.popularity)
            assertEquals(13, it.groupPopularity)
            assertEquals("", it.notes)
        }
    }

    @Test
    fun testSerializeFromCSVLineFailure() {
        val fredSeries = FredSeries.Companion.serializeFromCSVLine(INVALID_CSV_LINE)
        assertNull(fredSeries, "Serialization should fail for invalid CSV line")
    }

    @Test
    fun testSerializationFromReadingCSV() {
        val lines = readCsvByClassPath("mock_data.csv").drop(1) // drop header

        var successfulCount = 0
        var failedCount = 0

        lines.map {
            FredSeries.serializeFromCSVLine(it)?.also {
                successfulCount++
            } ?: run {
                failedCount++
            }
        }

        assertEquals(lines.size, successfulCount)
        assertEquals(0, failedCount)


    }
}
