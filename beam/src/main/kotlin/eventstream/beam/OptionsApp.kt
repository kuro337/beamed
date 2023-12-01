package eventstream.beam


import eventstream.beam.functional.pipeline.kafka.readFromKafkaByTimestamp
import eventstream.beam.functional.pipeline.logElements
import eventstream.beam.functional.pipeline.readCSVConvertToEntity
import eventstream.beam.models.FredSeries
import eventstream.beam.pipelines.createBeamPipeline
import eventstream.beam.pipelines.factory.createKafkaAWSOptions
import io.github.oshai.kotlinlogging.KotlinLogging

fun main(args: Array<String>) {

    val logger = KotlinLogging.logger {}



    logger.info { "Starting Options App" }

    val options = createKafkaAWSOptions(args)
    /* Direct Pipeline */

    val directPipeline = createBeamPipeline(args, options)

    val s3Read = directPipeline.readCSVConvertToEntity(
        listOf("s3://beam-kuro/fred_series.csv"),
        FredSeries::serializeFromCsvLine
    )

    s3Read.logElements("Serialized from S3 : ")


    val kafkaMessagesFromTimeStamp = directPipeline.readFromKafkaByTimestamp(
        topic = "new-topic",
        dateTimeStr = "2023-11-15 15:30:00" // Example date
    )

    kafkaMessagesFromTimeStamp.logElements("KafkaMsgFromTimestamp: ")


    directPipeline.run().waitUntilFinish()


    /* Flink Pipeline */

//    val flinkPipeline = createFlinkPipeline(jobName = "MyFlinkJob")
//
//    val fileLines =
//        flinkPipeline.readCSVConvertToEntity(listOf("s3://beam-kuro/fred_series.csv"), FredSeries::serializeFromCsvLine)
//
//    // Use the printLines function
//    fileLines.logElements("Serialized from S3: ")
//
//    flinkPipeline.run().waitUntilFinish()


}


