package com.blueskybone.arkscreen.ui.gacha

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.FragmentGachaStatisBinding
import com.blueskybone.arkscreen.ui.gacha.model.GachaPoolStats
import com.blueskybone.arkscreen.ui.gacha.model.GachaUiSnapshot
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import timber.log.Timber

/*
* 页面更新逻辑：
* 点击snipper触发model更新。
* 数据传递：
* 数据源：以卡池ID为准传递给model。这样的话实际上是text 和 id的对应关系
* 然后model返回的内容：关于这个卡池的数据：6,5,4,3星数量。剩下的前端自己算一下吧。然后再返回一个数据时间跨度？
* 然后递交给pieChart。
*
* 页面初始化的逻辑：先获取gacha list， 返回给前端，前端setupSpinner()，的时候，触发一次model.calc_gacha(gacha_id)
* 在observe,观察6,5,4,3星的变化。然后holecentertext直接抓取snipper的数据
* */

class GachaStatsFragment : Fragment() {
    private val model: GachaModel by activityViewModel()

    private var _binding: FragmentGachaStatisBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGachaStatisBinding.inflate(inflater)
        setupBinding()
        collectUiState()
        return binding.root
    }

    private fun setupBinding() {
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect { state ->
                    state.gachaUiSnapshot?.let{
                        renderGachaStats(it, state.selectedPoolId)
                    }
                }
            }
        }
    }

    private fun renderGachaStats(gachaUi: GachaUiSnapshot, selectedPoolId: String) {
        val poolStatsList = gachaUi.gachaPoolStats
        setupSpinner(poolStatsList, selectedPoolId)
    }

    private fun setPieChart(gachaInfo: GachaPoolStats) {
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

    private fun setupSpinner(gachaInfo: List<GachaPoolStats>, selectedPoolId: String) {
        if (gachaInfo.isEmpty()) {
            binding.PoolDropdown.setAdapter(null)
            binding.PoolDropdown.setText("", false)
            binding.RecordsCount.text = "0"
            binding.PieChart.clear()
            return
        }
        binding.PoolDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                gachaInfo.map { it.poolName },
            )
        )
        binding.PoolDropdown.setOnItemClickListener { _, _, position, _ ->
            gachaInfo.getOrNull(position)?.let { model.selectPool(it.poolId) }
        }

        val selectedInfo = gachaInfo.firstOrNull { it.poolId == selectedPoolId }
            ?: gachaInfo.first()
        binding.PoolDropdown.setText(selectedInfo.poolName, false)
        renderSelectedPool(selectedInfo)
    }

    private fun renderSelectedPool(info: GachaPoolStats) {
        try {
            setPieChart(info)
            val countSum = info.rare3 + info.rare4 + info.rare5 + info.rare6
            binding.RecordsCount.text = getString(R.string.gacha_result_count, countSum)

            binding.Rare6Count.text = getString(R.string.gacha_stat_count, info.rare6)
            binding.Rare6Percent.text = formatPercent(info.rare6, countSum)
            binding.Rare6Ave.text = formatAverage(countSum, info.rare6)

            binding.Rare5Count.text = getString(R.string.gacha_stat_count, info.rare5)
            binding.Rare5Percent.text = formatPercent(info.rare5, countSum)
            binding.Rare5Ave.text = formatAverage(countSum, info.rare5)

            binding.Rare4Count.text = getString(R.string.gacha_stat_count, info.rare4)
            binding.Rare4Percent.text = formatPercent(info.rare4, countSum)
            binding.Rare4Ave.text = formatAverage(countSum, info.rare4)

            binding.Rare3Count.text = getString(R.string.gacha_stat_count, info.rare3)
            binding.Rare3Percent.text = formatPercent(info.rare3, countSum)
            binding.Rare3Ave.text = formatAverage(countSum, info.rare3)
        } catch (error: Exception) {
            Toaster.show(error.message)
            Timber.e(error)
        }
    }

    private fun formatPercent(count: Int, total: Int): String =
        getString(
            R.string.gacha_stat_percent,
            if (total == 0) 0f else count.toFloat() / total * 100,
        )

    private fun formatAverage(total: Int, count: Int): String =
        if (count == 0) "-" else getString(R.string.gacha_stat_average, total / count)

    override fun onDestroyView() {
        binding.PieChart.clear()
        binding.BarChart.clear()
        _binding = null
        super.onDestroyView()
    }
}
