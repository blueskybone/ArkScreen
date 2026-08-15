package com.blueskybone.arkscreen.ui.realtime

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import coil.load
import com.blueskybone.arkscreen.util.launchApp
import com.blueskybone.arkscreen.databinding.ActivityRealTimeBinding
import com.blueskybone.arkscreen.databinding.InfoCardBinding
import com.blueskybone.arkscreen.ui.common.bindinginfo.Campaign
import com.blueskybone.arkscreen.ui.common.bindinginfo.DataInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.Dormitories
import com.blueskybone.arkscreen.ui.common.bindinginfo.Labor
import com.blueskybone.arkscreen.ui.common.bindinginfo.Manufactures
import com.blueskybone.arkscreen.ui.common.bindinginfo.Meeting
import com.blueskybone.arkscreen.ui.common.bindinginfo.Recruit
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitRefresh
import com.blueskybone.arkscreen.ui.common.bindinginfo.Tired
import com.blueskybone.arkscreen.ui.common.bindinginfo.Trading
import com.blueskybone.arkscreen.ui.common.bindinginfo.Train
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.blueskybone.arkscreen.ui.common.LogManagerActivity
import com.blueskybone.arkscreen.ui.common.formatSyncTime
import com.blueskybone.arkscreen.ui.common.renderSyncing
import com.blueskybone.arkscreen.ui.account.LoginWeb
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeUi
import com.hjq.toast.Toaster
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.launch

/**
 *   Created by blueskybone
 *   Date: 2025/1/5
 */

class RealTimeActivity : AppCompatActivity() {

    private var _binding: ActivityRealTimeBinding? = null
    private val binding get() = _binding!!

    private val model: RealTimeModel by viewModel()
    private var refreshMenuItem: MenuItem? = null
    private var currentOfficial = true
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
        _binding = ActivityRealTimeBinding.inflate(layoutInflater)
        setUpBinding()
        setupObserver()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.event.collect(Toaster::show)
            }
        }
        setContentView(binding.root)
    }

    private fun setUpBinding() {
        binding.Recruit.setUp(Recruit)
        binding.RecruitReFresh.setUp(RecruitRefresh)
        binding.Labor.setUp(Labor)
        binding.Meeting.setUp(Meeting)
        binding.Manufactures.setUp(Manufactures)
        binding.Trading.setUp(Trading)
        binding.Dormitories.setUp(Dormitories)
        binding.Tired.setUp(Tired)
        binding.Train.setUp(Train)
        binding.Campaign.setUp(Campaign)
        binding.Starter.setOnClickListener {
            if (currentOfficial) {
                launchApp("com.hypergryph.arknights") {
                    Toaster.show(getString(com.blueskybone.arkscreen.R.string.official_game_not_found))
                }
            } else {
                launchApp("com.hypergryph.arknights.bilibili") {
                    Toaster.show(getString(com.blueskybone.arkscreen.R.string.bilibili_game_not_found))
                }
            }
        }
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun InfoCardBinding.setUp(dataInfo: DataInfo) {
        Title.text = getString(dataInfo.title)
        Value.setTextColor(getColor(dataInfo.color))
    }

    private fun InfoCardBinding.setUp(pairInfo: RealTimeUi.PairInfo) {
        Time.text = pairInfo.time.resolve(this@RealTimeActivity)
        Value.text = pairInfo.value.resolve(this@RealTimeActivity)
        Notify.visibility = if(pairInfo.notify) View.VISIBLE else View.GONE
    }

    private fun setupObserver() {
        model.state.observe(this) { state ->
            when (state) {
                RealTimeScreenState.Loading -> displayLoadingView()
                RealTimeScreenState.Empty ->
                    displayWarningView(getString(com.blueskybone.arkscreen.R.string.realtime_no_account))
                is RealTimeScreenState.Error -> displayErrorView(state.kind)
                is RealTimeScreenState.Content -> {
                    binding.Toolbar.subtitle =
                        getString(com.blueskybone.arkscreen.R.string.last_synced_at, formatSyncTime(state.syncedAt))
                    displayView(state.data)
                }
            }
        }
        model.syncingState.observe(this) { syncing ->
            refreshMenuItem?.renderSyncing(this, syncing)
            if (syncing) {
                binding.Toolbar.subtitle = getString(com.blueskybone.arkscreen.R.string.syncing)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.blueskybone.arkscreen.R.menu.toolbar_sync_menu, menu)
        refreshMenuItem = menu.findItem(com.blueskybone.arkscreen.R.id.menu_refresh)
        refreshMenuItem?.renderSyncing(this, model.syncingState.value == true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.blueskybone.arkscreen.R.id.menu_refresh -> {
                binding.Toolbar.subtitle = getString(com.blueskybone.arkscreen.R.string.syncing)
                model.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayLoadingView() {
        binding.StatusProgress.visibility = View.VISIBLE
        binding.StatusIllustration.visibility = View.GONE
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        binding.EmptyAction.visibility = View.GONE
        binding.Message.setText(com.blueskybone.arkscreen.R.string.loading)
    }

    private fun displayErrorView(kind: RealTimeFailureKind) {
        binding.StatusProgress.visibility = View.GONE
        binding.StatusIllustration.visibility = View.VISIBLE
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.EmptySummary.visibility = View.GONE
        configureRecoveryAction(kind)
        binding.Message.setText(
            when (kind) {
                RealTimeFailureKind.NETWORK -> com.blueskybone.arkscreen.R.string.realtime_network_failed
                RealTimeFailureKind.TIMEOUT -> com.blueskybone.arkscreen.R.string.realtime_timeout
                RealTimeFailureKind.AUTH_EXPIRED -> com.blueskybone.arkscreen.R.string.realtime_login_expired
                RealTimeFailureKind.OTHER -> com.blueskybone.arkscreen.R.string.realtime_load_failed
            }
        )
    }

    private fun displayWarningView(msg: String) {
        binding.StatusProgress.visibility = View.GONE
        binding.StatusIllustration.visibility = View.VISIBLE
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.EmptySummary.visibility = View.VISIBLE
        binding.EmptyAction.visibility = View.VISIBLE
        binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.add_skland_account)
        binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_add)
        binding.EmptyAction.setOnClickListener {
            startActivity(Intent(this, AccountMngActivity::class.java))
        }
        binding.Message.text = msg
    }

    private fun configureRecoveryAction(kind: RealTimeFailureKind) {
        binding.EmptyAction.visibility = View.VISIBLE
        when (kind) {
            RealTimeFailureKind.NETWORK,
            RealTimeFailureKind.TIMEOUT -> {
                binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.retry)
                binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_refresh)
                binding.EmptyAction.setOnClickListener { model.refresh() }
            }
            RealTimeFailureKind.AUTH_EXPIRED -> {
                binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.relogin)
                binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_user)
                binding.EmptyAction.setOnClickListener {
                    reauthLauncher.launch(LoginWeb.startIntent(this, LoginWeb.Companion.LoginType.SKLAND))
                }
            }
            RealTimeFailureKind.OTHER -> {
                binding.EmptyAction.setText(com.blueskybone.arkscreen.R.string.view_logs)
                binding.EmptyAction.setIconResource(com.blueskybone.arkscreen.R.drawable.ic_log)
                binding.EmptyAction.setOnClickListener {
                    startActivity(Intent(this, LogManagerActivity::class.java))
                }
            }
        }
    }

    private fun displayView(data: RealTimeUi) {
        binding.Ap.text = data.apNow.toString()
        binding.ApMax.text =
            getString(com.blueskybone.arkscreen.R.string.realtime_ap_max, data.apMax)
        binding.CircularProgressBar.apply {
            progressMax = data.apMax.toFloat().coerceAtLeast(1F)
            progress = data.apNow.toFloat().coerceIn(0F, progressMax)
        }
        binding.Avatar.load(data.avatarUrl)
        binding.Level.text = getString(com.blueskybone.arkscreen.R.string.realtime_level, data.level)
        binding.ApFullTime.text = data.apFullTime.resolve(this)
        binding.ApResTime.text = data.apResTime.resolve(this)
        binding.NickName.text = data.nickName
        binding.LastLogin.text = data.lastLogin.resolve(this)
        binding.Server.setText(
            if (data.official) {
                com.blueskybone.arkscreen.R.string.official_server
            } else {
                com.blueskybone.arkscreen.R.string.bilibili_server
            }
        )
        binding.Recruit.setUp(data.recruit)
        binding.RecruitReFresh.setUp(data.recruitRefresh)
        binding.Labor.setUp(data.labor)
        binding.Meeting.setUp(data.meeting)
        binding.Manufactures.setUp(data.manufacture)
        binding.Trading.setUp(data.trading)
        binding.Dormitories.setUp(data.dormitories)
        binding.Tired.setUp(data.tired)
        binding.Train.setUp(data.train)
        binding.Campaign.setUp(data.campaign)

        binding.TrainChange.visibility =
            if (data.logosChange != null || data.ireneChange != null) View.VISIBLE else View.GONE
        binding.Logos.Layout.visibility =
            if (data.logosChange != null) View.VISIBLE else View.GONE
        binding.Logos.Text.text = data.logosChange?.text?.resolve(this).orEmpty()
        binding.Irene.Layout.visibility =
            if (data.ireneChange != null) View.VISIBLE else View.GONE
        binding.Irene.Text.text = data.ireneChange?.text?.resolve(this).orEmpty()
        currentOfficial = data.official
        binding.Page.visibility = View.GONE
        binding.ScrollView.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
