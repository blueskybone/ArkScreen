package com.blueskybone.arkscreen.ui.character.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemCharHeaderBinding
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.ui.character.model.CharStatistic

class CharHeaderAdapter(
    private val onAccountClick: () -> Unit,
    private val onFilterClick: () -> Unit,
    private val onViewClick: () -> Unit,
    private val onMissingClick: () -> Unit,
) : RecyclerView.Adapter<CharHeaderAdapter.HeaderViewHolder>() {

    private var statistic = CharStatistic()
    private var account: AccountSk? = null
    private var accountAvatarUrl: String? = null
    private var filterCount = 0
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

    fun submitAccountAvatar(value: String?) {
        accountAvatarUrl = value
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
        holder.bind(account, accountAvatarUrl, statistic, filterCount, viewType,
            onAccountClick, onFilterClick, onViewClick, onMissingClick)
    }

    override fun getItemCount(): Int = 1

    class HeaderViewHolder(
        private val binding: ItemCharHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            account: AccountSk?,
            accountAvatarUrl: String?,
            statistic: CharStatistic,
            filterCount: Int,
            viewType: ViewType,
            onAccountClick: () -> Unit,
            onFilterClick: () -> Unit,
            onViewClick: () -> Unit,
            onMissingClick: () -> Unit,
        ) {
            val context = binding.root.context
            binding.AccountRow.setOnClickListener { onAccountClick() }
            binding.Filter.setOnClickListener { onFilterClick() }
            binding.ViewChanger.setOnClickListener { onViewClick() }
            binding.MissingButton.setOnClickListener { onMissingClick() }
            binding.Filter.text = if (filterCount == 0) context.getString(R.string.filter)
                else context.getString(R.string.assets_filter_count, filterCount)
            binding.ViewChanger.setImageResource(
                if (viewType == ViewType.LIST) R.drawable.ic_list else R.drawable.ic_grid
            )
            binding.MissingButton.text = context.getString(R.string.assets_missing_count, statistic.totalMiss)
            binding.AccountRow.contentDescription = context.getString(R.string.skland_choose_account)
            binding.AccountNickName.text = account?.nickName.orEmpty()
            if (account == null) {
                binding.AccountIcon.setImageDrawable(null)
            } else if (!accountAvatarUrl.isNullOrBlank()) {
                binding.AccountIcon.load(accountAvatarUrl) {
                    crossfade(true)
                    fallback(
                        if (account.official) R.drawable.hg_icon_80x80
                        else R.drawable.bili_icon_75x71
                    )
                    error(
                        if (account.official) R.drawable.hg_icon_80x80
                        else R.drawable.bili_icon_75x71
                    )
                }
            } else {
                binding.AccountIcon.setImageResource(
                    if (account.official) R.drawable.hg_icon_80x80
                    else R.drawable.bili_icon_75x71
                )
            }
            binding.OwnedStat.Title.setText(R.string.assets_owned)
            binding.E2Stat.Title.setText(R.string.assets_e2)
            binding.M3Stat.Title.setText(R.string.assets_m3)
            binding.Module3Stat.Title.setText(R.string.assets_module3_short)
            binding.OwnedStat.Value.text =
                "${statistic.totalOwn}/${statistic.totalOwn + statistic.totalMiss}"
            binding.E2Stat.Value.text = statistic.totalE2.toString()
            binding.M3Stat.Value.text = statistic.totalM3.toString()
            binding.Module3Stat.Value.text = statistic.totalModule3.toString()
            binding.R6Progress.text = context.getString(
                R.string.assets_rarity_progress,
                6,
                statistic.r6Own,
                statistic.r6Own + statistic.r6Miss,
            )
            binding.R5Progress.text = context.getString(
                R.string.assets_rarity_progress,
                5,
                statistic.r5Own,
                statistic.r5Own + statistic.r5Miss,
            )
            binding.R4Progress.text = context.getString(
                R.string.assets_rarity_progress,
                4,
                statistic.r4Own,
                statistic.r4Own + statistic.r4Miss,
            )
        }
    }
}
