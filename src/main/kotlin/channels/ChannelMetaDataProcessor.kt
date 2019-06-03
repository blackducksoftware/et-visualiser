package channels

import Dimensions
import analytics.StructuredAnalytic
import com.beust.klaxon.Klaxon
import java.io.StringReader

class ChannelMetaDataProcessor {

    fun findMetaData(analytic: StructuredAnalytic): ChannelMetaData? {
        if (analytic.dimensions.containsKey(Dimensions.META_DATA.id)) {
            val metadataPayload = analytic.dimensions.get(Dimensions.META_DATA.id)!!
            val metadata = parseMetaData(metadataPayload);
            return metadata
        }
        return null;
    }

    fun parseMetaData(metadataJson: String): ChannelMetaData? {

        val json = Klaxon().parseJsonObject(StringReader(metadataJson))
        val channels = mutableMapOf<String, Int>()

        if (metadataJson.contains("channel.")){
            for (key in json.keys){
                if (key.startsWith("channel.")) {
                    var channel = key.substringAfter("channel.").replace("channel_", "").replace("_channel", "").replace("_group", "")
                    var channel_value = json.string(key)!!
                    channel += "-" + channel_value;
                    channels[channel] = channel_value.toInt()!!
                }
            }
        }

        return ChannelMetaData(channels)
    }
}

data class ChannelMetaData (val channels: Map<String, Int>)

