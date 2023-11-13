/*

Read Parquet Files from Disk to Memory

@Usage

```kotlin
// Specify Params to Read

val readParquetLines = ParquetReadParameters(options.files, options.beamEntityClass as Class<BeamEntity>)
val genericRecordsFromParquet = ParquetFileToGenericRecordCollection<BeamEntity>().read(
    pipeline,
    ParquetReadParameters(options.files, options.beamEntityClass)
   )

// Convert to GenericRecord (Required)

val convertedRecords = GenericRecordToBeamEntity<BeamEntity>().apply(
    genericRecordsFromParquet,
    SerializationParams(options.beamEntityClass)
   )

// Log
val loggedCollection = logEntityCollection(convertedRecords)
```
*/

package eventstream.beam.read

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.getParquetSchema
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection


class ParquetReadParameters<T : BeamEntity>(inputFiles: List<String>, val entityClass: Class<T>) :
    ReadParameters(inputFiles)

class ParquetFileToGenericRecordCollection<T : BeamEntity> : BeamReader<ParquetReadParameters<T>> {


    override fun read(pipeline: Pipeline, params: ParquetReadParameters<T>): PCollection<GenericRecord> {

        val parquetSchema = params.entityClass.getParquetSchema()


        // Read and return PCollection<GenericRecord>
        return pipeline.apply(
            "Read Parquet File",
            ParquetIO.read(parquetSchema).from(params.inputDescriptors.first())
        )


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


