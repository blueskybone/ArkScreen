package com.blueskybone.arkscreen.ui.recruit

import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.DialogRecruitOperatorBinding
import com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe
import com.blueskybone.arkscreen.ui.character.bindAvatarView
import com.blueskybone.arkscreen.ui.character.rarityColorMap
import com.blueskybone.arkscreen.ui.common.openLink
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.mp.KoinPlatform
import java.net.URLEncoder

class OperatorDialog(
    private val context: Context
) {
    private val prefManager: SettingPrefManager by KoinPlatform.getKoin().inject()

    fun show(operator: RecruitOpe) {
        val binding = DialogRecruitOperatorBinding.inflate(LayoutInflater.from(context))
        val rarityColor = ContextCompat.getColor(
            context,
            rarityColorMap[operator.rare] ?: R.color.rare_1,
        )

        binding.Name.text = operator.name
        binding.Rarity.text = "★".repeat(operator.rare)
        binding.Rarity.setTextColor(rarityColor)
        binding.Avatar.background = ContextCompat.getDrawable(
            context,
            rarityColorMap[operator.rare] ?: R.color.rare_1,
        )
        bindAvatarView(binding.Avatar, operator.skinId)

        operator.tags.forEach { tag ->
            val chip = LayoutInflater.from(context).inflate(
                R.layout.chip_recruit_metadata,
                binding.Tags,
                false,
            ) as Chip
            chip.text = tag
            binding.Tags.addView(chip)
        }

        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setNegativeButton(R.string.close, null)
            .setPositiveButton(R.string.goto_PRTS) { _, _ ->
                val url = "https://prts.wiki/w/" + URLEncoder.encode(operator.name, "UTF-8")
                openLink(context, url, prefManager)
            }
            .show()
    }
}
