package com.blueskybone.arkscreen.ui.recyclerview.paging

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
//abstract class PagingAdapter<T, V : RecyclerView.ViewHolder> :
//    RecyclerView.Adapter<V>() {
//
//    lateinit var currentList: MutableList<T>
//    private lateinit var allList: List<T>
//
//    private var totalPage: Int = 0
//    private var currentPage: Int = 1
//
//    abstract val PAGE_SIZE: Int
//
//    fun loadMoreData() {
//        if (currentPage == totalPage) return
//        if ((totalPage - currentPage) == 1) {
//
//            val subList = allList.subList(currentPage * PAGE_SIZE, allList.size)
//            println(subList.size)
//            currentList.addAll(subList)
//            notifyItemRangeInserted(
//                currentPage * PAGE_SIZE,
//                allList.size - currentPage * PAGE_SIZE
//            )
//            currentPage++
//        } else {
//            val subList = allList.subList(
//                currentPage * PAGE_SIZE,
//                (currentPage + 1) * PAGE_SIZE
//            )
//            currentList.addAll(subList)
//            notifyItemRangeInserted(currentPage * PAGE_SIZE, PAGE_SIZE)
//            currentPage++
//        }
//    }
//
//    fun refreshData(newList: List<T>) {
//        allList = newList
//        currentPage = 0
//        totalPage = if (allList.isEmpty()) {
//            0
//        } else {
//            allList.size / PAGE_SIZE + 1
//        }
//        loadFirstPage()
//        notifyDataSetChanged()
//    }
//
//    private fun loadFirstPage() {
//        println("loadFirstPage")
//        currentList = ArrayList()
//        println("currentList = ArrayList()")
//        if (currentPage == totalPage) return
//        if (currentPage == totalPage - 1) {
//            val subList = allList.subList(currentPage * PAGE_SIZE, allList.size)
//            currentList.addAll(subList)
//            currentPage++
//        } else {
//            val subList = allList.subList(
//                currentPage * PAGE_SIZE,
//                (currentPage + 1) * PAGE_SIZE
//            )
//            currentList.addAll(subList)
//            currentPage++
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return currentList.size
//    }
//
//}

abstract class PagingAdapter<T, V : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<V>() {

    // 立即初始化为空列表，避免未初始化状态
    var currentList: MutableList<T> = mutableListOf()
    private var allList: List<T> = listOf()

    private var totalPage: Int = 0
    private var currentPage: Int = 1

    abstract val PAGE_SIZE: Int

    override fun onBindViewHolder(holder: V, position: Int) {
        // 添加安全检查
        if (position < currentList.size) {
            bindViewHolder(holder, currentList[position])
        }
    }

    // 抽象方法让子类实现具体的绑定逻辑
    abstract fun bindViewHolder(holder: V, item: T)

    fun loadMoreData() {
        if (currentPage == totalPage) return

        val startPos = currentPage * PAGE_SIZE
        val endPos = if ((totalPage - currentPage) == 1) {
            allList.size
        } else {
            (currentPage + 1) * PAGE_SIZE
        }

        if (startPos >= allList.size) return

        val subList = allList.subList(startPos, min(endPos, allList.size))
        val insertPosition = currentList.size
        currentList.addAll(subList)
        notifyItemRangeInserted(insertPosition, subList.size)
        currentPage++
    }

    fun refreshData(newList: List<T>) {
        allList = newList
        currentPage = 1
        totalPage = if (allList.isEmpty()) {
            0
        } else {
            (allList.size - 1) / PAGE_SIZE + 1
        }

        currentList.clear()
        loadFirstPage()
        notifyDataSetChanged()
    }

    private fun loadFirstPage() {
        if (allList.isEmpty()) return

        val endPos = min(PAGE_SIZE, allList.size)
        val subList = allList.subList(0, endPos)
        currentList.addAll(subList)
        currentPage = 1
    }

    override fun getItemCount(): Int = currentList.size
}