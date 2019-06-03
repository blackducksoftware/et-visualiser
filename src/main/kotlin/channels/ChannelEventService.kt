package channels

import analytics.AnalyticsProcessor
import analytics.AnalyticsRequest
import analytics.AnalyticsService
import Dimensions
import Metrics
import analytics.StructuredAnalytics

class ChannelEventService(val analyticsService: AnalyticsService) {

    fun retreiveEvents(): Collection<ChannelHitEvent> {

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

    private fun parseEvents(structuredAnalytics: StructuredAnalytics): Collection<ChannelHitEvent> {
        val events = mutableListOf<ChannelHitEvent>()
        val metaDataPreProcessor = ChannelMetaDataProcessor()

        fun emit(channel: String, url: String, date: String, hits: Int) {
            events.add(ChannelHitEvent(channel, url, date, hits))
        }

        for (analytic in structuredAnalytics.analytics) {
            val metaData = metaDataPreProcessor.findMetaData(analytic)
            if (metaData != null){
                for (pair in metaData.channels){
                    val url = analytic.dimensions.getOrDefault(Dimensions.HOST_URL.id, "Unknown")
                    val date = analytic.dimensions.getOrDefault(Dimensions.DATE.id, "Unknown")
                    val hitStr = analytic.metrics.getOrDefault(Metrics.HITS.alias, "0")
                    val hits = hitStr.toIntOrNull() ?: 0
                    emit(channel = pair.key, url = url, date = date, hits = hits)
                }
            }
        }

        return events
    }

}


data class ChannelHitEvent(val channel: String, val url: String, val date: String, val hits: Int)
