package eventstream.beam.pipeline.options

import org.apache.beam.runners.flink.FlinkPipelineOptions
import org.apache.beam.runners.flink.FlinkRunner
import org.apache.beam.sdk.options.Default
import org.apache.beam.sdk.options.Description
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.PipelineOptionsFactory

fun createFlinkPipelineOptions(flinkMasterURL: String, jobName: String): FlinkPipelineOptions {
    /*
    @flinkMasterURL -> localhost:8081
    @s3BeamJob -> "Fed Data Parquet Run"
     */
    return PipelineOptionsFactory.fromArgs(
        "--flinkMaster=$flinkMasterURL",
        "--jobName=$jobName"
    ).`as`(FlinkPipelineOptions::class.java).also {
        it.runner = FlinkRunner::class.java
    }
}


interface FlinkPipelineOptions : PipelineOptions {
    @Description("Address of the Flink Master where the job should be submitted.")
    @Default.String("localhost:8081")
    fun getFlinkMaster(): String

    fun setFlinkMaster(value: String)
}
