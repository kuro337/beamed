package eventstream.beam.pipelines.decorators

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PipelineType(val type: String)
