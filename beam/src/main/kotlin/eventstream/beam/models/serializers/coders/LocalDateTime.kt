package eventstream.beam.models.serializers.coders


import org.apache.avro.Conversion
import org.apache.avro.LogicalType
import org.apache.avro.LogicalTypes
import org.apache.avro.Schema
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeConversion : Conversion<LocalDateTime>() {
    override fun getConvertedType(): Class<LocalDateTime> {
        return LocalDateTime::class.java
    }

    override fun getLogicalTypeName(): String {
        return "timestamp-millis"
    }

    override fun fromLong(value: Long?, schema: Schema?, type: LogicalType?): LocalDateTime {

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value!!), ZoneOffset.UTC)
    }

    override fun toLong(value: LocalDateTime, schema: Schema?, type: LogicalType?): Long {
        return value.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    override fun getRecommendedSchema(): Schema {
        return LogicalTypes.timestampMillis().addToSchema(Schema.create(Schema.Type.LONG))
    }
}
