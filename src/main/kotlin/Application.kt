import javafx.scene.text.FontWeight
import tornadofx.*

class HelloWorldApp : App(HelloWorld::class, Styles::class) {
    init {
        val analyticsService = AnalyticsService()

        try {
            val analyticsReporting = analyticsService.initializeAnalyticsReporting()

            val response = analyticsService.getReport(analyticsReporting)
            analyticsService.printResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
    override val root = hbox {
        label("Hello world")
        piechart("How Eric Spends His Time") {
            data("Eating Chicken", 120.0)
            data("Complaining", 6.5)
            data("Insulting Others", 4.5)
            data("Refusing Vaccinations", 6.0)
            data("Tired", 15.0)
            data("Threatening Others", 30.0)
        }
    }
}