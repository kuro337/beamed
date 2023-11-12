package eventstream.beam.demo

import eventstream.beam.models.FredSeriesMod
import eventstream.beam.pipeline.S3MemoryPipeline.logger
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptors


fun analyzeGenericColsFredSeries(
    fredCollection: PCollection<FredSeriesMod>,
    columns: List<String>
) {
    columns.forEach { columnName ->
        val fieldCounts: PCollection<KV<String, Long>> = fredCollection
            .apply(
                "Extract $columnName",
                MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.longs()))
                    .via(SerializableFunction<FredSeriesMod, KV<String, Long>> { fredSeriesMod ->
                        KV.of(fredSeriesMod.getFieldValue(columnName).toString(), 1L)
                    })
            )
            .apply("Count Per $columnName", Count.perKey())

        fieldCounts.apply(
            "Log $columnName Counts", ParDo.of(object : DoFn<KV<String, Long>, Void>() {
                @ProcessElement
                fun processElement(@Element count: KV<String, Long>, context: ProcessContext) {
                    println("===\n$columnName: ${count.key}, Count: ${count.value}")

                    // logger.info { "=========================\n$columnName: ${count.key}, Count: ${count.value}" }
                }
            })
        )
    }
}

/**
 * Utility function to get field value by name from FredSeriesMod.
 * This is a placeholder and needs to be implemented in the FredSeriesMod class.
 */
fun FredSeriesMod.getFieldValue(fieldName: String): Any? {
    return when (fieldName) {
        "id" -> this.id
        "title" -> this.title
        "observationStart" -> this.observationStart
        "observationEnd" -> observationEnd
        "frequency" -> frequency
        "units" -> units
        "seasonal_adjustment" -> seasonal_adjustment
        "lastUpdated" -> lastUpdated
        "popularity" -> popularity
        "groupPopularity" -> groupPopularity
        "notes" -> notes
        else -> throw IllegalArgumentException("Field not found")
    }
}


fun countCategoriesFredSeries(fredCollection: PCollection<FredSeriesMod>) {
    val titleCounts: PCollection<KV<String, Long>> = fredCollection
        .apply(
            "Extract Category",
            MapElements
                .into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.longs()))
                .via(SerializableFunction<FredSeriesMod, KV<String, Long>> { fredSeriesMod ->
                    KV.of(fredSeriesMod.title, 1L) // Replace 'title' with the actual field name
                })
        )
        .apply("Count Per Category", Count.perKey())

    titleCounts.apply(
        "Log Category Counts", ParDo.of(object : DoFn<KV<String, Long>, Void>() {
            @ProcessElement
            fun processElement(@Element count: KV<String, Long>, context: ProcessContext) {
                logger.info { "Category: ${count.key}, Count: ${count.value}" }
            }
        })
    )
}