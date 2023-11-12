package eventstream.beam


import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.coders.Coder
import java.io.Serializable

/* Interface that all Beam Entities Should Implement */
interface BeamEntity : Serializable {
    fun getFieldValue(fieldName: String): Any?
    fun getAvroGenericRecord(): GenericRecord

    fun parseCsvStatic(line: String): BeamEntity?

    fun toCsvLine(): String
    override fun toString(): String


}


/* Interface that the Companion Objects for Beam Entities must Implement */

interface StaticBeamEntity : Serializable {
    fun getAvroCoder(): Coder<out BeamEntity>
    fun getAvroSchema(): Schema

}


interface ParquetEntity<T : BeamEntity> {
    fun getParquetCoder(): Coder<out BeamEntity>

    fun getParquetSchema(): Schema

    fun getAvroCoder(): Coder<out BeamEntity>


}




