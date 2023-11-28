package eventstream.beam.pipelines.options

import org.apache.beam.sdk.io.aws2.options.AwsOptions
import org.apache.beam.sdk.options.PipelineOptions
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region


fun attachAWSCredsToFlinkPipelineOptions(
    options: PipelineOptions,
    accessKey: String,
    secretKey: String,
    region: String?
): PipelineOptions {
    return options.apply {
        `as`(AwsOptions::class.java).apply {
            awsCredentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
            awsRegion = mapRegionStrToAWSRegionEnum(region ?: "us-east-1")
        }
    }
}


fun mapRegionStrToAWSRegionEnum(region: String): Region {
    return when (region) {
        "us-east-1" -> Region.US_EAST_1
        "us-west-2" -> Region.US_WEST_2
        "us-east-2" -> Region.US_EAST_2
        "us-west-1" -> Region.US_WEST_1
        else -> throw IllegalArgumentException("Sorry, only these Regions Supported.")
    }
}

/*

US East (Ohio)	                us-east-2
US East (N. Virginia)	        us-east-1
US West (N. California)	        us-west-1
US West (Oregon)	            us-west-2
Africa (Cape Town)	            af-south-1
Asia Pacific (Hong Kong)	    ap-east-1
Asia Pacific (Hyderabad)	    ap-south-2
Asia Pacific (Jakarta)	        ap-southeast-3
Asia Pacific (Melbourne)	    ap-southeast-4
Asia Pacific (Mumbai)	        ap-south-1
Asia Pacific (Osaka)	        ap-northeast-3
Asia Pacific (Seoul)	        ap-northeast-2
Asia Pacific (Singapore)	    ap-southeast-1
Asia Pacific (Sydney)	        ap-southeast-2
Asia Pacific (Tokyo)	        ap-northeast-1
Canada (Central)	            ca-central-1
Europe (Frankfurt)	            eu-central-1
Europe (Ireland)	            eu-west-1
Europe (London)	                eu-west-2
Europe (Milan)	                eu-south-1
Europe (Spain)	                eu-south-2
Europe (Stockholm)	            eu-north-1
Europe (Zurich)	                eu-central-2
Israel (Tel Aviv)	            il-central-1
Middle East (Bahrain)	        me-south-1
Middle East (UAE)	            me-central-1
South America (São Paulo)	    sa-east-1
AWS GovCloud (US-East)	        us-gov-east-1
AWS GovCloud (US-West)	        us-gov-west-1

 */