package com.segmentationfault.saferoute.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.databinding.FragmentStatisticsBinding

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

        // get barChart
        val barChart = binding.barChart

        // add style to barChart
        barChart.setMaxVisibleValueCount(6) // if more then 6 entries are displayed, no values will be drawn
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 12f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Summer", "Spring", "Fall", "Winter"))
        xAxis.granularity = 1f // controls interval between axis values

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
        legend.orientation = Legend.LegendOrientation.HORIZONTAL

        val description = barChart.description
        description.isEnabled = false

        // setup chart entries
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 1100f))  // Summer
        entries.add(BarEntry(1f, 550f))   // Spring
        entries.add(BarEntry(2f, 800f))   // Fall
        entries.add(BarEntry(3f, 2000f))  // Winter

        // create BarDataSet with entries
        val barDataSet = BarDataSet(entries, "Car Crashes")
        barDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        barDataSet.valueTextSize = 18f

        // create and set BarData
        val barData = BarData(barDataSet)
        barChart.data = barData

        // refresh and apply changes
        barChart.invalidate()
    }
}