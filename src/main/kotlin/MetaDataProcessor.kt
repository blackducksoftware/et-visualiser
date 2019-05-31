import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.lookup
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import java.io.StringReader

class MetaDataPreProcessor {

    fun findMetaData(analytic: StructuredAnalytic): MetaData? {
        if (analytic.dimensions.containsKey(Dimensions.META_DATA.id)) {
            val metadataPayload = analytic.dimensions.get(Dimensions.META_DATA.id)!!
            return parseMetaData(metadataPayload)
        }
        return null;
    }

    fun parseMetaData(metadataJson: String): MetaData? {

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

        return MetaData(detectors, detectorTimings)
    }
}

data class MetaData (val detectors: List<String>, val detectorTimings: Map<String, String>)

