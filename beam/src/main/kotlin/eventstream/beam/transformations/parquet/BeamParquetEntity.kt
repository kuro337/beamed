package eventstream.beam.transformations.parquet

import eventstream.beam.transformations.parquet.BeamParquetEntity.genericRecordToBeamUser
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor
import java.io.Serializable

class BeamUser(val name: String, val age: Int) : Serializable {
    constructor() : this("", 0)

    companion object {
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


fun main() {
    val options = PipelineOptionsFactory.create()
    val pipeline = Pipeline.create(options)

    val user = BeamUser("John Doe", 34)

    // Define the path where the Parquet file will be written
    val outputPath = "data/output/beam"

    // Correctly create AvroCoder for GenericRecord with BeamUser's schema
    val schema = BeamUser.SCHEMA // Use the schema defined in the BeamUser companion object
    val avroCoder = AvroCoder.of(GenericRecord::class.java, schema)

    // Create a sample BeamUser object
    val users = listOf(BeamUser("John Doe", 34), BeamUser("James Jones", 22))


    // Create a sample BeamUser object and convert it to GenericRecord
    //  val usersGenericRecords = users.map { user -> user.beamuserGenericRecord() }

    // Create a PCollection<GenericRecord> from the list of GenericRecords
//    val pcollGenericRecords: PCollection<GenericRecord> = pipeline
//        .apply("Create BeamUser Records", Create.of(usersGenericRecords).withCoder(avroCoder))

    //Write the PCollection<GenericRecord> to a Parquet file
//    pcollGenericRecords.apply(
//        "Write to Parquet", FileIO.write<GenericRecord>()
//            .via(ParquetIO.sink(schema))
//            .to(outputPath)
//            .withSuffix(".parquet")
//    )


    // Apply the read transform to get a PCollection<GenericRecord>
    val readUserCollection = pipeline.apply(
        "Read Parquet File",
        ParquetIO.read(schema).from("data/output/beam/output-00000-of-00002.parquet")
    )

    // Log the contents of the PCollection read from the Parquet file
    val readParquetFromDisk =
        readUserCollection.apply("Log Read Records", ParDo.of(object : DoFn<GenericRecord, Void>() {
            @ProcessElement
            fun processElement(@Element record: GenericRecord, receiver: OutputReceiver<Void>) {
                println("Logging GenericRecord $record") // Log the record to the console
            }
        }))


    // Convert GenericRecords to BeamUser instances
    val beamUsers: PCollection<BeamUser> = readUserCollection.apply(
        "Convert to BeamUser",
        MapElements.into(TypeDescriptor.of(BeamUser::class.java))
            .via(SerializableFunction<GenericRecord, BeamUser> { record ->
                genericRecordToBeamUser(record)
            })
    ).setCoder(AvroCoder.of(BeamUser::class.java))

    beamUsers.apply("Log BeamUser Records", ParDo.of(LogBeamUserFn()))


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

/*

val avroCoderSchema = AvroCoder.of(BeamUser::class.java).schema.toString(true)
println(avroCoderSchema)
{
  "type" : "record",
  "name" : "BeamUser",
  "namespace" : "eventstream.app",
  "fields" : [ {
    "name" : "age",
    "type" : "int"
  }, {
    "name" : "name",
    "type" : "string"
  } ]
}

val manualSchema = BeamUser.SCHEMA.toString(true)
println(manualSchema)


{
  "type" : "record",
  "name" : "BeamUser",
  "fields" : [ {
    "name" : "name",
    "type" : "string"
  }, {
    "name" : "age",
    "type" : "int"
  } ]
}

*/


