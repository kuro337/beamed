package eventstream.beam


import org.apache.beam.sdk.coders.Coder

interface BeamEntity {
    fun getCoder(): Coder<*>
    fun getFieldValue(fieldName: String): Any?

}


interface SerializableEntity<T : BeamEntity> : java.io.Serializable {
    fun serializeFromCsvLine(line: String): T?
    fun getCoder(): Coder<T>
}
