plugins {
    id("eventstream.kotlin-application-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

application {
    mainClass.set("eventstream.beam.AppKt")
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation(project(":utilities"))
    implementation("org.apache.commons:commons-text")
    implementation("org.apache.beam:beam-sdks-java-core:2.50.0")
    implementation("org.apache.beam:beam-runners-direct-java:2.50.0")
    implementation("org.apache.beam:beam-runners-flink-1.16:2.50.0")
    implementation("org.apache.beam:beam-sdks-java-io-amazon-web-services2:2.51.0")
    implementation("software.amazon.awssdk:s3:2.21.10")

}

tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-java-parameters")
    }
}




tasks.shadowJar {
    archiveBaseName.set("beam-eventstreaming")
    archiveClassifier.set("")
    isZip64 = true
    manifest {
        attributes(
            "Main-Class" to "eventstream.beam.AppKt"
        )
    }
    mergeServiceFiles()
}


application {
    // Define the main class for the application.
    mainClass.set("eventstream.beam.AppKt")
}
