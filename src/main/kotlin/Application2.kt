import detectors.*
import tech.tablesaw.api.*
import tech.tablesaw.columns.AbstractColumn
import tech.tablesaw.columns.Column
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Layout
import tech.tablesaw.plotly.components.Marker
import tech.tablesaw.plotly.traces.PieTrace
import tech.tablesaw.plotly.traces.ScatterTrace
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun main() {
    val application2 = Application2()
    application2.run2()
}

class Application2 {
    private val rawData: MutableCollection<CustomerDetectorHitEvent> = mutableListOf()

    init {
        val analyticsService = AnalyticsService()
        val query = DetectorEventService(analyticsService)

        rawData.addAll(query.retreiveEvents())
    }

    fun run2() {
        val layout = Layout.builder()
            .title("# of Customers utilizing Detectors")
            .width(1300)
            .height(1000)
            .build()

        run {
            val detectors = mutableListOf<String>()
            val customerCount = mutableListOf<Int>()
            val dates = mutableListOf<LocalDate>()

            getDetectorCustomers()
                .sortedWith(Comparator { o1, o2 ->
                    if (o1.detector == o2.detector) {
                        val d1 = LocalDate.parse(o1.date, DateTimeFormatter.ofPattern("yyyyMMdd"))
                        val d2 = LocalDate.parse(o2.date, DateTimeFormatter.ofPattern("yyyyMMdd"))
                        return@Comparator d1.compareTo(d2)
                    } else {
                        return@Comparator o1.detector.compareTo(o2.detector)
                    }
                })
                .forEach {
                    detectors.add(it.detector)
                    customerCount.add(it.customers)
                    dates.add(LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyyMMdd")))
                }

            val detectorsColumn = StringColumn.create("Detectors", detectors)
            val customerCountColumn = IntColumn.create("Customer Count", customerCount.toIntArray())
            val datesColumn = DateColumn.create("Date", dates)

            val scatterPlot = createScatterTrace(detectorsColumn, customerCountColumn, datesColumn)

            Plot.show(scatterPlot, File("/tmp/figure1.html"))

        }

        run {
            val detectors = mutableListOf<String>()
            val customerCount = mutableListOf<Int>()
            getUnqiueCustomers().forEach {
                detectors.add(it.detector)
                customerCount.add(it.customers)
            }

            val detectorsColumn = StringColumn.create("Detectors", detectors)
            val customerCountColumn = IntColumn.create("Customer Count", customerCount.toIntArray())

            val pieTrace = createPieTrace(detectorsColumn, customerCountColumn)

            Plot.show(Figure(layout, pieTrace), File("/tmp/figure2.html"))
        }
    }

    private fun createPieTrace(detectorsColumn: Column<String>, customerCountColumn: NumberColumn<Int>): PieTrace {
        return PieTrace.builder(detectorsColumn, customerCountColumn).build()
    }

    private fun createScatterTrace(detectorsColumn: Column<String>, customerCountColumn: NumberColumn<Int>, datesColumn: AbstractColumn<LocalDate>): Figure {
        val table = Table.create(datesColumn, customerCountColumn, detectorsColumn)
        return createScatterPlot("Detector Usage Over Time", table, "Date", "Customer Count", "Detectors")
    }

    private fun createScatterPlot(title: String, table: Table, xCol: String, yCol: String, groupCol: String): Figure {
        val tables = table.splitOn(table.categoricalColumn(groupCol))
        val layout = Layout.builder(title, xCol, yCol)
            .showLegend(true)
            .width(1300)
            .height(1000)
            .build()
        val traces = arrayOfNulls<ScatterTrace>(tables.size())
        val marker = Marker.builder().opacity(0.75).build()
        for (i in 0 until tables.size()) {
            val tableList = tables.asTableList()
            traces[i] = ScatterTrace.builder(
                tableList[i].numberColumn(xCol),
                tableList[i].numberColumn(yCol))
                .showLegend(true)
                .marker(marker)
                .name(tableList[i].name())
                .mode(ScatterTrace.Mode.LINE_AND_MARKERS)
                .build()
        }
        return Figure(layout, *traces)
    }

    private fun getDetectorCustomers(): Collection<CustomersAndDateByDetector> {
        val customerEventProcessor = DetectorAggregateProcessor()
        return customerEventProcessor.aggregateUniqueCustomerAndDate(rawData)
    }

    private fun getUnqiueCustomers(): Collection<CustomersByDetector> {
        val customerEventProcessor = DetectorAggregateProcessor()
        return customerEventProcessor.aggregateUniqueCustomer(rawData)
    }
}