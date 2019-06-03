package analytics

import Dimensions
import Metrics
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
import kotlin.reflect.KClass


class AnalyticsService // Construct the Analytics Reporting service object.
@Throws(GeneralSecurityException::class, IOException::class)
constructor(keyFile: String, analyticsProcessor: AnalyticsProcessor) {
    private val VIEW_ID = "172593710"
    private val APPLICATION_NAME = "ET-Visualizer"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    private val analyticsReporting: AnalyticsReporting
    private val analyticsProcessor: AnalyticsProcessor

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = GoogleCredential
            .fromStream(FileInputStream(keyFile))
            .createScoped(AnalyticsReportingScopes.all())

        this.analyticsReporting = AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME).build()
        this.analyticsProcessor = analyticsProcessor
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
            .setFiltersExpression(defaultFilterExpression())

        if (token != null) {
            reportRequest.pageToken = token
        }

        val report = GetReportsRequest()
            .setReportRequests(listOf(reportRequest))

        return analyticsReporting.reports().batchGet(report).execute()
    }

    private fun defaultFilterExpression(): String {
        val includeCustomersPattern = "([a-zA-Z0-9]*(_hub){1}.*(_){1}[a-zA-Z0-9]{15}|^<unkown>\$|^<unknown>\$)"
        val excludeHostsPattern = "(?i)^.*(hubeval|internal|localhost|unknown|blackducksoftware.co.kr|dc1.lan).*"
        val excludeCustomersPattern = "(?i)^.*(pandersson|synopsys|test).*"

        val includeCustomersFilter = "${Dimensions.CUSTOMER_ID.id}=~$includeCustomersPattern"
        val excludeCustomersFilter = "${Dimensions.CUSTOMER_ID.id}!~$excludeCustomersPattern"
        val excludeHostsFilter = "${Dimensions.HOST_URL.id}!~$excludeHostsPattern"

        return "$includeCustomersFilter;$excludeCustomersFilter;$excludeHostsFilter"
    }

    // MUST be a single report
    fun executeAll(request: AnalyticsRequest): List<Report> {
        var currentResponse = executeRequest(request)
        val reports = mutableListOf(currentResponse.reports[0])

        var pageCount = 1
        while (currentResponse.reports[0].nextPageToken != null) {
            println("Getting page: $pageCount")
            currentResponse = executeRequest(request, currentResponse.reports[0].nextPageToken)
            reports.add(currentResponse.reports[0])
            pageCount++
        }

        return reports
    }

    fun <T : Any> executeToModel(request: AnalyticsRequest, targetClass: KClass<T>): Collection<T> {
        val response = executeAll(request)
        val reports = analyticsProcessor.processReports(response)
        return analyticsProcessor.processStructured(reports, targetClass)
    }
}

data class AnalyticsRequest(val from: String, val to: String, val dimensions: Set<Dimensions>, val metrics: Set<Metrics>)