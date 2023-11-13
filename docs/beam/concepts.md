# Beam Concepts

Pipeline

- User Constructed Graph of Transformations

PCollection

- A Data Set or Data Stream. The Data thhat a pipeline processes is part of a PCollection

PTransform

- A PTransform represents a data processing operatioon or step in thhe pipeline.
- Transform is applied to 0 or more PCollection objects and producer 0 or more PCollection objects

Schema

- Language independent type definition for a PCollection

Window

- A PCollection can be subdivided into windows based on the timestamps of the individual elements. Windows enable
  grouping operations over collections that grow over time by dividing the collection into windows of finite
  collections.

Watermark

- A watermark is a guess as to when all data in a certain window is expected to have arrived. This is needed because
  data isn’t always guaranteed to arrive in a pipeline in time order, or to always arrive at predictable intervals.

Trigger

- A trigger determines when to aggregate the results of each window.

State and timers

- Per-key state and timer callbacks are lower level primitives that give you full control over aggregating input
  collections that grow over time.

Splittable DoFn

- Splittable DoFns let you process elements in a non-monolithic way. You can checkpoint the processing of an element,
  and the runner can split the remaining work to yield additional parallelism.


- Beam Pipeline Execution

    - In Beam - a Pipeline is a graph of transformations that process data
    - he graph consists of PCollection objects (which represent data sets) and PTransform objects (which represent data
      processing operations). When you create a pipeline and apply transformations to it, you are building up this
      graph.
    - So when do we get a PCollection and run apply on it - the Pipeline knows that it is a step.

```kotlin
   fun csvCapitalizePipeline(options: InMemoryPipelineOptions) {
    val pipeline = Pipeline.create()
    val readParams = CSVReadParameters(options.data)
    val lines = CSVSource.read(pipeline, readParams)
    val upperCaseLines = CsvRowMapper.transformRowsToUppercase(lines, options.output, options.writeFiles)
    pipeline.run().waitUntilFinish()
}

```