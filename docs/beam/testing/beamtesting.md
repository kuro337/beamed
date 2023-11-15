# Beam Testing

- `org.apache.beam.sdk` Provides utilities to create Mock Pipelines and utilize your functionality.

- Methods

```kotlin
assertNotNull(objectUnderTest, "The object should not be null")

assertEquals(4, 2 + 2, "Addition result should be equal to 4")

assertTrue(2 + 2 == 4, "This expression should be true")

assertFalse(2 + 2 == 5, "This expression should be false")

assertThrows<IllegalArgumentException> {
    throw IllegalArgumentException("This should throw an IllegalArgumentException")
}


```

- Deps

```kotlin
    /* Test Deps */
testImplementation("org.apache.beam:beam-sdks-java-test-utils:2.51.0")
testImplementation("junit:junit:4.13.2") // JUnit 4 for TestPipeline support
testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.1")

```

- Creating Tests

The Beam Testing Library will implicitly run the `pipeline.run().waitUntilFinish()` lazily each time a test is run.
`

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.Rule
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.testing.PAssert
import org.apache.beam.sdk.testing.TestPipeline
import org.apache.beam.sdk.transforms.Count
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.avro.generic.GenericRecord

class TransformsTest {

    @JvmField
    @Rule
    @Transient
    val pipeline: TestPipeline = TestPipeline.create()

    @Test
    fun testEntityToGenericRecordTransformation() {
        // Arrange: Create a PCollection of BeamTestEntity entities.
        val beamUserEntities = listOf(BeamTestEntity("John Doe", 34), BeamTestEntity("Jane Doe", 28))
        val beamUserPCollection = pipeline.apply(Create.of(beamUserEntities))

        // Act: Transform BeamTestEntity entities to GenericRecords.
        val genericRecordsPCollection = beamUserPCollection.apply(
            ParDo.of(object : DoFn<BeamTestEntity, GenericRecord>() {
                @ProcessElement
                fun processElement(c: ProcessContext) {
                    val user = c.element()
                    c.output(user.beamuserGenericRecord())
                }
            })
        ).setCoder(AvroCoder.of(GenericRecord::class.java, BeamTestEntity.getParquetSchema()))

        PAssert.that(genericRecordsPCollection).satisfies { collection ->

            val countsCollection = genericRecordsPCollection.apply(Count.globally())

            PAssert.thatSingleton(countsCollection).isEqualTo(2L)
            null
        }
    }
}

```
