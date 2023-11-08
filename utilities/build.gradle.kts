plugins {
    id("eventstream.kotlin-library-conventions")

    `kotlin`
    `java-library`
    `maven-publish`
}

group = "eventstream"
version = "1.0.0"

java {
    withJavadocJar()
    withSourcesJar()
}


dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}


tasks.build {
    dependsOn("publishToMavenLocal")
}

tasks.register<Jar>("libJar") {
    from(sourceSets.main.get().output)
    archiveBaseName.set("utilities")
    archiveClassifier.set("libJar")

}