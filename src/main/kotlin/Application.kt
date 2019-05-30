fun main(args: Array<String>) {
    val analyticsService = AnalyticsService()

    try {
        val analyticsReporting = analyticsService.initializeAnalyticsReporting()

        val response = analyticsService.getReport(analyticsReporting)
        analyticsService.printResponse(response)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}