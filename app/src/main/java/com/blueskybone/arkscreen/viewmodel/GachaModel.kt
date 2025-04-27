package com.blueskybone.arkscreen.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.CharAllMap
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.Progress
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getNewRecords
import com.blueskybone.arkscreen.network.NetWorkUtils.Companion.getPoolType
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.room.Gacha
import com.blueskybone.arkscreen.room.Gachas
import com.blueskybone.arkscreen.room.Record
import com.blueskybone.arkscreen.room.Records
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStrYMD
import com.blueskybone.arkscreen.util.readFileAsJsonNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
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
    private val accountGcDao = database.getAccountGcDao()

    private val _gachaData = MutableLiveData<List<Gachas>>()
    val gachaData: LiveData<List<Gachas>> get() = _gachaData

    //var gachaData: List<Gachas>? = null
    private var fesPool: List<String>? = null

    private lateinit var charsNode: JsonNode
    var poolCountNormal = 0
    var poolCountFes = 0
    var poolCountCore = 0
    var finalCountSum = 0
    var rarity6Count = 0

    var dateRange = "-"

    init {
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("LOADING...")
            curAccount = prefManager.baseAccountGc.get()
            if (curAccount.uid == "") {
                _uiState.value = DataUiState.Error("请在 卡池账号管理 添加账号")
                return@launch
            }
            withContext(Dispatchers.IO) {
                try {
                    CharAllMap.update()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    charsNode = readFileAsJsonNode(CharAllMap.getResourceFilepath())["charInfoMap"]
                    loadGachaRecords(curAccount)
                    fesPool = getPoolType()
                    _gachaData.postValue(processGachaData(curAccount))
                    _uiState.postValue(DataUiState.Success(""))
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.postValue(DataUiState.Error(e.message ?: "error message null"))
                }
            }
        }
    }

    private suspend fun loadGachaRecords(account: AccountGc) {
        val list = gachaDao.getByUid(account.uid)
        val ts = if (list.isEmpty()) null else list.last().ts
        val newList = getNewRecords(account.token, account.channelMasterId, account.uid, ts)
        gachaDao.insert(newList.reversed())
    }


    private suspend fun processGachaData(account: AccountGc): List<Gachas> {
        val dataDb = gachaDao.getByUid(account.uid)
        val data = dataDb.sortedBy { it.ts }
        if (data.isEmpty()) return listOf()
        dateRange = getTimeStrYMD(data.first().ts) + "-" + getTimeStrYMD(data.last().ts)
        poolCountNormal = 0
        poolCountFes = 0
        poolCountCore = 0
        finalCountSum = 0
        rarity6Count = 0

        var id = 0
        val finalGachas = mutableListOf<Gachas>()
        val normalPoolList = mutableListOf<Gacha>()
        val corePoolList = mutableListOf<Gacha>()
        val groupByPool = data.groupBy { it.pool }
        for (group in groupByPool) {
            if (fesPool?.contains(group.key) == true) {
                poolCountFes = 0
                var count = 0
                val gachas = Gachas(pool = group.key)
                for (item in group.value) {
                    gachas.ts = item.ts
                    val charList = deserialize(item.record)
                    for (char in charList) {
                        count++
                        poolCountFes++
                        finalCountSum++
                        if (char.rarity == 5) {
                            rarity6Count++
                            val charId = findCharId(char.name)
                            gachas.data
                                .add(Records(id, char.name, charId, char.isNew, poolCountFes))
                            poolCountFes = 0
                            id++
                        }
                    }
                }
                gachas.count = count
                gachas.isFes = true
                finalGachas.add(gachas)
            } else if (group.key == "中坚寻访" || group.key == "中坚甄选") {
                corePoolList.addAll(group.value)
            } else {
                normalPoolList.addAll(group.value)
            }
        }
        val mapNormal = mutableMapOf<String, Gachas>()   //poolName, record
        for (item in normalPoolList) {
            if (!mapNormal.containsKey(item.pool)) {
                mapNormal[item.pool] = Gachas(pool = item.pool)
            }
            val charList = deserialize(item.record)
            mapNormal[item.pool]!!.ts = item.ts
            for (char in charList) {
                finalCountSum++
                mapNormal[item.pool]!!.count++ //池子抽数
                poolCountNormal++  //计算水位
                if (char.rarity == 5) {
                    rarity6Count++
                    val charId = findCharId(char.name)
                    mapNormal[item.pool]!!.data
                        .add(Records(id, char.name, charId, char.isNew, poolCountNormal))
                    poolCountNormal = 0
                    id++
                }
            }
        }
        val mapCore = mutableMapOf<String, Gachas>()   //poolName, record
        for (item in corePoolList) {
            if (!mapCore.containsKey(item.pool)) {
                mapCore[item.pool] = Gachas(pool = item.pool)
            }
            val charList = deserialize(item.record)
            mapCore[item.pool]!!.ts = item.ts
            for (char in charList) {
                finalCountSum++
                mapCore[item.pool]!!.count++ //池子抽数
                poolCountCore++  //计算水位
                if (char.rarity == 5) {
                    rarity6Count++
                    val charId = findCharId(char.name)
                    mapCore[item.pool]!!.data
                        .add(Records(id, char.name, charId, char.isNew, poolCountCore))
                    poolCountCore = 0
                    id++
                }
            }
        }
        finalGachas.addAll(mapNormal.values.toList())
        finalGachas.addAll(mapCore.values.toList())

        return finalGachas
            .sortedByDescending { it.ts }
            .map { item -> item.copy(data = item.data.asReversed()) }
    }

    private fun findCharId(name: String): String {
        for (char in charsNode.fields()) {
            if (char.value.get("name").asText() == name) {
                return char.key
            }
        }
        return ""
    }

    private fun deserialize(string: String): List<Record> {
        val list =
            string.split("@".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val records = mutableListOf<Record>()
        for (item in list) {
            val arrays = item.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            records.add(Record(arrays[0], arrays[1].toInt(), arrays[2].toBoolean()))
        }
        return records
    }

    private fun deserializeLine(line: String, uid: String): Gacha {
        val list =
            line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        return Gacha(uid = uid, ts = list[0].toLong(), pool = list[1], record = list[2])
    }

    //测试内容
    fun deleteRecords() {
        executeAsync {
            gachaDao.deleteByUid(curAccount.uid)
            _gachaData.postValue(processGachaData(curAccount))
        }
    }

    private fun executeAsync(function: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { function() }
    }
    //数据修正（对未知卡池）


    fun exportTxt(uri: Uri) {
        viewModelScope.launch {
            exportingBackup.value = Progress(true, 0, 0, true)
            withContext(Dispatchers.IO) {
                try {

                    val dataList = gachaDao.getByUid(curAccount.uid).asReversed()
                    val content = StringBuilder()
                    content.append(dataList.joinToString("\n") { data ->
                        "${data.ts},${data.pool},${data.record}"
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
            val combinedList = (newList + oldList).distinctBy { it.ts }.sortedBy { it.ts }
            withContext(Dispatchers.IO) {
                gachaDao.deleteByUid(curAccount.uid)
                gachaDao.insert(combinedList)
            }

            importingBackup.value = Progress(true, 7, 10, true)
            withContext(Dispatchers.IO) {
                _gachaData.postValue(processGachaData(curAccount))
            }
            importingBackup.value = Progress(true, 10, 10, true)
            _uiState.value = DataUiState.Success("")
        }
    }

    private fun readTextFile(uri: Uri): List<Gacha> {
        return APP.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.lineSequence() // 使用 lineSequence() 代替 forEachLine，使其更具可读性
                    .map { line -> deserializeLine(line, curAccount.uid) }
                    .toList() // 直接将结果转为 List
            }
        } ?: emptyList()
    }

    private fun readJsonFile(uri: Uri): List<Gacha> {
        val gachaList = mutableListOf<Gacha>()
        APP.contentResolver.openInputStream(uri)?.use { inputStream ->
            var dataNode = ObjectMapper().readTree(inputStream)
            if (dataNode.has("data")) {
                dataNode = dataNode["data"]
            }
            dataNode.fieldNames().forEach { fieldName ->
                val gachaNode = dataNode.get(fieldName)
                //println(gachaNode)
                val cNode = gachaNode.get("c")
                val pool = gachaNode.get("p").asText()
                val ts = fieldName.toLong()
                val recordStr = StringBuilder()
                cNode.forEach { node ->
                    recordStr.append(node[0].asText()).append("-")
                        .append(node[1].asInt()).append("-")
                        .append(node[2].asInt() == 1).append("@")
                }
                recordStr.deleteCharAt(recordStr.length - 1)
                gachaList.add(
                    Gacha(
                        uid = curAccount.uid,
                        ts = ts,
                        pool = pool,
                        record = recordStr.toString()
                    )
                )
            }
        }
        return gachaList
    }

    private fun generateCustomJson(dataList: List<Gacha>, uid: String): String {
        val mapper = ObjectMapper()
        val root = mapper.createObjectNode()
        val data = mapper.createObjectNode()
        val info = mapper.createObjectNode()
        info.put("uid", uid.toInt())
        info.put("export_timestamp", System.currentTimeMillis() / 1000)

        root.set<ObjectNode>("info", info)

        for (gacha in dataList) {
            val gachaData = mapper.createObjectNode()
            val records = deserialize(gacha.record)

            val cArray: ArrayNode = mapper.createArrayNode()
            for (record in records) {
                val recordArray = mapper.createArrayNode()
                recordArray.add(record.name)
                recordArray.add(record.rarity)
                recordArray.add(if (record.isNew) 1 else 0) // Convert true/false to 1/0
                cArray.add(recordArray)
            }
            gachaData.set<ArrayNode>("c", cArray)
            gachaData.put("p", gacha.pool)
            data.set<ObjectNode>(gacha.ts.toString(), gachaData)
        }

        root.set<ObjectNode>("data", data)

        return mapper.writeValueAsString(root)
    }

}