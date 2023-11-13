/**
 * This file defines transformation utilities for converting CSV string data into PCollection instances of BeamEntity types.
 * It includes custom DoFn (Dataflow operation function) implementations that parse CSV lines into typed entities using provided serialization functions.
 *
 * - EntityDoFn: A generic DoFn for converting a CSV line to a BeamEntity using a provided serialization function.
 * - ConvertCsvToEntityFn: A DoFn to convert CSV lines into BeamEntity objects using a SerializableEntity instance.
 * - applyCsvToEntityTransformation: A utility function to apply the ConvertCsvToEntityFn to a PCollection of strings and output a PCollection of typed entities.
 *
 * The file enables CSV data processing within Apache Beam pipelines, ensuring that raw string input can be transformed into structured data suitable for downstream processing and analysis.
 */

package eventstream.beam.transformations.helpers

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.createEntityFromCsvLine
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
    private val entity: Class<T>,
    private val coder: Coder<T?>
) : DoFn<String, T?>() {

    @ProcessElement
    fun processElement(c: ProcessContext, receiver: OutputReceiver<T?>) {
        val line = c.element()
        val result: T? = try {
            entity.createEntityFromCsvLine(line)
        } catch (e: Exception) {
            null // Handle error appropriately
        }
        if (result != null) {
            receiver.output(result)
        }
    }
}

fun <T : BeamEntity> applyCsvToEntityTransformation(
    input: PCollection<String>,
    entity: Class<T>,
    coder: Coder<T?>
): @UnknownKeyFor @Initialized PCollection<T?> {
    val doFn = ConvertCsvToEntityFn(entity, coder)
    val parsedEntityPCollection = input.apply("Parse CSV Lines to Entities", ParDo.of(doFn))
    return parsedEntityPCollection.setCoder(coder as Coder<T?>)
}

/* Test Serialization for Beam in Isolation */
//    val entity: SerializableEntity<FredSeriesMod> = FredSeriesMod.Companion
//    val coder: Coder<FredSeriesMod?> = NullableCoder.of(AvroCoder.of(FredSeriesMod::class.java))
//    val doFn = ConvertCsvToEntityFn(entity, coder)
//    SerializableUtils.ensureSerializable(doFn)