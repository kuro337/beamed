package eventstream.beam.transformations.helpers

import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection


/* @Log Generic Records */

fun logGenericRecords(records: PCollection<GenericRecord>): PCollection<Void> {
    return records.apply("Log GenericRecord", ParDo.of(LogGenericRecordFn()))
}

class LogGenericRecordFn : DoFn<GenericRecord, Void>() {
    @ProcessElement
    fun processElement(@Element record: GenericRecord, c: DoFn<GenericRecord, Void>.ProcessContext) {
        // Log the entire record as a string
        println(record)

        // Or log specific fields
        // println("Field value: ${record["fieldname"]}")
    }
}

