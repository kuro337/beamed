package eventstream.beam.interfaces.entity

import org.apache.avro.generic.GenericRecord
import kotlin.reflect.full.companionObjectInstance

interface CsvParsable<T> {
    fun serializeFromCsvLine(csvLine: String): T?
}

interface ParquetParsable<T> {
    fun serializeFromGenericRecord(record: GenericRecord): T?
}


/*
    @ Reified Inlined Function

Use function for Parsing and creating a BeamEntity without requiring Reflection
val fredSeries: FredSeries? = parseCsvLineToEntity<FredSeries>("csv,line,for,fred,series")

 */
inline fun <reified T : BeamEntity> parseCsvLineToEntity(csvLine: String): T? {
    val parser = T::class.companionObjectInstance as? CsvParsable<T>
    return parser?.serializeFromCsvLine(csvLine)
}





