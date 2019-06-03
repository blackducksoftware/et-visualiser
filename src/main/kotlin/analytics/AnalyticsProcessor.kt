package analytics

import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Report
import kotlin.reflect.KClass
import Dimensions
import Metrics
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

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

    fun <T:Any> processStructured(analytics: StructuredAnalytics, targetClass: KClass<T>) : Collection<T> {
        return analytics.analytics.map { processStructured(it, targetClass) }
    }

    fun <T:Any> processStructured(analytic: StructuredAnalytic, targetClass: KClass<T>) : T {
        val params = mutableMapOf<KParameter, Any>()
        val c = targetClass.constructors.first();
        c.parameters.map {
            run {
                val dimensionAnnotation = it.findAnnotation<DimensionValue>()
                if (dimensionAnnotation != null) {
                    val dimension = dimensionAnnotation.dimension
                    params[it] = analytic.dimensions.getOrDefault(dimension.id, "Unknown")
                }
            }
            run {
                val metricAnnotation = it.findAnnotation<MetricValue>()
                if (metricAnnotation != null) {
                    val metric = metricAnnotation.metric
                    params[it] = analytic.metrics.getOrDefault(metric.id, "Unknown")
                }
            }
        }
        return c.callBy(params)
    }
}

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DimensionValue(val dimension: Dimensions)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MetricValue(val metric: Metrics)

data class StructuredAnalytics (var analytics: List<StructuredAnalytic> )
data class StructuredAnalytic (var dimensions: Map<String, String>, var metrics: Map<String, String>)
