package eventstream.beam.transformations.csv

import eventstream.beam.BeamEntity
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptors


fun <T : BeamEntity> analyzeGenericCols(
    entityCollection: PCollection<T>,
    columns: List<String>,
) {
    columns.forEach { columnName ->
        val fieldCounts: PCollection<KV<String, Long>> = entityCollection
            .apply(
                "Extract $columnName",
                MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.longs()))
                    .via(SerializableFunction<T, KV<String, Long>> { entity ->
                        KV.of(entity.getFieldValue(columnName).toString(), 1L)
                    })
            )
            .apply(
                "Filter nulls for $columnName",
                Filter.by(SerializableFunction<KV<String, Long>, Boolean> { kv -> kv != null })
            )
            .apply("Count Per $columnName", Count.perKey())

        // Log the counts for each column
        fieldCounts.apply(
            "Log $columnName Counts", ParDo.of(object : DoFn<KV<String, Long>, Void>() {
                @ProcessElement
                fun processElement(@Element count: KV<String, Long>, context: ProcessContext) {
                    println("===\n$columnName: ${count.key}, Count: ${count.value}")
                }
            })
        )
    }
}
