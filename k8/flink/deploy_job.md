# Deploying a Beam Job to Flink Cluster

- k8 Cluster should have AWS Access Key, Secret, and Flink Cluster URL set

```bash
kubectl create secret generic aws-secret \
  --from-literal=aws_access_key_id=YOUR_ACCESS_KEY \
  --from-literal=aws_secret_access_key=YOUR_SECRET_KEY

kubectl create configmap flink-config --from-literal=FLINK_MASTER_URL="flink-cluster-session-rest:8081"
```

- Create `ShadowJar` for Flink Job

```kotlin
/* Build ShadowJar for Flink */

tasks.shadowJar {
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer::class.java)
    isZip64 = true
    archiveFileName.set("FlinkApp-all.jar")
    manifest {
        attributes["Main-Class"] = "eventstream.beam.FlinkAppKt"
    }
}

// Build ShadowJar   ./gradlew shadowJar
```

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

docker build -t flink-app-test -f containers/flink/Dockerfile .
docker tag flink-app-test:latest ghcr.io/kuro337/flink-app-test:latest
docker push ghcr.io/kuro337/flink-app-test:latest
```

- Deployment File

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flink-job-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: flink-app
  template:
    metadata:
      labels:
        app: flink-app
    spec:
      containers:
        - name: flink-app-container
          image: ghcr.io/kuro337/flink-app-test:latest
          env:
            - name: FLINK_MASTER_URL
              valueFrom:
                configMapKeyRef:
                  name: flink-config
                  key: FLINK_MASTER_URL
            - name: AWS_ACCESS_KEY_ID
              valueFrom:
                secretKeyRef:
                  name: aws-secret
                  key: aws_access_key_id
            - name: AWS_SECRET_ACCESS_KEY
              valueFrom:
                secretKeyRef:
                  name: aws-secret
                  key: aws_secret_access_key
      imagePullSecrets:
        - name: ghcr-secret
```