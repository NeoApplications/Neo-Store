package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import java.time.YearMonth

@Composable
fun MultiLineChart(data: Map<String, Map<String, Long>>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val legendItemLabelComponent =
        rememberTextComponent(TextStyle(MaterialTheme.colorScheme.onSurface))
    val dates = data.keys.sorted()
    val lineKeys = data.values.flatMap { it.keys }.distinct().sorted()
    val entriesPerLine: Map<String, List<Long>> = lineKeys.mapIndexed { _, client ->
        client to dates.mapIndexed { _, date ->
            data[date]?.get(client) ?: 0
        }
    }.toMap()

    val colors = buildList {
        if ("Droid-ify" in entriesPerLine.keys) add(Color.Green)
        if ("F-Droid" in entriesPerLine.keys) add(Color.Blue)
        if ("F-Droid Classic" in entriesPerLine.keys) add(Color.Red)
        if ("Flicky" in entriesPerLine.keys) add(Color.Cyan)
        if ("Neo Store" in entriesPerLine.keys) add(Color.Magenta)
        if ("_total" in entriesPerLine.keys) add(MaterialTheme.colorScheme.primary)
        if ("_unknown" in entriesPerLine.keys) add(Color.Yellow)
    }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries {
                entriesPerLine.forEach { (_, vals) ->
                    series(vals)
                }
            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        colors.map { color ->
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(color)),
                                areaFill = null,
                            )
                        }
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { _, index, _ ->
                        dates.getOrNull(index.toInt())?.let {
                            YearMonth.parse(it.removeSuffix("-00")).toString()
                        } ?: index.toString()
                    },
                ),
                legend = rememberHorizontalLegend(
                    items = { _ ->
                        entriesPerLine.keys.forEachIndexed { index, label ->
                            add(
                                LegendItem(
                                    ShapeComponent(Fill(colors[index]), RoundedCornerShape(50)),
                                    legendItemLabelComponent,
                                    label,
                                )
                            )
                        }
                    },
                ),
            ),
        )
    }
}

@Composable
fun ClientsChart(data: Map<String, Map<String, Long>>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val legendItemLabelComponent =
        rememberTextComponent(TextStyle(MaterialTheme.colorScheme.onSurface))
    val dates = data.keys.sorted()
    val lineKeys = data.values.flatMap { it.keys }.distinct().sorted()
    val entriesPerLine: Map<String, List<Long>> = lineKeys.mapIndexed { _, client ->
        client to dates.mapIndexed { _, date ->
            data[date]?.get(client) ?: 0
        }
    }.toMap()

    val colors = buildList {
        if ("Droid-ify" in entriesPerLine.keys) add(Color.Green)
        if ("F-Droid" in entriesPerLine.keys) add(Color.Blue)
        if ("F-Droid Classic" in entriesPerLine.keys) add(Color.Red)
        if ("Flicky" in entriesPerLine.keys) add(Color.Cyan)
        if ("Neo Store" in entriesPerLine.keys) add(Color.Magenta)
        if ("_total" in entriesPerLine.keys) add(MaterialTheme.colorScheme.primary)
        if ("_unknown" in entriesPerLine.keys) add(Color.Yellow)
    }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries {
                entriesPerLine.forEach { (_, vals) ->
                    series(vals)
                }
            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        colors.map { color ->
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(color)),
                                areaFill = null,
                            )
                        }
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { _, index, _ ->
                        dates.getOrNull(index.toInt())?.let {
                            YearMonth.parse(it.removeSuffix("-00")).toString()
                        } ?: index.toString()
                    },
                ),
                legend = rememberHorizontalLegend(
                    items = { _ ->
                        entriesPerLine.keys.forEachIndexed { index, label ->
                            add(
                                LegendItem(
                                    ShapeComponent(Fill(colors[index]), RoundedCornerShape(50)),
                                    legendItemLabelComponent,
                                    label,
                                )
                            )
                        }
                    },
                ),
            ),
        )
    }
}

@Composable
fun SimpleLineChart(data: Map<String, Map<String, Long>>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val modelProducer = remember { CartesianChartModelProducer() }
    val legendItemLabelComponent =
        rememberTextComponent(TextStyle(MaterialTheme.colorScheme.onSurface))
    val dates = data.keys.sorted()
    val lineKeys = data.values.flatMap { it.keys }.distinct().sorted()
    val entriesPerLine: Map<String, List<Long>> = lineKeys.mapIndexed { _, key ->
        key to dates.mapIndexed { _, date ->
            data[date]?.get(key) ?: 0
        }
    }.toMap()
    val lineColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            entriesPerLine["_total"]?.let { stats ->
                lineSeries {
                    series(stats)
                }
            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                            areaFill = null,
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { _, index, _ ->
                        dates.getOrNull(index.toInt())?.let {
                            YearMonth.parse(it.removeSuffix("-00")).toString()
                        } ?: index.toString()
                    },
                ),
                legend = rememberHorizontalLegend(
                    items = { _ ->
                        add(
                            LegendItem(
                                ShapeComponent(Fill(lineColor), RoundedCornerShape(50)),
                                legendItemLabelComponent,
                                context.getString(R.string.total_downloads),
                            )
                        )
                    },
                ),
            ),
        )
    }
}
