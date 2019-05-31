import com.beust.klaxon.Klaxon
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse

data class BomToolStat(val type: String, val count: Int)

class BomToolTypeProcessor {
    fun processResponse(structuredAnalytics: StructuredAnalytics): Collection<BomToolStat> {
        val bomToolMap = mutableMapOf<String, Int>()
        val metaDataPreProcessor = MetaDataPreProcessor()

        for (analytic in structuredAnalytics.analytics) {
            if (analytic.dimensions.containsKey(CustomDimensions.META_DATA.dimensionName)){
                val metadataPayload = analytic.dimensions.get(CustomDimensions.META_DATA.dimensionName)!!
                val metaData = metaDataPreProcessor.preprocessMetaData(metadataPayload)
                if (metaData != null){
                    for (type in metaData.detectors){
                        var metric = analytic.metrics.get("sessions")?.toInt()!!
                        if (bomToolMap.containsKey(type)){
                            bomToolMap[type] = bomToolMap[type]!! + metric
                        } else {
                            bomToolMap[type] = metric
                        }
                    }
                }
            }
        }

        return bomToolMap.entries.map { BomToolStat(it.key, it.value) }
    }
}