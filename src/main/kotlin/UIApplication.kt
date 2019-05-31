import channels.*
import javafx.scene.text.FontWeight
import tornadofx.*

class UIApplication : App(UIApplicationView::class, Styles::class) {
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

class UIApplicationView : View() {
    private val controller: PieChartController by inject()

    override val root = hbox {
        piechart("Alert customers: 1 Week") {
            controller.getDetectorCustomers().forEach {
                data("${it.channel}:${it.customers}", it.customers.toDouble())
            }
        }

        piechart("Alert events: 1 Week") {
            controller.getDetectorHits().forEach {
                data("${it.channel}:${it.hits}", it.hits.toDouble())
            }
        }
    }
}

class PieChartController : Controller() {
    private val data: MutableCollection<ChannelHitEvent> = mutableListOf()

    private var initialized = false

    private fun getChannelData(): Collection<ChannelHitEvent> {
        if (initialized) {
            return data
        }

        val analyticsService = AnalyticsService()
        val query = ChannelEventService(analyticsService);

        data.addAll(query.retreiveEvents())
        initialized = true

        return data
    }

    fun getDetectorCustomers(): Collection<CustomersByChannel> {
        val customerEventProcessor = ChannelAggregateProcessor()
        return customerEventProcessor.aggregateUniqueCustomer(getChannelData())
    }

    fun getDetectorHits(): Collection<HitsByChannel> {
        val customerEventProcessor = ChannelAggregateProcessor()
        return customerEventProcessor.aggregateCustomerHits(getChannelData())
    }
}