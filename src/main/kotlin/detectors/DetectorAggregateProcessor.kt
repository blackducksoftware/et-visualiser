package detectors

class DetectorAggregateProcessor() {

    fun aggregateCustomerHits(events: Collection<CustomerDetectorHitEvent>): Collection<HitsByDetector> {
        val aggregate = mutableMapOf<String, Int>()

        for (event in events) {
            if (event.detector == "") continue;

            if (!aggregate.containsKey(event.detector)){
                aggregate[event.detector] = 0
            }
            aggregate[event.detector] = aggregate[event.detector]!! + event.hits
        }

        val result = mutableListOf<HitsByDetector>()
        for (pair in aggregate){
            result.add(HitsByDetector(pair.key, pair.value))
        }
        return result;
    }

    fun aggregateUniqueCustomer(events: Collection<CustomerDetectorHitEvent>): Collection<CustomersByDetector> {
        val aggregate = mutableMapOf<String, MutableSet<String>>()

        for (event in events) {
            if (event.detector == "") continue;

            if (!aggregate.containsKey(event.detector)){
                aggregate[event.detector] = mutableSetOf()
            }
            aggregate[event.detector]!!.add(event.url)
        }

        val result = mutableListOf<CustomersByDetector>()
        for (pair in aggregate){
            result.add(CustomersByDetector(pair.key, pair.value.size))
        }
        return result;
    }

    fun aggregateUniqueCustomerAndDate(events: Collection<CustomerDetectorHitEvent>): Collection<CustomersAndDateByDetector> {
        val aggregate = mutableMapOf<DetectorDate, MutableSet<String>>()

        for (event in events) {
            if (event.detector == "") continue;

            val detectordate = DetectorDate(event.detector, event.date)
            if (!aggregate.containsKey(detectordate)){
                aggregate[detectordate] = mutableSetOf()
            }
            aggregate[detectordate]!!.add(event.url)
        }

        val result = mutableListOf<CustomersAndDateByDetector>()
        for (pair in aggregate){
            result.add(CustomersAndDateByDetector(pair.key.detector, pair.value.size, pair.key.date))
        }
        return result;
    }
}

data class CustomersAndDateByDetector (val detector: String, val customers: Int, val date: String)

data class CustomersByDetector (val detector: String, val customers: Int)

data class HitsByDetector (val detector: String, val hits: Int)

data class DetectorDate (val detector: String, val date: String)
