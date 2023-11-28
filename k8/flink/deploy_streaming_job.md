# Deploying a Streaming Beam Job to Kube that reads from kafka Topic

- Create a Docker Image

```dockerfile
# Use a base image with Java (matching the version used in your project)
FROM openjdk:11-jre-slim

# Copy the shadow JAR file into the Docker image
COPY beam/build/libs/FlinkApp-all.jar /app/FlinkApp-all.jar

# Specify the entry point command to run your application
ENTRYPOINT ["java", "-jar", "/app/FlinkApp-all.jar"]

# Run from root with eventstream/ as the build Context
# docker build -t flink-app -f containers/flink/Dockerfile .
```

- Then run this to build and push

```bash
# Run from path where COPY is correct - and point to the Dockerfile location 

docker build -t flink-streaming-app-kafka -f containers/flink/streaming-kafka/Dockerfile .
docker tag flink-streaming-app-kafka:latest ghcr.io/kuro337/flink-streaming-app-kafka:latest
docker push ghcr.io/kuro337/flink-streaming-app-kafka:latest
```

- Confirming Logging for jar

```bash
./gradlew shadowJar

 ./gradlew clean :beam:build
 
./gradlew clean shadowJar

java -Dlogback.debug=true -jar beam/build/libs/FlinkApp-all.jar

java -Dlogback.debug=true -jar app/build/libs/KafkaAppLibUsage-all.jar


./gradlew :beam:dependencies --configuration compileClasspath

./gradlew :beam:dependencies --configuration compileClasspath | grep 'org.slf4j'
./gradlew :beam:dependencyInsight --dependency logback-classic

./gradlew :beam:dependencyInsight --dependency slf4j

./gradlew :app:dependencyInsight --dependency slf4j

docker build --no-cache -t flink-streaming-app-kafka -f containers/flink/streaming-kafka/Dockerfile .
docker run --name flink-test -d flink-streaming-app-kafka
docker logs -f flink-test  > classpath_output.txt

docker stop flink-test
docker rm flink-test

implementation("org.slf4j:slf4j-logback:2.0.9")

java -cp beam/build/libs/FlinkApp-all.jar eventstream.beam.FlinkAppKt

java -jar beam/build/libs/beam-standalone.jar

jar tf beam/build/libs/FlinkApp-all.jar | grep 'META-INF/services'
jar tf beam/build/libs/FlinkApp-all.jar | grep 'org/slf4j'
jar tf beam/build/libs/FlinkApp-all.jar | grep 'ch/qos/logback'

./gradlew clean shadowJar


# Check Manually
docker run -it --rm --entrypoint sh flink-streaming-app-kafka
java -jar beam/build/libs/FlinkApp-all.jar

docker exec -it flink-test sh
ls -l /app
java -jar /app/FlinkApp-all.jar


jar tf beam/build/libs/FlinkApp-all.jar | grep 'org/slf4j'
jar tf beam/build/libs/FlinkApp-all.jar | grep 'ch/qos/logback'

jar tf beam/build/libs/FlinkApp-all.jar | grep 'org/slf4j/impl/StaticLoggerBinder.class'

jar tf beam/build/libs/FlinkApp-all.jar | grep 'StaticLoggerBinder.class'

# Make sure logback.xml in jar
jar tf beam/build/libs/FlinkApp-all.jar | grep 'logback.xml'

docker exec -it flink-test sh
java -cp /app/FlinkApp-all.jar org.slf4j.impl.StaticLoggerBinder


docker build -t flink-streaming-app-kafka -f containers/flink/streaming-kafka/Dockerfile .

docker build --no-cache -t flink-streaming-app-kafka -f containers/flink/streaming-kafka/Dockerfile .


docker run -it --rm --entrypoint sh flink-streaming-app-kafka

docker run --name flink-test -d flink-streaming-app-kafka
docker logs -f flink-test

docker run -it --rm --entrypoint sh flink-streaming-app-kafka
jar tf /app/FlinkApp-all.jar | grep 'org/slf4j'
jar tf /app/FlinkApp-all.jar | grep 'ch/qos/logback'

docker stop flink-test
docker rm flink-test

# Checking slf4j Bindings
jar tf beam/build/libs/FlinkApp-all.jar | grep 'slf4j'

jar xf beam/build/libs/FlinkApp-all.jar META-INF/services/org.slf4j.spi.SLF4JServiceProvider

ls beam/build/libs/

./gradlew :beam:dependencies --configuration runtimeClasspath

-Dlogback.configurationFile

mkdir extractedJar
cd extractedJar
jar xf ../beam/build/libs/FlinkApp-all.jar
find . -name '*logback*'

cat classpath_output.txt | grep "org.slf4j.impl.StaticLoggerBinder"
cat classpath_output.txt | grep "Proxy120"

cat classpath_output.txt | grep "logback"

```

- App

```bash

./gradlew clean shadowJar
./gradlew clean customJar

java -Dlogback.debug=true -jar app/build/libs/KafkaAppLibUsage-all.jar

java -Dlogback.debug=true -jar app/build/libs/custom-app.jar

ls app/build/libs/

KafkaAppLibUsage-all.jar

./gradlew dependencyInsight --dependency slf4j

jar tf app/build/libs/KafkaAppLibUsage-all.jar | grep -E 'logback|META-INF/services'


ch.qos.logback.classic.spi.LogbackServiceProvider

chinm@DESKTOP-H0OMSKL MINGW64 /d/Code/Kotlin/projects/eventstream/tempJarContents (main)
# Check Logback.xml
jar tf ../app/build/libs/KafkaAppLibUsage-all.jar | grep 'logback.xml'
logback.xml

# Check SLF4J Service Provider Configuration
jar tf ../app/build/libs/KafkaAppLibUsage-all.jar | grep 'META-INF/services/org.slf4j.spi.SLF4JServiceProvider'
META-INF/services/org.slf4j.spi.SLF4JServiceProvider

jar xf ../app/build/libs/KafkaAppLibUsage-all.jar META-INF/services/org.slf4j.spi.SLF4JServiceProvider

# Check that it points to LogbackServiceProvider
cat META-INF/services/org.slf4j.spi.SLF4JServiceProvider
ch.qos.logback.classic.spi.LogbackServiceProvider


jar xf ../app/build/libs/KafkaAppLibUsage-all.jar META-INF/services/org.slf4j.spi.SLF4JServiceProvider
cat META-INF/services/org.slf4j.spi.SLF4JServiceProvider

java -verbose:class -Dlogback.debug=true -jar app/build/libs/KafkaAppLibUsage-all.jar > classLoading.log

grep "org.slf4j" classLoading.log
grep "ch.qos.logback" classLoading.log
grep "slf4j" classLoading.log

jar tf app/build/libs/KafkaAppLibUsage-all.jar | grep 'org/slf4j'
jar tf app/build/libs/KafkaAppLibUsage-all.jar | grep 'ch/qos/logback'

jar tf beam/build/libs/FlinkApp-all.jar | grep 'org/slf4j'
jar tf beam/build/libs/FlinkApp-all.jar | grep 'ch/qos/logback'

https://gradlehero.com/how-to-exclude-gradle-dependencies/

./gradlew clean :app:build

./gradlew shadowJar
jar tf app/build/libs/KafkaAppLibUsage-all.jar | grep slf4j
jar tf app/build/libs/KafkaAppLibUsage-all.jar | grep logback

java -Dlogback.debug=true -jar app/build/libs/KafkaAppLibUsage-all.jar

java -jar app/build/libs/KafkaAppLibUsage-all.jar


```