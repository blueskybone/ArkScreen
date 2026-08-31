package com.blueskybone.arkscreen.ui.gacha

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityGachaBinding
import com.blueskybone.arkscreen.ui.UiStatus
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.blueskybone.arkscreen.ui.account.LoginWeb
import com.blueskybone.arkscreen.ui.common.ErrorRecovery
import com.blueskybone.arkscreen.ui.common.LogManagerActivity
import com.blueskybone.arkscreen.ui.common.recoveryFor
import com.blueskybone.arkscreen.ui.common.showDestructiveConfirmation
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */

class GachaActivity : AppCompatActivity() {
    private val model: GachaModel by viewModel()

    private var _binding: ActivityGachaBinding? = null
    private val binding get() = _binding!!
    private var launcherForTxt: ActivityResultLauncher<String>? = null
    private var launcherForJson: ActivityResultLauncher<String>? = null
    private var launcherForImport: ActivityResultLauncher<Array<String>>? = null
    private var toolbarMenu: Menu? = null
    private var latestState = GachaUiState()
    private var isFileOperationRunning = false
    private val reauthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        val token = data.getStringExtra("token")
        val userCenter = data.getStringExtra("userCenter")
        val xrToken = data.getStringExtra("xrToken")
        val channelMasterId = data.getIntExtra("channelMasterId", 1)
        if (!token.isNullOrBlank() && !userCenter.isNullOrBlank() && !xrToken.isNullOrBlank()) {
            model.reauthenticate(token, userCenter, xrToken, channelMasterId)
        }
    }
    private val pageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.TabLayout.selectTab(binding.TabLayout.getTabAt(position))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityGachaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBinding()
        binding.ViewPager.setCurrentItem(savedInstanceState?.getInt(KEY_TAB) ?: 0, false)
        registerLauncher()
        observeState()
        observeEvents()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect { state ->
                    latestState = state
                    toolbarMenu?.let(::renderToolbarMenu)
                    val hasCachedRecords = state.gachaUiSnapshot?.records?.isNotEmpty() == true
                    when (val status = state.status) {
                        is UiStatus.Loading -> {
                            if (hasCachedRecords) showContent()
                            else showStatus(status.message ?: "加载中...", loading = true)
                        }
                        is UiStatus.Empty -> showStatus(status.message, showAction = true)
                        is UiStatus.Error -> {
                            if (hasCachedRecords) showContent()
                            else showError(status.message)
                        }
                        is UiStatus.Success -> showContent()
                        UiStatus.Idle -> Unit
                    }
                }
            }
        }
    }

    private fun showStatus(
        message: String,
        showAction: Boolean = false,
        loading: Boolean = false,
    ) {
        binding.StatusProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.StatusIllustration.visibility = if (loading) View.GONE else View.VISIBLE
        binding.Message.text = message
        binding.EmptySummary.visibility = if (showAction) View.VISIBLE else View.GONE
        binding.EmptyAction.visibility = if (showAction) View.VISIBLE else View.GONE
        if (showAction) {
            binding.EmptyAction.setText(R.string.add_gacha_account)
            binding.EmptyAction.setIconResource(R.drawable.ic_add)
            binding.EmptyAction.setOnClickListener {
                startActivity(Intent(this, AccountMngActivity::class.java))
            }
        }
        binding.Page.visibility = View.VISIBLE
        binding.TabLayout.visibility = View.GONE
        binding.ViewPager.visibility = View.GONE
    }

    private fun showContent() {
        binding.Page.visibility = View.GONE
        binding.TabLayout.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        showStatus(message)
        binding.EmptyAction.visibility = View.VISIBLE
        when (recoveryFor(message)) {
            ErrorRecovery.RETRY -> {
                binding.EmptyAction.setText(R.string.retry)
                binding.EmptyAction.setIconResource(R.drawable.ic_refresh)
                binding.EmptyAction.setOnClickListener { model.retrySync() }
            }
            ErrorRecovery.RELOGIN -> {
                binding.EmptyAction.setText(R.string.relogin)
                binding.EmptyAction.setIconResource(R.drawable.ic_user)
                binding.EmptyAction.setOnClickListener {
                    launchReauthentication()
                }
            }
            ErrorRecovery.VIEW_LOGS -> {
                binding.EmptyAction.setText(R.string.view_logs)
                binding.EmptyAction.setIconResource(R.drawable.ic_log)
                binding.EmptyAction.setOnClickListener {
                    startActivity(Intent(this, LogManagerActivity::class.java))
                }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.event.collect { event ->
                    when (event) {
                        is GachaEvent.ShowError -> Toaster.show(event.message)
                        is GachaEvent.ShowMessage -> Toaster.show(event.message)
                    }
                }
            }
        }
    }

    private fun setUpBinding() {

        val vp = binding.ViewPager
        val ta = binding.TabLayout
        vp.adapter = ViewPagerFragmentAdapter(this)

        ta.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                vp.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        vp.registerOnPageChangeCallback(pageChangeCallback)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.Toolbar.subtitle = null
        binding.EmptyAction.setOnClickListener {
            startActivity(Intent(this, AccountMngActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_gacha_menu, menu)
        toolbarMenu = menu
        renderToolbarMenu(menu)
        return true
    }

    private fun renderToolbarMenu(menu: Menu) {
        val hasAccount = latestState.currAccount != null
        val hasRecords = latestState.gachaUiSnapshot?.records?.isNotEmpty() == true
        val available = !latestState.isSyncing && !isFileOperationRunning
        menu.findItem(R.id.menu_export)?.isEnabled = available && hasAccount && hasRecords
        menu.findItem(R.id.menu_import)?.isEnabled = available && hasAccount
        menu.findItem(R.id.menu_clear)?.isEnabled = available && hasAccount && hasRecords
        menu.findItem(R.id.gacha_correct)?.isEnabled = available && hasAccount && hasRecords
    }

    inner class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> GachaFragment()
                1 -> GachaStatsFragment()
                else -> GachaTextFragment()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_export -> {
                MenuDialog(this)
                    .add(getString(R.string.file_txt)) {
                        launcherForTxt?.launch("${model.exportFileBaseName()}.txt")
                    }.add(getString(R.string.file_json)) {
                        launcherForJson?.launch("${model.exportFileBaseName()}.json")
                    }.show()
                true
            }

            R.id.menu_import -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.import_data))
                    .setMessage(R.string.import_data_detail)
                    .setPositiveButton(R.string.import_data) { _, _ ->
                        val mimeTypes = arrayOf("text/plain", "application/json")
                        launcherForImport?.launch(mimeTypes)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }

            R.id.menu_clear -> {
                showDestructiveConfirmation(
                    titleRes = R.string.clear_data,
                    message = getString(R.string.confirm_clear_gacha_detail),
                    actionRes = R.string.clear,
                    onConfirm = model::deleteRecords,
                )
                true
            }

            R.id.gacha_correct -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.gacha_correct))
                    .setMessage(R.string.gacha_correct_detail)
                    .setPositiveButton(R.string.confirm) { _, _ -> model.correctUnCateRecord() }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun registerLauncher() {
        launcherForTxt = registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
            uri ?: return@registerForActivityResult
            writeExport(uri, model::buildExportText)
        }
        launcherForJson = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri ?: return@registerForActivityResult
            writeExport(uri, model::buildExportJson)
        }
        launcherForImport = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri ?: return@registerForActivityResult
            lifecycleScope.launch {
                setFileOperationRunning(true)
                val result = runCatching {
                    val content = readImportFile(uri)
                    model.prepareImport(readDisplayName(uri), content).getOrThrow()
                }
                setFileOperationRunning(false)
                result.onSuccess(::confirmImport)
                    .onFailure {
                        Toaster.show(getString(R.string.operation_import_failed, it.message))
                    }
            }
        }
    }

    private fun writeExport(
        uri: android.net.Uri,
        contentProvider: suspend () -> Result<String>,
    ) {
        lifecycleScope.launch {
            setFileOperationRunning(true)
            val result = runCatching {
                val content = contentProvider().getOrThrow()
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                        it.write(content)
                    } ?: error("无法创建导出文件")
                }
            }
            setFileOperationRunning(false)
            result.onSuccess { Toaster.show(getString(R.string.operation_export_completed)) }
                .onFailure {
                    Toaster.show(getString(R.string.operation_export_failed, it.message))
                }
        }
    }

    private suspend fun readImportFile(uri: android.net.Uri): String =
        withContext(Dispatchers.IO) {
            contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                val length = descriptor.length
                require(length < 0 || length <= MAX_IMPORT_BYTES) { "导入文件不能超过 20 MB" }
            }
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                val result = StringBuilder()
                val buffer = CharArray(8 * 1024)
                while (true) {
                    val count = reader.read(buffer)
                    if (count < 0) break
                    require(result.length + count <= MAX_IMPORT_CHARS) {
                        "导入文件不能超过 20 MB"
                    }
                    result.append(buffer, 0, count)
                }
                result.toString()
            } ?: error("无法读取导入文件")
        }

    private fun readDisplayName(uri: android.net.Uri): String? {
        val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
        return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
        }
    }

    private fun confirmImport(payload: com.blueskybone.arkscreen.data.gacha.GachaImportPayload) {
        val target = latestState.currAccount ?: run {
            Toaster.show(getString(R.string.select_gacha_account_first))
            return
        }
        val source = when {
            payload.sourceName != null && payload.sourceUid != null ->
                "${payload.sourceName}（${payload.sourceUid}）"
            payload.sourceUid != null -> payload.sourceUid
            else -> "未知账号"
        }
        val targetLabel = "${target.nickName}（${target.uid}）"
        val accountWarning = if (
            payload.sourceUid != null && payload.sourceUid != target.uid
        ) {
            "\n\n注意：备份来源与当前账号不同。"
        } else {
            ""
        }
        val warningText = payload.warnings
            .filter { warning -> warning.code in DISPLAYED_IMPORT_WARNING_CODES }
            .takeIf(List<*>::isNotEmpty)
            ?.joinToString(separator = "\n", prefix = "\n\n注意：\n") { "• ${it.message}" }
            .orEmpty()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.import_data)
            .setMessage(
                "来源：$source\n目标：$targetLabel\n记录：${payload.records.size} 条$accountWarning$warningText"
            )
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.import_data) { _, _ ->
                model.importRecords(payload, target.uid)
            }
            .show()
    }

    private fun setFileOperationRunning(running: Boolean) {
        isFileOperationRunning = running
        toolbarMenu?.let(::renderToolbarMenu)
    }

    private fun launchReauthentication() {
        val type = if (model.uiState.value.currAccount?.official != false) {
            LoginWeb.Companion.LoginType.GACHA_OFFICIAL
        } else {
            LoginWeb.Companion.LoginType.GACHA_BILI
        }
        reauthLauncher.launch(LoginWeb.startIntent(this, type))
    }

    override fun onDestroy() {
        binding.ViewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.ViewPager.adapter = null
        _binding = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_TAB, binding.ViewPager.currentItem)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val KEY_TAB = "gacha_tab"
        const val MAX_IMPORT_BYTES = 20L * 1024L * 1024L
        const val MAX_IMPORT_CHARS = 20 * 1024 * 1024
        val DISPLAYED_IMPORT_WARNING_CODES = setOf(
            com.blueskybone.arkscreen.data.gacha.GachaImportWarning.Code.UNKNOWN_POOL,
            com.blueskybone.arkscreen.data.gacha.GachaImportWarning.Code.UNMATCHED_OPERATOR,
            com.blueskybone.arkscreen.data.gacha.GachaImportWarning.Code.AMBIGUOUS_OPERATOR,
        )
    }
}
