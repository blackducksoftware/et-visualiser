package analytics

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CustomerAggregator {
    fun <T> aggregateByDate(data: List<T>, getCustomerId: (T) -> String, getDateStr: (T) -> String, getGroupValue: (T) -> String): Collection<CustomerDateAggregate> {
        val aggregate: MutableMap<GroupValueDate, MutableSet<String>> = mutableMapOf()

        for (datum in data) {
            val customerId = getCustomerId(datum)
            val date = getDateStr(datum).toDate()
            val value = getGroupValue(datum)

            val key = GroupValueDate(value, date)
            if (!aggregate.containsKey(key)){
                aggregate[key] = mutableSetOf()
            }

            aggregate[key]!!.add(customerId)
        }

        return aggregate.map {
            val valueDate = it.key
            val customerSet = it.value
            CustomerDateAggregate(customers = customerSet.size, date = valueDate.date, value = valueDate.value)
        }
    }

    fun <T> aggregateUsage(data: List<T>, getCustomerId: (T) -> String, getGroupValue: (T) -> String): Collection<CustomerAggregate> {
        val aggregate: MutableMap<String, MutableSet<String>> = mutableMapOf()

        for (datum in data) {
            val customerId = getCustomerId(datum)
            val key = getGroupValue(datum)

            if (!aggregate.containsKey(key)){
                aggregate[key] = mutableSetOf()
            }

            aggregate[key]!!.add(customerId)
        }

        return aggregate.map {
            val customerSet = it.value
            val value = it.key
            CustomerAggregate(customers = customerSet.size, value = value)
        }
    }
}

fun String.toDate(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
}

data class GroupValueDate(val value:String, val date:LocalDate)

data class CustomerDateAggregate(val customers: Int, val date: LocalDate, val value: String)
data class CustomerAggregate(val customers: Int, val value: String)