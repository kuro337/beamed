# flink k8

##### Launching the Flink Operator on k8

```bash
# Install the Cert Manager (req once per k8 cluster)
kubectl create -f https://github.com/jetstack/cert-manager/releases/download/v1.8.2/cert-manager.yaml
helm repo add flink-operator-repo https://downloads.apache.org/flink/flink-kubernetes-operator-1.6.1/
helm install flink-kubernetes-operator flink-operator-repo/flink-kubernetes-operator
```

<hr/>
<hr/>

Run a Sample Flink Job to confirm Flink is functional

```bash
# Create a Sample Job
kubectl create -f https://raw.githubusercontent.com/apache/flink-kubernetes-operator/release-1.6/examples/basic.yaml

# Check Logs
kubectl logs -f deploy/basic-example
# Access Dash
kubectl port-forward svc/basic-example-rest 8081
# Delete Job
kubectl delete flinkdeployment/basic-example
```

<hr/>
<hr/>

#### Launching Flink Session Cluster

- Launching a Persistent Flink Cluster where we can submit jobs using Beam Pipelines we create as required.

```yaml
apiVersion: flink.apache.org/v1beta1
kind: FlinkDeployment
metadata:
  name: flink-cluster-session
spec:
  image: flink:1.16
  flinkVersion: v1_16
  flinkConfiguration:
    taskmanager.numberOfTaskSlots: "2"
  serviceAccount: flink
  jobManager:
    resource:
      memory: "2048m"
      cpu: 1
  taskManager:
    resource:
      memory: "2048m"
      cpu: 1
```

- Deploy and Check Dash

```bash
kubectl apply -f flink-cluster.yaml
# Check Dash - apps deployed to the k8 Cluster can also submit jobs to this Flink cluster
kubectl port-forward svc/flink-cluster-session-rest 8081
```

- Now we can kick off our Pipelines to run on Flink and define parallelism, concurrency, output sinks, and much more.

```kotlin
fun createFlinkPipeline() {
    val options = createFlinkPipelineOptions("localhost:8081", "s3BeamJob").also { flinkOptions ->
        attachAWSCredsToFlinkPipelineOptions(
            flinkOptions,
            "*********************",
            "********************************",
            "us-east-1"
        )
    }

    val pipeline = Pipeline.create(options)

    /* Define Pipeline and Processing Logic ..... */
}
```

- In case apps external to the k8 Cluster need to submit jobs to the Flink Cluster - expose the Flink Service

```bash
kubectl get svc | grep jobmanager
kubectl logs -l app=flink-operator --all-containers=true
```

- Resetting Flink Operator and Resources

```bash
helm uninstall flink-kubernetes-operator
kubectl delete -f https://github.com/jetstack/cert-manager/releases/download/v1.8.2/cert-manager.yaml
helm repo remove flink-operator-repo
```

<br/>

Operator
Reference https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-release-1.6/docs/try-flink-kubernetes-operator/quick-start/
