package eventstream.beam.io


import org.apache.beam.sdk.io.aws2.options.S3Options
import org.apache.beam.sdk.io.aws2.s3.DefaultS3ClientBuilderFactory
import org.apache.beam.sdk.options.PipelineOptionsFactory
import software.amazon.awssdk.services.s3.S3ClientBuilder
import java.util.*

class S3Service(private val properties: Properties) {
    fun getS3Options(): S3Options {
        val args = arrayOf(
            "--awsAccessKey=${properties.getProperty("s3.accessKey")}",
            "--awsSecretKey=${properties.getProperty("s3.secretKey")}",
            "--endpoint=${properties.getProperty("s3.endpoint")}",
            "--region=${properties.getProperty("s3.region")}",
            "--s3ClientFactoryClass=eventstream.beam.io.GlobalAccessS3ClientBuilderFactory"
        )
        return PipelineOptionsFactory.fromArgs(*args)
            .withValidation()
            .`as`(S3Options::class.java)
    }
}

class GlobalAccessS3ClientBuilderFactory : DefaultS3ClientBuilderFactory() {
    override fun createBuilder(s3Options: S3Options): S3ClientBuilder {
        return super.createBuilder(s3Options)
    }
}

