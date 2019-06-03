package channels

import Dimensions
import Metrics
import analytics.AnalyticsRequest
import analytics.AnalyticsService
import analytics.CustomerMetadataHitAnalytic
import com.beust.klaxon.Klaxon
import java.io.StringReader

class ChannelEventService(val analyticsService: AnalyticsService) {
    fun retreiveEvents(): Collection<ChannelHitEvent> {
        val request = AnalyticsRequest(
            "2019-05-24",
            "2019-05-30",
            setOf(Dimensions.META_DATA, Dimensions.DATE, Dimensions.HOST_URL),
            setOf(Metrics.HITS)
        )
        val reports = analyticsService.executeToModel(request, CustomerMetadataHitAnalytic::class)

        return reports.flatMap { convert(it) }

    }

    private fun convert(analytic: CustomerMetadataHitAnalytic): List<ChannelHitEvent> {
        val metadata = parseMetaData(analytic.metadata)
        if (metadata != null) {
            return metadata.channels.map {
                ChannelHitEvent(it.key, analytic.url, analytic.date, analytic.hits.toIntOrNull() ?: 0)
            }
        }
        return emptyList()
    }

    private fun parseMetaData(metadataJson: String): ChannelMetaData? {
        val json = Klaxon().parseJsonObject(StringReader(metadataJson))
        val channels = mutableMapOf<String, Int>()

        if (metadataJson.contains("channel.")) {
            for (key in json.keys) {
                if (key.startsWith("channel.")) {
                    var channel = key.substringAfter("channel.").replace("channel_", "").replace("_channel", "").replace("_group", "")
                    val channelValue = json.string(key)!!
                    channel += "-$channelValue";
                    channels[channel] = channelValue.toInt()
                }
            }
        }

        return ChannelMetaData(channels)
    }

}

data class ChannelMetaData(val channels: Map<String, Int>)
data class ChannelHitEvent(val channel: String, val url: String, val date: String, val hits: Int)
