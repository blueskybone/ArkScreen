package com.blueskybone.arkscreen.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityAccountMngBinding
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.ui.account.adapter.AccountAdapter
import com.blueskybone.arkscreen.ui.account.common.AccountDialogHelper
import com.blueskybone.arkscreen.ui.account.model.AccountItemAction
import com.blueskybone.arkscreen.ui.UiStatus
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.blueskybone.arkscreen.ui.common.setDebouncedClickListener
import com.blueskybone.arkscreen.util.copyToClipboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 *   Created by blueskybone
 *   Date: 2025/11/1
 */
class AccountMngActivity : AppCompatActivity() {

    private val model: AccountModel by viewModel()
    private lateinit var binding: ActivityAccountMngBinding
    private lateinit var dialogHelper: AccountDialogHelper

    private lateinit var skAdapter: AccountAdapter
    private lateinit var gcAdapter: AccountAdapter
    private lateinit var efAdapter: AccountAdapter

    private lateinit var skLoginLauncher: ActivityResultLauncher<Intent>
    private lateinit var gcLoginLauncher: ActivityResultLauncher<Intent>

    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountMngBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialogHelper = AccountDialogHelper(this, layoutInflater)

        initToolbar()
        initBackPress()
        initAdapters()
        observeUi()
        observeOperating()
        initLaunchers()
        initClicks()
    }

    private fun observeUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 合并多个流的收集
                launch {
                    model.accountSkUiList.collect { list ->
                        skAdapter.submitList(list)
                        updateSectionVisibility(
                            hasData = list.isNotEmpty(),
                            header = binding.ArknightsHeader,
                            card = binding.ArknightsCard,
                            empty = binding.ArknightsEmpty,
                            alwaysShow = true
                        )
                    }
                }

                launch {
                    model.accountGcUiList.collect { list ->
                        gcAdapter.submitList(list)
                        updateSectionVisibility(
                            hasData = list.isNotEmpty(),
                            header = binding.GachaHeader,
                            card = binding.GachaCard,
                            empty = binding.GachaEmpty,
                            alwaysShow = true
                        )
                    }
                }

                launch {
                    model.accountEfUiList.collect { list ->
                        efAdapter.submitList(list)
                        updateSectionVisibility(
                            hasData = list.isNotEmpty(),
                            header = binding.EndfieldHeader,
                            card = binding.EndfieldCard,
                            empty = binding.EndfieldEmpty,
                            alwaysShow = true
                        )
                    }
                }

                // 收集事件流
                launch {
                    model.event.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> Toaster.show(event.message)
            is UiEvent.ShowError -> Toaster.show(event.message)
        }
    }

    //加载中
    private fun observeOperating() {
        lifecycleScope.launch {
            model.operationStatus
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { status ->
                    // 这里建议直接控制显隐
                    toggleLoading(status is UiStatus.Loading)
                }
        }
    }

    private fun toggleLoading(isOperating: Boolean) {
        binding.AddAccountSk.isEnabled = !isOperating
        binding.AddAccountGc.isEnabled = !isOperating
        if (isOperating) {
            if (loadingDialog == null) {
                loadingDialog = MaterialAlertDialogBuilder(this)
                    .setView(R.layout.dialog_operation_loading)
                    .setCancelable(false)
                    .create()
            }
            if (loadingDialog?.isShowing == false) {
                loadingDialog?.show()
            }
        } else {
            if (loadingDialog?.isShowing == true) {
                loadingDialog?.dismiss()
            }
        }
    }


    private fun updateSectionVisibility(
        hasData: Boolean,
        header: View,
        card: View,
        empty: View?,
        alwaysShow: Boolean
    ) {
        val showSection = alwaysShow || hasData
        header.visibility = if (showSection) View.VISIBLE else View.GONE
        card.visibility = if (showSection) View.VISIBLE else View.GONE
        empty?.visibility = if (showSection && !hasData) View.VISIBLE else View.GONE
    }


    private fun createItemAction(type: AccountType): AccountItemAction {
        return object : AccountItemAction {
            override fun onClick(account: Account) {
                if (type == AccountType.EF) {
                    Toaster.show(getString(R.string.not_support_set_default))
                    return
                }
                model.setDefaultAccount(account)
                Toaster.show(getString(R.string.set_default_account, account.nickName))
            }

            override fun onLongClick(account: Account) {
                dialogHelper.buildAccountMenu(
                    onCopyUid = {
                        copyToClipboard(this@AccountMngActivity, account.uid)
                        Toaster.show(getString(R.string.uid_copied))
                    },
                    onCopyNickname = {
                        copyToClipboard(this@AccountMngActivity, account.nickName)
                        Toaster.show(getString(R.string.nickname_copied))
                    },
                    onExportCookie = buildExportAction(account),
                    onDelete = {
                        dialogHelper.showDeleteConfirm {
                            model.deleteAccount(account)
                        }
                    }
                ).show()
            }
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun initAdapters() {
        skAdapter = AccountAdapter(createItemAction(AccountType.SK))
        gcAdapter = AccountAdapter(createItemAction(AccountType.GC))
        efAdapter = AccountAdapter(createItemAction(AccountType.EF))

        binding.RecyclerView.adapter = skAdapter
        binding.RecyclerViewGc.adapter = gcAdapter
        binding.RecyclerViewEf.adapter = efAdapter
    }

    private fun initLaunchers() {
        skLoginLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                handleSkLoginResult(result.data)
            }
        }

        gcLoginLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                handleGcLoginResult(result.data)
            }
        }
    }

    private fun initClicks() {
        binding.AddAccountSk.setDebouncedClickListener {
            showAddAccountMenu(AccountType.SK)
        }

        binding.AddAccountGc.setDebouncedClickListener {
            showAddAccountMenu(AccountType.GC)
        }

        binding.SkInfo.setOnClickListener {
            dialogHelper.showInfoDialog(R.string.account_info, R.string.sk_account_info)
        }

        binding.GcInfo.setOnClickListener {
            dialogHelper.showInfoDialog(R.string.account_info, R.string.gc_account_info)
        }
    }


    private fun buildExportAction(account: Account): (() -> Unit) {
        return {
            model.generateAccountCookie(account).fold(
                onSuccess = { cookie ->
                    dialogHelper.showExportDialog(cookie) {
                        copyToClipboard(this, cookie)
                    }
                },
                onFailure = { error ->
                    Toaster.show(error.message ?: "账号导出失败")
                },
            )
        }
    }

    private fun showAddAccountMenu(source: AccountType) {
        val menu = MenuDialog(this)

        when (source) {
            AccountType.SK -> {
                menu.add(getString(R.string.import_cookie)) {
                    dialogHelper.showCookieLoginDialog(
                        onConfirm = { input -> handleCookieLogin(AccountType.SK, input) }
                    )
                }
                menu.add(R.string.web_login) {
                    val intent = LoginWeb.startIntent(
                        this,
                        LoginWeb.Companion.LoginType.SKLAND
                    )
                    skLoginLauncher.launch(intent)
                }
                menu.add(R.string.password_login) {
                    dialogHelper.showPasswordLoginDialog { phone, password ->
                        doPasswordLogin(AccountType.SK, phone, password)
                    }
                }
            }

            AccountType.GC -> {
                menu.add(getString(R.string.import_cookie)) {
                    dialogHelper.showCookieLoginDialog(
                        onConfirm = { input -> handleCookieLogin(AccountType.GC, input) }
                    )
                }
                menu.add(R.string.web_login_official) {
                    val intent = LoginWeb.startIntent(
                        this,
                        LoginWeb.Companion.LoginType.GACHA_OFFICIAL
                    )
                    gcLoginLauncher.launch(intent)
                }
                menu.add(R.string.web_login_bili) {
                    val intent = LoginWeb.startIntent(
                        this,
                        LoginWeb.Companion.LoginType.GACHA_BILI
                    )
                    gcLoginLauncher.launch(intent)
                }
            }

            AccountType.EF -> Unit
        }

        menu.show()
    }

    private fun handleSkLoginResult(data: Intent?) {
        val token = data?.getStringExtra("token")
        val dId = data?.getStringExtra("dId")

        if (token.isNullOrBlank() || dId.isNullOrBlank()) {
            Toaster.show(getString(R.string.token_fetch_failed))
            return
        }
        model.loginSklandByToken(token, dId)
    }

    private fun handleGcLoginResult(data: Intent?) {
        val token = data?.getStringExtra("token").orEmpty()
        val xrToken = data?.getStringExtra("xrToken")
        val userCenter = data?.getStringExtra("userCenter")
        val channelMasterId = data?.getIntExtra("channelMasterId", 1) ?: 1

        if (xrToken.isNullOrBlank() || userCenter.isNullOrBlank()) {
            Toaster.show(getString(R.string.token_fetch_failed))
            return
        }

        model.loginGameByToken(token, userCenter, xrToken, channelMasterId)
    }

    private fun handleCookieLogin(type: AccountType, cookie: String) {
        when (type) {
            AccountType.SK, AccountType.EF -> model.loginSklandByCookie(cookie)
            AccountType.GC -> model.loginGameByCookie(cookie)
        }
    }

    private fun doPasswordLogin(type: AccountType, phone: String, password: String) {
        when (type) {
            AccountType.SK -> model.loginSklandByPhonePassword(phone, password)
            else -> Unit
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()
        loadingDialog = null
        binding.RecyclerView.adapter = null
        binding.RecyclerViewGc.adapter = null
        binding.RecyclerViewEf.adapter = null
        super.onDestroy()
    }
}
