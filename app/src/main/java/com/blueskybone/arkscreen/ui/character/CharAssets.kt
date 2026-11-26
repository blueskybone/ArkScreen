package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.ui.account.common.bindAccount
import android.content.Intent
import androidx.appcompat.widget.ListPopupWindow
import com.blueskybone.arkscreen.ui.account.common.showAccountChoices
import android.os.Bundle
import com.blueskybone.arkscreen.R
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blueskybone.arkscreen.databinding.ActivityCharAssetsBinding
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.blueskybone.arkscreen.ui.account.LoginWeb
import com.blueskybone.arkscreen.ui.common.ErrorRecovery
import com.blueskybone.arkscreen.ui.common.LogManagerActivity
import com.blueskybone.arkscreen.ui.common.recoveryFor
import com.blueskybone.arkscreen.ui.UiStatus
import com.hjq.toast.Toaster
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.launch


/**
 *   Created by blueskybone
 *   Date: 2025/1/18
 */

class CharAssets : AppCompatActivity() {

    private val model: CharViewModel by viewModel()
    private var _binding: ActivityCharAssetsBinding? = null
    private val binding get() = _binding!!
    private var shareAction: (() -> Unit)? = null
    private var shareEnabled = true
    private val reauthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val token = result.data?.getStringExtra("token")
        val dId = result.data?.getStringExtra("dId")
        if (!token.isNullOrBlank() && !dId.isNullOrBlank()) {
            model.reauthenticate(token, dId)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCharAssetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBinding()
        setupObserver()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.event.collect { Toaster.show(it) }
            }
        }
    }

    private fun setupBinding() {
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.RefreshButton.setOnClickListener { model.refresh() }
        binding.ShareButton.setOnClickListener { shareAction?.invoke() }
        binding.FallbackAccount.root.setOnClickListener { chooseAccount(it) }
    }

    private var accountPopup: ListPopupWindow? = null

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupObserver() {
        model.accounts.observe(this) { /* Keep account choices current for the picker. */ }
        model.uiState.observe(this) { value ->
            when (value) {
                is UiStatus.Loading -> displayLoadingView(value.message ?: getString(R.string.status_loading_ellipsis))
                is UiStatus.Error -> displayErrorView(value.message)
                is UiStatus.Empty -> displayEmptyView(value.message)
                is UiStatus.Success -> displayView()
                UiStatus.Idle -> Unit
            }
            renderActions()
        }
        model.currentAccount.observe(this) { binding.FallbackAccount.bindAccount(it) }
        model.syncingState.observe(this) { syncing ->
            binding.RefreshButton.isEnabled = !syncing && model.currentAccount.value != null
            renderLoading()
            renderActions()
        }
    }

    fun setShareAction(action: (() -> Unit)?) {
        shareAction = action
        renderActions()
    }

    fun setShareEnabled(enabled: Boolean) {
        shareEnabled = enabled
        renderActions()
    }

    private fun renderActions() {
        val ui = _binding ?: return
        val assetsVisible = model.uiState.value is UiStatus.Success
        ui.FallbackAccount.root.visibility = if (assetsVisible || model.uiState.value is UiStatus.Loading || model.uiState.value == UiStatus.Idle || model.syncingState.value == true) View.GONE else View.VISIBLE
        ui.ShareButton.visibility = if (assetsVisible) View.VISIBLE else View.GONE
        ui.ShareButton.isEnabled = shareEnabled && shareAction != null
    }

    private fun renderLoading() {
        val syncing = model.syncingState.value == true || model.uiState.value is UiStatus.Loading
        if (binding.Page.visibility != View.VISIBLE || binding.Loading.visibility == View.VISIBLE) {
            binding.Loading.visibility = if (syncing) View.VISIBLE else View.GONE
            if (syncing && binding.AssetsContent.visibility == View.VISIBLE) {
                binding.Loading.setMessage((model.uiState.value as? UiStatus.Loading)?.message?.takeIf { it.isNotBlank() } ?: getString(com.blueskybone.arkscreen.R.string.syncing))
            }
        }
    }

    fun chooseAccount(anchor: View) {
        val accounts = model.accounts.value.orEmpty()
        if (accounts.isEmpty()) {
            startActivity(Intent(this, AccountMngActivity::class.java))
            return
        }
        accountPopup?.dismiss()
        accountPopup = showAccountChoices(anchor, accounts, model.currentAccount.value?.uid) { account ->
            model.selectAccount(account)
        }
    }

    private fun displayLoadingView(msg: String) {
        binding.Loading.visibility = View.VISIBLE
        binding.Loading.setMessage(msg)
        binding.StatusIllustration.visibility = View.GONE
        binding.Page.visibility = View.GONE
        binding.AssetsContent.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        binding.EmptyAction.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayErrorView(msg: String) {
        binding.Loading.visibility = View.GONE
        binding.StatusIllustration.visibility = View.VISIBLE
        binding.Page.visibility = View.VISIBLE
        binding.AssetsContent.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        configureRecoveryAction(msg)
        binding.Message.text = msg
    }

    private fun displayEmptyView(msg: String) {
        binding.Loading.visibility = View.GONE
        binding.StatusIllustration.visibility = View.VISIBLE
        binding.Page.visibility = View.VISIBLE
        binding.AssetsContent.visibility = View.GONE
        binding.EmptySummary.visibility = View.VISIBLE
        binding.EmptyAction.visibility = View.VISIBLE
        binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.add_skland_account)
        binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_add)
        binding.EmptyAction.setOnClickListener {
            startActivity(Intent(this, AccountMngActivity::class.java))
        }
        binding.Message.text = msg
    }

    private fun configureRecoveryAction(message: String) {
        binding.EmptyAction.visibility = View.VISIBLE
        when (recoveryFor(message)) {
            ErrorRecovery.RETRY -> {
                binding.EmptyAction.visibility = View.GONE
            }
            ErrorRecovery.RELOGIN -> {
                binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.relogin)
                binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_user)
                binding.EmptyAction.setOnClickListener {
                    reauthLauncher.launch(
                        LoginWeb.startIntent(this, LoginWeb.Companion.LoginType.SKLAND)
                    )
                }
            }
            ErrorRecovery.VIEW_LOGS -> {
                binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.view_logs)
                binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_log)
                binding.EmptyAction.setOnClickListener {
                    startActivity(Intent(this, LogManagerActivity::class.java))
                }
            }
        }
    }

    private fun displayView() {
        val syncing = model.syncingState.value == true
        binding.Loading.visibility = if (syncing) View.VISIBLE else View.GONE
        if (syncing) binding.Loading.setMessage((model.uiState.value as? UiStatus.Loading)?.message?.takeIf { it.isNotBlank() } ?: getString(com.blueskybone.arkscreen.R.string.syncing))
        binding.Page.visibility = View.GONE
        binding.AssetsContent.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        _binding = null
        accountPopup?.dismiss()
        super.onDestroy()
    }

}
