package eventstream.utilities.io.csv

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * #### `CSV Read Lines`
 * - Example Usage
 * ```kotlin
 * val csvLines = readCsvLines("path/to/your/file.csv")
 * csvLines.forEach { line ->
 *     println(line)
 * }
 * ```
 *
 * @param filePath Path to the CSV File
 * @return Unit
 * @throws IOException If an input/output error occurs.
 * @author kuro337
 */
fun readCsvLines(filePath: String): List<String> {
    return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8)
}


/**
 * #### `CSV Read Lines by Class Path`
 * - Example Usage
 * ```kotlin
 * val csvLines = readCsvByClassPath("file.csv")
 * csvLines.forEach { line ->
 *     println(line)
 * }
 * ```
 *
 * @param resourcePath Path to the CSV File
 * @return Unit
 * @throws IOException If an input/output error occurs.
 * @author kuro337
 */
fun readCsvByClassPath(resourcePath: String): List<String> {
    val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
    val reader = inputStream.bufferedReader(StandardCharsets.UTF_8)
    return reader.readLines()
}



