import analytics.AnalyticsProcessor
import analytics.AnalyticsService
import detectors.DetectorEventService
import graph.DataVisualizationService
import java.io.File


fun main() {
    Application()
}

open class PropertyNotFound(message: String) : Exception(message)

class Application {
    init {
        val keyFile = getPropertyOrThrow("keyfile")
        val output = File(getPropertyOrThrow("output"))
        output.mkdirs()

        val analyticsProcessor = AnalyticsProcessor()
        val analyticsService = AnalyticsService(keyFile, analyticsProcessor)
        val query = DetectorEventService(analyticsService)

        val data = query.retrieveEvents()

        val viz = DataVisualizationService()
        viz.createLineChart(data, File(output, "line.html"))
        viz.createPieChart(data, File(output, "pie.html"))
    }

    @Throws(PropertyNotFound::class)
    fun getPropertyOrThrow(property: String): String {
        val env = System.getenv();
        if (env.containsKey(property)) {
            return env[property]!!
        } else {
            throw PropertyNotFound("Missing required property: $property")
        }
    }
}