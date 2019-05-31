import javafx.scene.text.FontWeight
import tornadofx.*

class HelloWorldApp : App(HelloWorld::class, Styles::class) {
    init {

    }
}


class Styles : Stylesheet() {
    init {
        label {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            backgroundColor += c("#cecece")
        }
    }
}

class HelloWorld : View() {
    private val controller: PieChartController by inject()

    override val root = hbox {
        piechart("Detector Usage: 1 Week") {
            controller.getData().forEach {
                data("${it.type}:${it.count}", it.count.toDouble())
            }
        }
    }
}

class PieChartController : Controller() {
    private val data: MutableCollection<BomToolStat> = mutableListOf()

    init {
        val metadataProcessor = BomToolTypeProcessor()
        val analyticsService = AnalyticsService()
        val analyticsProcessor = AnalyticsProcessor()

        val analyticsReporting = analyticsService.initializeAnalyticsReporting()
        val response = analyticsService.getReport(analyticsReporting)
        val structuredAnalytics = analyticsProcessor.processResponse(response)

        val processedResponse = metadataProcessor.processResponse(structuredAnalytics)

        data.addAll(processedResponse)
    }

    fun getData(): Collection<BomToolStat> {
        return data
    }
}