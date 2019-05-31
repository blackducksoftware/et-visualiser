class CustomerEventService(private val analyticsService: AnalyticsService) {

    fun retrieveEvents(): Collection<CustomerDetectorHitEvent> {

        val request = AnalyticsRequest("7DaysAgo", "today", setOf(Dimensions.META_DATA, Dimensions.HOST_URL, Dimensions.DATE), setOf(Metrics.HITS));
        val response = analyticsService.executeRequest(request)

        val data = AnalyticsProcessor().processResponse(response)
        return parseEvents(data)

    }

    private fun parseEvents(structuredAnalytics: StructuredAnalytics): Collection<CustomerDetectorHitEvent> {
        val events = mutableListOf<CustomerDetectorHitEvent>()
        val metaDataPreProcessor = MetaDataPreProcessor()

        fun emit(detector: String, url: String, date: String, hits: Int) {
            events.add(CustomerDetectorHitEvent(detector, url, date, hits))
        }

        for (analytic in structuredAnalytics.analytics) {
            val metaData = metaDataPreProcessor.findMetaData(analytic)
            if (metaData != null) {
                for (type in metaData.detectors) {
                    val url = analytic.dimensions.getOrDefault(Dimensions.HOST_URL.id, "Unknown")
                    val date = analytic.dimensions.getOrDefault(Dimensions.DATE.id, "Unknown")
                    val hitStr = analytic.metrics.getOrDefault(Metrics.HITS.alias, "0")
                    val hits = hitStr.toIntOrNull() ?: 0
                    emit(detector = type, url = url, date = date, hits = hits)
                }
            }
        }

        return events
    }

}


data class CustomerDetectorHitEvent(val detector: String, val url: String, val date: String, val hits: Int)
