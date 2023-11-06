package eventstream.beam.models.serializers.avro

import eventstream.beam.models.serializers.coders.LocalDateConversion
import org.apache.avro.reflect.ReflectData
import org.apache.beam.sdk.extensions.avro.schemas.utils.AvroJavaTimeConversions


object AvroConfiguration {
    init {
        val reflectData = ReflectData.get()
        reflectData.addLogicalTypeConversion(LocalDateConversion())
        reflectData.addLogicalTypeConversion(AvroJavaTimeConversions.TimestampMillisConversion())
    }
}