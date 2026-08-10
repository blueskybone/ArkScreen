package com.blueskybone.arkscreen.ui.character

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharAssetsUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharMissUseCase
import com.blueskybone.arkscreen.ui.UiState
import com.blueskybone.arkscreen.ui.character.adapter.ViewType
import com.blueskybone.arkscreen.ui.character.model.CharStatistic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class CharModel(
    private val repo: ResourceRepository,
    private val repoAcc: AccountRepository,
    private val getCharAssetsUseCase: GetCharAssetsUseCase,
    private val getCharMissUseCase: GetCharMissUseCase
) : ViewModel() {

    private val pref: InnerPrefManager by KoinPlatform.getKoin().inject()

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    private val _charsList = MutableLiveData<List<Operator>>()
    val charsList: LiveData<List<Operator>> get() = _charsList

    private var sourceCharsList: List<Operator> = emptyList()

    private var sourceCharsNotOwnList: List<Operator> = emptyList()

    private val _charsNotOwnList = MutableLiveData<List<Operator>>()
    val charsNotOwnList: LiveData<List<Operator>> get() = _charsNotOwnList

    private val _statistic = MutableLiveData<CharStatistic>()
    val statistic: LiveData<CharStatistic> get() = _statistic


    val viewType: Flow<ViewType> = pref.assetsViewType.flow()
        .map { ordinal ->
            when (ordinal) {
                ViewType.GRID.ordinal -> ViewType.GRID
                ViewType.LIST.ordinal -> ViewType.LIST
                else -> ViewType.GRID // 默认值
            }
        }
        .distinctUntilChanged()

    private val _update = MutableLiveData<String>()
    val update: LiveData<String> get() = _update

    val currentAccountSk: LiveData<AccountSk?> = repoAcc.observeCurrentSkAcc()
        .catch { e -> emit(null) }
        .asLiveData()

    init {
//        execute {
//            _uiState.value = UiState.Loading
////            repo.syncResource(ConfigType.CHAR_MAP)
//            if (currentAccountSk.value == null) {
//                _uiState.value = UiState.Error("未登录")
//                return@execute
//            } else {
//                loadCharAssets(currentAccountSk.value!!)
//            }
//            loadCharMiss(_charsList.value!!)
//            loadCharMapUpdateDate()
//            _statistic.postValue(calculateStatistic(ownList, notOwnList))
//        }
    }

    init {
//        _viewType.value = runCatching {
//            ViewType.entries[prefManager.assetsViewType.get()]
//        }.getOrElse { ViewType.GRID }

        refresh()
    }

    fun refresh() {
        execute {
            _uiState.value = UiState.Loading

            val account = currentAccountSk.value
            if (account == null) {
                _uiState.value = UiState.Error("未登录")
                return@execute
            }

            loadAll(account)
        }
    }

    private suspend fun loadAll(account: AccountSk) {
        val ownList = getCharAssetsUseCase(account).getOrElse { error ->
            _uiState.postValue(UiState.Error(error.message ?: "加载干员资产失败：请查看日志"))
            return
        }

        sourceCharsList = ownList
        _charsList.postValue(ownList)

        val notOwnList = getCharMissUseCase(ownList).getOrElse { error ->
            _uiState.postValue(UiState.Error(error.message ?: "加载未持有干员失败：请查看日志"))
            return
        }

        sourceCharsNotOwnList = notOwnList
        _charsNotOwnList.postValue(notOwnList)

        repo.getResourceDate(ConfigType.CHAR_MAP)
            .onSuccess { date -> _update.postValue(date) }

        _statistic.postValue(calculateStatistic(ownList, notOwnList))
        _uiState.postValue(UiState.Success(""))
    }


    //TODO: pref也改成flow类型的，然后首页观察，不要这样写了
//    fun toggleViewType() {
//        when (_viewType.value) {
//            ViewType.LIST -> {
//                _viewType.value = ViewType.GRID
//                pref.assetsViewType.set(ViewType.GRID.ordinal)
//            }
//
//            ViewType.GRID -> {
//                _viewType.value = ViewType.LIST
//                pref.assetsViewType.set(ViewType.LIST.ordinal)
//            }
//
//            null -> _viewType.value = ViewType.GRID
//        }
//    }
    fun toggleViewType() {
        viewModelScope.launch {
            // 读取当前值，计算新值，写入
            val currentOrdinal = pref.assetsViewType.get()
            val newViewType = when (currentOrdinal) {
                ViewType.LIST.ordinal -> ViewType.GRID
                ViewType.GRID.ordinal -> ViewType.LIST
                else -> ViewType.GRID
            }
            pref.assetsViewType.set(newViewType.ordinal)
        }
    }

//
//    private fun loadCharAssets(account: AccountSk) {
//        execute {
//            getCharAssetsUseCase(account).onSuccess { data ->
//                _charsList.value = data
//            }.onFailure { error ->
//                _uiState.value = UiState.Error(error.message ?: "加载干员资产失败：请查看日志")
//            }
//        }
//    }
//
//    private fun loadCharMapUpdateDate() {
//        execute {
//            repo.getResourceDate(ConfigType.CHAR_MAP).onSuccess { date ->
//                _update.value = date
//            }
//        }
//    }
//
//    private fun loadCharMiss(charOwnList: List<Operator>) {
//        execute {
//            getCharMissUseCase(charOwnList).onSuccess { data ->
//                _charsNotOwnList.value = data
//            }.onFailure { error ->
//                _uiState.value = UiState.Error(error.message ?: "加载资源失败：请查看日志")
//            }
//        }
//    }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                _uiState.value = UiState.Cancelled
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "未知错误")
            }
        }
    }

    //TODO: 统计信息
    //TODO：导出文件
    //TODO：生成图片

    fun applyFilter(
        profession: String = "ALL",
        level: String = "ALL",
        rarity: String = "ALL"
    ) {
        execute {
            val filtered = sourceCharsList
                .filterByProfession(profession)
                .filterByRarity(rarity)
                .filterByLevel(level)

            _charsList.value = filtered
        }
    }

    fun resetFilter() {
        _charsList.value = sourceCharsList
    }


    private fun List<Operator>.filterByProfession(profession: String): List<Operator> {
        return when (profession) {
            "ALL" -> this
            else -> filter { it.profession == profession }
        }
    }

    private fun List<Operator>.filterByRarity(rarity: String): List<Operator> {
        return when (rarity) {
            "1~3★" -> filter { it.rarity in 0..2 }
            "4★" -> filter { it.rarity == 3 }
            "5★" -> filter { it.rarity == 4 }
            "6★" -> filter { it.rarity == 5 }
            else -> this
        }
    }

    private fun List<Operator>.filterByLevel(level: String): List<Operator> {
        return when (level) {
            "精零" -> filter { it.evolvePhase == 0 }
            "精一" -> filter { it.evolvePhase == 1 }
            "精二" -> filter { it.evolvePhase == 2 }
            else -> this
        }
    }

    private fun calculateStatistic(
        ownList: List<Operator>,
        notOwnList: List<Operator>
    ): CharStatistic {
        val r6OwnList = ownList.filter { it.rarity == 5 }
        val r5OwnList = ownList.filter { it.rarity == 4 }
        val r4OwnList = ownList.filter { it.rarity == 3 }

        return CharStatistic(
            totalOwn = ownList.size,
            totalMiss = notOwnList.size,
            totalE2 = ownList.count { it.evolvePhase == 2 },
            totalM3 = ownList.sumOf { op -> op.skills.count { it.specializeLevel == 3 } },
            totalModule3 = ownList.sumOf { op -> op.equips.count { it.stage == 3 } },

            r6Own = r6OwnList.size,
            r6Miss = notOwnList.count { it.rarity == 5 },
            r6E2 = r6OwnList.count { it.evolvePhase == 2 },
            r6M3 = r6OwnList.sumOf { op -> op.skills.count { it.specializeLevel == 3 } },
            r6Module3 = r6OwnList.sumOf { op -> op.equips.count { it.stage == 3 } },

            r5Own = r5OwnList.size,
            r5Miss = notOwnList.count { it.rarity == 4 },
            r5E2 = r5OwnList.count { it.evolvePhase == 2 },
            r5M3 = r5OwnList.sumOf { op -> op.skills.count { it.specializeLevel == 3 } },
            r5Module3 = r5OwnList.sumOf { op -> op.equips.count { it.stage == 3 } },

            r4Own = r4OwnList.size,
            r4Miss = notOwnList.count { it.rarity == 3 },
            r4E2 = r4OwnList.count { it.evolvePhase == 2 },
            r4M3 = r4OwnList.sumOf { op -> op.skills.count { it.specializeLevel == 3 } },
            r4Module3 = r4OwnList.sumOf { op -> op.equips.count { it.stage == 3 } }
        )
    }

    fun generateStatisticMarkDownText(): String {
        val stat = _statistic.value ?: return ""

        return buildString {
            appendLine("### 全部干员")
            appendLine()
            appendLine("* 干员总数  **${stat.totalOwn}/${stat.totalOwn + stat.totalMiss}**")
            appendLine("* 精二  **${stat.totalE2}**")
            appendLine("* 专三  **${stat.totalM3}**")
            appendLine("* Stage3模组  **${stat.totalModule3}**")
            appendLine()
            appendLine("### 6★干员")
            appendLine()
            appendLine("* 干员总数  **${stat.r6Own}/${stat.r6Own + stat.r6Miss}**")
            appendLine("* 精二  **${stat.r6E2}**")
            appendLine("* 专三  **${stat.r6M3}**")
            appendLine("* Stage3模组  **${stat.r6Module3}**")
            appendLine()
            appendLine("### 5★干员")
            appendLine()
            appendLine("* 干员总数  **${stat.r5Own}/${stat.r5Own + stat.r5Miss}**")
            appendLine("* 精二  **${stat.r5E2}**")
            appendLine("* 专三  **${stat.r5M3}**")
            appendLine("* Stage3模组  **${stat.r5Module3}**")
            appendLine()
            appendLine("### 4★干员")
            appendLine()
            appendLine("* 干员总数  **${stat.r4Own}/${stat.r4Own + stat.r4Miss}**")
            appendLine("* 精二  **${stat.r4E2}**")
            appendLine("* 专三  **${stat.r4M3}**")
            appendLine("* Stage3模组  **${stat.r4Module3}**")
        }
    }


}