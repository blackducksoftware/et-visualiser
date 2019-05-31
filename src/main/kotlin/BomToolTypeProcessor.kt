import com.beust.klaxon.Klaxon
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse

data class BomToolStat(val type: String, val count: Int)


data class Dimension(val bomToolTypes: String)

class BomToolTypeProcessor {
    fun processResponse(structuredAnalytics: StructuredAnalytics): Collection<BomToolStat> {
        val bomToolMap = mutableMapOf<String, Int>()

        for (analytic in structuredAnalytics.analytics) {
            if (analytic.dimensions.containsKey(CustomDimensions.META_DATA.dimensionName)){
                val metadataPayload = analytic.dimensions.get(CustomDimensions.META_DATA.dimensionName);
                if (metadataPayload != null && metadataPayload.contains("bomToolTypes")) {
                    val dimension = Klaxon().parse<Dimension>(metadataPayload)
                    dimension?.bomToolTypes?.split(",")?.forEach {
                        var count = analytic.metrics.get("sessions")?.toInt() ?: 0;

                        var key = it.trim()
                        if (key.contains(":")) {
                            // Earlier versions of Detect would submit the count in a different format
                            key = key.split(":")[0]
                        }
                        key = key.trim()

                        if (key.contentEquals("")) {
                            key = "None"
                        }


                        val value = bomToolMap[key]
                        if (value != null) {
                            count += value
                        }
                        bomToolMap[key] = count
                    }
                }
            }
        }

        return bomToolMap.entries.map { BomToolStat(it.key, it.value) }
    }
}