package eventstream.beam.transformations.parquet

import eventstream.beam.BeamEntity
import eventstream.beam.BeamTransformation
import eventstream.beam.TRANSFORMATION
import eventstream.beam.effects.WriteParquetCollection
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor

data class CollectionToGenRecordParams(val schema: Schema, val writeParquetPath: String? = null)
object CollectionToGenericRecord :
    BeamTransformation<CollectionToGenRecordParams, PCollection<BeamEntity>, PCollection<GenericRecord>>() {
    override val transformationType = TRANSFORMATION.ENTITY_TO_GENERIC_RECORD

    override fun apply(
        input: PCollection<BeamEntity>,
        params: CollectionToGenRecordParams
    ): PCollection<GenericRecord> {

        val schema = params.schema

        val genericRecords = input
            .apply(
                "Convert to GenericRecord",
                MapElements.into(TypeDescriptor.of(GenericRecord::class.java))
                    .via(SerializableFunction<BeamEntity, GenericRecord> { entity ->
                        // Use the getAvroGenericRecord method from BeamEntity interface
                        entity.getAvroGenericRecord()
                    })
            ).setCoder(AvroCoder.of(GenericRecord::class.java, schema))


        params.writeParquetPath?.let { path ->
            WriteParquetCollection.writeParquetCollectionToDisk(genericRecords, schema, path)
        }


        return genericRecords

    }

}