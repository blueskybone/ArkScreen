package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.common.UiStrings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.gacha.Record
import com.blueskybone.arkscreen.data.gacha.GachaBackupCodec
import com.blueskybone.arkscreen.data.gacha.GachaImportPayload
import com.blueskybone.arkscreen.data.gacha.GachaImportDecoder
import com.blueskybone.arkscreen.data.gacha.GachaImportResolver
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.GachaRepository
import com.blueskybone.arkscreen.domain.usecase.gacha.SyncRecordsUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountGcUseCase
import com.blueskybone.arkscreen.ui.common.userFacingError
import com.blueskybone.arkscreen.ui.gacha.model.GachaUiMapper
import com.blueskybone.arkscreen.ui.UiStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class GachaViewModel(
    private val repo: GachaRepository,
    private val repoAcc: AccountRepository,
    private val syncRecordsUseCase: SyncRecordsUseCase,
    private val syncAccountGcUseCase: SyncAccountGcUseCase,
    private val backupCodec: GachaBackupCodec,
    private val importDecoder: GachaImportDecoder,
    private val importResolver: GachaImportResolver,
    private val texts: UiStrings,
) : ViewModel() {
    private val currentGcFlow = repoAcc.observeCurrentGcAcc() // 当前账号

    @OptIn(ExperimentalCoroutinesApi::class)
    private val recordsFlow: Flow<List<Record>> = currentGcFlow // 当前账号的寻访记录
        .flatMapLatest { account ->
            val uid = account?.uid.orEmpty()
            if (uid.isBlank()) {
                flowOf(emptyList())
            } else {
                repo.observeRecords(uid)
            }
        }

    private val _uiState = MutableStateFlow(GachaUiState())
    val uiState: StateFlow<GachaUiState> = _uiState

    private val _event = Channel<GachaEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private var observedUid: String? = null
    private var domainRecords: List<Record> = emptyList()

    private fun observeAccounts() {
        viewModelScope.launch {
            combine(repoAcc.observeGcAcc(), currentGcFlow) { accounts, current ->
                accounts to current
            }.collect { (accounts, current) ->
                _uiState.update {
                    it.copy(
                        accList = accounts,
                        currAccount = current,
                        status = if (current == null) {
                            UiStatus.Empty(texts.get(R.string.gacha_add_account_hint))
                        } else it.status,
                    )
                }
            }
        }
    }

    private fun observeRecords() {
        viewModelScope.launch {
            recordsFlow.collect { records ->
                domainRecords = records
                _uiState.update {
                    val snapshot = GachaUiMapper.map(records)
                    val selectedPoolStillExists = snapshot.gachaPoolStats.any { pool ->
                        pool.poolId == it.selectedPoolId
                    }
                    it.copy(
                        gachaUiSnapshot = snapshot,
                        selectedPoolId = if (selectedPoolStillExists) it.selectedPoolId else "ALL",
                    )
                }
            }
        }
    }

    init {
        observeAccounts()
        observeRecords()
        observeAccountSync()
    }

    private fun observeAccountSync() {
        viewModelScope.launch {
            currentGcFlow
                .distinctUntilChangedBy { it?.uid }
                .collectLatest { account ->
                    if (account == null) {
                        observedUid = null
                        _uiState.update {
                            it.copy(
                                status = UiStatus.Empty(texts.get(R.string.gacha_add_account_hint)),
                                gachaUiSnapshot = null,
                                lastSyncAt = null,
                            )
                        }
                        return@collectLatest
                    }
                    if (observedUid != account.uid) {
                        observedUid = account.uid
                        _uiState.update {
                            it.copy(
                                gachaUiSnapshot = null,
                                lastSyncAt = null,
                                status = UiStatus.Loading(texts.get(R.string.status_account_loading)),
                            )
                        }
                    }
                    syncAccount(account)
                }
        }
    }

    fun retrySync() {
        execute {
            if (_uiState.value.isSyncing) return@execute
            val account = requireCurrentAccount() ?: return@execute
            syncAccount(account)
        }
    }

    private suspend fun syncAccount(
        account: AccountGc,
        continueOperation: Boolean = false,
    ) {
        if (_uiState.value.isSyncing && !continueOperation) return
        val hasCachedRecords = _uiState.value.gachaUiSnapshot?.records?.isNotEmpty() == true
        _uiState.update {
            it.copy(
                isSyncing = true,
                status = if (hasCachedRecords) it.status
                else UiStatus.Loading(texts.get(R.string.status_account_loading))
            )
        }
        try {
            syncRecordsUseCase(account).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            status = UiStatus.Success(),
                            lastSyncAt = System.currentTimeMillis(),
                            isSyncing = false,
                        )
                    }
                },
                onFailure = { error ->
                    Timber.tag("Gacha").e(error, "Gacha record synchronization failed")
                    val message = userFacingError(texts, error.message ?: texts.get(R.string.error_gacha_sync))
                    _uiState.update {
                        it.copy(
                            status = if (hasCachedRecords) UiStatus.Success()
                            else UiStatus.Error(message),
                            isSyncing = false,
                        )
                    }
                    if (hasCachedRecords) {
                        _event.send(GachaEvent.ShowError(message))
                    }
                },
            )
        } finally {
            if (_uiState.value.isSyncing) {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun reauthenticate(
        token: String,
        akUserCenter: String,
        xrToken: String,
        channelMasterId: Int,
    ) {
        execute {
            if (_uiState.value.isSyncing) return@execute
            _uiState.update { it.copy(isSyncing = true) }
            syncAccountGcUseCase(
                SyncAccountGcUseCase.LoginWay.Token(
                    token = token,
                    akUserCenter = akUserCenter,
                    xrToken = xrToken,
                    channelMasterId = channelMasterId,
                )
            ).fold(
                onSuccess = {
                    _event.send(GachaEvent.ShowMessage(texts.get(R.string.status_login_resync)))
                    val account = repoAcc.observeCurrentGcAcc().first()
                    if (account != null) {
                        syncAccount(account, continueOperation = true)
                    } else {
                        _uiState.update { it.copy(isSyncing = false) }
                    }
                },
                onFailure = { error ->
                    Timber.tag("Gacha").e(error, "Gacha account reauthentication failed")
                    val message = userFacingError(texts, error.message ?: texts.get(R.string.error_relogin))
                    _uiState.update { it.copy(isSyncing = false) }
                    _event.send(GachaEvent.ShowError(message))
                },
            )
        }
    }

    fun deleteRecords() {
        execute {
            if (_uiState.value.isSyncing) return@execute
            val account = requireCurrentAccount() ?: return@execute
            _uiState.update { it.copy(status = UiStatus.Loading(), isSyncing = true) }
            try {
                repo.deleteRecords(account).fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(status = UiStatus.Success(), isSyncing = false)
                        }
                    },
                    onFailure = { error -> reportError(error.message ?: texts.get(R.string.error_gacha_delete)) },
                )
            } finally {
                if (_uiState.value.isSyncing) {
                    _uiState.update { it.copy(isSyncing = false) }
                }
            }
        }
    }

    fun correctUnCateRecord() {
        execute {
            if (_uiState.value.isSyncing) return@execute
            val account = requireCurrentAccount() ?: return@execute
            _uiState.update { it.copy(status = UiStatus.Loading(), isSyncing = true) }
            try {
                repo.correctUnCateRecord(account).fold(
                    onSuccess = { correctedCount ->
                        _uiState.update {
                            it.copy(status = UiStatus.Success(), isSyncing = false)
                        }
                        _event.send(
                            GachaEvent.ShowMessage(
                                if (correctedCount == 0) texts.get(R.string.gacha_no_corrections)
                                else texts.get(R.string.gacha_categories_corrected, correctedCount)
                            )
                        )
                    },
                    onFailure = { error -> reportError(error.message ?: texts.get(R.string.error_gacha_repair)) },
                )
            } finally {
                if (_uiState.value.isSyncing) {
                    _uiState.update { it.copy(isSyncing = false) }
                }
            }
        }
    }

    fun exportFileBaseName(): String =
        "${_uiState.value.currAccount?.uid ?: "gacha"}_gacha_records"

    suspend fun buildExportJson(): Result<String> = encodeExport(backupCodec::encodeJson)

    suspend fun buildExportText(): Result<String> = encodeExport(backupCodec::encodeText)

    private suspend fun encodeExport(
        encoder: (AccountGc, List<Record>) -> String,
    ): Result<String> {
        return try {
            val account = _uiState.value.currAccount
                ?: return Result.failure(IllegalStateException(texts.get(R.string.select_gacha_account_first)))
            val records = domainRecords
            if (records.isEmpty()) {
                return Result.failure(IllegalStateException(texts.get(R.string.gacha_export_empty)))
            }
            Result.success(withContext(Dispatchers.Default) { encoder(account, records) })
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun prepareImport(fileName: String?, content: String): Result<GachaImportPayload> = try {
        val document = withContext(Dispatchers.Default) {
            importDecoder.decode(fileName, content)
        }
        importResolver.resolve(document).map { it.payload }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    fun importRecords(payload: GachaImportPayload, expectedUid: String) {
        execute {
            if (_uiState.value.isSyncing) return@execute
            val account = requireCurrentAccount() ?: return@execute
            if (account.uid != expectedUid) {
                _event.send(GachaEvent.ShowError(texts.get(R.string.gacha_import_account_changed)))
                return@execute
            }
            _uiState.update { it.copy(isSyncing = true) }
            repo.importRecords(account, payload.records).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isSyncing = false) }
                    val message = if (result.skipped == 0) {
                        texts.get(R.string.gacha_import_success, result.inserted)
                    } else {
                        texts.get(R.string.gacha_import_deduplicated, result.inserted, result.skipped)
                    }
                    _event.send(
                        GachaEvent.ShowMessage(message)
                    )
                },
                onFailure = { error ->
                    Timber.tag("Gacha").e(error, "Gacha record import failed")
                    _uiState.update { it.copy(isSyncing = false) }
                    _event.send(GachaEvent.ShowError(error.message ?: texts.get(R.string.error_gacha_import)))
                },
            )
        }
    }

    fun checkoutAccount(account: Account) {
        execute {
            if (account is AccountGc) {
                repoAcc.setCurrentAccountGc(account)
                    .onFailure { error ->
                        val message = error.message ?: texts.get(R.string.error_account_switch)
                        reportError(message)
                    }
            }
        }
    }

    fun selectPool(poolId: String) {
        _uiState.update { state ->
            if (state.selectedPoolId == poolId) state else state.copy(selectedPoolId = poolId)
        }
    }

    fun setRawDataFilters(sixStarOnly: Boolean, newOnly: Boolean) {
        _uiState.update {
            it.copy(filterSixStar = sixStarOnly, filterNew = newOnly)
        }
    }

    fun setPoolExpanded(poolId: String, expanded: Boolean) {
        _uiState.update { state ->
            state.copy(
                expandedPoolIds = if (expanded) {
                    state.expandedPoolIds + poolId
                } else {
                    state.expandedPoolIds - poolId
                }
            )
        }
    }

    private fun requireCurrentAccount(): AccountGc? {
        val account = _uiState.value.currAccount
        if (account == null) {
            _uiState.update { it.copy(status = UiStatus.Empty(texts.get(R.string.gacha_add_account_hint))) }
            _event.trySend(GachaEvent.ShowMessage(texts.get(R.string.gacha_add_account_hint)))
        }
        return account
    }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (_: CancellationException) {
            } catch (error: Exception) {
                Timber.tag("Gacha").e(error, "Gacha operation failed")
                reportError(error.message ?: texts.get(R.string.error_operation_failed))
            }
        }
    }

    private suspend fun reportError(message: String) {
        val readable = userFacingError(texts, message)
        _uiState.update { it.copy(status = UiStatus.Error(readable)) }
        _event.send(GachaEvent.ShowError(readable))
    }

}
