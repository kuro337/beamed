package eventstream.beam.inlined

import eventstream.beam.functional.entity.*
import eventstream.beam.functional.pipeline.readCSVConvertToEntity
import eventstream.beam.functional.pipeline.toGenericRecords
import eventstream.beam.models.FredSeries
import org.apache.beam.sdk.testing.PAssert
import org.apache.beam.sdk.testing.TestPipeline
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class InlinedTests {
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
    fun readLinesFromCSV() {
        val entities = pipeline.readCSVConvertToEntity<FredSeries>(
            listOf("data/input/simple_data_noheaders.csv"),
            FredSeries::serializeFromCsvLine
        )

        PAssert.that(entities).satisfies { collection ->
            assertNotNull(collection)
            null // PAssert requires a return of null
        }


    }

    @Test
    fun convertPCollectionToGenericRecords() {
        /* Read Lines from CSV , Serialize, and Transform to Generic Record */
        val entities = pipeline.readCSVConvertToEntity<FredSeries>(
            listOf("data/input/simple_data_noheaders.csv"),
            FredSeries::serializeFromCsvLine
        )
        assertNotNull(entities)
        val genericRecords = entities.toGenericRecords<FredSeries>()
        assertNotNull(genericRecords)
    }

    @Test
    fun getSchemaFromReifiedType() {
        val fredSchema = getAvroSchema<FredSeries>()
        println("$fredSchema")
        assertNotNull(fredSchema)

    }

    @Test
    fun testExtensionGetAvroSchema() {
        val schema = getAvroSchema<FredSeries>()
        assertNotNull(schema, "Schema should not be null")
    }

    @Test
    fun testExtensionGetAvroCoder() {
        val coder = getAvroCoder<FredSeries>()
        assertNotNull(coder, "Coder should not be null")
    }

    @Test
    fun testExtensionGetParquetCoder() {
        val coder = getParquetCoder<FredSeries>()
        assertNotNull(coder, "Parquet Coder should not be null")
    }

    @Test
    fun testExtensionGetParquetSchema() {
        val schema = getParquetSchema<FredSeries>()
        assertNotNull(schema, "Parquet Schema should not be null")

        val expectedSchema = """
{"type":"record","name":"FredSeries","namespace":"eventstream.beam.models","fields":[{"name":"frequency","type":"string"},{"name":"groupPopularity","type":"int"},{"name":"id","type":"string"},{"name":"lastUpdated","type":"string"},{"name":"notes","type":"string"},{"name":"observationEnd","type":"string"},{"name":"observationStart","type":"string"},{"name":"popularity","type":"int"},{"name":"seasonal_adjustment","type":"string"},{"name":"title","type":"string"},{"name":"units","type":"string"}]}
        """.trimIndent()

        assertEquals(schema.toString(), expectedSchema)
    }


    @Test
    fun testExtensionCreateEntityFromCsvLine() {
        val fredSeriesParsed = createEntityFromCsvLine<FredSeries>(fredSeriesCsvLine)
        assertNotNull(fredSeriesParsed, "Parsed entity should not be null")
    }

    @Test
    fun testExtensionCreateEntityFromGenericRecord() {
        val genericRecord = fredSeries.getAvroGenericRecord()
        val fredSeriesParsed = createEntityFromGenericRecord<FredSeries>(fredSeries.getAvroGenericRecord())
        assertNotNull(fredSeriesParsed, "Parsed entity from generic record should not be null")
    }
}
