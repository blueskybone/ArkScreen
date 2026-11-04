package com.blueskybone.arkscreen.ui.realtime

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityEndfieldRealtimeBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldRealtimeDomainBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldRealtimeOverviewBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldRealtimeOverviewStatBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldRealtimeProfileBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldRealtimeSettlementBinding
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldProgress
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldPlayerProfile
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldRealtimeSnapshot
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldRecoverableValue
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.blueskybone.arkscreen.platform.time.TimeUtils.getTimeStr
import com.blueskybone.arkscreen.platform.time.TimeUtils.getRemainTimeStr
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EndfieldRealtimeActivity : AppCompatActivity() {
    private val model: EndfieldRealtimeModel by viewModel()
    private lateinit var binding: ActivityEndfieldRealtimeBinding
    private var currentState = EndfieldRealtimeUiState()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Endfield)
        super.onCreate(savedInstanceState)
        binding = ActivityEndfieldRealtimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.AccountButton.setOnClickListener { chooseAccount() }
        binding.RefreshButton.setOnClickListener { model.refresh() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect(::render)
            }
        }
    }

    private fun render(state: EndfieldRealtimeUiState) {
        currentState = state
        val selected = state.accounts.firstOrNull { it.uid == state.selectedUid }
        binding.AccountButton.text = selected?.nickName ?: getString(R.string.endfield_choose_account)
        binding.RefreshButton.isEnabled = selected != null && !state.loading
        binding.Loading.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.ProfileContainer.removeAllViews()
        binding.MetricsContainer.removeAllViews()
        binding.DomainsContainer.removeAllViews()
        binding.DomainHeader.visibility = View.GONE
        binding.StatusMessage.text = when {
            state.accounts.isEmpty() -> getString(R.string.endfield_chars_no_account)
            state.loading -> getString(R.string.endfield_realtime_loading)
            state.error != null -> state.error
            state.snapshot == null -> getString(R.string.realtime_no_data)
            else -> ""
        }
        binding.StatusMessage.visibility = if (state.snapshot != null && !state.loading && state.error == null) {
            View.GONE
        } else {
            View.VISIBLE
        }
        if (!state.loading && state.error == null) {
            state.snapshot?.let(::renderSnapshot)
        }
    }

    private fun renderSnapshot(snapshot: EndfieldRealtimeSnapshot) {
        snapshot.profile?.let(::renderProfile)
        renderOverview(snapshot)
        binding.DomainHeader.visibility = View.VISIBLE
        if (snapshot.domains.isEmpty()) {
            val card = ItemEndfieldRealtimeDomainBinding.inflate(
                layoutInflater, binding.DomainsContainer, false,
            )
            card.Title.setText(R.string.realtime_no_data)
            binding.DomainsContainer.addView(card.root)
        }
        snapshot.domains.forEach { domain ->
            val card = ItemEndfieldRealtimeDomainBinding.inflate(
                layoutInflater, binding.DomainsContainer, false,
            )
            card.Title.text = domain.name?.takeIf(String::isNotBlank)
                ?: getString(R.string.endfield_unnamed_domain)
            card.Level.text = domain.level?.let { getString(R.string.realtime_level, it) }.orEmpty()
            val tickets = domain.dispatchTickets
            card.Total.text = if (tickets?.baseValue == null && tickets?.maxValue == null) "" else {
                "${EndfieldRealtimeDisplay.tenThousands(tickets.baseValue)} / " +
                    EndfieldRealtimeDisplay.tenThousands(tickets.maxValue)
            }
            val percent = EndfieldRealtimeDisplay.progress(tickets?.baseValue, tickets?.maxValue)
            card.Progress.visibility = if (percent == null) View.GONE else View.VISIBLE
            if (percent != null) card.Progress.progress = percent
            if (domain.settlements.isEmpty()) {
                val row = ItemEndfieldRealtimeSettlementBinding.inflate(
                    layoutInflater, card.Settlements, false,
                )
                row.Title.setText(R.string.realtime_no_data)
                card.Settlements.addView(row.root)
            }
            domain.settlements.forEach { settlement ->
                val row = ItemEndfieldRealtimeSettlementBinding.inflate(
                    layoutInflater, card.Settlements, false,
                )
                row.Title.text = settlement.name?.takeIf(String::isNotBlank)
                    ?: getString(R.string.endfield_unnamed_settlement)
                val value = settlement.dispatchTickets
                row.Value.text = "${EndfieldRealtimeDisplay.tenThousands(value.displayValue)} / " +
                    EndfieldRealtimeDisplay.tenThousands(value.maxValue)
                card.Settlements.addView(row.root)
            }
            binding.DomainsContainer.addView(card.root)
        }
    }

    private fun renderProfile(profile: EndfieldPlayerProfile) {
        val card = ItemEndfieldRealtimeProfileBinding.inflate(
            layoutInflater, binding.ProfileContainer, false,
        )
        card.Name.text = profile.name?.takeIf(String::isNotBlank)
            ?: getString(R.string.endfield_unnamed_player)
        card.Level.visibility = if (profile.level == null) View.GONE else View.VISIBLE
        profile.level?.let { card.Level.text = getString(R.string.realtime_level, it) }
        card.WorldLevel.visibility = if (profile.worldLevel == null) View.GONE else View.VISIBLE
        profile.worldLevel?.let {
            card.WorldLevel.text = getString(R.string.endfield_world_level, it)
        }
        val serverName = profile.serverName?.takeIf(String::isNotBlank)
        card.Server.visibility = if (serverName == null) View.GONE else View.VISIBLE
        card.Server.text = serverName.orEmpty()
        card.Metadata.visibility = if (profile.level == null && profile.worldLevel == null && serverName == null) {
            View.GONE
        } else {
            View.VISIBLE
        }
        val roleId = profile.roleId?.takeIf(String::isNotBlank)
        card.RoleId.visibility = if (roleId == null) View.GONE else View.VISIBLE
        card.RoleId.text = roleId?.let { getString(R.string.endfield_player_id, it) }.orEmpty()
        val lastLogin = profile.lastLoginAt?.takeIf { it > 0L }
        card.LastLogin.visibility = if (lastLogin == null) View.GONE else View.VISIBLE
        if (lastLogin != null) {
            card.LastLogin.text = getString(
                R.string.endfield_last_login,
                getTimeStr(lastLogin * 1000L, "yyyy-MM-dd HH:mm"),
            )
        }
        card.Avatar.load(profile.avatarUrl?.takeIf(String::isNotBlank)) {
            placeholder(R.drawable.ic_user)
            error(R.drawable.ic_user)
        }
        binding.ProfileContainer.addView(card.root)
    }

    private fun renderOverview(snapshot: EndfieldRealtimeSnapshot) {
        val card = ItemEndfieldRealtimeOverviewBinding.inflate(
            layoutInflater, binding.MetricsContainer, false,
        )
        card.StaminaValue.text = snapshot.stamina?.let(::format) ?: "—"
        val stamina = snapshot.stamina
        val fullAt = stamina?.fullAt?.takeIf { it > 0 &&
            (stamina.displayValue ?: 0) < (stamina.maxValue ?: 0) }
        if (stamina == null ||
            (fullAt == null && (stamina.displayValue ?: 0) < (stamina.maxValue ?: 0))) {
            card.FullTime.visibility = View.GONE
            card.RemainingTime.visibility = View.GONE
        } else if (fullAt == null) {
            card.FullTime.setText(R.string.realtime_ap_full_recovered)
            card.RemainingTime.setText(R.string.realtime_ap_remaining_recovered)
        } else {
            card.FullTime.text = getString(R.string.realtime_ap_full_at,
                getTimeStr(fullAt * 1000, "MM-dd HH:mm"))
            card.RemainingTime.text = getString(R.string.realtime_ap_remaining,
                getRemainTimeStr((fullAt - System.currentTimeMillis() / 1000).coerceAtLeast(0)))
        }
        card.StaminaProgress.progressMax = snapshot.stamina?.maxValue?.toFloat()?.coerceAtLeast(1f) ?: 1f
        card.StaminaProgress.progress = snapshot.stamina?.displayValue?.toFloat()
            ?.coerceIn(0f, card.StaminaProgress.progressMax) ?: 0f
        listOf(
            R.string.endfield_daily_mission to snapshot.dailyMission,
            R.string.endfield_weekly_mission to snapshot.weeklyMission,
            R.string.endfield_battle_pass to snapshot.battlePass,
        ).forEach { (title, progress) ->
            val item = ItemEndfieldRealtimeOverviewStatBinding.inflate(
                layoutInflater, card.ProgressItems, false,
            )
            item.root.layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f,
            )
            item.Title.setText(title)
            item.Value.text = progress?.let(::format) ?: "—"
            card.ProgressItems.addView(item.root)
        }
        binding.MetricsContainer.addView(card.root)
    }

    private fun format(value: EndfieldRecoverableValue): String =
        "${value.displayValue?.toString() ?: "—"} / ${value.maxValue?.toString() ?: "—"}"

    private fun format(value: EndfieldProgress): String =
        "${value.value?.toString() ?: "—"} / ${value.maximum?.toString() ?: "—"}"

    private fun chooseAccount() {
        val accounts = currentState.accounts
        if (accounts.isEmpty()) {
            startActivity(Intent(this, AccountMngActivity::class.java))
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.endfield_choose_account)
            .setSingleChoiceItems(
                accounts.map { it.nickName }.toTypedArray(),
                accounts.indexOfFirst { it.uid == currentState.selectedUid },
            ) { dialog, index ->
                model.select(accounts[index].uid)
                dialog.dismiss()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
