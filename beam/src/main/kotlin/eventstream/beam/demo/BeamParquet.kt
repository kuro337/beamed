package eventstream.beam.demo

import eventstream.beam.models.FredSeries
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor
import org.apache.parquet.hadoop.metadata.CompressionCodecName

/*
Path - cd /d/Code/Kotlin/projects/eventstream/data/output/beam

Metadata
parq  output-00000-of-00005.parquet

parq data/output/beam/output-00000-of-00005.parquet

Schema
parq  output-00000-of-00005.parquet  --schema

parq  data/output/beam/output-00000-of-00005.parquet  --schema

Top n Rows
parq output-00000-of-00005.parquet --head 10

 */
object BeamParquet {
    fun writeFredSeriesToParquet(input: PCollection<FredSeries>, outputPath: String) {

        val schema = AvroCoder.of(FredSeries::class.java).schema

        // Convert PCollection<FredSeries> to PCollection<GenericRecord>
        val fredSeriesModGenericRecords = input
            .apply(
                "Convert to GenericRecord", MapElements.into(TypeDescriptor.of(GenericRecord::class.java))
                    .via(SerializableFunction<FredSeries, GenericRecord> { fredSeriesMod ->
                        // Convert FredSeries instance to GenericRecord using its Avro schema
                        val recordBuilder = GenericRecordBuilder(AvroCoder.of(FredSeries::class.java).schema)
                        recordBuilder.set("id", fredSeriesMod.getFieldValue("id"))
                        recordBuilder.set("title", fredSeriesMod.getFieldValue("title"))
                        recordBuilder.set("observationStart", fredSeriesMod.getFieldValue("observationStart"))
                        recordBuilder.set("observationEnd", fredSeriesMod.getFieldValue("observationEnd"))
                        recordBuilder.set("frequency", fredSeriesMod.getFieldValue("frequency"))
                        recordBuilder.set("units", fredSeriesMod.getFieldValue("units"))
                        recordBuilder.set("seasonal_adjustment", fredSeriesMod.getFieldValue("seasonal_adjustment"))
                        recordBuilder.set("lastUpdated", fredSeriesMod.getFieldValue("lastUpdated"))
                        recordBuilder.set("popularity", fredSeriesMod.getFieldValue("popularity"))
                        recordBuilder.set("groupPopularity", fredSeriesMod.getFieldValue("groupPopularity"))
                        recordBuilder.set("notes", fredSeriesMod.getFieldValue("notes"))

                        recordBuilder.build() // Build the GenericRecord
                    })
            ).setCoder(AvroCoder.of(GenericRecord::class.java, schema))


        // Write PCollection<GenericRecord> to Parquet file
        fredSeriesModGenericRecords.apply(
            "Write to Parquet", FileIO
                .write<GenericRecord>()
                .via(
                    ParquetIO.sink(AvroCoder.of(FredSeries::class.java).schema)
                        .withCompressionCodec(CompressionCodecName.SNAPPY)
                )
                .to(outputPath)
                .withSuffix(".parquet")
        )
    }
}


// AvroCoder.of(FredSeries::class.java).schema
