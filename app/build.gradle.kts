plugins {
    id("eventstream.kotlin-application-conventions")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java-library")
}



dependencies {

    api("eventstream:beam:1.0.4")

    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
}

application {
    mainClass.set("eventstream.app.AppKt")
}

tasks.shadowJar {
    mergeServiceFiles()

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    transform(com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer::class.java)

    isZip64 = true
    archiveFileName.set("FlinkApp-all.jar")

    manifest {
        attributes["Main-Class"] = "eventstream.app.AppKt"
    }
}


