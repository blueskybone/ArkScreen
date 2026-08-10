package com.blueskybone.arkscreen.ui.recruit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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
            chip.setOnClickListener { onClick(operator) }
        }
    }

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