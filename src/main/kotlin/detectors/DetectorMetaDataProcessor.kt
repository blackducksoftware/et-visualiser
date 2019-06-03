package detectors

import Dimensions
import analytics.StructuredAnalytic
import com.beust.klaxon.Klaxon
import java.io.StringReader

class DetectorMetaDataProcessor {

    fun findMetaData(analytic: StructuredAnalytic): DetectorMetaData? {
        if (analytic.dimensions.containsKey(Dimensions.META_DATA.id)) {
            val metadataPayload = analytic.dimensions.get(Dimensions.META_DATA.id)!!
            val metadata = parseMetaData(metadataPayload);
            return metadata
        }
        return null;
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

data class DetectorMetaData (val detectors: List<String>, val detectorTimings: Map<String, String>)

