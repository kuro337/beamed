package eventstream.beam.interfaces.entity

import org.apache.avro.generic.GenericRecord
import java.io.Serializable

/* Interface that all Beam Entities Should Implement */
interface BeamEntity : Serializable {
    fun getFieldValue(fieldName: String): Any?
    fun getAvroGenericRecord(): GenericRecord

    fun parseCsvStatic(line: String): BeamEntity?
    fun toCsvLine(): String
    override fun toString(): String

}




