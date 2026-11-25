package com.blueskybone.arkscreen.ui.character.adapter

import com.blueskybone.arkscreen.ui.account.common.bindAccount
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import android.view.View
import coil.load
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.content.res.ColorStateList
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemCharHeaderBinding
import com.blueskybone.arkscreen.databinding.AssetRarityBinding
import com.blueskybone.arkscreen.ui.character.model.CharStatistic

class CharHeaderAdapter(
    private val onAccountClick: (View) -> Unit,
    private val onFilterClick: () -> Unit,
    private val onViewClick: () -> Unit,
    private val onMissingClick: () -> Unit,
) : RecyclerView.Adapter<CharHeaderAdapter.HeaderViewHolder>() {

    var showMissing = false
    var missing = emptyList<com.blueskybone.arkscreen.domain.model.operator.Operator>()
    var updated = ""
    private var statistic = CharStatistic()
    private var account: AccountSk? = null
    private var avatarUrl: String? = null
    fun submitAvatar(value: String?) {
        avatarUrl = value
        notifyItemChanged(0)
    }

    private var filterCount = -1
    private var viewType = ViewType.GRID

    fun submitFilterCount(value: Int) {
        filterCount = value
        notifyItemChanged(0)
    }

    fun submitViewType(value: ViewType) {
        viewType = value
        notifyItemChanged(0)
    }

    fun submitStatistic(value: CharStatistic) {
        statistic = value
        notifyItemChanged(0)
    }

    fun submitAccount(value: AccountSk?) {
        account = value
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder =
        HeaderViewHolder(
            ItemCharHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(account, avatarUrl, statistic, filterCount, viewType,
            onAccountClick, onFilterClick, onViewClick, onMissingClick)
    }

    override fun getItemCount(): Int = 1

    inner class HeaderViewHolder(
        private val binding: ItemCharHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            account: AccountSk?,
            avatarUrl: String?,
            statistic: CharStatistic,
            filterCount: Int,
            viewType: ViewType,
            onAccountClick: (View) -> Unit,
            onFilterClick: () -> Unit,
            onViewClick: () -> Unit,
            onMissingClick: () -> Unit,
        ) {
            val context = binding.root.context
            binding.AccountRow.Avatar.load(avatarUrl) { placeholder(R.drawable.ic_user); error(R.drawable.ic_user) }
            binding.AccountRow.Avatar.setOnClickListener(onAccountClick)
            binding.AccountRow.AccountSelector.bindAccount(account)
            binding.AccountRow.AccountSelector.root.setOnClickListener(onAccountClick)
            binding.Filter.setOnClickListener { onFilterClick() }
            binding.ViewChanger.setOnClickListener { onViewClick() }
            binding.MissingButton.setOnClickListener { onMissingClick() }
            binding.Filter.text = if (filterCount < 0) context.getString(R.string.filter)
                else context.getString(R.string.assets_filter_count, filterCount)
            val filterColor = if (filterCount >= 0) {
                context.getColor(R.color.asset_action_selected_text)
            } else {
                context.getColor(R.color.home_text_primary)
            }
            binding.Filter.setTextColor(filterColor)
            binding.Filter.chipIconTint = ColorStateList.valueOf(filterColor)
            binding.ViewChanger.setChipIconResource(
                if (viewType == ViewType.LIST) R.drawable.ic_list else R.drawable.ic_grid
            )
            binding.MissingButton.text = context.getString(R.string.assets_missing_count, statistic.totalMiss)
            binding.MissingPanel.root.visibility = if (showMissing) View.VISIBLE else View.GONE
            binding.Filter.visibility = if (showMissing) View.INVISIBLE else View.VISIBLE
            binding.ViewChanger.visibility = if (showMissing) View.INVISIBLE else View.VISIBLE
            if (showMissing) {
                binding.MissingButton.setText(R.string.assets_held)
                binding.MissingPanel.Update.text = updated
                CharMissFlowAdapter(context, binding.MissingPanel.FlowLayout).submitList(missing)
            }
            binding.E2Stat.Title.setText(R.string.assets_e2)
            binding.M3Stat.Title.setText(R.string.assets_m3)
            binding.Module3Stat.Title.setText(R.string.assets_module3_short)
            binding.Owned.text = statistic.totalOwn.toString()
            binding.Total.text = "/ ${statistic.totalOwn + statistic.totalMiss}"
            binding.E2Stat.Value.text = statistic.totalE2.toString()
            binding.M3Stat.Value.text = statistic.totalM3.toString()
            binding.Module3Stat.Value.text = statistic.totalModule3.toString()
            bindRarity(binding.R6Progress, 6, statistic.r6Own, statistic.r6Miss, R.color.rare_6)
            bindRarity(binding.R5Progress, 5, statistic.r5Own, statistic.r5Miss, R.color.rare_5)
            bindRarity(binding.R4Progress, 4, statistic.r4Own, statistic.r4Miss, R.color.rare_4)
        }

        private fun bindRarity(row: AssetRarityBinding, stars: Int, owned: Int, missing: Int, color: Int) {
            val total = owned + missing
            val context = row.root.context
            row.Count.text = context.getString(R.string.assets_rarity_progress, stars, owned, total)
            row.Progress.setIndicatorColor(context.getColor(color))
            row.Progress.max = total.coerceAtLeast(1)
            row.Progress.progress = owned
        }
    }
}
