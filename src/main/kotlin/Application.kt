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
        val query = CustomerEventService(analyticsService)

        data.addAll(query.retrieveEvents())
    }

    fun run() {
        val plot = getDetectorCustomers().plot(
            x = CustomersAndDateByDetector::date,
            y = CustomersAndDateByDetector::customers,
            fill = CustomersAndDateByDetector::detector
        )

        plot
            .geomBar(stat = Stat.identity, showLegend = true, position = PositionStack())
            .title("# of Customers Using Detectors - May")
            .show()

        val newPlot = getDetectorCustomers().plot(
            x = CustomersAndDateByDetector::detector,
            y = CustomersAndDateByDetector::customers
        )
        newPlot
            .geomHistogram(stat = Stat.identity, showLegend = true, binWidth = 30.0)
            .coordFlip()
            .title("# of Customers Using Detectors - May")
            .show()
    }

    fun getDetectorCustomers(): Collection<CustomersAndDateByDetector> {
        val customerEventProcessor = CustomerEventProcessor()
        return customerEventProcessor.aggregateUniqueCustomerAndDate(data)
    }

    fun getDetectorHits(): Collection<HitsByDetector> {
        val customerEventProcessor = CustomerEventProcessor()
        return customerEventProcessor.aggregateCustomerHits(data)
    }
}