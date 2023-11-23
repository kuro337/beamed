package eventstream.beam.functional.pipeline

import eventstream.beam.interfaces.entity.BeamEntity
import eventstream.beam.logger.BeamLogger.logger

import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection


fun <T : BeamEntity> PCollection<T>.printBeamEntities(prefix: String = "Element: "): PCollection<T> {
    return this.apply("PrintBeamEntities", ParDo.of(object : DoFn<T, T>() {
        @ProcessElement
        fun processElement(@Element element: T, receiver: DoFn.OutputReceiver<T>) {

            logger.info { "$prefix${element.toString()}" }
            receiver.output(element)
        }
    }))
}

/*
@Usage

val pipeline = Pipeline.create(options)
val myPCollection: PCollection<FredSeries> = ...
myPCollection.printBeamEntities("Logged BeamEntity: ")

*/

fun <T> PCollection<T>.logElements(prefix: String = "Element: "): PCollection<T> {

    return this.apply("LogElements", ParDo.of(object : DoFn<T, T>() {
        @ProcessElement
        fun processElement(@Element element: T, receiver: OutputReceiver<T>) {
            logger.info { "$prefix$element" }
            receiver.output(element)
        }
    }))
}

/*
@Usage

val pipeline = Pipeline.create(options)
val myPCollection: PCollection<T> = ...
myPCollection.logElements("Logged Element: ")

 */

