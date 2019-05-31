import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes
import com.google.api.services.analyticsreporting.v4.model.*
import com.google.api.services.analyticsreporting.v4.model.Dimension
import java.io.FileInputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.*


class AnalyticsService {
    private val KEY_FILE_LOCATION = System.getenv("keyfile")
    private val VIEW_ID = "172593710"
    private val APPLICATION_NAME = "ET-Visualizer"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    /**
     * Initializes an Analytics Reporting API V4 service object.
     *
     * @return An authorized Analytics Reporting API V4 service object.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun initializeAnalyticsReporting(): AnalyticsReporting {

        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = GoogleCredential
            .fromStream(FileInputStream(KEY_FILE_LOCATION))
            .createScoped(AnalyticsReportingScopes.all())

        // Construct the Analytics Reporting service object.
        return AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME).build()
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @param service An authorized Analytics Reporting API V4 service object.
     * @return GetReportResponse The Analytics Reporting API V4 response.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getReport(service: AnalyticsReporting): GetReportsResponse {
        // Create the DateRange object.
        val dateRange = DateRange()
        dateRange.startDate = "7DaysAgo"
        dateRange.endDate = "today"

        // Create the Metrics object.
        val sessions = Metric()
            .setExpression("ga:sessions")
            .setAlias("sessions")

        val pageTitle = Dimension().setName("ga:dimension6")

        // Create the ReportRequest object.
        val request = ReportRequest()
            .setViewId(VIEW_ID)
            .setDateRanges(Arrays.asList(dateRange))
            .setMetrics(Arrays.asList<Metric>(sessions))
            .setDimensions(Arrays.asList<Dimension>(pageTitle))

        val requests = ArrayList<ReportRequest>()
        requests.add(request)

        // Create the GetReportsRequest object.
        val getReport = GetReportsRequest()
            .setReportRequests(requests)

        // Call the batchGet method.

        // Return the response.
        return service.reports().batchGet(getReport).execute()
    }

    /**
     * Parses and prints the Analytics Reporting API V4 response.
     *
     * @param response An Analytics Reporting API V4 response.
     */
    fun printResponse(response: GetReportsResponse) {

        for (report in response.reports) {
            val header = report.columnHeader
            val dimensionHeaders = header.dimensions
            val metricHeaders = header.metricHeader.metricHeaderEntries
            val rows = report.data.rows

            if (rows == null) {
                println("No data found for $VIEW_ID")
                return
            }

            for (row in rows) {
                val dimensions = row.dimensions
                val metrics = row.metrics

                var i = 0
                while (i < dimensionHeaders.size && i < dimensions.size) {
                    println(dimensionHeaders[i] + ": " + dimensions[i])
                    i++
                }

                for (j in metrics.indices) {
                    print("Date Range ($j): ")
                    val values = metrics[j]
                    var k = 0
                    while (k < values.getValues().size && k < metricHeaders.size) {
                        println(metricHeaders[k].name + ": " + values.getValues()[k])
                        k++
                    }
                }
            }
        }
    }
}