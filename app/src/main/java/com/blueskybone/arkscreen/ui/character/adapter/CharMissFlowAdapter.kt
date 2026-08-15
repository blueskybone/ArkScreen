package com.blueskybone.arkscreen.ui.character.adapter

import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.DialogCharMissBinding
import com.blueskybone.arkscreen.databinding.ItemCharMissBinding
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.ui.character.bindAvatarView
import com.blueskybone.arkscreen.ui.character.profIconMap
import com.blueskybone.arkscreen.ui.character.rarityColorMap
import com.blueskybone.arkscreen.ui.common.openLink
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nex3z.flowlayout.FlowLayout
import org.koin.mp.KoinPlatform
import java.net.URLEncoder

class CharMissFlowAdapter(
    private val context: Context,
    private val flowLayout: FlowLayout
) {

    private val prefManager: SettingPrefManager by KoinPlatform.getKoin().inject()
    fun submitList(newList: List<Operator>) {
        flowLayout.removeAllViews()
        newList.forEach { operator ->
            addOperatorView(operator, flowLayout)
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
        binding.Avatar.background = draw
        bindAvatarView(binding.Avatar, item.skinId)
        binding.setUpListener(item)
    }

    private fun ItemCharMissBinding.setUpListener(item: Operator) {
        this.root.setOnClickListener {
            val binding = DialogCharMissBinding.inflate(
                LayoutInflater.from(context),
                flowLayout,
                false
            )
            binding.Name.text = item.name
            binding.Rarity.text = "★".repeat(item.rarity + 1)
            binding.Profession.setImageResource(
                profIconMap[item.profession] ?: R.drawable.skill_icon_default
            )
            val colorId = rarityColorMap[item.rarity + 1] ?: R.color.red
            val draw = ContextCompat.getDrawable(context, colorId)

            binding.Avatar.background = draw
            bindAvatarView(binding.Avatar, item.skinId)

            binding.PRTSlink.setOnClickListener {
                val url = "https://prts.wiki/w/" + URLEncoder.encode(item.name, "UTF-8")
                openLink(context, url, prefManager)
            }

            MaterialAlertDialogBuilder(context)
                .setView(binding.root)
                .show()
        }
    }
}
