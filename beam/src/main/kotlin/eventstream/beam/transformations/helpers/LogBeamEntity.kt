package eventstream.beam.transformations.helpers

import eventstream.beam.interfaces.entity.BeamEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.beam.sdk.transforms.DoFn


object LoggerBeamEntity {
    val logger = KotlinLogging.logger {}
}

class LogBeamEntity<T : BeamEntity> : DoFn<T, T>() {


    @ProcessElement
    fun processElement(@Element entity: T?, out: OutputReceiver<T>) {
        LoggerBeamEntity.logger.info { entity.toString() }
        // Output the entity for downstream processing if necessary
        out.output(entity)
    }
}
