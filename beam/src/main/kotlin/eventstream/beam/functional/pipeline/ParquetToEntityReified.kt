/*
Read Parquet Files to Entities

@Usage

val pipeline = Pipeline.create(/* options */)
val parquetRecords: PCollection<GenericRecord> = pipeline.readParquetToGenericRecord<MyBeamEntityClass>(
   listOf("path/to/parquet/files.parquet")
  )

*/


package eventstream.beam.functional.pipeline

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.getParquetSchemaNoNamespace
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.transforms.Flatten
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PCollectionList

inline fun <reified T : BeamEntity> Pipeline.readParquetToGenericRecord(
    inputFiles: List<String>
): PCollection<GenericRecord> {
    val parquetSchema = T::class.java.getParquetSchemaNoNamespace()

    /* Map over Parquet files, convert to PCollection<GenericRecord> using Schema for Class */
    val filePCollections: List<PCollection<GenericRecord>> = inputFiles.map { filePath ->
        this.apply(
            "Read Parquet File - $filePath",
            ParquetIO.read(parquetSchema).from(filePath)
        )
    }

    /* Flatten PCollections from all Parquet Files */
    return if (filePCollections.size > 1) {
        PCollectionList.of(filePCollections)
            .apply("Flatten PCollectionList into one PCollection", Flatten.pCollections())
    } else {
        filePCollections.first()
    }
}