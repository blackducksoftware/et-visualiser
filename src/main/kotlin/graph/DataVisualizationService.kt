package graph

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

    fun customersOverDateLineGraph(graph: CustomersOverDateGraph, destination: File) {
        val detectors = mutableListOf<String>()
        val customerCount = mutableListOf<Int>()
        val dates = mutableListOf<LocalDate>()

        graph.rows.sortedWith(Comparator { event1, event2 ->
                if (event1.groupValue == event2.groupValue) {
                    return@Comparator event1.date.compareTo(event2.date)
                } else {
                    return@Comparator event1.groupValue.compareTo(event2.groupValue)
                }
            })
            .forEach {
                detectors.add(it.groupValue)
                customerCount.add(it.customers)
                dates.add(it.date)
            }

        val detectorsColumn = StringColumn.create(graph.groupLabel, detectors)
        val customerCountColumn = IntColumn.create("Customer Count", customerCount.toIntArray())
        val datesColumn = DateColumn.create("Date", dates)
        val scatterPlot = createScatterPlot(graph, detectorsColumn, customerCountColumn, datesColumn)

        Plot.show(scatterPlot, destination)
    }

    fun customerUsageGraph(data: CustomerUsageGraph, destination: File) {
        val detectors = mutableListOf<String>()
        val customerCount = mutableListOf<Int>()

        data.rows.forEach {
            detectors.add(it.groupValue)
            customerCount.add(it.customers)
        }

        val detectorsColumn = StringColumn.create(data.groupLabel, detectors)
        val customerCountColumn = IntColumn.create("Customer Count", customerCount.toIntArray())
        val pieTrace = createPieTrace(detectorsColumn, customerCountColumn)

        val layout = Layout.builder()
            .title(data.title)
            .width(1400)
            .height(1000)
            .build()

        Plot.show(Figure(layout, pieTrace), destination)
    }

    private fun createPieTrace(detectorsColumn: Column<String>, customerCountColumn: NumberColumn<Int>): PieTrace {
        return PieTrace.builder(detectorsColumn, customerCountColumn).build()
    }

    private fun createScatterPlot(
        graph: CustomersOverDateGraph,
        detectorsColumn: Column<String>,
        customerCountColumn: NumberColumn<Int>,
        datesColumn: AbstractColumn<LocalDate>
    ): Figure {
        val table = Table.create(datesColumn, customerCountColumn, detectorsColumn)
        val title = graph.title
        val xCol = "Date"
        val yCol = "Customer Count"
        val groupCol = graph.groupLabel
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

data class CustomerUsageGraph (val title: String, val groupLabel: String, val rows: List<CustomerUsageRow>)
data class CustomerUsageRow(val customers: Int, val groupValue: String)

data class CustomersOverDateGraph (val title: String, val groupLabel: String, val rows: List<CustomersOverDateRow>)
data class CustomersOverDateRow(val customers: Int, val groupValue: String, val date: LocalDate)