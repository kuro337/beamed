package eventstream.beam.io


import java.util.Properties

fun loadProperties(filePath: String): Properties {
    val properties = Properties()
    properties.load(Thread.currentThread().contextClassLoader.getResourceAsStream(filePath))
    return properties
}
