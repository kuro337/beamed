package eventstream.beam.functional.pcollections

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.getParquetSchemaNoNamespace
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.io.Compression
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.values.PCollection

inline fun <reified T : BeamEntity> PCollection<GenericRecord>.writeToParquet(path: String) {
    // Assume that T can provide its own Schema

    println("Writing ${T::class.simpleName} records as Parquet Files to path $path")

    this.apply(
        "Write to Parquet", FileIO
            .write<GenericRecord>()
            .via(ParquetIO.sink(T::class.java.getParquetSchemaNoNamespace()))
            .withCompression(Compression.SNAPPY)
            .to(path)
            .withSuffix(".parquet")
    ).also {
        println("Success Writing ${T::class.simpleName} records as Parquet Files to path $path")

    }
}