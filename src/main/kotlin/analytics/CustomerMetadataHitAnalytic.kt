package analytics

data class CustomerMetadataHitAnalytic(@DimensionValue(Dimensions.META_DATA) val metadata: String, @DimensionValue(Dimensions.HOST_URL) val url: String, @DimensionValue(Dimensions.DATE) val date: String, @MetricValue(Metrics.HITS) val hits: String)