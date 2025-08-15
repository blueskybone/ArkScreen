package com.blueskybone.arkscreen.task.recruit


import com.blueskybone.arkscreen.RecruitDb
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileInputStream

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
class RecruitManager {

    private lateinit var recruitDatabase: RecruitDatabase

    private object Holder {
        val INSTANCE = RecruitManager()
    }

    companion object {
        val instance: RecruitManager by lazy { Holder.INSTANCE }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RecruitDb.updateFile()
            } catch (e: Exception) {
                Timber.e("RecruitDb update failed: ${e.message}")
            }
            val om = ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            recruitDatabase = withContext(Dispatchers.IO) {
                om.readValue(
                    FileInputStream(RecruitDb.getFilePath()),
                    RecruitDatabase::class.java
                )
            }
        }
    }

    fun getRecruitResult(tags: List<String>, filter: Boolean = false): RecruitResult {
        return RecruitResult(tags, queryForRecruit(tags, filter))
    }


    //filter: 是否过滤2,3星干员
    private fun queryForRecruit(
        tags: List<String>,
        filter: Boolean = false
    ): MutableList<Operator> {
        val opeLists = mutableListOf<Operator>()
        if (tags.isNotEmpty()) {
            if (filter) {
                if (tags.contains("新手")) return opeLists
                if (lowListFilter(tags)) return opeLists
            }
            if (tags.contains("高级资深干员")) {
                opeLists.addAll(highOpeList(tags))
            }
            opeLists.addAll(opeList(tags))
            opeLists.addAll(lowOpeList(tags))
            opeLists.addAll(robotOpeList(tags))
        }
        return opeLists
    }

    private fun highOpeList(tags: List<String>): List<Operator> {
        val opeList = mutableListOf<Operator>()
        for (ope in recruitDatabase.operatorHighList) {
            if (ope.tag.containsAll(tags)) {
                opeList.add(Operator(ope.name, ope.star, ope.tag, ope.skin))
            }
        }
        return opeList
    }

    private fun opeList(tags: List<String>): List<Operator> {
        val opeList = mutableListOf<Operator>()
        for (ope in recruitDatabase.operatorList) {
            if (ope.tag.containsAll(tags)) {
                opeList.add(Operator(ope.name, ope.star, ope.tag, ope.skin))
            }
        }
        return opeList
    }

    private fun lowOpeList(tags: List<String>): List<Operator> {
        val opeList = mutableListOf<Operator>()
        for (ope in recruitDatabase.operatorLowList) {
            if (ope.tag.containsAll(tags)) {
                opeList.add(Operator(ope.name, ope.star, ope.tag, ope.skin))
            }
        }
        return opeList
    }

    private fun robotOpeList(tags: List<String>): List<Operator> {
        val opeList = mutableListOf<Operator>()
        for (ope in recruitDatabase.operatorRobotList) {
            if (ope.tag.containsAll(tags)) {
                opeList.add(Operator(ope.name, ope.star, ope.tag, ope.skin))
            }
        }
        return opeList
    }

    private fun lowListFilter(tags: List<String>): Boolean {
        for (ope in recruitDatabase.operatorLowList) {
            if (ope.tag.containsAll(tags)) return true
        }
        return false
    }


    data class RecruitResult(
        val tags: List<String> = listOf(),
        val operators: MutableList<Operator> = mutableListOf(),
        private var isSort: Boolean = false,
        var rare: Int = 0
    ) : Comparable<RecruitResult> {

        fun sort(downTo: Boolean = true) {
            if (downTo) operators.sortByDescending { operator -> operator.rare }
            else operators.sortBy { operator -> operator.rare }
            rare = rare()
            isSort = true
        }

        private fun rare(): Int {
            var r = 1
            for (operator in operators.toList()) {
                when (operator.rare) {
                    6 -> return 6
                    5 -> r = 5
                    4 -> r = 4
                    3 -> return 3
                    2 -> return 2
                }
            }
            return r
        }

        //稀有度 > tag数目 > 人数
        override operator fun compareTo(other: RecruitResult): Int {
            if (!isSort) this.sort()
            if (!other.isSort) other.sort()
            return if (this.rare > other.rare) {
                -1
            } else if (this.rare < other.rare) {
                1
            } else {
                if (this.rare == 6 || this.rare == 5 || this.rare == 1) {
                    return if (this.operators.size > other.operators.size) {
                        1
                    } else if (this.operators.size < other.operators.size) {
                        -1
                    } else {
                        if (this.tags.size > other.tags.size) {
                            1
                        } else -1
                    }
                }
                if (this.operators.size < other.operators.size) {
                    -1
                } else if (this.operators.size > other.operators.size) {
                    1
                } else {
                    if (this.tags.size < other.tags.size) {
                        -1
                    } else 1
                }
            }
        }
    }

    data class Operator(
        val name: String,
        val rare: Int,
        val tags: List<String>,
        val skin: String
    ) {
        operator fun compareTo(o: Operator): Int {
            return if (this.rare > o.rare) {
                -1
            } else if (this.rare < o.rare) {
                1
            } else 1
        }
    }

}