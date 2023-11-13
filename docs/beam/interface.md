# Beam Entity Interface

```kotlin
/*

Class
- Serializeable
- No Args Constructor present constructor()
- getFieldValue(fieldName) - method present to get Instance values
- getAvroGenericRecord(): GenericRecord - method present to return an Avro GenericRecord from current props
- getAvroCoder() : Coder<beamEntity> - method present to get the Coder
- toString() present - to get Stringified Object
- getParquetSchema() - get Schema for Parquet

Companion Object
- getAvroCoder() present to get the Coder
- getStaticSchema() present to get the Schema
- serializeFromCsvLine() present to serialize Object from a CSV Line
- getParquetSchema() - get Schema for Parquet

Reqs for Parquet Interface
- getAvroCoder()
- getParquetSchema() - get Schema for Parquet
- getFieldValue(fieldName)

Reqs for CSV Interface
-  getAvroCoder()
-  serializeFromCsvLine()
-  getFieldValue(fieldName)


*/
class BeamEntityClass(val name: String, val age: Int) : Serializable {

    constructor() : this("", 0)

    companion object {
        // Define the Avro schema for BeamUser
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

    }

    fun beamuserGenericRecord(): GenericRecord {
        // Use the predefined SCHEMA to create a GenericRecord
        val recordBuilder = GenericRecordBuilder(SCHEMA)
        recordBuilder.set("name", name)
        recordBuilder.set("age", age)
        return recordBuilder.build() // Build and Return -> GenericRecord
    }

}

```