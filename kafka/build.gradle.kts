plugins {
    id("eventstream.kotlin-application-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

application {
    mainClass.set("eventstream.kafka.AppKt")
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.apache.kafka:kafka-clients:3.6.0")


}

tasks.shadowJar {
    archiveBaseName.set("kafka-eventstreaming")
    archiveClassifier.set("")
    isZip64 = true
    manifest {
        attributes(
            "Main-Class" to "eventstream.kafka.AppKt"
        )
    }
    mergeServiceFiles()
}


application {
    // Define the main class for the application.
    mainClass.set("eventstream.kafka.AppKt")
}
