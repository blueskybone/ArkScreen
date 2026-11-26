package com.blueskybone.arkscreen.ui.gacha

import android.content.Intent
import androidx.appcompat.widget.ListPopupWindow
import com.blueskybone.arkscreen.ui.account.common.showAccountChoices
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import com.blueskybone.arkscreen.databinding.ActivityEndfieldGachaBinding
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsTxtBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldGachaPoolBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldGachaHitBinding
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EndfieldGachaActivity : AppCompatActivity() {
    private val model: EndfieldGachaModel by viewModel()
    private lateinit var binding: ActivityEndfieldGachaBinding
    val category = kotlinx.coroutines.flow.MutableStateFlow("char")
    private var mediator: com.google.android.material.tabs.TabLayoutMediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Endfield)
        super.onCreate(savedInstanceState)
        binding = ActivityEndfieldGachaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        category.value = savedInstanceState?.getString("type") ?: "char"
        binding.CharButton.isCheckable = true
        binding.WeaponButton.isCheckable = true
        fun select(type: String) {
            category.value = type
            binding.CharButton.isChecked = type == "char"
            binding.WeaponButton.isChecked = type == "weapon"
        }
        binding.CharButton.setOnClickListener { select("char") }
        binding.WeaponButton.setOnClickListener { select("weapon") }
        select(category.value)
        binding.ViewPager.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int) = EndfieldGachaPageFragment().apply {
                arguments = Bundle().apply { putInt("page", position) }
            }
        }
        mediator = com.google.android.material.tabs.TabLayoutMediator(binding.ViewTabs, binding.ViewPager) { tab, position ->
            tab.text = listOf("卡池详情", "统计报告", "原始记录")[position]
        }.also { it.attach() }
        binding.ViewPager.setCurrentItem(savedInstanceState?.getInt("viewMode") ?: 0, false)
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { model.state.collect { invalidateOptionsMenu() } } }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("type", category.value)
        outState.putInt("viewMode", binding.ViewPager.currentItem)
        super.onSaveInstanceState(outState)
    }
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menu.add(0, 1, 0, R.string.refresh).setIcon(
            androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_function_refresh)?.mutate()?.apply {
                setTint(androidx.core.content.ContextCompat.getColor(this@EndfieldGachaActivity, R.color.home_text_primary))
            })
            .setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }
    override fun onPrepareOptionsMenu(menu: android.view.Menu): Boolean {
        menu.findItem(1)?.isEnabled = model.state.value.selectedUid != null && !model.state.value.loading
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == 1) { model.refresh(); return true }
        return super.onOptionsItemSelected(item)
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
    override fun onDestroy() {
        mediator?.detach()
        binding.ViewPager.adapter = null
        super.onDestroy()
    }
}

internal class EndfieldPoolAdapter : ListAdapter<EndfieldPoolProgress, EndfieldPoolAdapter.Holder>(
    object : DiffUtil.ItemCallback<EndfieldPoolProgress>() {
        override fun areItemsTheSame(a: EndfieldPoolProgress, b: EndfieldPoolProgress) =
            a.key == b.key
        override fun areContentsTheSame(a: EndfieldPoolProgress, b: EndfieldPoolProgress) = a == b
    },
) {
    private val expanded = mutableSetOf<String>()
    private var accountUid: String? = null
    fun setAccount(uid: String?) {
        if (accountUid != uid) { expanded.clear(); accountUid = uid }
    }

    class Holder(val binding: ItemEndfieldGachaPoolBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        ItemEndfieldGachaPoolBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val pool = getItem(position)
        val binding = holder.binding
        binding.PoolName.text = pool.name
        binding.Count.text = "${pool.total} 次"
        binding.Pity.root.text = "当前水位 ${pool.smallCount} 抽"
        binding.Pity.root.isClickable = false
        binding.Hits.removeAllViews()
        val visible = if (pool.key in expanded) pool.hits else pool.hits.take(3)
        visible.forEach { hit ->
            val row = ItemEndfieldGachaHitBinding.inflate(
                LayoutInflater.from(binding.Hits.context), binding.Hits, false,
            )
            row.Date.text = formatGachaTime(hit.gachaTs, "MM-dd")
            row.Name.text = hit.name + if (hit.isNew) " · N" else ""
            row.Progress.max = pool.smallMax ?: 80
            row.Progress.progress = hit.pity.coerceAtMost(row.Progress.max)
            row.Progress.setIndicatorColor(androidx.core.content.ContextCompat.getColor(
                row.root.context, gachaPullColor(hit.pity),
            ))
            row.Count.text = if (hit.isFree) "免费" else "${hit.pity} 次"
            binding.Hits.addView(row.root)
        }
        binding.Empty.visibility = if (pool.hits.isEmpty()) View.VISIBLE else View.GONE
        val canExpand = pool.hits.size > 3
        binding.ExpandHint.visibility = if (canExpand) View.VISIBLE else View.GONE
        if (canExpand) {
            binding.ExpandHint.text = if (pool.key in expanded) {
                binding.root.context.getString(R.string.collapse_gacha_records)
            } else {
                binding.root.context.getString(R.string.expand_gacha_records, pool.hits.size)
            }
            binding.root.setOnClickListener {
                if (!expanded.add(pool.key)) expanded.remove(pool.key)
                val index = holder.bindingAdapterPosition
                if (index != RecyclerView.NO_POSITION) notifyItemChanged(index)
            }
        } else binding.root.setOnClickListener(null)
    }
}

internal data class EndfieldRawRow(val record: EndfieldGachaRecord, val pity: Int, val total: Int)

internal fun rawRows(records: List<EndfieldGachaRecord>): List<EndfieldRawRow> {
    val totals = mutableMapOf<String, Int>()
    val pity = mutableMapOf<String, Int>()
    val versions = mutableMapOf<String, Int>()
    return records.asReversed().map { record ->
        val key = record.poolId.ifBlank { record.poolName.ifBlank { record.poolType } }
        if (record.type == "weapon" && (record.poolType == "rerun" || record.poolId.startsWith("rerun", true))) {
            record.poolVersion?.let { version ->
                if (versions[key] != null && versions[key] != version) pity[key] = 0
                versions[key] = version
            }
        }
        val total = (totals[key] ?: 0) + 1
        totals[key] = total
        val count = if (record.isFree) 0 else (pity[key] ?: 0) + 1
        if (!record.isFree) pity[key] = if (record.rarity == 6) 0 else count
        EndfieldRawRow(record, count, total)
    }.asReversed()
}

internal class RecordAdapter : ListAdapter<EndfieldRawRow, RecordAdapter.Holder>(
    object : DiffUtil.ItemCallback<EndfieldRawRow>() {
        override fun areItemsTheSame(a: EndfieldRawRow, b: EndfieldRawRow) =
            a.record.roleId == b.record.roleId && a.record.type == b.record.type && a.record.seqId == b.record.seqId
        override fun areContentsTheSame(a: EndfieldRawRow, b: EndfieldRawRow) = a == b
    },
) {
    class Holder(val binding: ItemGachaRecordsTxtBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemGachaRecordsTxtBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = getItem(position)
        val record = row.record
        val binding = holder.binding
        val context = binding.root.context
        binding.OperatorName.text = record.itemName.ifBlank { record.itemId.ifBlank { "未知物品" } }
        val pool = record.poolName.ifBlank { record.poolType }
        binding.PoolInfo.text = if (record.isFree) "$pool · 免费" else context.getString(R.string.gacha_record_pool_info, pool, row.pity)
        binding.Rarity.text = context.getString(R.string.gacha_rarity, record.rarity)
        binding.GainTime.text = formatGachaTime(record.gachaTs, "yyyy-MM-dd HH:mm:ss")
        binding.PoolCount.text = context.getString(R.string.current_pool_gacha_count, row.total)
        binding.IsNew.visibility = if (record.isNew) View.VISIBLE else View.GONE
        val color = androidx.core.content.ContextCompat.getColor(context,
            com.blueskybone.arkscreen.ui.character.rarityColorMap[record.rarity] ?: R.color.red)
        binding.OperatorName.setTextColor(color)
        binding.Rarity.setTextColor(color)
    }
}

internal fun formatGachaTime(raw: String, pattern: String): String {
    val timestamp = raw.toLongOrNull() ?: return raw
    val millis = if (timestamp < 10_000_000_000L) timestamp * 1000 else timestamp
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}
