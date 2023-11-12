/*
Path - cd /d/Code/Kotlin/projects/eventstream/data/output/beam

Metadata
parq  output-00000-of-00005.parquet

data/output/beam/output-00001-of-00002.parquet

parq data/output/beam/output-00001-of-00002.parquet

Schema
parq  output-00000-of-00003.parquet  --schema

parq  data/output/beam/output-00001-of-00002.parquet  --schema

Top n Rows
parq data/output/beam/output-00001-of-00002.parquet --head 10

 */
package eventstream.beam.source

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection

class ParquetReadParameters(inputFiles: List<String>, val schema: Schema) : ReadParameters(inputFiles)

object ParquetFileToGenericRecordCollection : PCollectionSource<GenericRecord> {


    override fun read(pipeline: Pipeline, params: ReadParameters): PCollection<GenericRecord> {
        if (params !is ParquetReadParameters) {
            throw IllegalArgumentException("Params must be a ParquetReadParameters")
        }

        val genSchhema = AvroCoder.of(
            GenericRecord::class.java, params.schema
        )
        // You should not parse the schema from JSON here, use the schema directly.
        // val schema = params.schema

        // Correct usage of ParquetIO.read with schema
        val genericReads = pipeline.apply(
            "Read Parquet File",
            ParquetIO.read(genSchhema.schema).from(params.inputDescriptors.first())
        ).setCoder(genSchhema)


        return genericReads

        // Rest of your reading logic...
        // ...
    }

    fun readAndLogParquetFiles(path: String, pipeline: Pipeline, schema: Schema): PCollection<GenericRecord> {
        val readTransform = ParquetIO.parseGenericRecords(SerializableFunction<GenericRecord, GenericRecord> { record ->
            // Log each field from the record
            record.schema.fields.forEach { field ->
                val fieldValue = record.get(field.pos()) // Get value of field by position
                println("Field name: ${field.name()} - Field value: $fieldValue")
            }
            record // Return the GenericRecord without transformation
        }).withCoder(AvroCoder.of(GenericRecord::class.java, schema)) // Explicitly setting the coder

        return pipeline.apply("Read Parquet Files", readTransform.from(path))
    }

}


/*

Writing with schema:

{"type":"record","name":"FredSeriesMod",
"namespace":"eventstream.beam.models",
"fields":[
{"name":"frequency","type":"string"},
{"name":"groupPopularity","type":"int"},
{"name":"id","type":"string"},
{"name":"lastUpdated","type":"string"},
{"name":"notes","type":"string"},
{"name":"observationEnd","type":"string"},
{"name":"observationStart","type":"string"},
{"name":"popularity","type":"int"},
{"name":"seasonal_adjustment","type":"string"},
{"name":"title","type":"string"},
{"name":"units","type":"string"}]}

Writing with schema: {"type":"record","name":"FredSeriesMod","namespace":"eventstream.beam.models","fields":[{"name":"frequency","type":"string"},{"name":"groupPopularity","type":"int"},{"name":"id","type":"string"},{"name":"lastUpdated","type":"string"},{"name":"notes","type":"string"},{"name":"observationEnd","type":"string"},{"name":"observationStart","type":"string"},{"name":"popularity","type":"int"},{"name":"seasonal_adjustment","type":"string"},{"name":"title","type":"string"},{"name":"units","type":"string"}]}

required group field_id=-1 eventstream.beam.models.FredSeriesMod {
  required binary field_id=-1 frequency (String);
  required int32 field_id=-1 groupPopularity;
  required binary field_id=-1 id (String);
  required binary field_id=-1 lastUpdated (String);
  required binary field_id=-1 notes (String);
  required binary field_id=-1 observationEnd (String);
  required binary field_id=-1 observationStart (String);
  required int32 field_id=-1 popularity;
  required binary field_id=-1 seasonal_adjustment (String);
  required binary field_id=-1 title (String);
  required binary field_id=-1 units (String);
}


https://stackoverflow.com/questions/75219530/error-when-using-apache-beam-parquetio-to-read-data-from-parquet-file-due-to-avr

  return pipeline.apply(
            "Read Parquet File",
            ParquetIO.read(params.schema)
                .from(params.inputDescriptors.first()) // for first file only, or implement a loop for multiple.
        )

 */