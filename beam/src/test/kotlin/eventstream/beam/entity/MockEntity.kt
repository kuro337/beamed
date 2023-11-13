package eventstream.beam.entity

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.beam.sdk.coders.Coder
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.schemas.annotations.SchemaCreate
import java.io.Serializable

class BeamTestEntity @SchemaCreate constructor(val name: String, val age: Int) : Serializable {
    constructor() : this("", 0)

    override fun toString(): String {
        return "BeamTestEntity(name='$name', age=$age)"
    }

    fun beamuserGenericRecord(): GenericRecord {
        val recordBuilder = GenericRecordBuilder(SCHEMA)
        recordBuilder.set("name", name)
        recordBuilder.set("age", age)
        return recordBuilder.build()
    }

    companion object {

        fun getCoder(): Coder<BeamTestEntity> {
            return AvroCoder.of(BeamTestEntity::class.java, getParquetSchema())
        }

        fun getGenericCoder(): Coder<GenericRecord> {
            return AvroCoder.of(GenericRecord::class.java, getParquetSchema())
        }

        val SCHEMA: Schema = Schema.Parser().parse(
            """
            {
              "type": "record",
              "name": "BeamTestEntity",
              "fields": [
                {"name": "age", "type": "int"},
                {"name": "name", "type": "string"}
              ]
            }
        """.trimIndent()
        )

        fun fromGenericRecord(record: GenericRecord): BeamTestEntity {
            val name = record["name"] as String
            val age = record["age"] as Int
            return BeamTestEntity(name, age)

        }

        fun getParquetSchema(): Schema {
            var dSchema: Schema = AvroCoder.of(BeamTestEntity::class.java).schema
            var schemaTostring = dSchema.toString().trim()
            val posFirstN = schemaTostring.indexOf("namespace")
            val posEnd = schemaTostring.substring(posFirstN + 8).indexOf(",")
            val firstHalf = schemaTostring.substring(0, posFirstN - 2)
            val secondHalf = schemaTostring.substring(posEnd + posFirstN + 1 + 8)
            val conv = "$firstHalf,$secondHalf"
            val schemaParsed: Schema = Schema.Parser().parse(conv)

            return schemaParsed
        }
    }


}