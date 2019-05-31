package channels

class ChannelAggregateProcessor() {

    fun aggregateCustomerHits(events: Collection<ChannelHitEvent>): Collection<HitsByChannel> {
        val aggregate = mutableMapOf<String, Int>()

        for (event in events) {
            if (event.channel == "") continue;

            if (!aggregate.containsKey(event.channel)){
                aggregate[event.channel] = 0
            }
            aggregate[event.channel] = aggregate[event.channel]!! + event.hits
        }

        val result = mutableListOf<HitsByChannel>()
        for (pair in aggregate){
            result.add(HitsByChannel(pair.key, pair.value))
        }
        return result;
    }

    fun aggregateUniqueCustomer(events: Collection<ChannelHitEvent>): Collection<CustomersByChannel> {
        val aggregate = mutableMapOf<String, MutableSet<String>>()

        for (event in events) {
            if (event.channel == "") continue;

            if (!aggregate.containsKey(event.channel)){
                aggregate[event.channel] = mutableSetOf()
            }
            aggregate[event.channel]!!.add(event.url)
        }

        val result = mutableListOf<CustomersByChannel>()
        for (pair in aggregate){
            result.add(CustomersByChannel(pair.key, pair.value.size))
        }
        return result;
    }

}

data class CustomersByChannel (val channel: String, val customers: Int)

data class HitsByChannel (val channel: String, val hits: Int)

