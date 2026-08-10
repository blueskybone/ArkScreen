package com.blueskybone.arkscreen.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.Progress
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.pullNewRecords
import com.blueskybone.arkscreen.playerinfo.Gachas
import com.blueskybone.arkscreen.playerinfo.Records
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.room.Gacha
import com.blueskybone.arkscreen.room.GachaWithNum
import com.blueskybone.arkscreen.ui.model.GachaInfo
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStrYMD
import com.blueskybone.arkscreen.util.toCate
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 *   Created by blueskybone
 *   Date: 2025/2/1
 */
class GachaModel : ViewModel() {
    private val prefManager: PrefManager by getKoin().inject()

    private val _uiState = MutableLiveData<DataUiState>()
    val uiState: LiveData<DataUiState> get() = _uiState

    val importingBackup = MutableLiveData<Progress>()
    val exportingBackup = MutableLiveData<Progress>()

    private lateinit var curAccount: AccountGc

    private val database = ArkDatabase.getDatabase(APP)
    private val gachaDao = database.getGachaDao()

    private val _gachaData = MutableLiveData<List<Gachas>>()
    val gachaData: LiveData<List<Gachas>> get() = _gachaData

    private val _gachaRecords = MutableLiveData<List<Gacha>>()

    private val _gachaRecordsCount = MutableLiveData<List<GachaWithNum>>()
    val gachaRecordsCount: LiveData<List<GachaWithNum>> get() = _gachaRecordsCount

    private val _gachaInfoList = MutableLiveData<List<GachaInfo>>()
    val gachaInfoList: LiveData<List<GachaInfo>> get() = _gachaInfoList

    private var gachaRecordsList: List<GachaWithNum> = mutableListOf()

    var poolCountNormal = 0
    var poolCountFes = 0
    var poolCountCore = 0
    var finalCountSum = 0
    var rarity6Count = 0

    var id = 0
    var dateRange = "-"

    init {
        initialize()
    }

    private fun initValue() {
        poolCountNormal = 0
        poolCountFes = 0
        poolCountCore = 0
        finalCountSum = 0
        rarity6Count = 0
        id = 0
    }


    fun initialize() {
        initValue()
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("加载中...")
            curAccount = prefManager.baseAccountGc.get()
            if (curAccount.uid == "") {
                _uiState.value = DataUiState.Error("请在 卡池账号管理 添加账号")
                return@launch
            }
            withContext(Dispatchers.IO) {
                try {
                    val listLocal = loadLocalRecords(curAccount)
                    val lastTs =
                        if (listLocal.isEmpty()) 0L else listLocal.last().ts   //获取最后一条历史时间避免每次pull全部数据
                    val listNewPull = pullRecords(curAccount, lastTs)
                    gachaDao.insert(listNewPull)
                    val records = loadLocalRecords(curAccount)
                    gachaRecordsList = processGachaCount(records)
                    _gachaRecords.postValue(records.sortByTsAndPosDescending())
                    _gachaRecordsCount.postValue(gachaRecordsList)
                    _gachaData.postValue(convertRecordsToList(records))
                    _gachaInfoList.postValue(processGachaInfo(records))
                    _uiState.postValue(DataUiState.Success(""))
                } catch (e: Exception) {
                    Timber.e("加载失败：${e.message}")
                    _uiState.postValue(DataUiState.Error("加载失败：${e.message}"))
                }
            }
        }
    }

    private fun List<Gacha>.sortByTsAndPosDescending(): List<Gacha> {
        return sortedWith(
            compareByDescending<Gacha> { it.ts }
                .thenByDescending { it.pos }
        )
    }

    private fun List<GachaWithNum>.sortDescending(): List<GachaWithNum> {
        return sortedWith(
            compareByDescending<GachaWithNum> { it.gacha.ts }
                .thenByDescending { it.gacha.pos }
        )
    }

    private suspend fun loadLocalRecords(account: AccountGc): List<Gacha> {
        return gachaDao.getByUid(account.uid)
    }

    private suspend fun pullRecords(account: AccountGc, lastTs: Long): List<Gacha> {
        return pullNewRecords(account, lastTs)
    }

    private fun convertRecordsToList(recordsDb: List<Gacha>): List<Gachas> {
        val data = recordsDb.sortByTsAndPosDescending()
        if (data.isEmpty()) return listOf()
        dateRange =
            getTimeStrYMD(data.last().ts / 1000) + "-" + getTimeStrYMD(data.first().ts / 1000)

        //分类
        val groupByCate = data.groupBy { it.poolCate }
        val limited = groupByCate["LIMITED"] ?: listOf()
        val normal = groupByCate["NORMAL"] ?: listOf()
        val classic = groupByCate["CLASSIC"] ?: listOf()
        if (groupByCate["UN"] != null) Toaster.show("存在未知卡池，请注意修正")
        finalCountSum = data.size

        //处理标准池
        val normalIdx = normal.withIndex()
            .filter { it.value.rarity == 5 }
            .map { it.index }
        //generate <pool, records>Map.
        val mapNormal = normal.distinctBy { it.poolId }
            .associateBy(
                keySelector = { it.poolId },
                valueTransform = { Gachas(pool = it.pool) }
            ).toMutableMap()
        rarity6Count += normalIdx.size
        processCateGachaRecords(normal, normalIdx, mapNormal)
        poolCountNormal = normalIdx.firstOrNull() ?: normal.size

        //处理中坚池
        val classicIdx = classic.withIndex()
            .filter { it.value.rarity == 5 }
            .map { it.index }
        rarity6Count += classicIdx.size
        val mapClassic = classic.distinctBy { it.poolId }
            .associateBy(
                keySelector = { it.poolId },
                valueTransform = { Gachas(pool = it.pool) }
            ).toMutableMap()

        processCateGachaRecords(classic, classicIdx, mapClassic)
        poolCountCore = classicIdx.firstOrNull() ?: classic.size
        //单独处理限定池

        val mapLimited = limited.distinctBy { it.poolId }
            .associateBy(
                keySelector = { it.poolId },
                valueTransform = { Gachas(pool = it.pool, isFes = true) }
            ).toMutableMap()
        val oneLimitGacha = limited.groupBy { it.poolId }
        oneLimitGacha.forEach { (poolId, gacha) ->
            val gachaIdx = gacha.withIndex()
                .filter { it.value.rarity == 5 }
                .map { it.index }
            rarity6Count += gachaIdx.size

            processCateGachaRecords(gacha, gachaIdx, mapLimited)
            if (poolId == limited.first().poolId) {
                poolCountFes = gachaIdx.firstOrNull() ?: gacha.size
            }
        }

        val mapAll = mapLimited + mapClassic + mapNormal
        //给卡池TS赋值用于最终结果排序
        val allPoolGacha = data.groupBy { it.poolId }
        allPoolGacha.forEach { (poolId, list) ->
            list.sortedByDescending { it.ts }
            mapAll[poolId]?.ts = list.first().ts
            mapAll[poolId]?.count = list.size
        }


        //mapAll转List
        return mapAll.values
            .toList()
            .sortedByDescending { it.ts }
    }


    //处理中坚池和标准池
    private fun processCateGachaRecords(
        gachaList: List<Gacha>,         //待处理的全部records
        filteredIndices: List<Int>,     //出货的record在records的顺序坐标
        targetMap: MutableMap<String, Gachas>   //对应的卡池Map
    ) {
        // 处理中间记录
        filteredIndices.windowed(2, 1).forEach { (currentIdx, nextIdx) ->
            val count = nextIdx - currentIdx
            val record = gachaList[currentIdx]
            targetMap[record.poolId]?.data?.add(
                Records(id++, record.charName, record.charId, record.isNew, count, record.ts)
            )
        }

        // 处理最后一条记录
        if (filteredIndices.isNotEmpty()) {
            val lastIdx = filteredIndices.last()
            val record = gachaList[lastIdx]
            val count = gachaList.size - lastIdx
            targetMap[record.poolId]?.data?.add(
                Records(id++, record.charName, record.charId, record.isNew, count, record.ts)
            )
        }
    }


    private fun processGachaInfo(recordsDb: List<Gacha>): List<GachaInfo> {
        val list = recordsDb.groupBy { it.poolId }.map { (poolId, list) ->
            val poolName = list.first().pool
            val isLimit = poolId.startsWith("LIMITED") || poolId.startsWith("LINKAGE")
            val rare6 = list.count { it.rarity == 5 }
            val rare5 = list.count { it.rarity == 4 }
            val rare4 = list.count { it.rarity == 3 }
            val rare3 = list.count { it.rarity == 2 }
            GachaInfo(poolName, poolId, isLimit, rare6, rare5, rare4, rare3)
        }

        val all = GachaInfo(
            "全部卡池",
            "ALL",
            false,
            recordsDb.count { it.rarity == 5 },
            recordsDb.count { it.rarity == 4 },
            recordsDb.count { it.rarity == 3 },
            recordsDb.count { it.rarity == 2 })
        return listOf(all) + list.reversed()
    }

    private fun processGachaCount(recordsDb: List<Gacha>): List<GachaWithNum> {
        val gachaList = mutableListOf<GachaWithNum>()
        // 先按照Gacha.poolId分类，然后在每一个list中倒序排序List<Gacha>.sortByTsAndPosDescending().reversed()。
        // 然后遍历，维护两个值countSum和countNum。具体规则：countSum每次+1；countNum每次+1,遇到rarity == 6时归零。然后创建GachaWithNum
        // 最后把所有的list再次收集起来,倒序排序返回
        recordsDb.groupBy { it.poolId }.map { (_, list) ->
            val newList = list.sortByTsAndPosDescending().reversed()
            var countSum = 0
            var countNum = 0
            for (record in newList) {
                countSum++
                countNum++
                gachaList.add(GachaWithNum(record, countNum, countSum))
                if (record.rarity == 5) countNum = 0
            }
        }
        return gachaList.sortDescending()
    }

    fun postPoolGachaList(poolId: String) {
        val list = getProcessGachaCount(gachaRecordsList, poolId)
        viewModelScope.launch {
            _gachaRecordsCount.postValue(list)
        }
    }

    private fun getProcessGachaCount(
        recordsDb: List<GachaWithNum>,
        poolId: String
    ): List<GachaWithNum> {
        //获取recordsDb中item.gacha.poolId == poolId的列表直接返回：若poolId == "ALL"直接返回recordsDb
        if (poolId == "ALL") return recordsDb
        return recordsDb.filter { it.gacha.poolId == poolId }
    }

    fun deleteRecords() {
        executeAsync {
            gachaDao.deleteByUid(curAccount.uid)
            Toaster.show("已删除所有数据")
//            _gachaData.postValue(processGachaData(curAccount))
        }
    }

    private fun executeAsync(function: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { function() }
    }


    //修正未知卡池（实验性）：用于版本更新后修复旧的被标注为UN的数据
    fun correctUnCateRecord() {
        //把数据库所有数据全部拿出来，对UN的数据进行如下的处理，然后保存回数据库，重新跑一遍初始化。
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("尝试卡池修正...")
            withContext(Dispatchers.IO) {
                val gachaList = gachaDao.getByCate("UN")
                val updatedRecords = gachaList.map { gachaEntity ->
                    // 这里根据你的业务需求更新字段
                    gachaEntity.copy(
                        poolCate = gachaEntity.poolId.toCate(),  // 尝试修复卡池
                    )
                }
                gachaDao.updateGachas(updatedRecords)
            }
        }
        initialize()
    }

    fun exportTxt(uri: Uri) {
        viewModelScope.launch {
            exportingBackup.value = Progress(true, 0, 0, true)
            withContext(Dispatchers.IO) {
                try {
                    val dataList = gachaDao.getByUid(curAccount.uid).asReversed()
                    val content = StringBuilder()
                    content.append("ARKSCREEN,${System.currentTimeMillis()},${curAccount.uid}")
                    content.append(dataList.joinToString("\n") { data ->
                        "${data.poolId},${data.poolCate},${data.ts},${data.pool},${data.charName},${data.charId},${data.rarity},${data.isNew},${data.pos}"
                    })
                    val contentStr = content.toString().replace("true", "1").replace("false", "0")
                    APP.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(contentStr.toByteArray())
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

    fun exportJson(uri: Uri) {
        viewModelScope.launch {
            exportingBackup.value = Progress(true, 0, 0, true)
            withContext(Dispatchers.IO) {
                try {
                    val dataList = gachaDao.getByUid(curAccount.uid).asReversed()
                    val content = generateCustomJson(dataList, curAccount.uid)
                    APP.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
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

    //读取：完成后合并本地数据，删除重复数据，
    fun importData(uri: Uri) {
        viewModelScope.launch {
            importingBackup.value = Progress(true, 0, 0, true)
            val newList: List<Gacha>
            try {
                newList = withContext(Dispatchers.IO) {
                    when (APP.contentResolver.getType(uri)) {
                        "text/plain" -> readTextFile(uri) // 对于TXT文件
                        "application/json" -> readJsonFile(uri) // 对于JSON文件
                        else -> throw Exception("Unsupported file type")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toaster.show("读取失败：" + e.message)
                return@launch
            }
            Toaster.show("读取成功，处理中")

            _uiState.value = DataUiState.Loading("LOADING...")
            importingBackup.value = Progress(true, 5, 10, true)

            val oldList = withContext(Dispatchers.IO) {
                gachaDao.getByUid(curAccount.uid)
            }
            val combinedList = (newList + oldList).distinctBy { "${it.ts}-${it.pos}" }.sortedBy { it.ts }
            withContext(Dispatchers.IO) {
                gachaDao.deleteByUid(curAccount.uid)
                gachaDao.insert(combinedList)
            }

            importingBackup.value = Progress(true, 7, 10, true)
            withContext(Dispatchers.IO) {
                initialize()
            }
            importingBackup.value = Progress(true, 10, 10, true)
            _uiState.value = DataUiState.Success("")
        }
    }

    private fun readTextFile(uri: Uri): List<Gacha> {
        val gachaList: List<Gacha>
        try{
            gachaList =  APP.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val lines = reader.lineSequence()
                    // 单独处理第一行
                    val firstLine = lines.first()
                    val info = firstLine.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val uid = info[2]
                    // 处理剩余的行（跳过第一行）
                    lines.drop(1) // 跳过第一行
                        .map { line -> deserializeLine(line,uid) }
                        .toList()
                }
            } ?: emptyList()
        }catch (e : Exception){
            Timber.e(e)
            throw Exception("text文件导入失败：${e.message}")
        }
        return gachaList
    }

    private fun deserializeLine(line: String,uid: String): Gacha {
        val list = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        return Gacha(
            uid = uid,
            poolId = list[0],
            poolCate =  list[1],
            ts = list[2].toLong(),
            pool =  list[3],
            charName =  list[4],
            charId = list[5],
            rarity = list[6].toInt(),
            isNew = list[7].toBoolean(),
            pos = list[8].toInt(),
        )
    }

    private fun readJsonFile(uri: Uri): List<Gacha> {
        val gachaList = mutableListOf<Gacha>()
        try {
            APP.contentResolver.openInputStream(uri)?.use { inputStream ->
                var dataNode = ObjectMapper().readTree(inputStream)
                val infoNode = dataNode["info"]
                val uid = infoNode.get("uid").asText()
                dataNode = dataNode["data"]
                dataNode.toList().forEach { record ->
                    gachaList.add(
                        Gacha(
                            uid = uid,
                            poolId = record.get("poolId").asText(),
                            poolCate = record.get("poolCate").asText(),
                            ts = record.get("ts").asLong(),
                            pool = record.get("pool").asText(),
                            charName = record.get("charName").asText(),
                            charId = record.get("charId").asText(),
                            rarity = record.get("rarity").asInt(),
                            isNew = record.get("isNew").asBoolean(),
                            pos = record.get("pos").asInt(),
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e.message)
            throw Exception(e.message)
        }
        return gachaList
    }

    private fun generateCustomJson(dataList: List<Gacha>, uid: String): String {
        val mapper = ObjectMapper()
        val root = mapper.createObjectNode()

        val info = root.putObject("info")
        info.put("uid", uid)
        info.put("export_timestamp", System.currentTimeMillis())
        info.put("export_app", "arkscreen")

        val dataArray = root.putArray("data")
        dataList.forEach { gacha ->
            val gachaNode = dataArray.addObject()
            gachaNode.put("poolId", gacha.poolId)
            gachaNode.put("poolCate", gacha.poolCate)
            gachaNode.put("ts", gacha.ts)
            gachaNode.put("pool", gacha.pool)
            gachaNode.put("charName", gacha.charName)
            gachaNode.put("charId", gacha.charId)
            gachaNode.put("rarity", gacha.rarity)
            gachaNode.put("isNew", gacha.isNew)
            gachaNode.put("pos", gacha.pos)
        }
        return mapper.writeValueAsString(root)
    }

}