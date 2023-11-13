package eventstream.beam.functional.pipeline

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.getGenericRecordAvroCoder
import eventstream.beam.logger.BeamLogger
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor

inline fun <reified T : BeamEntity> PCollection<T>.toGenericRecords(): PCollection<GenericRecord> {

    BeamLogger.logger.info { "Converting PCollection<${T::class.simpleName} to  PCollection<GenericRecord>" }

    return this
        .apply(
            "Convert to GenericRecord",
            MapElements.into(TypeDescriptor.of(GenericRecord::class.java))
                .via(SerializableFunction { entity: T ->
                    entity.getAvroGenericRecord()
                })
        ).setCoder(T::class.java.getGenericRecordAvroCoder()).also {
            BeamLogger.logger.info { "Successfully Converted PCollection<${T::class.simpleName} to  PCollection<GenericRecord>" }
        }


}

/*
@Usage

val entityCollection : PCollection<MyEntity> = ...
val genericRecords = myEntities.toGenericRecords<MyEntity>()

*/

// Usage in your pipeline code:
