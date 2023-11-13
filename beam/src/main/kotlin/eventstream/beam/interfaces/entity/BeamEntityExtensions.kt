package eventstream.beam.interfaces.entity

import eventstream.beam.transformations.helpers.LogBeamEntity
import eventstream.beam.transformations.helpers.SchemaParser
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.coders.Coder
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection


fun <T : BeamEntity> Class<T>.getAvroSchema(): Schema = AvroCoder.of(this).schema

fun <T : BeamEntity> Class<T>.getAvroCoder(): Coder<out BeamEntity> = AvroCoder.of(this)

fun <T : BeamEntity> Class<T>.getParquetCoder(): Coder<T> = AvroCoder.of(this)

fun <T : BeamEntity> Class<T>.getParquetSchema(): Schema = AvroCoder.of(this).schema

fun <T : BeamEntity> Class<T>.getGenericRecordAvroCoder(): Coder<GenericRecord> =
    AvroCoder.of(GenericRecord::class.java, getParquetSchemaNoNamespace())

fun <T : BeamEntity> Class<T>.getParquetSchemaNoNamespace(): Schema =
    SchemaParser.removeNamespaceFromSchema(AvroCoder.of(this).schema)


/**
 * `Serialize Object from a CSV Line`
 *
 * - Usage
 * ```kotlin
 *  val someObjClass : Class<BeamEntity> = entityClass
 *  val serializedEntity : BeamEntity    = entityClass.createEntityFromCsvLine(line)
 *
 *  val beamEntity : Class<T:BeamEntity> = T::class.java.createEntityFromCsvLine(line)
 *
 *  /* If we have a for loop - we can use Reflection to Get it Once and Cache it */
 *  val cachedFn : (String) -> T? = { csvLine -> params.entityClass.createEntityFromCsvLine(csvLine) }
 *
 *  val csvLines : List<String> = readLinesFromCSV()
 *  csvLines.forEach { line ->  parseCsvToEntity(line)?.let { println(it.toString()) } }
 * ```
 * @param csvLine : String CSV Line
 * @return T : BeamEntity
 * @author kuro337
 * @sample eventstream.beam.interfaces.entity
 *
 */
fun <T : BeamEntity> Class<T>.createEntityFromCsvLine(csvLine: String): T? {
    //val companion = this.companionObject?.java
    println("Calling CSV Line Parse on Base Class $csvLine")

    val method = this?.getDeclaredMethod("serializeFromCsvLine", String::class.java)
    return method?.invoke(null, csvLine) as? T
}


fun <T : BeamEntity> Class<T>.createEntityFromGenericRecord(record: GenericRecord): T? {
    println("Calling serializeFromGenericRecord on Base Class")

    val method = this?.getDeclaredMethod("serializeFromGenericRecord", GenericRecord::class.java)
    return method?.invoke(null, record) as? T
}


/* @Static Function : Log Items from a PCollection<T>  */
fun <T : BeamEntity> logEntityCollection(collection: PCollection<T>): PCollection<T> {
    return collection.apply("Log BeamEntity", ParDo.of(LogBeamEntity<T>()))
}

