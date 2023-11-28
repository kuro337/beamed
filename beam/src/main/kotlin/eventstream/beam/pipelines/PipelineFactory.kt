package eventstream.beam.pipelines

import eventstream.beam.logger.BeamLogger
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.aws2.options.AwsOptions
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.PipelineOptionsFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region

/*
fun main() {
    val pipeline = PipelineUtils.createWithAWSCredsFromEnv().apply {
        // Pipeline transforms
    }
    pipeline.run().waitUntilFinish()
}
*/

object PipelineFactory {
    fun createWithAWSCredsFromEnv(
        awsAccessKey: String? = System.getenv("AWS_ACCESS_KEY_ID"),
        awsSecretKey: String? = System.getenv("AWS_SECRET_ACCESS_KEY"),
        region: String? = System.getenv("AWS_REGION"),
    ): Pipeline {
        val options: PipelineOptions = PipelineOptionsFactory.fromArgs("--runner=DirectRunner").create()
        options.`as`(AwsOptions::class.java).apply {
            awsRegion = mapRegionStrToAWSRegionEnum(region)
        }
        return Pipeline.create(options)

    }

    fun createWithAwsCredsFromEnv(
        awsAccessKey: String? = System.getenv("AWS_ACCESS_KEY_ID"),
        awsSecretKey: String? = System.getenv("AWS_SECRET_ACCESS_KEY"),
    ): Pipeline {


        val options = PipelineOptionsFactory.fromArgs("--runner=DirectRunner").create().`as`(AwsOptions::class.java)

        if (awsSecretKey == null && awsAccessKey == null) {
            BeamLogger.logger.warn { "Access Key and Secret Key from Environment is Null" }
            BeamLogger.logger.warn { "AK FROM ENV $awsAccessKey" }
        } else {
            options.setAwsCredentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
                )
            )
        }

        options.setAwsRegion(Region.US_EAST_1)

        return Pipeline.create(options)
    }

    private fun mapRegionStrToAWSRegionEnum(region: String? = "us-east-1"): Region {
        return when (region) {
            "us-east-1" -> Region.US_EAST_1
            "us-west-2" -> Region.US_WEST_2
            // ... other regions ...
            else -> throw IllegalArgumentException("Unsupported AWS Region: $region")
        }
    }
}

/*

val options: PipelineOptions = PipelineOptionsFactory.fromArgs("--runner=DirectRunner").create()
            .apply {
                `as`(AwsOptions::class.java).awsRegion = Region.US_EAST_1
            }

 */