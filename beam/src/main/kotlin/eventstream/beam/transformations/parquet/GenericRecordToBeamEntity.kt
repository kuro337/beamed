package eventstream.beam.transformations.parquet

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.createEntityFromGenericRecord
import eventstream.beam.interfaces.entity.getParquetCoder
import eventstream.beam.interfaces.transformation.BeamTransformation
import eventstream.beam.interfaces.transformation.TRANSFORMATION
import eventstream.beam.transformations.csv.SerializationParams
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor
import java.io.Serializable

data class SerializationParams<T : BeamEntity>(val entityClass: Class<T>) : Serializable
class GenericRecordToBeamEntity<T : BeamEntity> :
    BeamTransformation<SerializationParams<T>, PCollection<GenericRecord>, PCollection<T>>() {
    override val transformationType = TRANSFORMATION.ENTITY_TO_GENERIC_RECORD

    override fun apply(
        input: PCollection<GenericRecord>,
        params: SerializationParams<T>
    ): PCollection<T> {

        val convertGenericRecordToEntity: (GenericRecord) -> T? = { record ->
            params.entityClass.createEntityFromGenericRecord(record)
        }

        return input.apply(
            "Convert to BeamEntity",
            MapElements.into(TypeDescriptor.of(params.entityClass))
                .via(SerializableFunction<GenericRecord, T> { record ->
                    convertGenericRecordToEntity(record) ?: throw RuntimeException("Conversion failed")
                })
        ).setCoder(params.entityClass.getParquetCoder())


    }

    class PrintGenericRecordFieldsFn : DoFn<GenericRecord, Void>() {
        @ProcessElement
        fun processElement(c: ProcessContext) {
            val record = c.element()
            println("Record has fields: ${record.schema.fields.map { it.name() }}")
        }
    }

    fun printGenericRecordFields(input: PCollection<GenericRecord>) {
        println("Triggered - triggering GenericRecordReader")

        input.apply("Print GenericRecord Fields", ParDo.of(PrintGenericRecordFieldsFn()))
    }


}