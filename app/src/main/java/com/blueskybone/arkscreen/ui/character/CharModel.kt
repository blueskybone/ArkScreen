package com.blueskybone.arkscreen.ui.character

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.network.resolveUrl
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.service.TextTranslator
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharAssetsUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.GetCharMissUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.BuildOperatorPosterUseCase
import com.blueskybone.arkscreen.domain.usecase.operator.OperatorPoster
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val buildOperatorPosterUseCase: BuildOperatorPosterUseCase,
    private val settings: SettingPrefManager,
    private val syncAccountSkUseCase: SyncAccountSkUseCase,
    private val textTranslator: TextTranslator,
) : ViewModel() {
    data class FilterSelection(
        val profession: String? = null,
        val evolve: EvolveFilter = EvolveFilter.ALL,
        val rarity: RarityFilter = RarityFilter.ALL,
    )

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
    private var subProfessionNames: Map<String, String> = emptyMap()
    var currentFilter = FilterSelection()
        private set

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
    private val _accountAvatarUrl = MutableLiveData<String?>()
    val accountAvatarUrl: LiveData<String?> get() = _accountAvatarUrl
    private var loadJob: Job? = null
    private var loadRequestId = 0L
    private var syncing = false
    private var loadedUid: String? = null
    private val _lastSyncAt = MutableLiveData<Long?>()
    val lastSyncAt: LiveData<Long?> get() = _lastSyncAt
    private val _syncingState = MutableLiveData(false)
    val syncingState: LiveData<Boolean> get() = _syncingState
    private val _event = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            repoAcc.observeCurrentSkAcc()
                .distinctUntilChanged()
                .collectLatest(::startLoad)
        }
    }

    fun refresh() {
        if (syncing) return
        viewModelScope.launch {
            startLoad(repoAcc.observeCurrentSkAcc().first())
        }
    }

    fun reauthenticate(token: String, dId: String) {
        if (syncing) return
        viewModelScope.launch {
            syncAccountSkUseCase(SyncAccountSkUseCase.LoginWay.Token(token, dId))
                .onSuccess {
                    _event.emit("登录成功，正在重新同步")
                    refresh()
                }
                .onFailure { error ->
                    _event.emit(com.blueskybone.arkscreen.ui.common.userFacingError(error.message ?: "登录失败"))
                }
        }
    }

    private fun startLoad(account: AccountSk?) {
        if (syncing && loadedUid == account?.uid) return
        val requestId = ++loadRequestId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val accountChanged = loadedUid != account?.uid
            if (accountChanged) {
                sourceCharsList = emptyList()
                subProfessionNames = emptyMap()
                _accountAvatarUrl.value = null
                loadedUid = account?.uid
            }
            syncing = true
            _syncingState.value = true
            try {
                if (sourceCharsList.isEmpty()) {
                    _uiState.value = UiStatus.Loading("正在加载账号数据…")
                }
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
                handleFailure(e.message ?: "未知错误")
            } finally {
                if (requestId == loadRequestId) {
                    syncing = false
                    _syncingState.value = false
                }
            }
        }
    }

    private suspend fun loadAll(account: AccountSk) {
        val assets = getCharAssetsUseCase(account).getOrElse { error ->
            handleFailure(error.message ?: "加载干员资产失败：请查看日志")
            return
        }
        val ownList = assets.operators
        _accountAvatarUrl.value = assets.avatar.resolveUrl().takeIf(String::isNotBlank)
        subProfessionNames = textTranslator.translateAll(
            ownList.map(Operator::subProfessionId)
        )

        sourceCharsList = ownList
        _charsList.value = ownList

        // Refresh the complete operator map before calculating the difference. If the network
        // update fails, GameResourceRepository keeps the last valid local/bundled resource.
        repo.syncResource(ConfigType.CHAR_MAP).lastOrNull()

        val notOwnList = getCharMissUseCase(ownList).getOrElse { error ->
            handleFailure(error.message ?: "加载未持有干员失败：请查看日志")
            return
        }

        _charsNotOwnList.value = notOwnList

        repo.getResourceDate(ConfigType.CHAR_MAP)
            .onSuccess { date -> _update.value = date }

        _statistic.value = calculateStatistic(ownList, notOwnList)
        _lastSyncAt.value = System.currentTimeMillis()
        _uiState.value = UiStatus.Success()
    }

    private suspend fun handleFailure(message: String) {
        val userMessage = com.blueskybone.arkscreen.ui.common.userFacingError(message)
        if (sourceCharsList.isNotEmpty()) {
            _uiState.value = UiStatus.Success()
            _event.emit(userMessage)
        } else {
            _uiState.value = UiStatus.Error(userMessage)
        }
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

    fun translatedSubProfession(id: String): String = subProfessionNames[id] ?: id

    fun applyFilter(
        profession: String? = null,
        level: EvolveFilter = EvolveFilter.ALL,
        rarity: RarityFilter = RarityFilter.ALL,
    ) {
        currentFilter = FilterSelection(profession, level, rarity)
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

    fun buildOperatorPoster(): OperatorPoster? {
        val account = currentAccountSk ?: return null
        return buildOperatorPosterUseCase(account, sourceCharsList, _accountAvatarUrl.value)
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
}
