package com.blueskybone.arkscreen.ui.common.adapter.paging

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
abstract class PagingAdapter<T, V : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<V>() {

    private val visibleItems = mutableListOf<T>()
    val currentList: List<T> get() = visibleItems
    private var allList: List<T> = listOf()

    private var totalPage: Int = 0
    private var currentPage: Int = 1

    abstract val PAGE_SIZE: Int

    protected open fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
        oldItem == newItem

    protected open fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        oldItem == newItem

    val hasMore: Boolean
        get() = currentPage < totalPage

    override fun onBindViewHolder(holder: V, position: Int) {
        if (position < visibleItems.size) {
            bindViewHolder(holder, visibleItems[position])
        }
    }

    abstract fun bindViewHolder(holder: V, item: T)

    fun loadMoreData() {
        if (!hasMore) return

        val startPos = currentPage * PAGE_SIZE
        val endPos = if ((totalPage - currentPage) == 1) {
            allList.size
        } else {
            (currentPage + 1) * PAGE_SIZE
        }

        if (startPos >= allList.size) return

        val subList = allList.subList(startPos, min(endPos, allList.size))
        val insertPosition = visibleItems.size
        visibleItems.addAll(subList)
        notifyItemRangeInserted(insertPosition, subList.size)
        currentPage++
    }

    fun refreshData(newList: List<T>) {
        val oldVisibleItems = visibleItems.toList()
        allList = newList
        currentPage = 1
        totalPage = if (allList.isEmpty()) {
            0
        } else {
            (allList.size - 1) / PAGE_SIZE + 1
        }

        visibleItems.clear()
        loadFirstPage()
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldVisibleItems.size
            override fun getNewListSize(): Int = visibleItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                this@PagingAdapter.areItemsTheSame(
                    oldVisibleItems[oldItemPosition],
                    visibleItems[newItemPosition],
                )

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                this@PagingAdapter.areContentsTheSame(
                    oldVisibleItems[oldItemPosition],
                    visibleItems[newItemPosition],
                )
        }).dispatchUpdatesTo(this)
    }

    private fun loadFirstPage() {
        if (allList.isEmpty()) return

        val endPos = min(PAGE_SIZE, allList.size)
        val subList = allList.subList(0, endPos)
        visibleItems.addAll(subList)
        currentPage = 1
    }

    override fun getItemCount(): Int = visibleItems.size
}
