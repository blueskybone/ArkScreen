package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemCharMissBinding
import com.blueskybone.arkscreen.playerinfo.Operator
import com.blueskybone.arkscreen.playerinfo.bindAvatarView
import com.blueskybone.arkscreen.playerinfo.profIconMap
import com.blueskybone.arkscreen.playerinfo.rarityColorMap
import com.nex3z.flowlayout.FlowLayout

class CharMissFlowAdapter(
    private val context: Context,
    private val flowLayout: FlowLayout
) {

    private var operators: List<Operator> = emptyList()

    fun submitList(newList: List<Operator>) {
        operators = newList
        flowLayout.removeAllViews()
        newList.forEach { operator ->
            addOperatorView(operator,flowLayout)
        }
    }

    private fun addOperatorView(operator: Operator, flowLayout: FlowLayout) {
        val binding = ItemCharMissBinding.inflate(
            LayoutInflater.from(context),
            flowLayout,
            false
        )
        bindView(binding, operator)
        flowLayout.addView(binding.root)
    }

    private fun bindView(binding: ItemCharMissBinding, item: Operator) {
        binding.Profession.setImageResource(
            profIconMap[item.profession] ?: R.drawable.skill_icon_default
        )
        val colorId = rarityColorMap[item.rarity + 1] ?: R.color.red
        val draw = ContextCompat.getDrawable(context, colorId)
        binding.Avatar.setBackgroundDrawable(draw)
        bindAvatarView(binding.Avatar, item.skinId)
    }
}