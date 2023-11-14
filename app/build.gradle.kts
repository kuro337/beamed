plugins {
    id("eventstream.kotlin-application-conventions")
    application
}

dependencies {
    implementation("eventstream:beam:1.0.2")
    api("org.slf4j:slf4j-simple:2.0.3")
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")
    api("eventstream:utilities:1.0.0")

//    api("org.apache.commons:commons-text")
//    api("org.apache.beam:beam-sdks-java-core:2.50.0")
//    api("org.apache.beam:beam-runners-direct-java:2.50.0")
//    api("org.apache.beam:beam-runners-flink-1.16:2.50.0")
//    api("org.apache.beam:beam-sdks-java-io-amazon-web-services2:2.51.0")
//    api("org.apache.beam:beam-sdks-java-io-parquet:2.51.0")
//    implementation("org.apache.hadoop:hadoop-core:1.2.1")
//
//
//    api("software.amazon.awssdk:s3:2.21.10")
}

application {
    mainClass.set("eventstream.app.AppKt")
}
