import com.beust.klaxon.Klaxon
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse

data class BomToolStat(val type: String, val count: Int)


data class Dimension(val bomToolTypes: String)

class BomToolTypeProcessor {
    fun processResponse(response: GetReportsResponse): Collection<BomToolStat> {
        val bomToolMap = mutableMapOf<String, Int>()

        for (report in response.reports) {
            val rows = report.data.rows

            if (rows == null) {
                println("No data found")
                break
            }

            for (row in rows) {
                val dimensions = row.dimensions
                val metrics = row.metrics

                var i = 0
                while (i < metrics.size && i < dimensions.size) {
                    val json = dimensions[i]
                    if (json.contains("bomToolTypes")) {
                        val sessionCount = metrics[i].getValues()[0].replace("session:", "").trim().toInt()
                        val dimension = Klaxon().parse<Dimension>(json)
                        dimension?.bomToolTypes?.split(",")?.forEach {
                            var count = sessionCount

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
                    i++
                }
            }
        }

        return bomToolMap.entries.map { BomToolStat(it.key, it.value) }
    }
}