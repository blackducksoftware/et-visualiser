import analytics.*
import channels.ChannelEventConverter
import detectors.DetectorEventConverter
import graph.*
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
        val startDate = getPropertyOrThrow("startDate")
        val endDate = getPropertyOrThrow("endDate")

        val analyticsProcessor = AnalyticsProcessor()
        val analyticsService = AnalyticsService(keyFile, analyticsProcessor)

        val request = AnalyticsRequest(
            startDate,
            endDate,
            setOf(Dimensions.META_DATA, Dimensions.DATE, Dimensions.HOST_URL),
            setOf(Metrics.HITS)
        )

        val reports = analyticsService.executeToModel(request, CustomerMetadataHitAnalytic::class)
        val viz = DataVisualizationService()
        val aggregator = CustomerAggregator()

        run {
            val detectorConverter = DetectorEventConverter(analyticsService)
            val detectorEvents = reports.flatMap { detectorConverter.convert(it) }

            val customersByDate = aggregator.aggregateByDate(detectorEvents, { event -> event.url }, { event -> event.date }, { event -> event.detector })
            val timeGraph = CustomersOverDateGraph(title = "Detectors By Customer", groupLabel = "Detectors", rows = customersByDate.map { CustomersOverDateRow(it.customers, it.value, it.date) })
            viz.customersOverDateLineGraph(timeGraph, File(output, "detector-customers-over-time.html"))

            val customerUsage = aggregator.aggregateUsage(detectorEvents, { event -> event.url }, { event -> event.detector })
            val usageGraph = CustomerUsageGraph(title = "Detectors By Customer", groupLabel = "Detectors", rows = customerUsage.map { CustomerUsageRow(it.customers, it.value) })
            viz.customerUsageGraph(usageGraph, File(output, "detector-customers-usage.html"))
        }

        run {
            val channelConverter = ChannelEventConverter(analyticsService)
            val channelEvents = reports.flatMap { channelConverter.convert(it) }

            val customersByDate = aggregator.aggregateByDate(channelEvents, { event -> event.url }, { event -> event.date }, { event -> event.channel })
            val timeGraph = CustomersOverDateGraph(title = "Channels By Customer", groupLabel = "Channels", rows = customersByDate.map { CustomersOverDateRow(it.customers, it.value, it.date) })
            viz.customersOverDateLineGraph(timeGraph, File(output, "channel-customers-over-time.html"))

            val customersUsage = aggregator.aggregateUsage(channelEvents, { event -> event.url }, { event -> event.channel })
            val usageGraph = CustomerUsageGraph(title = "Channels By Customer", groupLabel = "Channels", rows = customersUsage.map { CustomerUsageRow(it.customers, it.value) })
            viz.customerUsageGraph(usageGraph, File(output, "channel-customers-usage.html"))
        }
    }

    @Throws(PropertyNotFound::class)
    fun getPropertyOrThrow(property: String): String {
        val env = System.getenv()
        if (env.containsKey(property)) {
            return env[property]!!
        } else {
            throw PropertyNotFound("Missing required property: $property")
        }
    }
}