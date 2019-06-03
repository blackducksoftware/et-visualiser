import detectors.*
import kravis.*
import kravis.render.Docker


fun main() {
    SessionPrefs.RENDER_BACKEND = Docker()
    SessionPrefs.OUTPUT_DEVICE = kravis.device.JupyterDevice()
    Application().run()
}

class Application {
    private val data: MutableCollection<CustomerDetectorHitEvent> = mutableListOf()

    init {
        val analyticsService = AnalyticsService()
        val query = DetectorEventService(analyticsService)

        data.addAll(query.retreiveEvents())
    }

    fun run() {
        val colorMap = mutableMapOf(
            Pair("MAVEN", RColor.black)
        )

        val plot = getDetectorCustomers().plot(
            x = CustomersAndDateByDetector::date,
            y = CustomersAndDateByDetector::customers,
            fill = CustomersAndDateByDetector::detector
        )

        plot
            .geomBar(stat = Stat.identity, showLegend = true, position = PositionStack(), color = RColor.black)
            .themeDark()
            .scaleColorManual(values = colorMap)
            .title("# of Customers Using Detectors - May")
            .show()

        val newPlot = getDetectorCustomers().plot(
            x = CustomersAndDateByDetector::detector,
            y = CustomersAndDateByDetector::customers
        )
        newPlot
            .geomHistogram(stat = Stat.identity, showLegend = true)
            .title("# of Customers Using Detectors - May")
            .coordFlip()
            .show()
    }

    fun getDetectorCustomers(): Collection<CustomersAndDateByDetector> {
        val customerEventProcessor = DetectorAggregateProcessor()
        return customerEventProcessor.aggregateUniqueCustomerAndDate(data)
    }

    fun getDetectorHits(): Collection<HitsByDetector> {
        val customerEventProcessor = DetectorAggregateProcessor()
        return customerEventProcessor.aggregateCustomerHits(data)
    }
}