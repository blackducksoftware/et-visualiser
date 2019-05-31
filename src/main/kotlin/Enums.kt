enum class Dimensions(val id: String) {
    META_DATA("ga:dimension6"),
    HOST_URL("ga:dimension7"),
    DATE("ga:date")
}

enum class Metrics(val id: String, val alias: String) {
    SESSIONS("ga:sessions", "sessions"),
    HITS("ga:hits", "hits"),
    USERS("ga:users", "users")
}
