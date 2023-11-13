package eventstream.beam.transforms

import eventstream.beam.entity.BeamTestEntity
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.testing.PAssert
import org.apache.beam.sdk.testing.TestPipeline
import org.apache.beam.sdk.transforms.Count
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.junit.Rule
import org.junit.jupiter.api.Test

class TransformsTest {

    @JvmField
    @Rule
    @Transient
    val pipeline: TestPipeline = TestPipeline.create()


    @Test
    fun testEntityToGenericRecordTransformation() {
        val beamUserEntities = listOf(BeamTestEntity("John Doe", 34), BeamTestEntity("Jane Doe", 28))
        val beamUserPCollection = pipeline.apply(Create.of(beamUserEntities))

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

    @Test
    fun testGenericRecordToEntityTransformation() {
        val beamUserEntities = listOf(BeamTestEntity("John Doe", 34), BeamTestEntity("Jane Doe", 28))
        val genericRecords = beamUserEntities.map { it.beamuserGenericRecord() }
        val genericRecordsPCollection =
            pipeline.apply(Create.of(genericRecords).withCoder(BeamTestEntity.getGenericCoder()))

        val beamUserPCollection = genericRecordsPCollection.apply(
            ParDo.of(object : DoFn<GenericRecord, BeamTestEntity>() {
                @ProcessElement
                fun processElement(c: ProcessContext) {
                    val record = c.element()
                    val user =
                        BeamTestEntity.fromGenericRecord(record)
                    c.output(user)
                }
            })
        ).setCoder(AvroCoder.of(BeamTestEntity::class.java, BeamTestEntity.getParquetSchema()))

        PAssert.that(genericRecordsPCollection).satisfies { collection ->
            null
        }

    }
}




