package com.blueskybone.arkscreen.ui.character

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private var exportAction: (() -> Unit)? = null
    private var exportEnabled = true
    private var exportMenuItem: MenuItem? = null
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
            binding.Toolbar.setTitle(
                if (position == 0) com.blueskybone.arkscreen.R.string.operator_assets
                else com.blueskybone.arkscreen.R.string.not_own
            )
            binding.AccountButton.visibility = if (position == 0) View.GONE else View.VISIBLE
            invalidateOptionsMenu()
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
        binding.AccountButton.setOnClickListener { chooseAccount() }
    }

    fun openMissing() {
        binding.ViewPager.setCurrentItem(1, false)
    }

    fun openAssets() {
        binding.ViewPager.setCurrentItem(0, false)
    }

    fun registerExportAction(action: (() -> Unit)?) {
        exportAction = action
        invalidateOptionsMenu()
    }

    fun setExportEnabled(enabled: Boolean) {
        exportEnabled = enabled
        exportMenuItem?.isEnabled = enabled
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.blueskybone.arkscreen.R.menu.toolbar_char_assets, menu)
        exportMenuItem = menu.findItem(com.blueskybone.arkscreen.R.id.menu_export_assets)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(com.blueskybone.arkscreen.R.id.menu_export_assets)?.apply {
            isVisible = binding.ViewPager.currentItem == 0
            isEnabled = exportEnabled && exportAction != null
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == com.blueskybone.arkscreen.R.id.menu_export_assets) {
            exportAction?.invoke()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

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
        }
        model.currentAccount.observe(this) { account ->
            binding.AccountButton.text = account?.nickName ?: getString(com.blueskybone.arkscreen.R.string.skland_choose_account)
        }
        model.syncingState.observe(this) {
            renderLoading()
        }
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

    fun chooseAccount() {
        val accounts = model.accounts.value.orEmpty()
        if (accounts.isEmpty()) {
            startActivity(Intent(this, AccountMngActivity::class.java))
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(com.blueskybone.arkscreen.R.string.skland_choose_account)
            .setSingleChoiceItems(
                accounts.map { it.nickName }.toTypedArray(),
                accounts.indexOfFirst { it.uid == model.currentAccount.value?.uid },
            ) { dialog, index ->
                model.selectAccount(accounts[index])
                dialog.dismiss()
            }
            .show()
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
        exportAction = null
        exportMenuItem = null
        _binding = null
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
