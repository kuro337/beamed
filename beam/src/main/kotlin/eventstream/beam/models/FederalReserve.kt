package eventstream.beam.models

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.schemas.JavaFieldSchema
import org.apache.beam.sdk.schemas.annotations.DefaultSchema
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

@DefaultSchema(JavaFieldSchema::class)
data class FredSeries(
    var id: String = "",
    var title: String = "",
    var observationStart: LocalDate = LocalDate.of(2000, 1, 1), // Example default date
    var observationEnd: LocalDate = LocalDate.of(2000, 1, 1),   // Example default date
    var frequency: String = "",
    var units: String = "",
    var seasonal_adjustment: String = "",
    var lastUpdated: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0), // Example default datetime
    var popularity: Int = 0,
    var groupPopularity: Int = 0,
    var notes: String = ""
) : Serializable {


    companion object {
        private val logger = KotlinLogging.logger {}

        val pattern = Pattern.compile("\\s*(\"[^\"]*\"|[^,]*)\\s*,")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[XXX][X]")

        fun serializeFromCSVLine(line: String): FredSeries? {
            try {
                val matcher = pattern.matcher(line + ",")
                val splitCols = mutableListOf<String>()

                while (matcher.find()) {
                    val match = matcher.group(1)
                    splitCols.add(
                        if (match.startsWith("\"") && match.endsWith("\"")) {
                            match.substring(1, match.length - 1).replace("\"\"", "\"")
                        } else {
                            match
                        }
                    )
                }

                if (splitCols.size < 11) {
                    throw IllegalArgumentException("CSV line has fewer columns than expected: found ${splitCols.size}, expected at least 11.")
                }

                val observationStart = tryParseDate(splitCols[2].trim(), "observationStart")
                val observationEnd = tryParseDate(splitCols[3].trim(), "observationEnd")
                val lastUpdated = tryParseDateTime(splitCols[7].trim(), "lastUpdated")

                if (observationStart == null || observationEnd == null || lastUpdated == null) {
                    return null // Early return if any date parse failed
                }

                return FredSeries(
                    id = splitCols[0].trim(),
                    title = splitCols[1].trim(),
                    observationStart = observationStart,
                    observationEnd = observationEnd,
                    frequency = splitCols[4].trim(),
                    units = splitCols[5].trim(),
                    seasonal_adjustment = splitCols[6].trim(),
                    lastUpdated = lastUpdated,
                    popularity = splitCols[8].toIntOrNull() ?: 0,
                    groupPopularity = splitCols[9].toIntOrNull() ?: 0,
                    notes = if (splitCols[10].isNotBlank()) splitCols[10].trim() else ""
                )
            } catch (e: IllegalArgumentException) {
                println("Error with CSV line format: $line; ${e.message}")
                return null
            } catch (e: Exception) {
                println("Unexpected error processing CSV line: $line; ${e.message}")
                return null
            }
        }


        fun tryParseDate(input: String, fieldName: String): LocalDate? {
            return try {
                LocalDate.parse(input, dateFormatter)
            } catch (e: DateTimeParseException) {
                logger.error(e) { "Error parsing $fieldName in CSV line: $input" }
                null
            }
        }

        fun tryParseDateTime(input: String, fieldName: String): LocalDateTime? {
            return try {
                val regex = Regex(".+[+-]\\d{2}$")
                val modifiedInput = if (regex.matches(input)) {
                    "$input:00" // Append missing ":00"
                } else {
                    input // Use the input as is
                }
                // Try parsing the modified input
                LocalDateTime.parse(modifiedInput, dateTimeFormatter)
            } catch (e: DateTimeParseException) {
                logger.error(e) { "Error parsing $fieldName in CSV line: $input" }
                null
            }
        }


    }
}
