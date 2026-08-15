package com.blueskybone.arkscreen.ui.character

import android.content.Intent
import android.os.Bundle
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
import com.blueskybone.arkscreen.ui.common.formatSyncTime
import com.blueskybone.arkscreen.ui.UiStatus
import com.hjq.toast.Toaster
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
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
            binding.TabLayout.selectTab(binding.TabLayout.getTabAt(position))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCharAssetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBinding()
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
    }

    private fun setupObserver() {
        model.uiState.observe(this) { value ->
            when (value) {
                is UiStatus.Loading -> displayLoadingView(value.message ?: "加载中...")
                is UiStatus.Error -> displayErrorView(value.message)
                is UiStatus.Empty -> displayEmptyView(value.message)
                is UiStatus.Success -> displayView()
                UiStatus.Idle -> Unit
            }
        }
        model.lastSyncAt.observe(this) {
            renderToolbarSubtitle()
        }
        model.syncingState.observe(this) {
            renderToolbarSubtitle()
        }
    }

    private fun renderToolbarSubtitle() {
        binding.Toolbar.subtitle = when {
            model.syncingState.value == true ->
                getString(com.blueskybone.arkscreen.R.string.syncing)

            model.lastSyncAt.value != null ->
                getString(
                    com.blueskybone.arkscreen.R.string.last_synced_at,
                    formatSyncTime(requireNotNull(model.lastSyncAt.value)),
                )

            else -> null
        }
    }

    private fun displayLoadingView(msg: String) {
        binding.StatusProgress.visibility = View.VISIBLE
        binding.StatusIllustration.visibility = View.GONE
        binding.Page.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        binding.EmptyAction.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayErrorView(msg: String) {
        binding.StatusProgress.visibility = View.GONE
        binding.StatusIllustration.visibility = View.VISIBLE
        binding.Page.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        configureRecoveryAction(msg)
        binding.Message.text = msg
    }

    private fun displayEmptyView(msg: String) {
        binding.StatusProgress.visibility = View.GONE
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
                binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.retry)
                binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_refresh)
                binding.EmptyAction.setOnClickListener { model.refresh() }
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
