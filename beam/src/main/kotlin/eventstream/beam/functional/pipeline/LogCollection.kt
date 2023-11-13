package eventstream.beam.functional.pipeline

import eventstream.beam.logger.BeamLogger.logger

import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection

fun <T> PCollection<T>.logElements(prefix: String = "Element: "): PCollection<T> {

    return this.apply("LogElements", ParDo.of(object : DoFn<T, T>() {
        @ProcessElement
        fun processElement(@Element element: T, receiver: OutputReceiver<T>) {
            logger.debug { "$prefix$element" }
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
