package eventstream.beam.models.serializers.coders

import org.apache.avro.Conversion
import org.apache.avro.LogicalType
import org.apache.avro.LogicalTypes
import org.apache.avro.Schema
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * #### `Serializer Overrides for Date Types`
 *
 * @author kuro337
 * @sample eventstream.beam.models.serializers.coders.LocalDateConversion
 */
class LocalDateConversion : Conversion<LocalDate>() {
    override fun getConvertedType(): Class<LocalDate> {
        return LocalDate::class.java
    }

    override fun getLogicalTypeName(): String {
        return LogicalTypes.date().name
    }

    override fun fromCharSequence(value: CharSequence?, schema: Schema?, type: LogicalType?): LocalDate {
        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    override fun toCharSequence(value: LocalDate, schema: Schema?, type: LogicalType?): CharSequence {
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    override fun getRecommendedSchema(): Schema {
        return LogicalTypes.date().addToSchema(Schema.create(Schema.Type.INT))
    }
}

