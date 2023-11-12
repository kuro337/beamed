package eventstream.beam.transformations.csv
/*

Read a T : BeamEntity from a CSV File

Input Params : PCollection<String> (CSV Lines) , BeamEntityClass
Output : PCollection<T:BeamEntity>

*/
import eventstream.beam.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.coders.Coder
import org.apache.beam.sdk.coders.NullableCoder
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection
import java.io.Serializable


data class SerializationParams<T : BeamEntity>(val entityClass: Class<T>) : Serializable

class SerializeEntityFromCSVLines<T : BeamEntity> :
    BeamTransformation<SerializationParams<T>, PCollection<String>, PCollection<T>>() {
    private val logger = KotlinLogging.logger {}
    override val transformationType = TRANSFORMATION.CSV_TO_ENTITY
    override fun apply(input: PCollection<String>, params: SerializationParams<T>): PCollection<T> {

        val avroCoder = params.entityClass.getAvroCoder()

        val parseCsvToEntity: (String) -> T? = { csvLine ->
            params.entityClass.createEntityFromCsvLine(csvLine)
        }
        logger.info { "Coder from CSV to Entity Serialize ${avroCoder.toString()}" }

        /* Convert PCollection<String> -> PCollection<BeamEntity> */
        return input.apply(
            ParDo.of(object : DoFn<String, T>() {
                @ProcessElement
                fun processElement(@Element line: String?, out: OutputReceiver<T>) {
                    if (line != null) {
                        parseCsvToEntity(line)?.let { entityFromParse -> out.output(entityFromParse) }
                    }

                }
            })
        ).setCoder(NullableCoder.of(avroCoder as Coder<T>))

    }
}



