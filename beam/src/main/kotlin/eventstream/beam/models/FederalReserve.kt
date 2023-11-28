package eventstream.beam.models

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.CsvParsable
import eventstream.beam.interfaces.entity.ParquetParsable
import eventstream.beam.logger.BeamLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.schemas.JavaFieldSchema
import org.apache.beam.sdk.schemas.annotations.DefaultSchema
import org.apache.beam.sdk.schemas.annotations.SchemaCreate
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

@DefaultSchema(JavaFieldSchema::class)
data class FredSeries @SchemaCreate constructor(
    var id: String = "",
    var title: String = "",
    var observationStart: String = "",
    var observationEnd: String = "",
    var frequency: String = "",
    var units: String = "",
    var seasonal_adjustment: String = "",
    var lastUpdated: String = "",
    var popularity: Int = 0,
    var groupPopularity: Int = 0,
    var notes: String = ""
) : BeamEntity {


    override fun getFieldValue(fieldName: String): Any? {
        return when (fieldName) {
            "id" -> this.id
            "title" -> this.title
            "observationStart" -> this.observationStart
            "observationEnd" -> observationEnd
            "frequency" -> frequency
            "units" -> units
            "seasonal_adjustment" -> seasonal_adjustment
            "lastUpdated" -> lastUpdated
            "popularity" -> popularity
            "groupPopularity" -> groupPopularity
            "notes" -> notes
            else -> throw IllegalArgumentException("Field not found")
        }
    }

    override fun getAvroGenericRecord(): GenericRecord {
        // Convert FredSeries instance to GenericRecord using its Avro schema
        val recordBuilder = GenericRecordBuilder(AvroCoder.of(FredSeries::class.java).schema)
        recordBuilder.set("id", getFieldValue("id"))
        recordBuilder.set("title", getFieldValue("title"))
        recordBuilder.set("observationStart", getFieldValue("observationStart"))
        recordBuilder.set("observationEnd", getFieldValue("observationEnd"))
        recordBuilder.set("frequency", getFieldValue("frequency"))
        recordBuilder.set("units", getFieldValue("units"))
        recordBuilder.set("seasonal_adjustment", getFieldValue("seasonal_adjustment"))
        recordBuilder.set("lastUpdated", getFieldValue("lastUpdated"))
        recordBuilder.set("popularity", getFieldValue("popularity"))
        recordBuilder.set("groupPopularity", getFieldValue("groupPopularity"))
        recordBuilder.set("notes", getFieldValue("notes"))

        return recordBuilder.build() // Build and Return -> GenericRecord
    }


    override fun parseCsvStatic(line: String): FredSeries? {
        println("Serialize Fred Invoked $line")
        return Companion.serializeFromCsvLine(line)
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


    companion object : CsvParsable<FredSeries>, ParquetParsable<FredSeries>, Serializable {


        private val logger = KotlinLogging.logger {}

        val pattern = Pattern.compile("\\s*(\"[^\"]*\"|[^,]*)\\s*,")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[XXX][X]")


        fun getParquetSchemaManual(): Schema {
            // Your logic to provide a Parquet Schema...
            val SCHEMA: Schema = Schema.Parser().parse(
                """
            {
              "type": "record",
              "name": "BeamUser",
              "fields": [
                {"name": "name", "type": "string"},
                {"name": "age", "type": "int"}
              ]
            }
        """.trimIndent()
            )
            return SCHEMA
        }

        @JvmStatic
        override fun serializeFromGenericRecord(record: GenericRecord): FredSeries {
            // Extract fields from GenericRecord using the field names
            val id = record["id"]?.toString() ?: ""
            val title = record["title"]?.toString() ?: ""
            val observationStart = record["observationStart"]?.toString() ?: ""
            val observationEnd = record["observationEnd"]?.toString() ?: ""
            val frequency = record["frequency"]?.toString() ?: ""
            val units = record["units"]?.toString() ?: ""
            val seasonal_adjustment = record["seasonal_adjustment"]?.toString() ?: ""
            val lastUpdated = record["lastUpdated"]?.toString() ?: ""
            val popularity = record["popularity"] as? Int ?: 0
            val groupPopularity = record["groupPopularity"] as? Int ?: 0
            val notes = record["notes"]?.toString() ?: ""

            // Create and return a new FredSeries instance using the extracted values
            return FredSeries(
                id = id,
                title = title,
                observationStart = observationStart,
                observationEnd = observationEnd,
                frequency = frequency,
                units = units,
                seasonal_adjustment = seasonal_adjustment,
                lastUpdated = lastUpdated,
                popularity = popularity,
                groupPopularity = groupPopularity,
                notes = notes
            )
        }

        @JvmStatic
        override fun serializeFromCsvLine(line: String): FredSeries? {

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

                return FredSeries(
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
                BeamLogger.logger.error { "Error with CSV line format: $line; ${e.message}" }
                return null
            } catch (e: Exception) {
                BeamLogger.logger.error { "Unexpected error processing CSV line: $line; ${e.message}" }
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
                    "$input:00"
                } else {
                    input
                }
                LocalDateTime.parse(modifiedInput, dateTimeFormatter)
            } catch (e: DateTimeParseException) {
                logger.error(e) { "Error parsing $fieldName in CSV line: $input" }
                null
            }
        }
    }

    fun getManualParquetSchema(): Schema {
        // Your logic to provide a Parquet Schema...
        val SCHEMA: Schema = Schema.Parser().parse(
            """
            {
              "type": "record",
              "name": "BeamUser",
              "fields": [
                {"name": "name", "type": "string"},
                {"name": "age", "type": "int"}
              ]
            }
        """.trimIndent()
        )
        return SCHEMA
    }

    override fun toString(): String {
        return "FredSeries(id='$id', title='$title', observationStart=$observationStart, observationEnd=$observationEnd, frequency='$frequency', units='$units', seasonal_adjustment='$seasonal_adjustment', lastUpdated=$lastUpdated, popularity=$popularity, groupPopularity=$groupPopularity, notes='$notes')"
    }

    override fun toCsvLine(): String {
        return "FredSeries($id,$title,$observationStart,$observationEnd,$frequency,$units,$seasonal_adjustment,$lastUpdated,$popularity,$groupPopularity,$notes)"
    }
}

/*

//    var id: String
//    var title: String
//    var observationStart: String
//    var observationEnd: String
//    var frequency: String
//    var units: String
//    var seasonal_adjustment: String
//    var lastUpdated: String
//    var popularity: Int
//    var groupPopularity: Int
//    var notes: String


//    @SchemaCreate
//    constructor(
//        id: String,
//        title: String,
//        observationStart: String,
//        observationEnd: String,
//        frequency: String,
//        units: String,
//        seasonal_adjustment: String,
//        lastUpdated: String,
//        popularity: Int,
//        groupPopularity: Int,
//        notes: String,
//    ) {
//        this.id = id
//        this.title = title
//        this.observationStart = observationStart
//        this.observationEnd = observationEnd
//        this.frequency = frequency
//        this.units = units
//        this.seasonal_adjustment = seasonal_adjustment
//        this.lastUpdated = lastUpdated
//        this.popularity = popularity
//        this.groupPopularity = groupPopularity
//        this.notes = notes
//
//    }

//    constructor() {
//        id = ""
//        title = ""
//        observationStart = ""
//        observationEnd = ""
//        frequency = ""
//        units = ""
//        seasonal_adjustment = ""
//        lastUpdated = ""
//        popularity = 0
//        groupPopularity = 0
//        notes = ""
//    }

fun getObservationStartDate(): LocalDate = LocalDate.parse(observationStart, DateTimeFormatter.ISO_LOCAL_DATE)

fun getObservationEndDate(): LocalDate = LocalDate.parse(observationEnd, DateTimeFormatter.ISO_LOCAL_DATE)

*/
