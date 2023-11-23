package eventstream.beam.helpers

import org.apache.beam.sdk.io.FileSystems


/**
 * #### Helper for Checking File Existence - `fileExists(path:String)`
 * - Example Usage
 * ```kotlin
 *   val existingFiles = inputFiles.filter { fileExists(it) }
 *     if (existingFiles.isEmpty()) {
 *         // do something
 *     }
 * ```
 */
fun fileExists(filePath: String): Boolean {
    return try {
        val matchResults = FileSystems.match(listOf(filePath))
        matchResults.isNotEmpty() && matchResults.first().metadata().isNotEmpty()
    } catch (e: Exception) {
        false
    }
}
