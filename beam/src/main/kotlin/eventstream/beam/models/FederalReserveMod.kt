package eventstream.beam.models

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.schemas.JavaFieldSchema
import org.apache.beam.sdk.schemas.annotations.DefaultSchema
import org.apache.beam.sdk.schemas.annotations.SchemaCreate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

@DefaultSchema(JavaFieldSchema::class)

class FredSeriesMod {
    var id: String
    var title: String
    var observationStart: String
    var observationEnd: String
    var frequency: String
    var units: String
    var seasonal_adjustment: String
    var lastUpdated: String
    var popularity: Int
    var groupPopularity: Int
    var notes: String


    // Constructor with parameters
    @SchemaCreate
    constructor(
        id: String,
        title: String,
        observationStart: String,
        observationEnd: String,
        frequency: String,
        units: String,
        seasonal_adjustment: String,
        lastUpdated: String,
        popularity: Int,
        groupPopularity: Int,
        notes: String,
    ) {
        this.id = id
        this.title = title
        this.observationStart = observationStart
        this.observationEnd = observationEnd
        this.frequency = frequency
        this.units = units
        this.seasonal_adjustment = seasonal_adjustment
        this.lastUpdated = lastUpdated
        this.popularity = popularity
        this.groupPopularity = groupPopularity
        this.notes = notes

    }

    // No-argument constructor for Avro
    constructor() {
        id = ""
        title = ""
        observationStart = ""
        observationEnd = ""
        frequency = ""
        units = ""
        seasonal_adjustment = ""
        lastUpdated = ""
        popularity = 0
        groupPopularity = 0
        notes = ""
    }


    fun getObservationStartDate(): LocalDate? {
        return tryParseDate(this.observationStart, "observationStart")
    }

    fun getObservationEndDate(): LocalDate? {
        return tryParseDate(this.observationEnd, "observationEnd")
    }

    fun getLastUpdatedDateTime(): LocalDateTime? {
        return tryParseDateTime(this.lastUpdated, "lastUpdated")
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        val pattern = Pattern.compile("\\s*(\"[^\"]*\"|[^,]*)\\s*,")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[XXX][X]")


        fun serializeFromCSVLine(line: String): FredSeriesMod? {
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

                return FredSeriesMod(
                    id = splitCols[0].trim(),
                    title = splitCols[1].trim(),
                    observationStart = splitCols[2].trim(),
                    observationEnd = splitCols[3].trim(),
                    frequency = splitCols[4].trim(),
                    units = splitCols[5].trim(),
                    seasonal_adjustment = splitCols[6].trim(),
                    lastUpdated = splitCols[7].trim(),
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

    override fun toString(): String {
        return "FredSeriesMod(id='$id', title='$title', observationStart=$observationStart, observationEnd=$observationEnd, frequency='$frequency', units='$units', seasonal_adjustment='$seasonal_adjustment', lastUpdated=$lastUpdated, popularity=$popularity, groupPopularity=$groupPopularity, notes='$notes')"
    }
}

/*

fun getObservationStartDate(): LocalDate = LocalDate.parse(observationStart, DateTimeFormatter.ISO_LOCAL_DATE)

fun getObservationEndDate(): LocalDate = LocalDate.parse(observationEnd, DateTimeFormatter.ISO_LOCAL_DATE)

*/