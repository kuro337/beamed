/*
./gradlew :beam:run

./gradlew clean :beam:build
*/

plugins {
    id("eventstream.kotlin-application-conventions")
    id("maven-publish")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java-library")
}

dependencies {

    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    api("org.apache.beam:beam-sdks-java-core:2.51.0")

    implementation("org.apache.beam:beam-runners-direct-java:2.51.0")
    implementation("org.apache.beam:beam-runners-flink-1.16:2.51.0")
    implementation("org.apache.beam:beam-sdks-java-io-amazon-web-services2:2.51.0")
    implementation("org.apache.beam:beam-sdks-java-io-parquet:2.51.0")
    implementation("org.apache.beam:beam-sdks-java-io-kafka:2.51.0")     // 2.5.2 Causes Slf4J Failures
    implementation("org.apache.kafka:kafka-clients:3.6.0") // Kafka Interaction Dependency
    implementation("io.confluent:kafka-avro-serializer:7.5.1")
    implementation("io.confluent:kafka-schema-registry-client:7.5.1")
    implementation("org.apache.hadoop:hadoop-core:1.2.1")
    implementation("software.amazon.awssdk:s3:2.21.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
    implementation("org.apache.commons:commons-text")

    /* Test Deps */
    testImplementation("eventstream:utilities:1.0.0")
    testImplementation("org.apache.beam:beam-sdks-java-test-utils:2.51.0")
    testImplementation("junit:junit:4.13.2") // JUnit 4 for TestPipeline support
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.1")

}


/* Build ShadowJar for Flink */

tasks.shadowJar {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer::class.java)
    isZip64 = true

    archiveBaseName.set("OptionsApp")
    archiveClassifier.set("pipeline")


    manifest {
        attributes["Main-Class"] = "eventstream.beam.OptionsAppKt"
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass.set("eventstream.beam.OptionsAppKt")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "beam"

            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            groupId = "eventstream"
            version = "1.0.4"


        }
    }
    repositories {
        mavenLocal()
    }
}

//val libJar = tasks.register("libJar", Jar::class) {
//    dependsOn(":utilities:classes")
//
//    from(sourceSets["main"].output)
//
//    archiveBaseName.set("eventstream")
//    archiveClassifier.set("beam")
//    archiveVersion.set("1.0.4")
//
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//}
//
//artifacts {
//    add("archives", tasks.named("libJar"))
//}
//
//tasks.named("build") {
//    dependsOn(tasks.named("libJar"))
//}


/*

https://docs.gradle.org/current/userguide/publishing_maven.html


jar tf FlinkApp-all.jar | grep 'org/slf4j'
jar tf FlinkApp-all.jar | grep 'ch/qos/logback'

./gradlew :utilities:build
./gradlew :utilities:publishToMavenLocal
./gradlew  :beam:build :beam:publishToMavenLocal
./gradlew clean :beam:build :beam:publishToMavenLocal

cd ~/.m2/repository/eventstream/beam/1.0.2
jar tf beam-1.0.2.jar
 */

