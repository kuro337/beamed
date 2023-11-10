package eventstream.beam.transformations

import eventstream.beam.BeamEntity
import eventstream.beam.SerializableEntity
import org.apache.beam.sdk.coders.Coder
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection
import org.checkerframework.checker.initialization.qual.Initialized
import org.checkerframework.checker.nullness.qual.UnknownKeyFor


class EntityDoFn<T>(
    private val serializeFn: (String) -> T?
) : DoFn<String, T>() where T : BeamEntity {
    @ProcessElement
    fun processElement(@Element line: String, out: OutputReceiver<T>) {
        val entity: T? = serializeFn(line)
        if (entity != null) {
            out.output(entity)
        }
    }
}

// Define a separate DoFn class
class ConvertCsvToEntityFn<T : BeamEntity>(
    private val entity: SerializableEntity<T>,
    private val coder: Coder<T?>
) : DoFn<String, T?>() {

    @ProcessElement
    fun processElement(c: ProcessContext, receiver: OutputReceiver<T?>) {
        val line = c.element()
        val result: T? = try {
            entity.serializeFromCsvLine(line)
        } catch (e: Exception) {
            null // Handle error appropriately
        }
        if (result != null) {
            receiver.output(result)
        }
    }
}

// Modify your applyCsvToEntityTransformation function to use the new DoFn class
fun <T : BeamEntity> applyCsvToEntityTransformation(
    input: PCollection<String>,
    entity: SerializableEntity<T>,
    coder: Coder<T?>
): @UnknownKeyFor @Initialized PCollection<T?> {
    val doFn = ConvertCsvToEntityFn(entity, coder)
    val parsedEntityPCollection = input.apply("Parse CSV Lines to Entities", ParDo.of(doFn))
    return parsedEntityPCollection.setCoder(coder as Coder<T?>)
}
