plugins {
    id("eventstream.kotlin-application-conventions")
    id("maven-publish")
    id("java-library")
    application
}

dependencies {

    configurations {
        all {
            exclude(group = "org.slf4j", module = "slf4j-simple")
        }
    }

    implementation("ch.qos.logback:logback-classic:1.4.11")

    api("io.github.oshai:kotlin-logging-jvm:5.1.0")

    //api("eventstream:utilities:1.0.0")

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
    testImplementation("eventstream:utilities:1.0.0")

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


/*

./gradlew :utilities:build
./gradlew :utilities:publishToMavenLocal
./gradlew  :beam:build :beam:publishToMavenLocal
./gradlew clean :beam:build :beam:publishToMavenLocal

cd ~/.m2/repository/eventstream/beam/1.0.2
jar tf beam-1.0.2.jar

 */