package detectors

import analytics.AnalyticsService
import analytics.CustomerMetadataHitAnalytic
import com.beust.klaxon.Klaxon
import java.io.StringReader

class DetectorEventConverter(val analyticsService: AnalyticsService) {

    fun convert(analytic: CustomerMetadataHitAnalytic): List<CustomerDetectorHitEvent> {
        val metadata = parseMetaData(analytic.metadata)
        if (metadata != null) {
            return metadata.detectors.filter { it.isNotBlank() }.map { it.trim() }.map {
                CustomerDetectorHitEvent(it, analytic.url, analytic.date, analytic.hits.toIntOrNull() ?: 0)
            }
        }
        return emptyList()
    }

    private fun parseMetaData(metadataJson: String): DetectorMetaData? {
        val json = Klaxon().parseJsonObject(StringReader(metadataJson))
        val detectors = mutableListOf<String>()
        val detectorTimings = mutableMapOf<String, String>()

        if (json.containsKey("bomToolTypes")) {
            val allBomToolTypes: String = json.string("bomToolTypes")!!

            val bomToolTypes = allBomToolTypes.split(",")
            for (type in bomToolTypes) {
                val pieces = type.split(":")
                if (pieces.size == 1) {
                    detectors.add(pieces[0])
                } else if (pieces.size == 2) {
                    detectors.add(pieces[0])
                    detectorTimings[pieces[0]] = pieces[1]
                }
            }
        }

        return DetectorMetaData(detectors, detectorTimings)
    }
}

data class CustomerDetectorHitEvent(val detector: String, val url: String, val date: String, val hits: Int)
data class DetectorMetaData(val detectors: List<String>, val detectorTimings: Map<String, String>)
