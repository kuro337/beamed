package eventstream.beam.models.serializers.coders

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.values.TypeDescriptor

/**
 * #### `Coders`
 * - Set the Avro Coders for Beam Usage of Custom Classes
 *
 * - @ Usage
 * ```kotlin
 * setAvroCoders(pipeline, MyClass::class.java)
 * ```
 * @constructor @static Coders
 * @param pipeline Beam Pipeline
 * @param dataClass POJO Class
 * @return Unit
 * @author kuro337
 * @sample eventstream.beam.models.serializers.coders
 */
object Coders {
    fun <T> setAvroCoders(pipeline: Pipeline, dataClass: Class<T>) {
        // @Register Class
        pipeline.getSchemaRegistry().registerPOJO(dataClass)

        // @Register Coder for Class
        pipeline.getCoderRegistry().registerCoderForClass(dataClass, AvroCoder.of(dataClass))

        // @Register Coder for TypeDescriptor
        pipeline.getCoderRegistry().registerCoderForType(TypeDescriptor.of(dataClass), AvroCoder.of(dataClass))
    }
}
