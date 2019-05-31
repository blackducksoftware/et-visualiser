import com.beust.klaxon.Klaxon
import java.io.StringReader

class MetaDataPreProcessor {
    fun preprocessMetaData(metadataJson: String): MetaData? {
        val json = Klaxon().parseJsonObject(StringReader(metadataJson))
        val detectors = mutableListOf<String>()
        val detectorTimings = mutableMapOf<String, String>()

        fun addDetector(detector: String, detectorTiming: String = "") {
            detectors.add(detector)
            if (detectorTiming.isNotBlank()) {
                detectorTimings[detector] = detectorTiming
            }
        }

        if (json.containsKey("bomToolTypes")) {
            val allBomToolTypes: String = json.string("bomToolTypes")!!

            val bomToolTypes = allBomToolTypes.split(",")
            for (type in bomToolTypes) {
                val pieces = type.split(":")
                if (pieces.size == 1) {
                    addDetector(pieces[0])
                } else if (pieces.size == 2) {
                    addDetector(pieces[0], pieces[1])
                }
            }
        }

        return MetaData(detectors, detectorTimings)
    }
}

data class MetaData(val detectors: List<String>, val detectorTimings: Map<String, String>)

