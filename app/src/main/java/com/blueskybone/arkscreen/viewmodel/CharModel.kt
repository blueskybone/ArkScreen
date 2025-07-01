package com.blueskybone.arkscreen.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.CharAllMap
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.Progress
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getGameInfoConnectionTaskTest
import com.blueskybone.arkscreen.playerinfo.Operator
import com.blueskybone.arkscreen.playerinfo.compareOperators
import com.blueskybone.arkscreen.playerinfo.getOperatorData
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.util.readFileAsJsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import java.io.IOException


/**
 *   Created by blueskybone
 *   Date: 2025/1/17
 */

class CharModel : ViewModel() {
    private val prefManager: PrefManager by getKoin().inject()

    val importingBackup = MutableLiveData<Progress>()
    val exportingBackup = MutableLiveData<Progress>()

    private val _uiState = MutableLiveData<DataUiState>()
    val uiState: LiveData<DataUiState> get() = _uiState

    private val _filterProf = MutableLiveData<String>()
    val filterProf: LiveData<String> get() = _filterProf

    private val _filterRarity = MutableLiveData<String>()
    val filterRarity: LiveData<String> get() = _filterRarity

    private val _filterLevel = MutableLiveData<String>()
    val filterLevel: LiveData<String> get() = _filterLevel

    private val _charsList = MutableLiveData<List<Operator>>()
    val charsList: LiveData<List<Operator>> get() = _charsList

    private val _charsNotOwnList = MutableLiveData<List<Operator>>()
    val charsNotOwnList: LiveData<List<Operator>> get() = _charsNotOwnList

    private lateinit var charList: List<Operator>
    private lateinit var charNotOwnList: List<Operator>

    private var totalSize = 0
    private var totalMissSize = 0
    private var totalEvo2Size = 0       //精二
    private var totalSpecialSize = 0    //专精
    private var totalStage3Size = 0     //模组数
    private var rarity6Size = 0
    private var rarity6MissSize = 0
    private var rarity6Evo2Size = 0
    private var rarity6SpecialSize = 0
    private var rarity6Stage3Size = 0
    private var rarity5Size = 0
    private var rarity5MissSize = 0
    private var rarity5Evo2Size = 0
    private var rarity5SpecialSize = 0
    private var rarity5Stage3Size = 0
    private var rarity4Size = 0
    private var rarity4MissSize = 0
    private var rarity4Evo2Size = 0
    private var rarity4SpecialSize = 0
    private var rarity4Stage3Size = 0


    fun generateStatisticMarkDownText(): String {
        val builder = StringBuffer()
        builder.append(
            "### 全部干员\n\n " +
                    "* 招募干员数  $totalSize/${totalSize + totalMissSize}\n" +
                    "* 精二干员数  $totalEvo2Size\n" +
                    "* 专三技能数  $totalSpecialSize\n" +
                    "* Stage3模组数  $totalStage3Size\n" +
                    "### 六星干员  \n\n" +
                    "* 招募干员数  $rarity6Size/${rarity6Size + rarity6MissSize}\n" +
                    "* 精二干员数  $rarity6Evo2Size\n" +
                    "* 专三技能数  $rarity6SpecialSize\n" +
                    "* Stage3模组数  $rarity6Stage3Size\n" +
                    "### 五星干员  \n\n" +
                    "* 招募干员数  $rarity5Size/${rarity5Size + rarity5MissSize}\n" +
                    "* 精二干员数  $rarity5Evo2Size\n" +
                    "* 专三技能数  $rarity5SpecialSize\n" +
                    "* Stage3模组数  $rarity5Stage3Size\n" +
                    "### 四星干员  \n\n" +
                    "* 招募干员数  $rarity4Size/${rarity4Size + rarity4MissSize}\n" +
                    "* 精二干员数  $rarity4Evo2Size\n" +
                    "* 专三技能数  $rarity4SpecialSize\n" +
                    "* Stage3模组数 $rarity4Stage3Size \n"
        )
        return builder.toString()
    }

    init {
        viewModelScope.launch {
            _filterProf.value = ProfFilter.defaultValue
            _filterRarity.value = RarityFilter.defaultValue
            _filterLevel.value = LevelFilter.defaultValue
            _uiState.value = DataUiState.Loading("LOADING...")
            withContext(Dispatchers.IO) {
                loadCharAssets()
            }
        }
    }

    private suspend fun loadCharAssets() {
        _uiState.postValue(DataUiState.Loading("LOADING..."))
        val accountSk = prefManager.baseAccountSk.get()

        //TODO: bad practice.
        if (accountSk.uid == "") {
            _uiState.postValue(DataUiState.Error("未登录"))
            return
        }
        try {
            charList = getCharsData(accountSk)
            _charsList.postValue(charList)
            charNotOwnList = getCharsNotOwn()
            _charsNotOwnList.postValue(charNotOwnList)
            statistic()
            _uiState.postValue(DataUiState.Success(""))

        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.postValue(DataUiState.Error(e.message ?: "LOADING FAILED"))
        }
    }

    private fun statistic() {
        charsList.value?.let {
            totalSize = it.size
            val rarity6list = it.filter { chars -> chars.rarity == 5 }
            val rarity5list = it.filter { chars -> chars.rarity == 4 }
            val rarity4list = it.filter { chars -> chars.rarity == 3 }
            rarity6Size = rarity6list.size
            rarity5Size = rarity5list.size
            rarity4Size = rarity4list.size
            rarity6Evo2Size = rarity6list.filter { chars -> chars.evolvePhase == 2 }.size
            rarity5Evo2Size = rarity5list.filter { chars -> chars.evolvePhase == 2 }.size
            rarity4Evo2Size = rarity4list.filter { chars -> chars.evolvePhase == 2 }.size
            rarity6SpecialSize = rarity6list.flatMap { char -> char.skills }
                .count { skill -> skill.specializeLevel == 3 }
            rarity5SpecialSize = rarity5list.flatMap { char -> char.skills }
                .count { skill -> skill.specializeLevel == 3 }
            rarity4SpecialSize = rarity4list.flatMap { char -> char.skills }
                .count { skill -> skill.specializeLevel == 3 }
            rarity6Stage3Size = rarity6list.flatMap { char -> char.equips }
                .count { equip -> equip.stage == 3 }
            rarity5Stage3Size = rarity5list.flatMap { char -> char.skills }
                .count { skill -> skill.specializeLevel == 3 }
            rarity4Stage3Size = rarity4list.flatMap { char -> char.skills }
                .count { skill -> skill.specializeLevel == 3 }
            totalEvo2Size = rarity6Evo2Size + rarity5Evo2Size + rarity4Evo2Size
            totalSpecialSize = rarity6SpecialSize + rarity5SpecialSize + rarity4SpecialSize
            totalStage3Size = rarity6Stage3Size + rarity5Stage3Size + rarity4Stage3Size
        }
        charsNotOwnList.value?.let {
            totalMissSize = it.size
            rarity6MissSize = it.filter { chars -> chars.rarity == 5 }.size
            rarity5MissSize = it.filter { chars -> chars.rarity == 4 }.size
            rarity4MissSize = it.filter { chars -> chars.rarity == 3 }.size
        }
    }

    private fun getCharsNotOwn(): List<Operator> {
        executeAsync {
            try {
                CharAllMap.updateFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val charsAllNode = readFileAsJsonNode(CharAllMap.getFilePath())["charInfoMap"]
        if(charsAllNode == null) {
            println("charsAllNode == null")
        }
        for (item in charList) {
            (charsAllNode as ObjectNode).remove(item.charId)
        }
        val list = ArrayList<Operator>()
        val charNotOwnIds = charsAllNode.fieldNames()
        for (charId in charNotOwnIds) {
            val charInfo = charsAllNode[charId]
            val operator = Operator()
            operator.charId = charId
            operator.skinId = "$charId#1"
            operator.name = charInfo["name"].asText()
            operator.rarity = charInfo["rarity"].asInt()
            operator.profession = charInfo["profession"].asText()
            list.add(operator)
        }
        return list.sortedWith(compareOperators).toMutableList() as ArrayList<Operator>
    }



    private suspend fun getCharsData(account:AccountSk): List<Operator> {
        val response = getGameInfoConnectionTaskTest(account)
        if (!response.isSuccessful) throw Exception("!response.isSuccessful")
        response.body() ?: throw Exception("response empty")
        return getOperatorData(response.body()!!)

//        response.body()?.use{ body ->
//            val gzip = GZIPInputStream(body.byteStream())
//            val result = getOpeData(ObjectMapper().readTree(gzip))
//            return result
//        }?: throw Exception("response body is empty")
    }


    private fun applyFilter(): List<Operator> {
        val prof = filterProf.value
        val rarity = filterRarity.value
        val level = filterLevel.value

        val list1 = when (prof) {
            ProfFilter.ALL -> charList
            else -> charList.filter { chars -> chars.profession == prof }
        }
        val list2 = when (rarity) {
            RarityFilter.RARITY_1_3 -> list1.filter { chars -> chars.rarity == 0 || chars.rarity == 1 || chars.rarity == 2 }
            RarityFilter.RARITY_4 -> list1.filter { chars -> chars.rarity == 3 }
            RarityFilter.RARITY_5 -> list1.filter { chars -> chars.rarity == 4 }
            RarityFilter.RARITY_6 -> list1.filter { chars -> chars.rarity == 5 }
            else -> list1
        }
        val list3 = when (level) {
            LevelFilter.EVOLVE_0 -> list2.filter { chars -> chars.evolvePhase == 0 }
            LevelFilter.EVOLVE_1 -> list2.filter { chars -> chars.evolvePhase == 1 }
            LevelFilter.EVOLVE_2 -> list2.filter { chars -> chars.evolvePhase == 2 }
            else -> list2
        }
        return list3
    }

    fun setFilter(filter: Filter, item: String, context: Context) {
        when (filter) {
            is ProfFilter -> {
                _filterProf.value = ProfFilter.getValue(item, context)
            }

            is RarityFilter -> {
                _filterRarity.value = RarityFilter.getValue(item, context)
            }

            is LevelFilter -> {
                _filterLevel.value = LevelFilter.getValue(item, context)
            }
        }
        executeAsync {
            _charsList.postValue(applyFilter())
        }
    }

    sealed interface Filter {
        val defaultValue: String
        fun getEntryValues(): Array<String>
        fun getEntries(context: Context): Array<String>
    }

    data object ProfFilter : Filter {
        const val ALL = "ALL"
        private const val PIONEER = "PIONEER"
        private const val WARRIOR = "WARRIOR"
        private const val TANK = "TANK"
        private const val SNIPER = "SNIPER"
        private const val CASTER = "CASTER"
        private const val MEDIC = "MEDIC"
        private const val SUPPORT = "SUPPORT"
        private const val SPECIAL = "SPECIAL"

        override val defaultValue = ALL

        override fun getEntryValues() = arrayOf(
            ALL, PIONEER, WARRIOR, TANK, SNIPER, CASTER, MEDIC, SUPPORT, SPECIAL
        )

        override fun getEntries(context: Context): Array<String> {
            return context.resources.getStringArray(R.array.profession)
        }

        fun getValue(item: String, context: Context): String {
            val index = this.getEntries(context).indexOf(item)
            return this.getEntryValues()[index]
        }
    }

    data object RarityFilter : Filter {
        private const val ALL = "ALL"
        const val RARITY_1_3 = "RARITY_1_3"
        const val RARITY_4 = "RARITY_4"
        const val RARITY_5 = "RARITY_5"
        const val RARITY_6 = "RARITY_6"

        override val defaultValue = ALL

        override fun getEntryValues() = arrayOf(
            ALL, RARITY_1_3, RARITY_4, RARITY_5, RARITY_6
        )

        override fun getEntries(context: Context): Array<String> {
            return context.resources.getStringArray(R.array.rarity)
        }

        fun getValue(item: String, context: Context): String {
            val index = this.getEntries(context).indexOf(item)
            return this.getEntryValues()[index]
        }

    }

    data object LevelFilter : Filter {
        private const val ALL = "ALL"
        const val EVOLVE_0 = "EVOLVE_0"
        const val EVOLVE_1 = "EVOLVE_1"
        const val EVOLVE_2 = "EVOLVE_2"

        override val defaultValue = ALL

        override fun getEntryValues() = arrayOf(
            ALL, EVOLVE_0, EVOLVE_1, EVOLVE_2
        )

        override fun getEntries(context: Context): Array<String> {
            return context.resources.getStringArray(R.array.level)
        }

        fun getValue(item: String, context: Context): String {
            val index = this.getEntries(context).indexOf(item)
            return this.getEntryValues()[index]
        }

    }

    private fun executeAsync(function: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { function() }
    }

    //TXT
    private fun exportForTxt() {
        //char_name,rarity,level,evelop,potential, main-rank, 0,0, equip1,2,3
    }

    /*
    * char name, rarity, profession, subProfession, level, evol, potnetialRank, mainskill, favorPercent,
* gainTime, speciallevel@speciallevel, name-level@name-level@name-level
    * */
    fun exportTxt(uri: Uri) {
        viewModelScope.launch {
            exportingBackup.value = Progress(true, 0, 0, true)
            withContext(Dispatchers.IO) {
                try {
                    val content = StringBuilder()
                    content.append(_charsList.value?.joinToString("\n") { data ->
                        "${data.name},${data.rarity},${data.profession}," +
                                "${data.subProfessionId},${data.level},${data.evolvePhase}," +
                                "${data.potentialRank},${data.mainSkillLvl},${data.favorPercent}," +
                                "${data.gainTime}," + data.skills.joinToString("@") { it.specializeLevel.toString() } + "," + data.equips.joinToString(
                            "@"
                        ) { it.typeName2 + "-" + it.stage }
                    })
                    APP.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toString().toByteArray())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toaster.show("导出失败：" + e.message)
                }
            }
            exportingBackup.value = Progress(false, 0, 0, false)
            Toaster.show("导出完成")
        }
    }
}