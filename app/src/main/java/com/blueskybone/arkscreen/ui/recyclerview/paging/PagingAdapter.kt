package com.blueskybone.arkscreen.ui.recyclerview.paging

import androidx.recyclerview.widget.RecyclerView

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
abstract class PagingAdapter<T, V : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<V>() {

    lateinit var currentList: MutableList<T>
    private lateinit var allList: List<T>

    private var totalPage: Int = 0
    private var currentPage: Int = 1

    abstract val PAGE_SIZE: Int

    fun loadMoreData() {
        if (currentPage == totalPage) return
        if ((totalPage - currentPage) == 1) {

            val subList = allList.subList(currentPage * PAGE_SIZE, allList.size)
            println(subList.size)
            currentList.addAll(subList)
            notifyItemRangeInserted(
                currentPage * PAGE_SIZE,
                allList.size - currentPage * PAGE_SIZE
            )
            currentPage++
        } else {
            val subList = allList.subList(
                currentPage * PAGE_SIZE,
                (currentPage + 1) * PAGE_SIZE
            )
            currentList.addAll(subList)
            notifyItemRangeInserted(currentPage * PAGE_SIZE, PAGE_SIZE)
            currentPage++
        }
    }

    fun refreshData(newList: List<T>) {
        allList = newList
        currentPage = 0
        totalPage = if (allList.isEmpty()) {
            0
        } else {
            allList.size / PAGE_SIZE + 1
        }
        loadFirstPage()
        notifyDataSetChanged()
    }

    private fun loadFirstPage() {
        currentList = ArrayList()
        if (currentPage == totalPage) return
        if (currentPage == totalPage - 1) {
            val subList = allList.subList(currentPage * PAGE_SIZE, allList.size)
            currentList.addAll(subList)
            currentPage++
        } else {
            val subList = allList.subList(
                currentPage * PAGE_SIZE,
                (currentPage + 1) * PAGE_SIZE
            )
            currentList.addAll(subList)
            currentPage++
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

}