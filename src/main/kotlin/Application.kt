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
        piechart("Detector customers: 1 Week") {
            controller.getDetectorCustomers().forEach {
                if (it == null) {
                    return@forEach
                }
                data("${it.detector}:${it.customers}", it.customers.toDouble())
            }
        }

        piechart("Detector events: 1 Week") {
            controller.getDetectorHits().forEach {
                if (it == null) {
                    return@forEach
                }
                data("${it.detector}:${it.hits}", it.hits.toDouble())
            }
        }
    }
}

class PieChartController : Controller() {
    private val data: MutableCollection<CustomerDetectorHitEvent> = mutableListOf()

    private var initialized = false

    fun getData(): Collection<CustomerDetectorHitEvent> {
        if (initialized) {
            return data
        }

        val analyticsService = AnalyticsService()
        val query = CustomerEventService(analyticsService);

        data.addAll(query.retreiveEvents())
        initialized = true

        return data
    }

    fun getDetectorCustomers(): Collection<CustomersByDetector> {
        val customerEventProcessor = CustomerEventProcessor()
        return customerEventProcessor.aggregateUniqueCustomer(getData())
    }

    fun getDetectorHits(): Collection<HitsByDetector> {
        val customerEventProcessor = CustomerEventProcessor()
        return customerEventProcessor.aggregateCustomerHits(getData())
    }
}