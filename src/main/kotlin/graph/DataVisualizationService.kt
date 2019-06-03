package graph

import detectors.CustomerDetectorHitEvent
import detectors.DetectorAggregateProcessor
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

class DataVisualizationService {
    private val aggregateProcessor = DetectorAggregateProcessor()

    fun createLineChart(data: Collection<CustomerDetectorHitEvent>, destination: File) {
        val detectors = mutableListOf<String>()
        val customerCount = mutableListOf<Int>()
        val dates = mutableListOf<LocalDate>()

        aggregateProcessor.aggregateUniqueCustomerAndDate(data)
            .sortedWith(Comparator { event1, event2 ->
                if (event1.detector == event2.detector) {
                    return@Comparator event1.date.compareTo(event1.date)
                } else {
                    return@Comparator event1.detector.compareTo(event2.detector)
                }
            })
            .forEach {
                detectors.add(it.detector)
                customerCount.add(it.customers)
                dates.add(it.date)
            }

        val detectorsColumn = StringColumn.create("Detectors", detectors)
        val customerCountColumn = IntColumn.create("Customer Count", customerCount.toIntArray())
        val datesColumn = DateColumn.create("Date", dates)
        val scatterPlot = createScatterPlot(detectorsColumn, customerCountColumn, datesColumn)

        Plot.show(scatterPlot, destination)
    }

    fun createPieChart(data: Collection<CustomerDetectorHitEvent>, destination: File) {
        val detectors = mutableListOf<String>()
        val customerCount = mutableListOf<Int>()

        aggregateProcessor.aggregateUniqueCustomer(data).forEach {
            detectors.add(it.detector)
            customerCount.add(it.customers)
        }

        val detectorsColumn = StringColumn.create("Detectors", detectors)
        val customerCountColumn = IntColumn.create("Customer Count", customerCount.toIntArray())
        val pieTrace = createPieTrace(detectorsColumn, customerCountColumn)

        val layout = Layout.builder()
            .title("Detector Usage")
            .width(1400)
            .height(1000)
            .build()

        Plot.show(Figure(layout, pieTrace), destination)
    }

    private fun createPieTrace(detectorsColumn: Column<String>, customerCountColumn: NumberColumn<Int>): PieTrace {
        return PieTrace.builder(detectorsColumn, customerCountColumn).build()
    }

    private fun createScatterPlot(
        detectorsColumn: Column<String>,
        customerCountColumn: NumberColumn<Int>,
        datesColumn: AbstractColumn<LocalDate>
    ): Figure {
        val table = Table.create(datesColumn, customerCountColumn, detectorsColumn)
        val title = "Detector Usage Over Time"
        val xCol = "Date"
        val yCol = "Customer Count"
        val groupCol = "Detectors"
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
                tableList[i].numberColumn(yCol)
            )
                .showLegend(true)
                .marker(marker)
                .name(tableList[i].name())
                .mode(ScatterTrace.Mode.LINE_AND_MARKERS)
                .build()
        }
        return Figure(layout, *traces)
    }
}