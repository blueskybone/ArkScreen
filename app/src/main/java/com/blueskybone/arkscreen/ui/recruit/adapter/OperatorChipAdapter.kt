package com.blueskybone.arkscreen.ui.recruit.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe
import com.google.android.material.chip.Chip

class OperatorChipAdapter(
    private val onClick: (RecruitOpe) -> Unit
) : ListAdapter<RecruitOpe, OperatorChipAdapter.OperatorChipViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperatorChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruit_ope_chip, parent, false)
        return OperatorChipViewHolder(view as Chip, onClick)
    }

    override fun onBindViewHolder(holder: OperatorChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OperatorChipViewHolder(
        private val chip: Chip,
        private val onClick: (RecruitOpe) -> Unit
    ) : RecyclerView.ViewHolder(chip) {

        fun bind(operator: RecruitOpe) {
            chip.text = operator.name
            chip.isCheckable = false
            chip.isChecked = false
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
            applyRarityColors(operator.rare)
            chip.setOnClickListener { onClick(operator) }
        }

        private fun applyRarityColors(rarity: Int) {
            val (lineColor, surfaceColor) = rarityColors(rarity)
            val context = chip.context

            chip.setTextColor(ContextCompat.getColor(context, lineColor))
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(context, lineColor)
            )
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(context, surfaceColor)
            )
        }

        private fun rarityColors(rarity: Int): RarityColors = when (rarity) {
            6 -> RarityColors(R.color.rare_6_line, R.color.rare_6_surface)
            5 -> RarityColors(R.color.rare_5_line, R.color.rare_5_surface)
            4 -> RarityColors(R.color.rare_4_line, R.color.rare_4_surface)
            3 -> RarityColors(R.color.rare_3_line, R.color.rare_3_surface)
            2 -> RarityColors(R.color.rare_2_line, R.color.rare_2_surface)
            else -> RarityColors(R.color.rare_1_line, R.color.rare_1_surface)
        }
    }

    private data class RarityColors(
        @ColorRes val line: Int,
        @ColorRes val surface: Int,
    )

    private class DiffCallback : DiffUtil.ItemCallback<RecruitOpe>() {
        override fun areItemsTheSame(
            oldItem: RecruitOpe,
            newItem: RecruitOpe
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: RecruitOpe,
            newItem: RecruitOpe
        ): Boolean {
            return oldItem == newItem
        }
    }
}
