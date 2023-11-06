## Flink Cluster

- The Operator manages Flink and Kubernetes Resources

- In order to submit Beam jobs - we need to set up a Flink Cluster

- The Operator will create a Flink Cluster for us when we create a FlinkDeployment

<hr/>

##### Typical Usage Pattern

- When we run the `Kotlin Beam` Code (pipeline), the `FlinkRunner` will automatically package the pipeline code and its
  dependencies into a `JAR` file, submit that `JAR` to the `Flink cluster`, and initiate the execution of
  your `pipeline` on the
  cluster.

<hr/>

###### Session Cluster

- Launches a `Session Cluster` - which is a long running cluster that can run multiple jobs

###### Job Cluster

- Launches a `Job Cluster` - which is cluster launched to handle Computer per Job or by Groups of Jobs depending on
  the use case.