package eventstream.beam.transformations.csv
/*

Read a T : BeamEntity from a CSV File

Input Params : PCollection<String> (CSV Lines) , BeamEntityClass
Output : PCollection<T:BeamEntity>

*/
import eventstream.beam.BeamEntity
import eventstream.beam.BeamTransformation
import eventstream.beam.TRANSFORMATION
import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SimpleFunction
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptors


data class TransformationParams(
    val delimiter: Char = ',',
    val qualifier: Char = '\"'
    // Add any other necessary parameters
)

class EntityToCsv<T : BeamEntity>(
    val params: TransformationParams = TransformationParams() // defaults are used here

)

    : BeamTransformation<TransformationParams, PCollection<T>, PCollection<String>>() {
    override val transformationType: TRANSFORMATION = TRANSFORMATION.ENTITY_TO_CSV


    override fun apply(input: PCollection<T>, params: TransformationParams): PCollection<String> {
        return input
            .apply(MapElements.into(TypeDescriptors.strings()).via(
                object : SimpleFunction<T, String>() {
                    override fun apply(entity: T): String {
                        return entity.toCsvLine()
                    }
                }
            )).setCoder(StringUtf8Coder.of())
    }
}
