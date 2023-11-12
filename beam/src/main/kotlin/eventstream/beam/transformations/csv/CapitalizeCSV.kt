package eventstream.beam.transformations.csv

import eventstream.beam.BeamTransformation
import eventstream.beam.TRANSFORMATION
import eventstream.beam.effects.WriteCollection
import eventstream.beam.transformations.helpers.StringTransform
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor

data class UppercaseTransformParams(val output: String?, val writeFiles: Boolean?)

object CsvTransformation {

    object ConvertLinesUppercase :
        BeamTransformation<UppercaseTransformParams, PCollection<String>, PCollection<String>>() {
        override val transformationType = TRANSFORMATION.CAPITALIZE_LINE
        override fun apply(input: PCollection<String>, params: UppercaseTransformParams): PCollection<String> {
            val upperCaseLines = input
                .apply(
                    "Uppercase Lines", MapElements.into(TypeDescriptor.of(String::class.java))
                        .via(StringTransform.StringUpperCaseFunction())
                )

            // Optionally write to output if specified in params
            params.output?.takeIf { params.writeFiles ?: false }?.let { outputPath ->
                WriteCollection.outputCollections(upperCaseLines, outputPath, ".txt")
            }

            return upperCaseLines // Return the transformed PCollection
        }

    }

}