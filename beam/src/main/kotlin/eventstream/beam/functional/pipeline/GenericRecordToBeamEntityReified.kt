/*
Convert Generic Records to BeamEntity

@Usage

/* Reading from Disk Parquet */
val pipeline = Pipeline.create()
val parquetRecords: PCollection<GenericRecord> = pipeline.readParquetToGenericRecord<FredSeries>(
    listOf("path/to/parquet/files.parquet")
)

val beamEntityRecords: PCollection<FredSeries> = parquetRecords.convertToBeamEntity()
beamEntityRecords.logElements("Converted Elements: ")
 */
package eventstream.beam.functional.pipeline

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.createEntityFromGenericRecord
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor

inline fun <reified T : BeamEntity> PCollection<GenericRecord>.convertToBeamEntity(): PCollection<T> {
    return this.apply(
        "Convert GenericRecord to ${T::class.simpleName}",
        MapElements.into(TypeDescriptor.of(T::class.java))
            .via(SerializableFunction<GenericRecord, T> { record ->
                T::class.java.createEntityFromGenericRecord(record)
                    ?: throw RuntimeException("Failed to convert GenericRecord to BeamEntity")
            })
    )
}