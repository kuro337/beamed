package eventstream.beam.effects

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.values.PCollection

object WriteParquetCollection {
    fun writeParquetCollectionToDisk(input: PCollection<GenericRecord>, schema: Schema, path: String) {
        println("Writing with schema: $schema") // Add this line to print the schema

        input.apply(
            "Write to Parquet", FileIO
                .write<GenericRecord>()
                .via(
                    ParquetIO.sink(schema)

                    // .withCompressionCodec(CompressionCodecName.SNAPPY)
                )
                .to(path)
                .withSuffix(".parquet")
        )
    }

}