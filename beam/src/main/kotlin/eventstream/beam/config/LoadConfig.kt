package eventstream.beam.config


import java.util.*

fun loadProperties(filePath: String): Properties {
    val properties = Properties()
    properties.load(Thread.currentThread().contextClassLoader.getResourceAsStream(filePath))
    return properties
}
