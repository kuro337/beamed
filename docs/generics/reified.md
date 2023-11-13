# Reified

```kotlin
interface BeamEntity : Serializable {
    fun getFieldValue(fieldName: String): Any?
    fun getAvroGenericRecord(): GenericRecord

    fun toCsvLine(): String
    override fun toString(): String

    companion object {
        /* Types are erased at Runtime - so this would not let us get the Coder */
        fun getCoder(): Coder<T> {
            return AvroCoder.of(T::class.java)
        }

        /* Specify reified to maintain Types at Runtime */
        inline fun <reified T : BeamEntity> getAvroCoder(): Coder<T> {
            return AvroCoder.of(T::class.java)
        }


    }
}

fun main() {
    /*
        By using Reified we can obtain the Coder Dynamically at Runtime
        From the SuperType
     */

    val coderFromInterface = BeamEntity.getAvroCoder<FredSeries>()
    println(coderFromInterface.schema)


    /*
       Using the Companion Object - 
       we would need to hardcode the Coder 
       in the Companion Object for our BeamEntity

     */

    val specificCoder = FredSeries.getSpecificCoder()
    println(specificCoder.schema)
}

```

- Compiler copies the fucntions' bytecode into every place the function is called instead of actually calling the
  function

- Inline expansion happens during compilation

- Eliminates overhead with Function Calls especially for Higher Order Functions (funcs that take params) or funcs with
  types

- Increases size of Compiled Binary

```kotlin
inline fun <reified T> printClassInfo() {
    println(T::class)
}

// Usage
printClassInfo<MyClass>() // No need to pass MyClass::class, `T` is reified to `MyClass`

```