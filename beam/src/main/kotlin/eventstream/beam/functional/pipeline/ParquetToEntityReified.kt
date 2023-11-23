/*
Read Parquet Files to Entities

@Usage

val pipeline = Pipeline.create(/* options */)
val parquetRecords: PCollection<GenericRecord> = pipeline.readParquetToGenericRecord<MyBeamEntityClass>(
   listOf("path/to/parquet/files.parquet")
  )

*/


package eventstream.beam.functional.pipeline

import eventstream.beam.helpers.fileExists
import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.interfaces.entity.getParquetSchemaNoNamespace
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.avro.generic.GenericRecord
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.Flatten
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PCollectionList


object ParquetReadLogger {
    val logger = KotlinLogging.logger { }
}


/**
 * #### Read Parquet Files from Disk - `Pipeline.readParquetToGenericRecord`
 * - Example Usage
 * ```kotlin
 * /* Reading the transformed Parquet Files , Serializing , and Logging */
 *
 * Pipeline.create().apply {
 *        readParquetToGenericRecord<FredSeries>(
 *            listOf(
 *                "data/output/beam/parquet/output-00000-of-00004.parquet",
 *                "data/output/beam/parquet/output-00001-of-00004.parquet",
 *                "data/output/beam/parquet/output-00002-of-00004.parquet",
 *                "data/output/beam/parquet/output-00003-of-00004.parquet",
 *            )
 *        ).apply {
 *                 convertToBeamEntity<FredSeries>()
 *             }.apply {
 *                 logElements().also { BeamLogger.logger.info { "Successfully Serialized from Parquet Files." } }
 *             }
 *     }.run().waitUntilFinish()
 *
 * ```
 */
inline fun <reified T : BeamEntity> Pipeline.readParquetToGenericRecord(
    inputFiles: List<String>, exitNotFound: Boolean = false
): PCollection<GenericRecord> {
    val parquetSchema = T::class.java.getParquetSchemaNoNamespace()

    val existingFiles = inputFiles.filter { fileExists(it) }
    if (existingFiles.isEmpty()) {
        if (exitNotFound) {
            throw RuntimeException("No files found from $inputFiles. Exiting pipeline execution.")
        }

        ParquetReadLogger.logger.warn {
            """
           No files found from $inputFiles.
           Please make sure Files are Present. 
           Returning an empty PCollection<GenericRecord>. 
           If you desire for the app to exit here - pass flag exitNotFound = true 
           Pipeline.readParquetToGenericRecord(listOf<String> , true) 
            """.trimIndent().replace("\n", " ")
        }

        // Return Empty PCollection<GenericRecord> if no Files Found.
        val coder = AvroCoder.of(GenericRecord::class.java, parquetSchema)
        return this.apply(Create.empty(coder))
    }

    /* Map over Parquet files, convert to PCollection<GenericRecord> using Schema for Class */
    val filePCollections: List<PCollection<GenericRecord>> = existingFiles.map { filePath ->
        this.apply(
            "Read Parquet File - $filePath",
            ParquetIO.read(parquetSchema).from(filePath)
        )
    }

    /* Flatten PCollections from all Parquet Files */
    return if (filePCollections.size > 1) {
        PCollectionList.of(filePCollections)
            .apply("Flatten PCollectionList into one PCollection", Flatten.pCollections())
    } else {
        filePCollections.first()
    }
}

/* Read Parquet Files from a Directory such as "data/output/beam/parquet/" */

/**
 * #### `Pipeline.readAllParquetFromDirectory`
 * - Example Usage
 * ```kotlin
 * val pipeline = Pipeline.create()
 * val parquetRecords: PCollection<GenericRecord> = pipeline.readAllParquetFromDirectory<MyEntityClass>(
 *     "data/output/beam/parquet/"
 * )
 *
 * ```
 */
inline fun <reified T : BeamEntity> Pipeline.readAllParquetFromDirectory(
    directoryPath: String, exitNotFound: Boolean = false
): PCollection<GenericRecord> {
    val parquetSchema = T::class.java.getParquetSchemaNoNamespace()

    // Check if any files exist in the directory
    val filePattern = "$directoryPath/*"
    if (!fileExists(filePattern)) {
        if (exitNotFound) {
            throw RuntimeException("No files found in directory $directoryPath. Exiting pipeline execution.")
        }

        ParquetReadLogger.logger.warn {
            """
           No files found from $directoryPath.
           Please make sure Files are Present. 
           Returning an empty PCollection<GenericRecord>. 
           If you desire for the app to exit here - pass flag exitNotFound = true 
           Pipeline.readParquetToGenericRecord(listOf<String> , true) 
            """.trimIndent().replace("\n", " ")
        }

        val coder = AvroCoder.of(GenericRecord::class.java, parquetSchema)
        return this.apply(Create.empty(coder))
    }

    return this.apply(
        "Read All Parquet Files in Directory",
        ParquetIO.read(parquetSchema).from(filePattern)
    ).setCoder(AvroCoder.of(GenericRecord::class.java, parquetSchema))
}
