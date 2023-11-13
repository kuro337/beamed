/*

Converts a PCollection<BeamEntity> to PCollection<GenericRecord>

*/
package eventstream.beam.transformations.parquet

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.getGenericRecordAvroCoder
import eventstream.beam.interfaces.entity.getParquetSchema
import eventstream.beam.interfaces.transformation.BeamTransformation
import eventstream.beam.interfaces.transformation.TRANSFORMATION
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor
import java.io.Serializable

data class EntityToGenericRecordParams<T : BeamEntity>(val entityClass: Class<T>) : Serializable

class CollectionToGenericRecord<T : BeamEntity>() :
    BeamTransformation<EntityToGenericRecordParams<T>, PCollection<T>, PCollection<GenericRecord>>() {
    override val transformationType = TRANSFORMATION.ENTITY_TO_GENERIC_RECORD

    override fun apply(
        input: PCollection<T>,
        params: EntityToGenericRecordParams<T>
    ): PCollection<GenericRecord> {

        println("Converting a PCollection<BeamEntity> to PCollection<GenericRecord>\nCollectionToGenericRecord.kt")

        val schema = params.entityClass.getParquetSchema()

        val genericCoder = params.entityClass.getGenericRecordAvroCoder()

        println("Using Schema - $schema")
        println("Using genericCoder - $genericCoder")

        val genericRecords = input
            .apply(
                "Convert to GenericRecord",
                MapElements.into(TypeDescriptor.of(GenericRecord::class.java))
                    .via(SerializableFunction<T, GenericRecord> { entity ->
                        // Use the getAvroGenericRecord method from BeamEntity interface
                        entity.getAvroGenericRecord()
                    })
            ).setCoder(genericCoder)


//        params.writeParquetPath?.let { path ->
//            WriteParquetCollection.writeParquetCollectionToDisk(genericRecords, schema, path)
//        }


        return genericRecords

    }

}