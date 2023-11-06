package eventstream.beam.pipeline.decorators

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PipelineType(val type: String)
