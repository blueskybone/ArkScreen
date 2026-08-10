package com.blueskybone.arkscreen.ui.recruit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

class ResultAdapter(
    private val onOperatorClick: (RecruitOpe) -> Unit
) : ListAdapter<RecruitResult, ResultAdapter.ResultViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruit_result, parent, false)
        return ResultViewHolder(view, onOperatorClick)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<RecruitResult>() {
        override fun areItemsTheSame(
            oldItem: RecruitResult,
            newItem: RecruitResult
        ): Boolean {
            return oldItem.tags == newItem.tags && oldItem.rare == newItem.rare
        }

        override fun areContentsTheSame(
            oldItem: RecruitResult,
            newItem: RecruitResult
        ): Boolean {
            return oldItem == newItem
        }
    }

    class ResultViewHolder(
        itemView: View,
        private val onOperatorClick: (RecruitOpe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tagRecyclerView: RecyclerView = itemView.findViewById(R.id.rvResultTags)
        private val operatorRecyclerView: RecyclerView = itemView.findViewById(R.id.rvOperators)

        private val tagAdapter = ResultTagAdapter()
        private val operatorAdapter = OperatorChipAdapter(onOperatorClick)

        init {
            tagRecyclerView.layoutManager = FlexboxLayoutManager(itemView.context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            tagRecyclerView.adapter = tagAdapter
            tagRecyclerView.setHasFixedSize(false)
            tagRecyclerView.isNestedScrollingEnabled = false

            operatorRecyclerView.layoutManager = FlexboxLayoutManager(itemView.context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            operatorRecyclerView.adapter = operatorAdapter
            operatorRecyclerView.setHasFixedSize(false)
            operatorRecyclerView.isNestedScrollingEnabled = false
        }

        fun bind(result: RecruitResult) {
            val tags = buildList {
                add("${result.rare}★")
                addAll(result.tags)
            }
            tagAdapter.submitList(tags)
            operatorAdapter.submitList(result.operators)
        }
    }
}