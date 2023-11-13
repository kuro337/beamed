# Beam Transforms

- The Entities defined with `BeamEntity` interface can be used out of the box with the following operations

```kotlin
enum class PIPELINE {
    FLINK_S3,
    CAPITALIZE_LINE,
    CSV_SERIALIZE_ROWS,
    CATEGORICAL_ANALYSIS,
    CSV_TO_ENTITY,
    CSV_TO_ENTITY_COUNT_FIELDS,
    CSV_TO_ENTITY_TO_PARQUET,
    PARQUET_TO_ENTITY
}


```

## Parquet

- `PARQUET_TO_ENTITY` - `DiskParquetToGenericRecord.kt`

```kotlin

```

- `ENTITY_TO_PARQUET` - `WriteParquetCollection.kt`

```kotlin

```

- `PARQUET_TO_ENTITY` - `DiskParquetToGenericRecord.kt`

```kotlin

```

## CSV

- `CSV_TO_ENTITY` - `DiskParquetToGenericRecord.kt`

```kotlin

```

- `CSV_TO_ENTITY_TO_PARQUET` - `DiskParquetToGenericRecord.kt`

```kotlin

```

