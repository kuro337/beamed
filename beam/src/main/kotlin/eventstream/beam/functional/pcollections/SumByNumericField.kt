package eventstream.beam.functional.pcollections

import eventstream.beam.interfaces.entity.BeamEntity
import org.apache.beam.sdk.transforms.Filter
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.transforms.Sum
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor
import kotlin.reflect.KProperty1


/*

@Usage

val pcollection: PCollection<MyEntity> = // ... obtain a PCollection of MyEntity

// Filter entities where the numeric field 'age' is greater than 25

val filteredByAge = pcollection.filterByNumericField(MyEntity::age) { it > 25 }

// Filter entities where the string field 'name' starts with "J"

val filteredByName = pcollection.filterByStringField(MyEntity::name) { it.startsWith("J") }


*/

fun <T : BeamEntity, R : Number> PCollection<T>.sumField(field: KProperty1<T, R>): PCollection<Double> {
    return this
        .apply(
            MapElements.into(TypeDescriptor.of(Double::class.java))
                .via(SerializableFunction { entity: T -> field.get(entity).toDouble() })
        )
        .apply(Sum.doublesGlobally())
}

fun <T : BeamEntity, R : Number> PCollection<T>.filterByNumericField(
    field: KProperty1<T, R>,
    condition: (R) -> Boolean
): PCollection<T> {
    return this.apply(Filter.by(SerializableFunction { entity: T ->
        val fieldValue = field.get(entity)
        val result = condition(fieldValue)
        /* logger.info { "Entity: $entity, Field: $fieldValue, Passed Filter: $result" } */
        result
    }))
}


fun <T : BeamEntity> PCollection<T>.filterByStringField(
    field: KProperty1<T, String>,
    condition: (String) -> Boolean
): PCollection<T> {
    return this.apply(Filter.by(SerializableFunction { entity: T ->
        condition(field.get(entity))
    }))
}
