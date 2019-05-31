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

data class EricActivity(val name: String, val value: Double)

class HelloWorld : View() {
    private val controller: PieChartController by inject()

    override val root = hbox {
        piechart("Detector usage: 1 Week") {
            controller.getData().forEach {
                if (it == null) {
                    return@forEach
                }
                data("${it.type}:${it.count}", it.count.toDouble())
            }
        }
    }
}

class PieChartController : Controller() {
    private val data: MutableCollection<BomToolStat?> = mutableListOf()

    private var initialized = false

    fun getData(): Collection<BomToolStat?> {
        if (initialized) {
            return data
        }

        val metadataProcessor = BomToolTypeProcessor()
        val analyticsService = AnalyticsService()

        val analyticsReporting = analyticsService.initializeAnalyticsReporting()
        val response = analyticsService.getReport(analyticsReporting)
        analyticsService.printResponse(response)
        val processedResponse = metadataProcessor.processResponse(response)

        data.addAll(processedResponse)
        initialized = true

        return data
    }
}