package com.segmentationfault.saferoute.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.databinding.FragmentStatisticsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class StatisticsFragment : Fragment() {

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var app: MyApplication

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        app = requireContext().applicationContext as MyApplication

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()

        // listener for spinner changes
        binding.spinnerOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Call your function here with the selected item's value
                val selectedValue = parentView?.getItemAtPosition(position).toString()
                getStatisticsFromApi(selectedValue)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
    }

    private fun setupChart() {
        // get barChart
        val barChart = binding.barChart

        // add style to barChart
        barChart.setMaxVisibleValueCount(4) // if more then 6 entries are displayed, no values will be drawn
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelCount = 0
        xAxis.setDrawGridLines(false)

        val leftAxis = barChart.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.textSize = 10f
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 2200f
        leftAxis.labelCount = 5

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false

        val legend = barChart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.SQUARE
        legend.textSize = 10f
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.xOffset = 50f
        legend.setDrawInside(true)

        val description = barChart.description
        description.isEnabled = false
    }

    private fun getStatisticsFromApi(apiRoute: String) {
        val url = MyApplication.DATABASE_API + "/common-stats/" + apiRoute.replace(' ', '-').lowercase()
        Log.d("StatisticsFragment", "API call to $url")

        val request = Request.Builder()
            .get()
            .url(url)
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("StatisticsFragment", "API Call Failure", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    Log.e("StatisticsFragment", "API Call Error: ${response.code}")
                    return
                }

                val responseBody = response.body
                if (responseBody != null) {
                    val res = JSONObject(responseBody.string())
                    Log.d("StatisticsFragment", "API call Success")
                    createChartFromData(res, apiRoute)
                } else {
                    Log.e("StatisticsFragment", "API Call Error: Empty response body")
                }
            }
        })
    }

    private fun createChartFromData(data: JSONObject, title: String) {
        try {
            val keysList = data.keys().asSequence().toList()
            val values = keysList.map { data.getInt(it) }

            // update
            val barChart = binding.barChart
            barChart.axisLeft.axisMaximum = values.max().toFloat() + 400f

            // Create BarChart entries
            val entries = values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }

            // create BarDataSet with entries
            val barDataSet = BarDataSet(entries, title.replace('-', ' ').uppercase())
            val colors = ColorTemplate.MATERIAL_COLORS.toList()
            barDataSet.colors = colors
            barDataSet.valueTextSize = 18f

            // update legend entries
            val legendEntries = keysList.mapIndexed { index, key ->
                LegendEntry().apply {
                    formColor = colors[index % colors.size]
                    label = key
                }
            }
            barChart.legend.setCustom(legendEntries)

            // create and set BarData
            val barData = BarData(barDataSet)
            barChart.data = barData

            barChart.invalidate()

        } catch (e: JSONException) {
            Log.e("StatisticsFragment", "JSON Parsing Error", e)
        }
    }
}