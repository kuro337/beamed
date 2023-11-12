package eventstream.beam.transformations.parquet

import eventstream.beam.BeamEntity
import eventstream.beam.BeamTransformation
import eventstream.beam.TRANSFORMATION
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection

data class GenericRecordToBeamEntityParams(val schema: Schema)
object GenericRecordToBeamEntity :
    BeamTransformation<GenericRecordToBeamEntityParams, PCollection<GenericRecord>, PCollection<BeamEntity>?>() {
    override val transformationType = TRANSFORMATION.ENTITY_TO_GENERIC_RECORD

    override fun apply(
        input: PCollection<GenericRecord>,
        params: GenericRecordToBeamEntityParams
    ): PCollection<BeamEntity>? {
        val pipeline = Pipeline.create()

        return null

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