# Shadow Jar

- Creating `shadowJar` executables with self contained deps.

- These are useful for running applications in settings that only have access to a `JVM` runtime.


- Settings in `build.gradle.kts` (Subproject) - so that the project has a self contained JAR with all dependencies

```bash
plugins {
    id("eventstream.kotlin-application-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

dependencies {
    ....
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
```

- Debugging

- Confirming the Package Classes are compiled to JVM ByteCode and Present
- Making sure the Dependencies are Included in the JAR

```bash
unzip -l beam/build/libs/beam-eventstreaming.jar | grep FlinkRunner

1484  1980-02-01 00:00   org/apache/beam/runners/flink/FlinkRunnerRegistrar$Options.class
6884  1980-02-01 00:00   org/apache/beam/runners/flink/FlinkRunner.class
530   1980-02-01 00:00   org/apache/beam/runners/flink/FlinkRunnerRegistrar.class
3388  1980-02-01 00:00   org/apache/beam/runners/flink/TestFlinkRunner.class
3433  1980-02-01 00:00   org/apache/beam/runners/flink/FlinkRunnerResult.class
2515  1980-02-01 00:00   org/apache/beam/runners/flink/FlinkRunner$1.class
1625  1980-02-01 00:00   org/apache/beam/runners/flink/FlinkRunnerRegistrar$Runner.class
```

```kotlin
tasks.shadowJar {
    mergeServiceFiles()

    /* Different Configurations */
    configurations = listOf(
        project.configurations.runtimeClasspath.get(),
        project.configurations.runtimeOnly.get(),
        project.configurations.implementation.get(),
        project.configurations.apiElements.get(),
        project.configurations.compileClasspath.get()
    ).onEach { it.isCanBeResolved = true }

    /* Excluding Dependencies for including deps' JAR files  */

    dependencies {
        exclude("org.slf4j:slf4j-simple")
        exclude("org.slf4j:slf4j-nop")
        exclude("org.slf4j:slf4j-jdk14")
        exclude("org.slf4j:slf4j-log4j12")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    transform(com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer::class.java)

    isZip64 = true
    archiveFileName.set("KafkaAppLibUsage-all.jar")

    manifest {
        attributes["Main-Class"] = "eventstream.app.AppKt"
    }
}
```