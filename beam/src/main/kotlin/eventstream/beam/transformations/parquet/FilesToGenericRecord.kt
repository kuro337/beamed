package eventstream.beam.transformations.parquet


object FilesToGenericRecord {
    fun readParquetFiles() {

        /* READ PARQUET */

        // Apply the read transform to get a PCollection<GenericRecord>
//    val readUserCollection = pipeline.apply(
//        "Read Parquet File",
//        ParquetIO.read(BeamUser.getParquetSchema()).from("data/output/beam/output-00000-of-00002.parquet")
//    )
//
//    //Log the contents of the PCollection read from the Parquet file
//    val readParquetFromDisk =
//        readUserCollection.apply("Log Read Records", ParDo.of(object : DoFn<GenericRecord, Void>() {
//            @ProcessElement
//            fun processElement(@Element record: GenericRecord, receiver: OutputReceiver<Void>) {
//                println("Logging GenericRecord $record") // Log the record to the console
//            }
//        }))
//
//
//    // Convert GenericRecords to BeamUser instances
//    val beamUsers: PCollection<BeamUser> = readUserCollection.apply(
//        "Convert to BeamUser",
//        MapElements.into(TypeDescriptor.of(BeamUser::class.java))
//            .via(SerializableFunction<GenericRecord, BeamUser> { record ->
//                genericRecordToBeamUser(record)
//            })
//    ).setCoder(AvroCoder.of(BeamUser::class.java))
//
//    beamUsers.apply("Log BeamUser Records", ParDo.of(LogBeamUserFn()))
    }

}