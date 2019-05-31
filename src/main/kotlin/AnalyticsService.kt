import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes
import com.google.api.services.analyticsreporting.v4.model.*
import java.io.FileInputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.*


class AnalyticsService// Construct the Analytics Reporting service object.
@Throws(GeneralSecurityException::class, IOException::class) constructor() {
    private val KEY_FILE_LOCATION = System.getenv("keyfile")
    private val VIEW_ID = "172593710"
    private val APPLICATION_NAME = "ET-Visualizer"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    private val analyticsReporting: AnalyticsReporting;

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = GoogleCredential
            .fromStream(FileInputStream(KEY_FILE_LOCATION))
            .createScoped(AnalyticsReportingScopes.all())

        analyticsReporting = AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME).build()
    }

    @Throws(IOException::class)
    fun executeRequest(request: AnalyticsRequest, token: String? = null): GetReportsResponse {
        val dateRange = DateRange()
        dateRange.startDate = request.from
        dateRange.endDate = request.to

        val metrics = mutableListOf<Metric>();
        for (metric in request.metrics) {
            val gaMetric = Metric()
                .setExpression(metric.id)
                .setAlias(metric.alias)
            metrics.add(gaMetric)
        }

        val dimensions = mutableListOf<Dimension>();
        for (dimension in request.dimensions) {
            val gaDimension = Dimension().setName(dimension.id)
            dimensions.add(gaDimension)
        }

        val reportRequest = ReportRequest()
            .setViewId(VIEW_ID)
            .setDateRanges(Arrays.asList(dateRange))
            .setMetrics(metrics)
            .setDimensions(dimensions)
            .setPageSize(10000)

        if (token != null) {
            reportRequest.pageToken = token
        }

        val report = GetReportsRequest()
            .setReportRequests(listOf(reportRequest))

        return analyticsReporting.reports().batchGet(report).execute()
    }

    //MUST be a single report
    fun executeAll(request: AnalyticsRequest): List<Report> {
        var currentResponse = executeRequest(request)
        var reports = mutableListOf(currentResponse.reports[0])

        var pageCount = 1
        while (currentResponse.reports[0].nextPageToken != null) {
            println("Getting page: $pageCount")
            currentResponse = executeRequest(request, currentResponse.reports[0].nextPageToken)
            reports.add(currentResponse.reports[0])
            pageCount++
        }

        return reports
    }
}

data class AnalyticsRequest(val from: String, val to: String, val dimensions: Set<Dimensions>, val metrics: Set<Metrics>) {

}