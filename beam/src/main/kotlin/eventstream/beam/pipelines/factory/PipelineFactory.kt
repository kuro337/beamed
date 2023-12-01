package eventstream.beam.pipelines.factory


import eventstream.beam.logger.BeamLogger
import eventstream.beam.pipelines.options.KafkaPipelineOptions
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


interface UnifiedPipelineOptions : KafkaPipelineOptions, AwsOptions {
    // Additional configurations can be added here if needed
}


fun createKafkaAWSOptions(args: Array<String>): UnifiedPipelineOptions {
    val password: String = System.getenv("KAFKA_PASSWORD") ?: ""
   
    val options = PipelineOptionsFactory.fromArgs(*args)
        .withValidation()
        .`as`(UnifiedPipelineOptions::class.java).apply {
            if (getBootstrapServers().isNullOrBlank()) {
                setBootstrapServers("kafka.default.svc.cluster.local:9092")
            }
            if (getSaslJaasConfig().isNullOrBlank()) {
                setSaslJaasConfig(
                    "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"${password}\";"
                )
            }
            if (getSecurityProtocol().isNullOrBlank()) {
                setSecurityProtocol("SASL_PLAINTEXT")
            }
            if (getSaslMechanism().isNullOrBlank()) {
                setSaslMechanism("SCRAM-SHA-256")
            }
            awsCredentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    System.getenv("AWS_ACCESS_KEY_ID"),
                    System.getenv("AWS_SECRET_ACCESS_KEY")
                )
            )
            setAwsRegion(Region.US_EAST_1)

        }


    return options
}


object PipelineFactory {


    fun createAWSBeamOptions(
        awsAccessKey: String? = System.getenv("AWS_ACCESS_KEY_ID"),
        awsSecretKey: String? = System.getenv("AWS_SECRET_ACCESS_KEY"),
    ): PipelineOptions {
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

        return options

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