<br/>
<br/>

<div align="center">
  <img alt="Kotlin logo" height="200px" src="data/assets/kotlin.png">
</div>

# Beamed

_Big Data Streaming & Processing_
<hr/>
<hr/>

- Ingest, Transform, Analyze, and Export **Data** at any **Scale** Independent of Compute and Runtime.


- 100% _Kotlin Native_ libraries to fully create and use with a Real Time Event Streaming System.


- Fully Setup [Beam](#usage), [Flink](k8/flink/README.md), and [Kafka](k8/kafka/README.md) on a
  machine or running [Kubernetes](k8/k9/README.md).

<hr/>

### Apache Beam

- Define declarative runtime-independent flows for data ingest, transform, analysis, and much more.

### Apache Flink

- Hardware and Vendor Agnostic Compute Engine for Running Pipelines.

### Apache Kafka

- Distributed Event Broker Abstraction to use centrally with n number of applications and services.

### Kotlin

- Completely Interops with any JVM Runtime

- Clean, expressive, flexible type system and powerful language features; highly recommend checking out Kotlin!

<hr/>

## Usage

Functional and Object Oriented Utilities that can be utilized with your own Entity Classes.

<br/>

- Simple Beam Pipeline to Process and Analyze Data and Persist Outputs

```kotlin
import eventstream.beam.*

fun main() {
    InMemoryPipeline.runCategoricalAnalysis("data/input/simple_data.csv")
}
```

- Simply define classes for your Entities and leverage Beam in a runtime independent fashion using functionality
  utilities.

```kotlin
import eventstream.beam.*

fun main() {

    /* @Usage of Beam Library using Functional Utilities */

    /* Read Lines from CSV , Serialize, Transform to Generic Record, and Write as Parquet Files */

    Pipeline.create().apply {
        readCSVConvertToEntity<YourEntityClass>(
            listOf("data/input/yourentities.csv"),
            FredSeries::serializeFromCsvLine
        ).apply {
            toGenericRecords<YourEntityClass>().apply {
                logElements("Serialized Entities: ")
            }.apply {
                writeToParquet<FredSeries>("data/output/beam/parquet/")
            }
        }
    }.run().waitUntilFinish()

    /* Reading Back Transformed Parquet Files */

    Pipeline.create().apply {
        readParquetToGenericRecord<YourEntityClass>(
            listOf("data/output/beam/parquet/*",)
        ).apply {
            convertToBeamEntity<YourEntityClass>()
        }.apply {
            logElements().also { BeamLogger.logger.info { "Successfully Serialized from Parquet Files." } }
        }
    }.run().waitUntilFinish()
}
```

<hr/>

- Using `eventstream.kafka` package to interact with your Cluster.

```kotlin
import eventstream.kafka.*

fun main() {
    val logger = KotlinLogging.logger("Kafka.EntryPoint")
    logger.info { "Event Stream Kafka App Running." }

    val kafkaController = KafkaController.getDefaultKafkaController()

    try {
        kafkaController.createTopic("some-topic", 3, 1)
        kafkaController.sendMessage("some-topic", "someKey", "Hello Kafka!")

        kafkaController.createTopic("another-topic", 1, 1)
        kafkaController.sendMessage("another-topic", "anotherKey", "Hello Kafka!")

        /* Poll from the Beginning of Time 5s */
        kafkaController.readMessages("some-topic", 5000, EVENTSTREAM.KAFKA_EARLIEST)

        /* Poll from post Consumer Creation 10s */
        kafkaController.readMessages("another-topic", 10000, EVENTSTREAM.KAFKA_LATEST)

    } catch (e: Exception) {
        logger.error(e) { "An error occurred in the Kafka operations." }
    } finally {
        kafkaController.close()
    }

    logger.info { "Event Stream Kafka App Ending!" }
}
```

<hr/>

- Processing Data from an Object Store and using Beam and Flink for declarative Data Processing

```kotlin
import eventstream.beam.*

fun main() {

    FlinkS3Pipeline.run("s3://bucket_with_series")

    /* 
    
    Additionally:
    Run against S3 Compatible Interfaces
    
    Data Processing Pipeline against Minio - 
        - High Performance 
        - S3 Compatible Object Store
        - Run as a Server for higher Throughput
        
     */

    FlinkS3Pipeline.run("myminio/data/series/*")

}
```

<hr/>

- Java `8` and `11` compatible `Gradle` builds and `fatJars` for each independent package.


- ` Dockerfiles`  and `kube yaml` [Deployments](k8/) to get `Kafka` and `Flink` up and running either locally
  using `Docker` or
  deploying directly to `Kubernetes`

- `./BUILD_EVENTSTREAM.sh` - Bash Script to compile and build and publish libraries to `Maven Central` if users want to
  extend the utilities or fork the library.

<hr/>

##### Building and Running

```bash
# Run Tests, Compile, Build, and Publish Library
./BUILD_EVENTSTREAM.sh

## Individual Commands (Optionally)

# Build all Packages
gradle clean build

# Running
gradle clean run :beam
gradle clean run :kafka
gradle clean run :flink

# Uber jar's 
gradle shadowJar :beam

# Running Tests
gradle clean test

# Output Java Runtime Version for Final Jar's 
gradle checkBytecodeVersion
```

<hr/>

###### Additional Concepts and Documentation

Launching a Fully Functional Kafka and Flink Cluster on k8

- [Flink](k8/flink/README.md): `k8/flink`
- [Kafka](k8/kafka/README.md): `k8/kafka`

Beam Concepts

- [Beam](docs/beam): `docs/beam`

Generics in Kotlin

- [Generics](docs/generics/reified.md): `docs/generics`

Gradle Docs for Writing and Publishing Large Library Codebases

- [Gradle](docs/gradle/multiapp_setup.md): `docs/gradle`

<hr/>


`Class Version Spec`

| Java Class Version | Java SE |
|--------------------|---------|
| 50                 | Java 6  |
| 51                 | Java 7  |
| 52                 | Java 8  |
| 53                 | Java 9  |
| 54                 | Java 10 |
| 55                 | Java 11 |
| 56                 | Java 12 |
| 57                 | Java 13 |
| 58                 | Java 14 |
| 59                 | Java 15 |
| 60                 | Java 16 |
| 61                 | Java 17 |
| 62                 | Java 18 |
| 63                 | Java 19 |
| 64                 | Java 20 |
| 65                 | Java 21 |
| 66                 | Java 22 |

<hr/>

Author: [kuro337](https://github.com/kuro337)