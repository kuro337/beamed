package eventstream.beam

import org.apache.avro.Schema
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance


/*

// Extend KClass to add a method for getting the AvroCoder
fun <T: BeamEntity> KClass<T>.getAvroCoder(): Coder<T> = AvroCoder.of(this.java)

// Extend KClass to add a method for getting the AvroSchema
fun <T: BeamEntity> KClass<T>.getAvroSchema(): Schema = AvroCoder.of(this.java).schema

// Similarly, extend KClass for ParquetCoder and ParquetSchema
fun <T: BeamEntity> KClass<T>.getParquetCoder(): Coder<T> = AvroCoder.of(this.java) // Assuming AvroCoder is used for Parquet as well
fun <T: BeamEntity> KClass<T>.getParquetSchema(): Schema = AvroCoder.of(this.java).schema // Use the correct implementation for your Parquet schema




*/


interface CsvParsable<T> {
    companion object {
        inline fun <reified T : BeamEntity> parse(csvLine: String, noinline parser: (String) -> T?): T? {
            return parser(csvLine)
        }
    }

    fun serializeFromCsvLine(csvLine: String): T?
}

/*
Use function for Parsing and creating a BeamEntity without requiring Reflection

val fredSeries: FredSeries? = parseCsvLineToEntity<FredSeries>("csv,line,for,fred,series")

 */
inline fun <reified T : BeamEntity> parseCsvLineToEntity(csvLine: String): T? {
    val parser = T::class.companionObjectInstance as? CsvParsable<T>
    return parser?.serializeFromCsvLine(csvLine)
}

abstract class ParquetSchemaGen<T : BeamEntity>(private val kClass: KClass<T>) {
    protected open fun getAvroSchema(): Schema = AvroCoder.of(kClass.java).schema

    open fun getParquetSchema(): Schema {
        val klassSchema = getAvroSchema()

        var dSchema: Schema = AvroCoder.of(klassSchema::class.java).schema

        var schemaString = dSchema.toString().trim()

        val posFirstN = schemaString.indexOf("namespace")
        val posEnd = schemaString.substring(posFirstN + 8).indexOf(",")
        val firstHalf = schemaString.substring(0, posFirstN - 2)
        val secondHalf = schemaString.substring(posEnd + posFirstN + 1 + 8)
        val conv = "$firstHalf,$secondHalf"

        val schemaParsed: Schema = Schema.Parser().parse(conv)
        println("Abstract Class Auto Generated Schema\n $schemaParsed")

        return schemaParsed
    }
}




