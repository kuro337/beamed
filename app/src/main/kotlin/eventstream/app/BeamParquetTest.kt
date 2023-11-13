package eventstream.app


import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.schemas.annotations.SchemaCreate
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.PCollection
import java.io.Serializable

class BeamUser @SchemaCreate constructor(val name: String, val age: Int) : Serializable {
    constructor() : this("", 0)


    companion object {
        val SCHEMA: Schema = Schema.Parser().parse(
            """
            {
              "type": "record",
              "name": "BeamUser",
              "fields": [
                {"name": "age", "type": "int"},
                {"name": "name", "type": "string"}
              ]
            }
        """.trimIndent()
        )


        fun getParquetSchema(): Schema {
            var dSchema: Schema = AvroCoder.of(BeamUser::class.java).schema
            var schemaStr = dSchema.toString().trim()

            val posStart = schemaStr.indexOf("namespace")
            //val posEnd = schemaStr.substring(posStart + 8).indexOf(",")
            //println("Index of first , $posEnd")
            //val firstHalf = schemaStr.substring(0, posStart - 2)
            //val secondHalf = schemaStr.substring(posEnd + posStart + 1 + 8)

            //val conv = "$firstHalf,$secondHalf"
            // val schemaParsed: Schema = Schema.Parser().parse(conv)

            val strWithoutNamespace =
                "${schemaStr.indexOf("namespace")},${schemaStr.substring(posStart + 8).indexOf(",")}"

            return Schema.Parser().parse(strWithoutNamespace)

            //val schemaParsed: Schema = Schema.Parser().parse(strWithoutNamespace)

            // println("PARSED BY CLASS $schemaParsed")
            //return schemaParsed
        }
    }


    override fun toString(): String {
        return "BeamUser(name='$name', age=$age)"
    }

    fun beamuserGenericRecord(): GenericRecord {
        // Use the predefined SCHEMA to create a GenericRecord
        val recordBuilder = GenericRecordBuilder(SCHEMA)
        recordBuilder.set("name", name)
        recordBuilder.set("age", age)
        return recordBuilder.build() // Build and Return -> GenericRecord
    }
}


/*
Correct
{"type":"record","name":"BeamUser","fields":[{"name":"age","type":"int"},{"name":"name","type":"string"}]}

Parsed
{"type":"record","name":"BeamUser","fields":[{"name":"age","type":"int"},{"name":"name","type":"string"}]}

*/


fun main() {
    val options = PipelineOptionsFactory.create()

    val pipeline = Pipeline.create(options)

    var dSchema: Schema = AvroCoder.of(BeamUser::class.java).schema
    var schemaTostring = dSchema.toString().trim()

    val posFirstN = schemaTostring.indexOf("namespace")
    val posEnd = schemaTostring.substring(posFirstN + 8).indexOf(",")
    println("Index of first , $posEnd")
    val firstHalf = schemaTostring.substring(0, posFirstN - 2)
    val secondHalf = schemaTostring.substring(posEnd + posFirstN + 1 + 8)

    val conv = "$firstHalf,$secondHalf"


    val schemaParsed: Schema = Schema.Parser().parse(conv)

    //println("NEW PARSED SCHEMA \n $schemaParsed")


    val user = BeamUser("John Doe", 34)

    // Define the path where the Parquet file will be written
    val outputPath = "data/output/beam"

    // Correctly create AvroCoder for GenericRecord with BeamUser's schema
    val schema = BeamUser.SCHEMA
    println("(CORRECT) - MANUAL SCHEMA\n $schema")


    val originalSchema: Schema = AvroCoder.of(BeamUser::class.java).schema

    val avroCoder = AvroCoder.of(GenericRecord::class.java, schemaParsed)

    // Create a sample BeamUser object
    val users = listOf(BeamUser("John Doe", 34), BeamUser("James Jones", 22))


    /*    WRITE   PARQUET        */
//    // Create a sample BeamUser object and convert it to GenericRecord
    val usersGenericRecords = users.map { user -> user.beamuserGenericRecord() }

    //Create a PCollection<GenericRecord> from the list of GenericRecords
    val pcollGenericRecords: PCollection<GenericRecord> = pipeline
        .apply("Create BeamUser Records", Create.of(usersGenericRecords).withCoder(avroCoder))

    // Write the PCollection<GenericRecord> to a Parquet file
    pcollGenericRecords.apply(
        "Write to Parquet", FileIO.write<GenericRecord>()
            .via(ParquetIO.sink(schemaParsed))
            .to(outputPath)
            .withSuffix(".parquet")
    )


    /* READ PARQUET */

    // Apply the read transform to get a PCollection<GenericRecord>
//    val readUserCollection = pipeline.apply(
//        "Read Parquet File",
//        ParquetIO.read(BeamUser.getParquetSchema()).from("data/output/beam/output-00000-of-00002.parquet")
//    )
//
//    //Log the contents of the PCollection read from the Parquet file
//    val readParquetFromDisk =
//        readUserCollection.apply("Log Read Records", ParDo.of(object : DoFn<GenericRecord, Void>() {
//            @ProcessElement
//            fun processElement(@Element record: GenericRecord, receiver: OutputReceiver<Void>) {
//                println("Logging GenericRecord $record") // Log the record to the console
//            }
//        }))
//
//
//    // Convert GenericRecords to BeamUser instances
//    val beamUsers: PCollection<BeamUser> = readUserCollection.apply(
//        "Convert to BeamUser",
//        MapElements.into(TypeDescriptor.of(BeamUser::class.java))
//            .via(SerializableFunction<GenericRecord, BeamUser> { record ->
//                genericRecordToBeamUser(record)
//            })
//    ).setCoder(AvroCoder.of(BeamUser::class.java))
//
//    beamUsers.apply("Log BeamUser Records", ParDo.of(LogBeamUserFn()))


    // Execute the pipeline
    pipeline.run().waitUntilFinish()


}

class LogBeamUserFn : DoFn<BeamUser, Void>() {
    @ProcessElement
    fun processElement(@Element user: BeamUser) {
        // Log the BeamUser object to the console
        val userBeamString = user.toString()

        println("Logging Converted GenericRecord to BeamUser ${user.toString()} ")
    }
}

object BeamParquetEntity {

    fun writeEntityToParquetFile(input: PCollection<GenericRecord>, schema: Schema, path: String) {
        println("Writing with schema: $schema")

        input.apply(
            "Write to Parquet", FileIO.write<GenericRecord>()
                .via(ParquetIO.sink(schema))
                .to(path)
                .withSuffix(".parquet")
        )
    }


    fun readEntityFromParquetFile(filePath: String, schema: Schema): PCollection<GenericRecord> {
        val pipeline = Pipeline.create(PipelineOptionsFactory.create())

        println("Reading")

        return pipeline.apply(
            "Read Parquet File",
            ParquetIO.read(schema)
                .from(filePath) // for first file only, or implement a loop for multiple.
        )

    }

    fun genericRecordToBeamUser(record: GenericRecord): BeamUser {
        val name = record["name"].toString() // Cast to String
        val age = record["age"] as Int // Cast to Int
        return BeamUser(name, age)
    }

}



