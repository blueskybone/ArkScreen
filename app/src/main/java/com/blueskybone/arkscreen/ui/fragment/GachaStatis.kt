package com.blueskybone.arkscreen.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.space
import com.blueskybone.arkscreen.databinding.FragmentGachaStatisBinding
import com.blueskybone.arkscreen.databinding.FragmentGachaTextBinding
import com.blueskybone.arkscreen.ui.model.GachaInfo
import com.blueskybone.arkscreen.ui.recyclerview.GachaTextAdapter
import com.blueskybone.arkscreen.util.getColorFromAttr
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.blueskybone.arkscreen.viewmodel.GachaModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hjq.toast.Toaster
import timber.log.Timber

/*
* 这个页面的更新逻辑：
* 点击snipper触发model更新。
* 数据传递：
* 数据源：以卡池ID为准传递给model。这样的话实际上是text 和 id的对应关系
* 然后model返回的内容：关于这个卡池的数据：6,5,4,3星数量。剩下的前端自己算一下吧。然后再返回一个数据时间跨度？
* 然后递交给pieChart。
*
* 所以页面初始化的逻辑：先获取gacha list， 返回给前端，前端setupSpinner()，的时候，触发一次model.calc_gacha(gacha_id)
* 在observe,观察6,5,4,3星的变化。然后holecentertext直接抓取snipper的数据
* */

class GachaStatis : Fragment() {
    private val model : GachaModel by activityViewModels()

    private var _binding: FragmentGachaStatisBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGachaStatisBinding.inflate(inflater)
        setupBinding()
        setUpObserver()
//        setupSimpleBarChart()
        return binding.root
    }

    private fun setupBinding() {
    }

    private fun setPieChart(gachaInfo: GachaInfo) {
        val pieChart = binding.PieChart
        val entries = listOf(
            PieEntry(gachaInfo.rare6.toFloat(), "6★"),
            PieEntry(gachaInfo.rare5.toFloat(), "5★"),
            PieEntry(gachaInfo.rare4.toFloat(), "4★"),
            PieEntry(gachaInfo.rare3.toFloat(), "3★"),
        )

        val rare6Color = requireContext().getColor(R.color.rare_6)
        val rare5Color = requireContext().getColor(R.color.rare_5)
        val rare4Color = requireContext().getColor(R.color.rare_4)
        val rare3Color = requireContext().getColor(R.color.rare_3)
        val textColor = requireContext().getColor(R.color.pie_text)

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(rare6Color, rare5Color, rare4Color, rare3Color)
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        val pieData = PieData(dataSet)
        pieChart.data = pieData

        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true) // 显示百分比
            centerText = gachaInfo.poolName
            isRotationEnabled = false
            animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    // 保留一位小数
                    return "%.1f%%".format(value)
                }
            })
            // 中间空心部分
            isDrawHoleEnabled = true
            setDrawCenterText(true)
            setHoleColor(requireContext().getColor(R.color.transparent)) //设置中间颜色
            setCenterTextColor(textColor)
            legend.textColor = textColor
        }
        pieChart.invalidate()
    }

    private fun setupSpinner(gachaInfo: List<GachaInfo>) {

        val dataText = gachaInfo.map { item -> item.poolName }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,  // 默认布局
            dataText
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)  // 下拉项布局

        // 设置适配器
        val spinner: Spinner = binding.Spinner
        spinner.adapter = adapter

        // 设置选择监听器
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                try {
                    val info = gachaInfo[position]
                    setPieChart(info)
                    val countSum = info.rare3 + info.rare4 + info.rare5 + info.rare6
                    binding.RecordsCount.text = countSum.toString() + "抽"

                    binding.Rare6Count.text = "共 " + info.rare6.toString() + " 个"
                    binding.Rare6Percent.text = "占 " + "%.1f%%".format(info.rare6.toFloat() / countSum * 100)
                    binding.Rare6Ave.text = if(info.rare6 == 0) "-" else (countSum /info.rare6).toString()  + "抽/个"

                    binding.Rare5Count.text = "共 " + info.rare5.toString() + " 个"
                    binding.Rare5Percent.text = "占 " + "%.1f%%".format(info.rare5.toFloat() / countSum * 100)
                    binding.Rare5Ave.text = if(info.rare5 == 0) "-" else (countSum /info.rare5).toString()  + "抽/个"

                    binding.Rare4Count.text = "共 " + info.rare4.toString() + " 个"
                    binding.Rare4Percent.text = "占 " + "%.1f%%".format(info.rare4.toFloat() / countSum * 100)
                    binding.Rare4Ave.text = if(info.rare4 == 0) "-" else (countSum /info.rare4).toString()  + "抽/个"

                    binding.Rare3Count.text = "共 " + info.rare3.toString() + " 个"
                    binding.Rare3Percent.text = "占 " + "%.1f%%".format(info.rare3.toFloat() / countSum * 100)
                    binding.Rare3Ave.text = if(info.rare3 == 0) "-" else (countSum /info.rare3).toString()  + "抽/个"
                }catch (e: Exception){
                    Toaster.show(e.message)
                    Timber.e(e)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 设置默认选择（可选）
        // 选择第一项,并触发监听器
        spinner.setSelection(0)
    }

    private fun setupSimpleBarChart() {

        val barChart = binding.BarChart
        // 数据
        val entries = listOf(
            BarEntry(0f, 120f),
            BarEntry(1f, 85f),
            BarEntry(2f, 150f),
            BarEntry(3f, 65f),
            BarEntry(4f, 180f)
        )

        val labels = arrayOf("2025.05", "2025.06", "2025.07", "2025.08", "2025.09")

        val dataSet = BarDataSet(entries, "寻访次数")
        val themeColor = requireContext().getColor(R.color.rare_3)
        dataSet.color = themeColor
        dataSet.valueTextSize = 12f


        val barData = BarData(dataSet)
        barData.barWidth = 0.4f

        barChart.apply {
            data = barData
            description.isEnabled = false
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            axisRight.isEnabled = false
            animateY(1000)


            // X轴配置（横向柱状图中X轴在左侧）
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM // 标签显示在底部
                valueFormatter = IndexAxisValueFormatter(labels) // 设置标签
                granularity = 1f
                setDrawGridLines(false)
                textSize = 12f
            }

            // 左侧Y轴配置
            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 50f // 最小间隔50
                axisMinimum = 0f // 从0开始
                textSize = 12f
            }

            // 右侧Y轴配置
            axisRight.isEnabled = false // 禁用右侧Y轴

            invalidate()

        }
    }

    private fun setUpObserver() {
        model.gachaInfoList.observe(requireActivity()) { value ->
            setupSpinner(value)
        }
    }
}