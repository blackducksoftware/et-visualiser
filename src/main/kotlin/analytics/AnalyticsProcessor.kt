package analytics

import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Report

class AnalyticsProcessor {

    fun processResponse(response: GetReportsResponse): StructuredAnalytics {
        return processReports(response.reports)
    }

    //Converts a given response to a analytics.StructuredAnalytics object that gives a map of dimensions and metrics.
    fun processReports(reports: List<Report>): StructuredAnalytics {

        val analytics = emptyList<StructuredAnalytic>().toMutableList()

        for (report in reports) {
            val header = report.columnHeader
            val dimensionHeaders = header.dimensions
            val metricHeaders = header.metricHeader.metricHeaderEntries
            val rows = report.data.rows

            if (rows == null) {
                continue
            }

            for (row in rows) {
                val dimensions = row.dimensions
                val metrics = row.metrics

                var dimensionMap = mutableMapOf<String, String>();
                var i = 0
                while (i < dimensionHeaders.size && i < dimensions.size) {

                    dimensionMap[dimensionHeaders[i]] = dimensions[i];
                    i++
                }

                var metricMap = mutableMapOf<String, String>();
                for (j in metrics.indices) {
                    val values = metrics[j]
                    var k = 0
                    while (k < values.getValues().size && k < metricHeaders.size) {
                        metricMap[metricHeaders[k].name] = values.getValues()[k];
                        k++
                    }
                }
                analytics.add(StructuredAnalytic(dimensionMap, metricMap));
            }
        }

        return StructuredAnalytics(analytics)
    }
}

data class StructuredAnalytics (var analytics: List<StructuredAnalytic> )
data class StructuredAnalytic (var dimensions: Map<String, String>, var metrics: Map<String, String>)
