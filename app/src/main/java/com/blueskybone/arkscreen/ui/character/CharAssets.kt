package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.ui.account.common.bindAccount
import android.content.Intent
import androidx.appcompat.widget.ListPopupWindow
import com.blueskybone.arkscreen.ui.account.common.showAccountChoices
import android.os.Bundle
import com.blueskybone.arkscreen.R
import androidx.activity.OnBackPressedCallback
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
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

    private val model: CharModel by viewModel()
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
    private val pageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            renderActions()
            binding.Toolbar.setTitle(
                if (position == 0) com.blueskybone.arkscreen.R.string.operator_assets
                else com.blueskybone.arkscreen.R.string.not_own
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCharAssetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBinding()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.ViewPager.currentItem == 1) openAssets() else finish()
            }
        })
        binding.ViewPager.setCurrentItem(savedInstanceState?.getInt(KEY_TAB) ?: 0, false)
        setupObserver()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.event.collect { Toaster.show(it) }
            }
        }
    }

    private fun setupBinding() {
        val vp = binding.ViewPager
        vp.adapter = ViewPagerFragmentAdapter(this)
        vp.isUserInputEnabled = false
        vp.registerOnPageChangeCallback(pageChangeCallback)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.RefreshButton.setOnClickListener { model.refresh() }
        binding.ShareButton.setOnClickListener { shareAction?.invoke() }
        binding.FallbackAccount.root.setOnClickListener { chooseAccount(it) }
    }

    fun openMissing() {
        binding.ViewPager.setCurrentItem(1, true)
    }

    private fun openAssets() {
        binding.ViewPager.setCurrentItem(0, true)
    }

    private var accountPopup: ListPopupWindow? = null

    override fun onSupportNavigateUp(): Boolean {
        if (binding.ViewPager.currentItem == 1) openAssets() else finish()
        return true
    }

    private fun setupObserver() {
        model.accounts.observe(this) { /* Keep account choices current for the picker. */ }
        model.uiState.observe(this) { value ->
            when (value) {
                is UiStatus.Loading -> displayLoadingView(value.message ?: "加载中...")
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
        val assetsVisible = model.uiState.value is UiStatus.Success && ui.ViewPager.currentItem == 0
        ui.FallbackAccount.root.visibility = if (assetsVisible) View.GONE else View.VISIBLE
        ui.ShareButton.visibility = if (assetsVisible) View.VISIBLE else View.GONE
        ui.ShareButton.isEnabled = shareEnabled && shareAction != null
    }

    private fun renderLoading() {
        val syncing = model.syncingState.value == true
        if (binding.Page.visibility != View.VISIBLE || binding.Loading.visibility == View.VISIBLE) {
            binding.Loading.visibility = if (syncing) View.VISIBLE else View.GONE
            binding.LoadingMessage.visibility = if (syncing) View.VISIBLE else View.GONE
            if (syncing && binding.ViewPager.visibility == View.VISIBLE) {
                binding.LoadingMessage.setText(com.blueskybone.arkscreen.R.string.syncing)
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
        binding.LoadingMessage.visibility = View.VISIBLE
        binding.LoadingMessage.text = msg
        binding.StatusIllustration.visibility = View.GONE
        binding.Page.visibility = View.GONE
        binding.ViewPager.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        binding.EmptyAction.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayErrorView(msg: String) {
        binding.Loading.visibility = View.GONE
        binding.LoadingMessage.visibility = View.GONE
        binding.StatusIllustration.visibility = View.VISIBLE
        binding.Page.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        configureRecoveryAction(msg)
        binding.Message.text = msg
    }

    private fun displayEmptyView(msg: String) {
        binding.Loading.visibility = View.GONE
        binding.LoadingMessage.visibility = View.GONE
        binding.StatusIllustration.visibility = View.VISIBLE
        binding.Page.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.GONE
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
        binding.LoadingMessage.visibility = if (syncing) View.VISIBLE else View.GONE
        if (syncing) binding.LoadingMessage.setText(com.blueskybone.arkscreen.R.string.syncing)
        binding.Page.visibility = View.GONE
        binding.ViewPager.visibility = View.VISIBLE
    }

    class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CharOwn()
                else -> CharNotOwn()
            }
        }
    }

    override fun onDestroy() {
        binding.ViewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.ViewPager.adapter = null
        _binding = null
        accountPopup?.dismiss()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_TAB, binding.ViewPager.currentItem)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val KEY_TAB = "char_assets_tab"
    }
}
