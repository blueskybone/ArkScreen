package com.blueskybone.arkscreen.ui.character

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharAssetsUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharMissUseCase
import com.blueskybone.arkscreen.ui.UiStatus
import com.blueskybone.arkscreen.ui.character.adapter.ViewType
import com.blueskybone.arkscreen.ui.character.model.CharStatistic
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class CharModel(
    private val repo: GameResourceRepository,
    private val repoAcc: AccountRepository,
    private val getCharAssetsUseCase: GetCharAssetsUseCase,
    private val getCharMissUseCase: GetCharMissUseCase,
    private val settings: SettingPrefManager,
) : ViewModel() {

    enum class EvolveFilter(val phase: Int?) {
        ALL(null),
        E0(0),
        E1(1),
        E2(2),
    }

    enum class RarityFilter(val range: IntRange?) {
        ALL(null),
        R1_TO_R3(0..2),
        R4(3..3),
        R5(4..4),
        R6(5..5),
    }

    private val _uiState = MutableLiveData<UiStatus>(UiStatus.Idle)
    val uiState: LiveData<UiStatus> get() = _uiState

    private val _charsList = MutableLiveData<List<Operator>>()
    val charsList: LiveData<List<Operator>> get() = _charsList

    private var sourceCharsList: List<Operator> = emptyList()

    private val _charsNotOwnList = MutableLiveData<List<Operator>>()
    val charsNotOwnList: LiveData<List<Operator>> get() = _charsNotOwnList

    private val _statistic = MutableLiveData<CharStatistic>()
    val statistic: LiveData<CharStatistic> get() = _statistic


    val viewType: Flow<ViewType> = settings.assetsViewType.flow()
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

    private var currentAccountSk: AccountSk? = null
    private val _currentAccount = MutableLiveData<AccountSk?>()
    val currentAccount: LiveData<AccountSk?> get() = _currentAccount
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            repoAcc.observeCurrentSkAcc()
                .distinctUntilChanged()
                .collectLatest(::startLoad)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            startLoad(repoAcc.observeCurrentSkAcc().first())
        }
    }

    private fun startLoad(account: AccountSk?) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                _uiState.value = UiStatus.Loading()
                currentAccountSk = account
                _currentAccount.value = account
                if (account == null) {
                    _uiState.value = UiStatus.Empty("未登录")
                    return@launch
                }
                loadAll(account)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiStatus.Error(e.message ?: "未知错误")
            }
        }
    }

    private suspend fun loadAll(account: AccountSk) {
        val ownList = getCharAssetsUseCase(account).getOrElse { error ->
            _uiState.postValue(UiStatus.Error(error.message ?: "加载干员资产失败：请查看日志"))
            return
        }

        sourceCharsList = ownList
        _charsList.postValue(ownList)

        // Refresh the complete operator map before calculating the difference. If the network
        // update fails, GameResourceRepository keeps the last valid local/bundled resource.
        repo.syncResource(ConfigType.CHAR_MAP).lastOrNull()

        val notOwnList = getCharMissUseCase(ownList).getOrElse { error ->
            _uiState.postValue(UiStatus.Error(error.message ?: "加载未持有干员失败：请查看日志"))
            return
        }

        _charsNotOwnList.postValue(notOwnList)

        repo.getResourceDate(ConfigType.CHAR_MAP)
            .onSuccess { date -> _update.postValue(date) }

        _statistic.postValue(calculateStatistic(ownList, notOwnList))
        _uiState.postValue(UiStatus.Success())
    }


    fun toggleViewType() {
        viewModelScope.launch {
            val currentOrdinal = settings.assetsViewType.get()
            val newViewType = when (currentOrdinal) {
                ViewType.LIST.ordinal -> ViewType.GRID
                ViewType.GRID.ordinal -> ViewType.LIST
                else -> ViewType.GRID
            }
            settings.assetsViewType.set(newViewType.ordinal)
        }
    }

    fun exportFileBaseName(): String =
        currentAccountSk?.nickName?.takeIf { it.isNotBlank() } ?: "arknights"

    fun applyFilter(
        profession: String? = null,
        level: EvolveFilter = EvolveFilter.ALL,
        rarity: RarityFilter = RarityFilter.ALL,
    ) {
        _charsList.value = sourceCharsList.filter { operator ->
            (profession == null || operator.profession == profession) &&
                (level.phase == null || operator.evolvePhase == level.phase) &&
                (rarity.range == null || operator.rarity in rarity.range)
        }
    }

    fun resetFilter() {
        _charsList.value = sourceCharsList
    }


    fun generateExportText(): String =
        sourceCharsList.joinToString("\n") { data ->
            "${data.name},${data.rarity},${data.profession}," +
                "${data.subProfessionId},${data.level},${data.evolvePhase}," +
                "${data.potentialRank},${data.mainSkillLvl},${data.favorPercent}," +
                "${data.gainTime}," +
                data.skills.joinToString("@") { it.specializeLevel.toString() } + "," +
                data.equips.joinToString("@") { "${it.typeName2}-${it.stage}" }
        }

    private fun calculateStatistic(
        ownList: List<Operator>,
        notOwnList: List<Operator>
    ): CharStatistic {
        val ownByRarity = IntArray(6)
        val missByRarity = IntArray(6)
        val e2ByRarity = IntArray(6)
        val m3ByRarity = IntArray(6)
        val module3ByRarity = IntArray(6)
        var totalE2 = 0
        var totalM3 = 0
        var totalModule3 = 0

        ownList.forEach { operator ->
            val m3Count = operator.skills.count { it.specializeLevel == 3 }
            val module3Count = operator.equips.count { it.stage == 3 }

            if (operator.evolvePhase == 2) totalE2++
            totalM3 += m3Count
            totalModule3 += module3Count

            operator.rarity.takeIf { it in ownByRarity.indices }?.let { rarity ->
                ownByRarity[rarity]++
                if (operator.evolvePhase == 2) e2ByRarity[rarity]++
                m3ByRarity[rarity] += m3Count
                module3ByRarity[rarity] += module3Count
            }
        }
        notOwnList.forEach { operator ->
            operator.rarity.takeIf { it in missByRarity.indices }
                ?.let { missByRarity[it]++ }
        }

        return CharStatistic(
            totalOwn = ownList.size,
            totalMiss = notOwnList.size,
            totalE2 = totalE2,
            totalM3 = totalM3,
            totalModule3 = totalModule3,

            r6Own = ownByRarity[5],
            r6Miss = missByRarity[5],
            r6E2 = e2ByRarity[5],
            r6M3 = m3ByRarity[5],
            r6Module3 = module3ByRarity[5],

            r5Own = ownByRarity[4],
            r5Miss = missByRarity[4],
            r5E2 = e2ByRarity[4],
            r5M3 = m3ByRarity[4],
            r5Module3 = module3ByRarity[4],

            r4Own = ownByRarity[3],
            r4Miss = missByRarity[3],
            r4E2 = e2ByRarity[3],
            r4M3 = m3ByRarity[3],
            r4Module3 = module3ByRarity[3]
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
