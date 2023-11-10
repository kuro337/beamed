package eventstream.beam.transformations

import eventstream.beam.BeamEntity
import eventstream.beam.BeamTransformation
import eventstream.beam.SerializableEntity
import eventstream.beam.TRANSFORMATION
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.coders.NullableCoder
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptors

fun interface CsvLineSerializer<T> {
    fun serializeFromCsvLine(line: String): T?
}

class EntityOptions(
    val inputs: List<String>,
    val serializer: SerializableEntity<out BeamEntity>? // Use the out-projected type for flexibility
)

data class SerializationParams(val serializer: SerializableEntity<BeamEntity>)

class BeamSerialization {
    object SerializeEntityFromCSVLines :
        BeamTransformation<SerializationParams, PCollection<String>, PCollection<BeamEntity>>() {
        private val logger = KotlinLogging.logger {}
        override val transformationType = TRANSFORMATION.SERIALIZE_ENTITY
        override fun apply(input: PCollection<String>, params: SerializationParams): PCollection<BeamEntity> {
            return applyCsvToEntityTransformation(input, params.serializer)
        }

        fun <T : BeamEntity> applyCsvToEntityTransformation(
            input: PCollection<String>,
            entity: SerializableEntity<T>
        ): PCollection<T> {
            val avroEntityCoder = entity.getCoder()
            val parsedEntityPCollection = input.apply(
                "Parse CSV Lines to Entities",
                ParDo.of(EntityDoFn(entity::serializeFromCsvLine)) // Pass method reference directly.
            ).setCoder(NullableCoder.of(avroEntityCoder))

            // If you have further processing to do that requires a non-null PCollection, apply a filter transform.
            val nonNullEntityPCollection = parsedEntityPCollection.apply(
                "Filter Null Entities",
                ParDo.of(object : DoFn<T?, T>() {
                    @ProcessElement
                    fun processElement(@Element entity: T?, out: OutputReceiver<T>) {
                        if (entity != null) {
                            out.output(entity)
                            logger.info { "Successfully parsed: ${entity.toString()}" }

                        }
                    }
                })
            ).setCoder(avroEntityCoder)

            // Return the non-null PCollection.
            return nonNullEntityPCollection
        }

        fun <T : BeamEntity> analyzeGenericCols(
            entityCollection: PCollection<T>,
            columns: List<String>,
            // getFieldValue: (T, String) -> Any? // Generic getFieldValue passed as a lambda
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
                            // ... Additional logging if necessary
                        }
                    })
                )
            }
        }

    }


}
