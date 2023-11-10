package eventstream.beam

// Define an enum to represent the types of pipelines
enum class PIPELINE {
    FLINK_S3, IN_MEMORY, CAPITALIZE_LINE, CSV_SERIALIZE_ROWS, CATEGORICAL_ANALYSIS, SERIALIZE_ENTITY_FROM_CSV, SIMPLE_SCHEMA
}

// Define the interface
interface BeamPipeline {

    fun run(pipelineType: PIPELINE)
    fun getPipelines(): List<PIPELINE>
    fun getOptions(): BeamPipelineOptions
}

interface BeamPipelineOptions {
    fun requiredOptions(): Unit
}
