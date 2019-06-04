package graph

import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Page
import tech.tablesaw.plotly.display.Browser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Displays plots in a development setting, by exporting a file containing the HTML and Javascript, and then opening
 * the file in the default browser on the developer's machine.
 */
object Plot {
    private const val DEFAULT_DIV_NAME = "target"
    private const val DEFAULT_OUTPUT_FILE = "output.html"
    private const val DEFAULT_OUTPUT_FILE_NAME = "output"
    private const val DEFAULT_OUTPUT_FOLDER = "testoutput"

    fun generate(figure: Figure, outputFile: File = defaultFile(), show: Boolean = false, divName: String = DEFAULT_DIV_NAME) {
        val page = Page.pageBuilder(figure, divName).build()
        val output = page.asJavascript()

        try {
            OutputStreamWriter(FileOutputStream(outputFile), StandardCharsets.UTF_8).use { writer -> writer.write(output) }
            if (show) {
                Browser().browse(outputFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun defaultFile(): File {
        val path = Paths.get(DEFAULT_OUTPUT_FOLDER, DEFAULT_OUTPUT_FILE)
        try {
            Files.createDirectories(path.parent)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return path.toFile()
    }

    private fun randomFile(): File {
        val path = Paths.get(DEFAULT_OUTPUT_FOLDER, randomizedFileName())
        try {
            Files.createDirectories(path.parent)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return path.toFile()
    }

    private fun randomizedFileName(): String {
        return DEFAULT_OUTPUT_FILE_NAME + UUID.randomUUID().toString() + ".html"
    }
}
