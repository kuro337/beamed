# kafka k8

Launching Kafka

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install kafka bitnami/kafka
kubectl get pods

# Processes with Top Memory
ps aux --sort=-%mem | head -10
ps aux --sort=-%mem | awk '{print $4, $11}' | head -10
ps aux --sort=-%mem | awk '{print $4, $11, $12, $13}' | head -10
```

Apps deployed to the cluster just needs to reference the `environment variable` to access Kafka brokers

```bash
name : kafka-user-passwords
key  : client-passwords

# Optionally - get the auto-generated password for the client directly from the Cluster
kubectl get secret kafka-user-passwords --namespace default -o jsonpath='{.data.client-passwords}' | base64 --decode
```

Sample deployment using a Ktor server that uses the `eventstream.kafka` library to interact with Kafka

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-app-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka-app
  template:
    metadata:
      labels:
        app: kafka-app
    spec:
      containers:
        - name: kafka-app
          image: <image-repo-preferred/<imagename>:latest
          env:
            - name: KAFKA_PASSWORD # 
              valueFrom:
                secretKeyRef:
                  name: kafka-user-passwords
                  key: client-passwords
          imagePullPolicy: IfNotPresent
      imagePullSecrets: # For kube runtime to have access to the App Image
        - name: ghcr-secret
```

Within the application you write - simply access the Kafka password using the environment variable

```kotlin
val password = System.getenv("KAFKA_PASSWORD")
```

Deploy App - and check Logs to see Topic Creation , Partition Creation, and Reading from the Offset

```bash
kubectl apply -f kafka-app.yaml
kubectl logs -l app=kafka-app --all-containers=true
```
