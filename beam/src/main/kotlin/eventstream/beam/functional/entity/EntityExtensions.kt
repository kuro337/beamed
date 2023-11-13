package eventstream.beam.functional.entity


import eventstream.beam.interfaces.entity.BeamEntity
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.coders.Coder
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder

/* @Usage : getAvroSchema<FredSeries>()  */
inline fun <reified T : BeamEntity> getAvroSchema(): Schema = AvroCoder.of(T::class.java).schema

/* @Usage : getAvroCoder<FredSeries>()  */
inline fun <reified T : BeamEntity> getAvroCoder(): Coder<out BeamEntity> = AvroCoder.of(T::class.java)

/* @Usage : getParquetCoder<FredSeries>()  */
inline fun <reified T : BeamEntity> getParquetCoder(): Coder<T> = AvroCoder.of(T::class.java)

/* @Usage : getParquetSchema<FredSeries>()  */
inline fun <reified T : BeamEntity> getParquetSchema(): Schema = AvroCoder.of(T::class.java).schema


/* @Usage : createEntityFromCsvLine<FredSeries>("some,csv,line")  */
inline fun <reified T : BeamEntity> createEntityFromCsvLine(csvLine: String): T? {
    val method = T::class.java?.getDeclaredMethod("serializeFromCsvLine", String::class.java)
    return method?.invoke(null, csvLine) as? T
}

/* @Usage : createEntityFromGenericRecord<FredSeries>()  */
inline fun <reified T : BeamEntity> createEntityFromGenericRecord(record: GenericRecord): T? {
    println("Calling serializeFromGenericRecord on Base Class")

    val method = T::class.java?.getDeclaredMethod("serializeFromGenericRecord", GenericRecord::class.java)
    return method?.invoke(null, record) as? T
}



