package eventstream.beam.entity

import eventstream.beam.interfaces.entity.*
import eventstream.beam.models.FredSeries
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.testing.PAssert
import org.apache.beam.sdk.testing.TestPipeline
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.values.PCollection
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntityExtensionFunctionsTest {
    @JvmField
    @Rule
    @Transient
    val pipeline: TestPipeline = TestPipeline.create()

    private val fredSeriesCsvLine =
        "id,title,observationStart,observationEnd,frequency,units,seasonal_adjustment,lastUpdated,popularity,groupPopularity,notes"

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

    @Test
    fun testExtensionGetAvroSchema() {
        val schema = FredSeries::class.java.getAvroSchema()
        assertNotNull(schema, "Schema should not be null")
    }

    @Test
    fun testExtensionGetAvroCoder() {
        val coder = FredSeries::class.java.getAvroCoder()
        assertNotNull(coder, "Coder should not be null")
    }

    @Test
    fun testExtensionGetParquetCoder() {
        val coder = FredSeries::class.java.getParquetCoder()
        assertNotNull(coder, "Parquet Coder should not be null")
    }

    @Test
    fun testExtensionGetParquetSchema() {
        val schema = FredSeries::class.java.getParquetSchema()
        assertNotNull(schema, "Parquet Schema should not be null")

        val expectedSchema = """
{"type":"record","name":"FredSeries","namespace":"eventstream.beam.models","fields":[{"name":"frequency","type":"string"},{"name":"groupPopularity","type":"int"},{"name":"id","type":"string"},{"name":"lastUpdated","type":"string"},{"name":"notes","type":"string"},{"name":"observationEnd","type":"string"},{"name":"observationStart","type":"string"},{"name":"popularity","type":"int"},{"name":"seasonal_adjustment","type":"string"},{"name":"title","type":"string"},{"name":"units","type":"string"}]}
        """.trimIndent()

        assertEquals(schema.toString(), expectedSchema)
    }

    @Test
    fun testExtensionGetParquetSchemaNoNamespace() {
        val schema = FredSeries::class.java.getParquetSchemaNoNamespace()

        assertNotNull(schema, "Schema without namespace should not be null")
        assertFalse { schema.namespace != null }
        assertTrue { schema.namespace == null }
    }


    @Test
    fun testExtensionGetGenericRecordAvroCoder() {
        val coder = FredSeries::class.java.getGenericRecordAvroCoder()

        val genericRecords: List<GenericRecord> = listOf(fredSeries.getAvroGenericRecord())
        val genericRecordCollection =
            pipeline.apply(Create.of(genericRecords).withCoder(FredSeries::class.java.getGenericRecordAvroCoder()))

        genericRecordCollection.setCoder(coder)

        PAssert.that(genericRecordCollection).satisfies { collection ->
            assertNotNull(collection, "GenericRecord collection should not be null")
            null
        }
    }


    @Test
    fun testExtensionLogEntityCollection() {
        val fredSeriesCollection: PCollection<FredSeries> = pipeline.apply(Create.of(fredSeries))
        val loggedCollection = logEntityCollection(fredSeriesCollection)
    }

    @Test
    fun testExtensionCreateEntityFromCsvLine() {
        val fredSeriesParsed = FredSeries::class.java.createEntityFromCsvLine(fredSeriesCsvLine)
        assertNotNull(fredSeriesParsed, "Parsed entity should not be null")
    }

    @Test
    fun testExtensionCreateEntityFromGenericRecord() {
        val genericRecord = fredSeries.getAvroGenericRecord()
        val fredSeriesParsed = FredSeries::class.java.createEntityFromGenericRecord(genericRecord)
        assertNotNull(fredSeriesParsed, "Parsed entity from generic record should not be null")
    }
}