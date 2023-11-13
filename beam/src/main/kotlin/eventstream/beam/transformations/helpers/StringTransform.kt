package eventstream.beam.transformations.helpers

import org.apache.beam.sdk.transforms.SimpleFunction

object StringTransform {

    class StringUpperCaseFunction : SimpleFunction<String, String>() {
        override fun apply(input: String): String {
            return input.uppercase()
        }
    }
}