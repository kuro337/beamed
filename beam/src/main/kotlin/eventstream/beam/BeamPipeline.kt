package eventstream.beam


/* Pipelines Available - All Implement this Base Interface */
interface BeamPipeline {

    fun run(pipelineType: PIPELINE)
    fun getPipelines(): List<PIPELINE>
    fun getOptions(): BeamPipelineOptions
}

interface BeamPipelineOptions {
    fun requiredOptions(): Unit
}

/* Types of Pipeline Actions */
enum class PIPELINE {
    FLINK_S3,
    CAPITALIZE_LINE,
    ENTITY_TO_CSV,
    CSV_SERIALIZE_ROWS,
    CATEGORICAL_ANALYSIS,
    CSV_TO_ENTITY,
    CSV_TO_ENTITY_COUNT_FIELDS,
    CSV_TO_ENTITY_TO_PARQUET,
    PARQUET_TO_ENTITY
}
