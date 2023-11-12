import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.values.PCollection

fun createGenericRecords(schema: Schema, pipeline: Pipeline): PCollection<GenericRecord> {
    val recordList = ArrayList<GenericRecord>()
    val newRecord = GenericData.Record(schema).apply {
        put("frequency", "Daily")
        put("groupPopularity", 42)
        put("id", "series-1")
        put("lastUpdated", "2023-11-01")
        put("notes", "Sample notes here.")
        put("observationEnd", "2023-11-01")
        put("observationStart", "2021-01-01")
        put("popularity", 100)
        put("seasonal_adjustment", "Adjusted")
        put("title", "Sample Series Title")
        put("units", "Units")
    }
    recordList.add(newRecord)
    return pipeline.apply(Create.of(recordList)).setCoder(AvroCoder.of(GenericRecord::class.java, schema))
}

fun writeParquetCollectionToDisk(input: PCollection<GenericRecord>, schema: Schema, path: String) {
    input.apply(
        "Write to Parquet", FileIO
            .write<GenericRecord>()
            .via(ParquetIO.sink(schema))
            .to(path)
            .withSuffix(".parquet")
    )
}

fun readAndLogParquetFiles(path: String, pipeline: Pipeline, schema: Schema): PCollection<GenericRecord> {
    // Create a read transform using ParquetIO
    val readTransform = ParquetIO.read(schema)

    // Apply the read transform and then set the AvroCoder explicitly
    return pipeline
        .apply("Read Parquet Files", readTransform.from(path))
        .setCoder(AvroCoder.of(GenericRecord::class.java, schema))
}


fun main(args: Array<String>) {


    println("zhello")
    // Define path to your parquet files
    val parquetFilePath = "/data/output/beam/output-00001-of-00003.parquet"

    // Define the schema for your GenericRecord
    val schemaString = """
{
    "type": "record",
    "name": "FredSeriesMod",
    "namespace": "eventstream.beam.models",
    "fields": [
        {"name": "frequency", "type": "string"},
        {"name": "groupPopularity", "type": "int"},
        {"name": "id", "type": "string"},
        {"name": "lastUpdated", "type": "string"},
        {"name": "notes", "type": "string"},
        {"name": "observationEnd", "type": "string"},
        {"name": "observationStart", "type": "string"},
        {"name": "popularity", "type": "int"},
        {"name": "seasonal_adjustment", "type": "string"},
        {"name": "title", "type": "string"},
        {"name": "units", "type": "string"}
    ]
}
""".trimIndent()

    val schema = Schema.Parser().parse(schemaString)

    val options = PipelineOptionsFactory.fromArgs(*args).withValidation().create()

    val pipeline =
        Pipeline.create(options)

    val coderRegistry = pipeline.coderRegistry
    coderRegistry.registerCoderForClass(GenericRecord::class.java, AvroCoder.of(GenericRecord::class.java, schema))


    // Create the test records
    val testRecords = createGenericRecords(schema, pipeline)

    // Define the output path for the Parquet file
    val outputPath = "/data/output/beam"

    // Write the records to disk
    writeParquetCollectionToDisk(testRecords, schema, outputPath)

    // Now, read from the Parquet file path you've just written to
    // (Ensure the file is written before reading it, possibly by using a wait or by running the pipeline twice, once for write and once for read)
    //val records = readAndLogParquetFiles(parquetFilePath, pipeline, schema)

    // Run the pipeline
    pipeline.run().waitUntilFinish()
}
