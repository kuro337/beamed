package eventstream.beam.logger

import io.github.oshai.kotlinlogging.KotlinLogging

object BeamLogger {
    val logger = KotlinLogging.logger { "eventstream.beam" }
}