package detectors

import AnalyticsProcessor
import AnalyticsRequest
import AnalyticsService
import Dimensions
import Metrics
import StructuredAnalytics

class DetectorEventService(val analyticsService: AnalyticsService) {

    fun retreiveEvents(): Collection<CustomerDetectorHitEvent> {

        var request = AnalyticsRequest(
            "2019-05-24",
            "2019-05-30",
            setOf(Dimensions.META_DATA, Dimensions.DATE, Dimensions.HOST_URL),
            setOf(Metrics.HITS)
        );
        var reports = analyticsService.executeAll(request)

        var data = AnalyticsProcessor().processReports(reports)
        println("Found rows from google: " + data.analytics.size)

        return parseEvents(data)

    }

    private fun parseEvents(structuredAnalytics: StructuredAnalytics): Collection<CustomerDetectorHitEvent> {
        val events = mutableListOf<CustomerDetectorHitEvent>()
        val metaDataPreProcessor = DetectorMetaDataProcessor()

        fun emit(detector: String, url: String, date: String, hits: Int) {
            events.add(CustomerDetectorHitEvent(detector, url, date, hits))
        }

        for (analytic in structuredAnalytics.analytics) {
            val metaData = metaDataPreProcessor.findMetaData(analytic)
            if (metaData != null){
                for (type in metaData.detectors){
                    val url = analytic.dimensions.getOrDefault(Dimensions.HOST_URL.id, "Unknown")
                    val date = analytic.dimensions.getOrDefault(Dimensions.DATE.id, "Unknown")
                    val hitStr = analytic.metrics.getOrDefault(Metrics.HITS.alias, "0")
                    val hits = hitStr.toIntOrNull() ?: 0
                    emit(detector = type, url = url, date = date, hits = hits)
                }
            }
        }

        return events
    }

}


data class CustomerDetectorHitEvent(val detector: String, val url: String, val date: String, val hits: Int)
