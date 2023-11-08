# Publishing Library to Maven

- We can create a Seperate Library Module which provides the interface to expose from other folders

- Dependency `api` vs `implementation`

    - api is
        - public
        - Transitive - users will have access to the api dependencies in their compile classpath without needing to
          explicitly include them.
        - Compilation - Classes are available at compile time
        -
    - implementation is private
        - No Transitivity
        - Faster Build Times - because when implementation deps change - only the module gets recompiled.
        - Encapsulation: It allows better encapsulation of your code because you're signaling that the dependency should
          not be used directly by the consumers of your library.

```bash
dependencies {
    api("some.dependency:foo:1.0") // Exposed to consumers
    implementation("another.dependency:bar:1.0") // Not exposed to consumers
}

```

- `build.gradle.kts` for Library Folder

```gradle
plugins {
    // Other plugins
    `maven-publish`
}

// Configure publishing
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.yourdomain"
            artifactId = "module-name"
            version = "1.0.0"
        }
    }
    repositories {
        maven {
            url = uri("https://repo.maven.apache.org/maven2")
            credentials {
                username = "yourUsername"
                password = "yourPassword"
            }
        }
    }
}

```

- Build LibJar and Publish

- For each module we have - create a `libJar`.
- We will combine all of these `libJar` files - in our final Package we expose to the end User.

```gradle
tasks.register<Jar>("libJar") {
    from(sourceSets.main.get().output)
    archiveBaseName.set("beam")
    archiveClassifier.set("libJar") // adds a classifier to distinguish from the shadow JAR
}

/*
- Includes the main source set which will be all files under beam/src/main/ which is
    - kotlin/
    - resources/

from(sourceSets.main.get().output)

*/
```

- Then run `gradle clean libJar` to build the Lib Jars

- Aggregate jar for `library`

```
tasks.register<Jar>("aggregateJar") {
    // Add the compiled outputs of subprojects
    from({ zipTree(project(":beam").tasks.named("libJar").get().archivePath) })
    from({ zipTree(project(":kafka").tasks.named("jar").get().archivePath) }) // Assuming 'kafka' has a similar libJar task
    from({ zipTree(project(":utilities").tasks.named("jar").get().archivePath) })
    // Add any other subprojects as necessary

    // Set the classifier to null to create a primary artifact
    archiveClassifier.set(null)
}

```

- Then run `gradle clean aggregateJar` to create the library jar file

```bash
gradle clean aggregateJar

> Cannot expand ZIP 'D:\Code\Kotlin\projects\eventstream\beam\build\libs\beam-libJar.jar' as it does not exist.

```

```bash
# note - gradle clean will clear artifacts from prev ran gradle <task> commands
# to build the library JAR.
gradle libJar 
gradle aggregateJar
gradle publishToMavenLocal

gradle build publishToMavenLocal

gradle --refresh-dependencies                                         
 
# to publish your artifacts to the specified repository.
gradle publish

gradle publishToMavenLocal

gradle :library:tasks --all

# Build the Lib to publish as a Module
gradle build publishToMavenLocal
gradle :library:publishToMavenLocal

import com.github.kuro337.beamed

    beamed.SerializeModels.serializeFedSeries()

```

- Library in Local Maven

```bash
jar tf <your-jar-file>.jar

jar tf beam-1.0.0.jar 
cd /c/Users/chinm/.m2/repository/com/github/kuro337/beamed/1.0.0
jar tf beam-1.0.0.jar | grep eventstream/beam


/c/Users/chinm/.m2/repository/com/github/kuro337/beamed/1.0.0/beamed-1.0.0.jar 

 gradle :app:run

 gradle :app:build

./gradlew :beam:libJar

./gradlew :library:publishToMavenLocal

import eventstream.beam

./gradlew libJar

./gradlew publishToMavenLocal

    implementation 'eventstream:beam:1.0.0'

java -jar beam/build/libs/beam-eventstreaming-1.0.0.jar 

gradle --refresh-dependencies

gradle clean build publishToMavenLocal 

gradle :beam:publishToMavenLocal build publishToMavenLocal taskTree

task-tree

# Build, and Publish Lib to MavenLocal for :beam
gradle :beam:clean :beam:build :beam:publishToMavenLocal

gradle :app:clean :app:build :app:run


```
