package eventstream.beam.inlined


import eventstream.beam.functional.pipeline.convertToBeamEntity
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.functional.pipeline.readAllParquetFromDirectory
import eventstream.beam.functional.pipeline.readParquetToGenericRecord
import eventstream.beam.models.FredSeries
import org.apache.beam.sdk.testing.PAssert
import org.apache.beam.sdk.testing.TestPipeline
import org.junit.Rule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BeamIOTests {
    @JvmField
    @Rule
    @Transient
    val pipeline: TestPipeline = TestPipeline.create()

    private val fredSeries = FredSeries(
        id = "id",
        title = "title",
        observationStart = "2021-01-01",
        observationEnd = "2021-01-31",
        frequency = "Monthly",
        units = "Units",
        seasonal_adjustment = "Adjusted",
        lastUpdated = "2021-01-31 00:00:00+00",
        popularity = 100,
        groupPopularity = 200,
        notes = "Some notes"
    )
    private val fredSeriesCsvLine =
        "id,title,observationStart,observationEnd,frequency,units,seasonal_adjustment,lastUpdated,popularity,groupPopularity,notes"


    @Test
    fun testReadParquetToGenericRecord() {
        val inputFiles = listOf(
            "src/test/resources/parquet_files/mock-data.parquet",
        )

        val parquetRecords = pipeline.readParquetToGenericRecord<FredSeries>(inputFiles)
        Assertions.assertNotNull(parquetRecords, "PColl<GR> should not be null")
        PAssert.that(parquetRecords).empty()

        val converted = parquetRecords.convertToBeamEntity<FredSeries>()

        
        Assertions.assertNotNull(converted, "PColl<GR> should not be null")

        converted.logElements("Logging in ParquetReadTestKt")


        PAssert.that(parquetRecords).satisfies { collection ->
            Assertions.assertNotNull(collection, "PColl<GR> should not be null")
            null
        }

    }

    @Test
    fun testReadAllParquetFromDirectory() {
        val directoryPath = "src/test/resources/parquet_files/"

        val parquetRecords = pipeline.readAllParquetFromDirectory<FredSeries>(directoryPath)
        PAssert.that(parquetRecords).empty()

        Assertions.assertNotNull(parquetRecords, "PColl<GR> should not be null")

        val converted = parquetRecords.convertToBeamEntity<FredSeries>()

        Assertions.assertNotNull(converted, "PColl<GR> should not be null")

        converted.logElements("Logging in ParquetReadTestKt")

        // Perform assertions on the resulting PCollection
        PAssert.that(parquetRecords).satisfies { collection ->
            Assertions.assertNotNull(collection, "PColl<Fred> should not be null")

            null // PAssert requires a return of null
        }

    }
}
