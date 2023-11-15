#### buildSrc

- Defines Common Plugin settings by the files in buildSrc/src/main/kotlin

```bash
 - buildSrc/src/main/kotlin/
      eventstream.kotlin-application-conventions.gradle.kts  
      eventstream.kotlin-common-conventions.gradle.kts  
      eventstream.kotlin-library-conventions.gradle.kts

# We can reference this in our App such as 

plugins {
    id("eventstream.kotlin-application-conventions")
    application
}
```

- This will apply it for that app

```bash
./gradlew :app:dependencyInsight --dependency eventstream:beam:1.0.0
```