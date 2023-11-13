# Beam Parquet

- [Reference](https://beam.apache.org/releases/javadoc/current/org/apache/beam/sdk/io/parquet/ParquetIO.html)


- We can use `org.apache.beam.sdk.io.parquet` package


- For our Entity Class - the class must be Serializable , have a Constructor that accepts Empty Params, and Return a
  Parquet Schema.

```kotlin
class BeamUser(val name: String, val age: Int) : Serializable {
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

// Create a sample BeamUser object
val users = listOf(BeamUser("John Doe", 34), BeamUser("James Jones", 22))

```

- To Write any Class to a Parquet File - we need to first convert our List<Class> to a PCollection<GenericRecord>

```kotlin
val users = listOf(BeamUser("John Doe", 34), BeamUser("James Jones", 22))

// Create a sample BeamUser object and convert it to GenericRecord
val usersGenericRecords = users.map { user -> user.beamuserGenericRecord() }

// Create a PCollection<GenericRecord> from the list of GenericRecords
val pcollGenericRecords: PCollection<GenericRecord> = pipeline
    .apply("Create BeamUser Records", Create.of(usersGenericRecords).withCoder(avroCoder))

```

- Then we write the file to Disk - and make sure to Provide the Parquet Schema for the Class

```kotlin

// Write the PCollection<GenericRecord> to a Parquet file
pcollGenericRecords.apply(
    "Write to Parquet", FileIO.write<GenericRecord>()
        .via(ParquetIO.sink(BeamUser.SCHEMA))
        .to(outputPath)
        .withSuffix(".parquet")
)


```

- Reading Parquet from Disk to PCollection<GenericRecord>

```kotlin
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
            println(record) // Log the record to the console
        }
    }))

val beamUserCoder = SerializableCoder.of(BeamUser::class.java)

// Convert GenericRecords to BeamUser instances
val beamUsers: PCollection<BeamUser> = readUserCollection.apply(
    "Convert to BeamUser",
    MapElements.into(TypeDescriptor.of(BeamUser::class.java))
        .via(SerializableFunction<GenericRecord, BeamUser> { record ->
            genericRecordToBeamUser(record)
        })
).setCoder(SerializableCoder.of(BeamUser::class.java))

beamUsers.apply("Log BeamUser Records", ParDo.of(LogBeamUserFn()))

class LogBeamUserFn : DoFn<BeamUser, Void>() {
    @ProcessElement
    fun processElement(@Element user: BeamUser) {
        // Log the BeamUser object to the console
        val userBeamString = user.toString()
        println("Logging Converted GenericRecord to BeamUser ${user.toString()} ")
    }
}
```