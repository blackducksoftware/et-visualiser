package detectors

import Dimensions
import Metrics
import analytics.*
import com.beust.klaxon.Klaxon
import java.io.StringReader

class DetectorEventService(val analyticsService: AnalyticsService) {

    fun retreiveEvents(): Collection<CustomerDetectorHitEvent> {

        var request = AnalyticsRequest(
            "2019-05-20",
            "2019-05-30",
            setOf(Dimensions.META_DATA, Dimensions.DATE, Dimensions.HOST_URL),
            setOf(Metrics.HITS)
        );
        var reports = analyticsService.executeAll(request)

        var data = AnalyticsProcessor().processReports(reports)
        println("Found rows from google: " + data.analytics.size)

        var models = AnalyticsProcessor().processStructured(data, CustomerDetectorHitAnalytic::class)
        println("Processed rows into models: " + models.size)

        var events = models.flatMap { convert(it) }
        println("Parsed metadata: " + models.size)

        return events

    }

    fun convert(analytic: CustomerDetectorHitAnalytic): List<CustomerDetectorHitEvent> {
        val metadata = parseMetaData(analytic.metadata)
        if (metadata != null){
            return metadata.detectors.map { CustomerDetectorHitEvent(it, analytic.url, analytic.date, analytic.hits.toIntOrNull() ?: 0) }
        }
        return emptyList()
    }

    fun parseMetaData(metadataJson: String): DetectorMetaData? {
        val json = Klaxon().parseJsonObject(StringReader(metadataJson))
        val detectors = mutableListOf<String>()
        val detectorTimings = mutableMapOf<String, String>()

        if (json.containsKey("bomToolTypes")){
            val allBomToolTypes:String = json.string("bomToolTypes")!!

            val bomToolTypes = allBomToolTypes.split(",")
            for (type in bomToolTypes){
                var pieces = type.split(":")
                if (pieces.size == 1){
                    detectors.add(pieces[0]);
                } else if (pieces.size == 2) {
                    detectors.add(pieces[0]);
                    detectorTimings[pieces[0]] = pieces[1]
                }
            }
        }

        return DetectorMetaData(detectors, detectorTimings)
    }
}

data class CustomerDetectorHitAnalytic(@DimensionValue(Dimensions.META_DATA) val metadata: String, @DimensionValue(Dimensions.HOST_URL) val url: String, @DimensionValue(Dimensions.DATE) val date: String, @MetricValue(Metrics.HITS) val hits: String)
data class CustomerDetectorHitEvent(val detector: String, val url: String, val date: String, val hits: Int)
data class DetectorMetaData (val detectors: List<String>, val detectorTimings: Map<String, String>)
