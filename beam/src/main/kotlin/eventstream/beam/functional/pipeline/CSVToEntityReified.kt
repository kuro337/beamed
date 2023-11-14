/*
Read CSV Files to Entities

@Usage

val pipeline = Pipeline.create(options)
pipeline.readCSVTransformAndWrite<FredSeries>(files, FredSeries::parseCsvToEntity)
*/

package eventstream.beam.functional.pipeline

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.logger.BeamLogger.logger
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.NullableCoder
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.Flatten
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PCollectionList

inline fun <reified T : BeamEntity> Pipeline.readCSVConvertToEntity(
    csvFiles: List<String>,
    noinline createEntityFromCsvLine: (String) -> T?
): PCollection<T> {


    logger.info { "Reading ${csvFiles.size} CSV Files and Converting to PCollection<${T::class.simpleName}> " }

    /* Map over csv Files and Map Lines */
    val filePCollections: List<PCollection<String>> = csvFiles.map { pattern ->
        this.apply("Read CSV File Pattern $pattern", TextIO.read().from(pattern))
    }

    /* Flatten all PCollections into a single PCollection of lines */
    val allLines: PCollection<String> = PCollectionList.of(filePCollections)
        .apply("Flatten PCollectionList into one PCollection", Flatten.pCollections())

    /* Use function passed to convert Lines to Entities of Type T */
    val entityPCollection: PCollection<T> =
        allLines.apply("Convert CSV to Entity", ParDo.of(object : DoFn<String, T>() {
            @ProcessElement
            fun processElement(@Element line: String?, out: OutputReceiver<T>) {
                line?.let { csvLine ->
                    createEntityFromCsvLine(csvLine)?.let { entity ->
                        out.output(entity)
                    }
                }
            }
        }))

    // .setCoder(NullableCoder.of(T::class.java.getAvroCoder() as Coder<T>))

    logger.info { "Successfully Converted CSV to PCollection<${T::class.simpleName}> " }

    //   return entityPCollection
    val avroCoder = AvroCoder.of(T::class.java)
    return entityPCollection.setCoder(NullableCoder.of(avroCoder))
}

