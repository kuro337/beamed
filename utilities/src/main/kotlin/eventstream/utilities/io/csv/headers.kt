package eventstream.utilities.io.csv

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File


/**
 * #### `CSV Schema Printer`
 * - Usage
 * ```kotlin
 * val csvFile = "path/to/your/csvfile.csv"
 * printCsvHeaders(csvFile)
 * ```
 *
 * @param filePath Path to the CSV File
 * @return Unit
 * @throws IOException If an input/output error occurs.
 * @author kuro337
 */

fun printCsvHeaders(filePath: String, printOnNewLines: Boolean = false) {
    val logger = KotlinLogging.logger {}
    try {
        File(filePath).bufferedReader().use { reader ->
            val headerLine = reader.readLine()
            if (headerLine.isNullOrEmpty()) {
                logger.warn { "The file is empty or the header is missing." }
            } else {
                val headers = headerLine.split(",")
                if (printOnNewLines) {
                    headers.forEach { header ->
                        logger.info { header }
                    }
                } else {
                    logger.info { "CSV Headers: ${headers.joinToString(", ")}" }
                }
            }
        }
    } catch (e: Exception) {
        logger.error(e) { "An error occurred while reading the file" }
    }
}

