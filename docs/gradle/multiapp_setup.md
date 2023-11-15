# Gradle Multi Module Library Setup

```bash
# Gradle 8.4

gradle init
Application # 2
Kotlin      # 4
Multi-App   # yes
Script DSL  # 1

```

- This sets up a Gradle Multi Module Library Repo

- We can create new packages as Required for reusable Functionality

- Root `settings.gradle.kts` - define our packages for each Module.

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "eventstream"
include("app", "utilities", "beam", "kafka")

```

- Root `build.gradle.kts` - to define consistent settings

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java

}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

allprojects {


    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {

            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    tasks.withType<KotlinCompile> {


        kotlinOptions {
            jvmTarget = "11"
        }
    }
}


/* gradle checkBytecodeVersion --console=rich */
tasks.register("checkBytecodeVersion") {
    doLast {
        val classes = listOf(
            "app/build/classes/kotlin/main/eventstream/app/AppKt.class",
            "beam/build/classes/kotlin/main/eventstream/beam/AppKt.class",
            "kafka/build/classes/kotlin/main/eventstream/kafka/AppKt.class"
        )
        for (classPath in classes) {
            println("Checking bytecode version for $classPath")
            exec {
                commandLine("sh", "-c", "javap -verbose $classPath | grep major")
                /* Verbose Output commandLine("javap", "-verbose", classPath) */
                standardOutput = System.out
            }
        }
    }
}

/*
For Java 9 Target, Change to

java {
    toolchain {
        //languageVersion.set(JavaLanguageVersion.of(9))
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

allprojects {
    ....
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_1_9
            targetCompatibility = JavaVersion.VERSION_1_9
        }
    }
*/


/*
Java Class Version Reference

50 = Java 6
51 = Java 7
52 = Java 8
53 = Java 9
54 = Java 10
55 = Java 11
56 = Java 12
57 = Java 13
58 = Java 14
59 = Java 15
60 = Java 16
61 = Java 17
62 = Java 18
63 = Java 19
64 = Java 20
65 = Java 21
66 = Java 22
*/

```

- For individual packages - they can have their own settings and `build.gradle.kts`

- As an illustration - here I wish to establish runtime dependencies and settings to publish this module as a Library.

```kotlin
plugins {
    id("eventstream.kotlin-application-conventions")
    id("maven-publish")
    id("java-library")
    application
}

dependencies {
    api("org.slf4j:slf4j-simple:2.0.3")
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")
    api("org.jetbrains.kotlin:kotlin-reflect:1.9.20")

    api("org.apache.commons:commons-text")
    api("org.apache.beam:beam-sdks-java-core:2.50.0")
    api("org.apache.beam:beam-runners-direct-java:2.50.0")
    api("org.apache.beam:beam-runners-flink-1.16:2.50.0")
    api("org.apache.beam:beam-sdks-java-io-amazon-web-services2:2.51.0")
    api("org.apache.beam:beam-sdks-java-io-parquet:2.51.0")
    implementation("org.apache.hadoop:hadoop-core:1.2.1")
    api("software.amazon.awssdk:s3:2.21.10")

    /* Test Deps */
    testImplementation("org.apache.beam:beam-sdks-java-test-utils:2.51.0")
    testImplementation("junit:junit:4.13.2") // JUnit 4 for TestPipeline support
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.1")

}



java {
    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass.set("eventstream.beam.AppKt")
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "eventstream"
            artifactId = "beam"
            version = "1.0.2"
        }
    }
    repositories {
        mavenLocal()
    }
}

val libJar = tasks.register("libJar", Jar::class) {
    dependsOn(":utilities:classes")

    from(sourceSets["main"].output)

    archiveBaseName.set("eventstream")
    archiveClassifier.set("beam")
    archiveVersion.set("1.0.2")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

}

artifacts {
    add("archives", tasks.named("libJar"))
}

tasks.named("build") {
    dependsOn(tasks.named("libJar"))
}
```

- Now in our other apps - we can simply import the library we published!

- For example if we have a runtime Application called App

```kotlin
# project / app / build.gradle.kts
plugins {
    id("eventstream.kotlin-application-conventions")
    application
}

dependencies {
    implementation("eventstream:beam:1.0.2")
}

# project / app / src / main / kotlin / eventstream / app / App.kt

import eventstream . beam . functional . pipeline . *

fun main() {
    ClassFromLibrary.run()
}
```
