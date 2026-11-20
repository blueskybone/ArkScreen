package com.blueskybone.arkscreen.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.data.local.room.WidgetConfig
import com.blueskybone.arkscreen.data.local.room.dao.WidgetConfigDao
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Shared by launcher configuration/reconfiguration and the in-app widget list. */
class WidgetAccountConfigActivity : AppCompatActivity() {
    private val configs: WidgetConfigDao by inject()
    private val accounts: AccountRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID || AppWidgetManager.getInstance(this).getAppWidgetInfo(id)?.provider?.packageName != packageName) {
            finish(); return
        }
        lifecycleScope.launch {
            val list = accounts.observeSkAcc().first()
            val config = configs.get(id) ?: WidgetConfig(id)
            val labels = listOf("跟随当前账号") + list.map { "${it.nickName} · ${if (it.official) "官服" else "B服"}" }
            var selected = if (config.accountMode == WidgetConfig.FOLLOW_CURRENT) 0 else
                list.indexOfFirst { it.uid == config.roleUid && it.channelMasterId == config.serverKey }.let { if (it < 0) -1 else it + 1 }
            MaterialAlertDialogBuilder(this@WidgetAccountConfigActivity)
                .setTitle("组件账号")
                .setSingleChoiceItems(labels.toTypedArray(), selected) { _, index -> selected = index }
                .setNegativeButton("取消") { _, _ -> finish() }
                .setPositiveButton("保存", null)
                .setOnCancelListener { finish() }
                .create().apply {
                    setOnShowListener {
                        getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                            if (selected < 0) return@setOnClickListener
                            getButton(android.content.DialogInterface.BUTTON_POSITIVE).isEnabled = false
                            lifecycleScope.launch {
                                val account = list.getOrNull(selected - 1)
                                configs.put(if (selected == 0) WidgetConfig(id) else WidgetConfig(id,
                                    WidgetConfig.FIXED, "arknights", account!!.channelMasterId, account.uid))
                                WidgetReceiver.renderCached(this@WidgetAccountConfigActivity)
                                WidgetReceiver.enqueueUpdate(this@WidgetAccountConfigActivity)
                                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                                dismiss()
                                finish()
                            }
                        }
                    }
                    show()
                }
        }
    }

    companion object {
        fun pendingIntent(context: Context, id: Int): PendingIntent = PendingIntent.getActivity(
            context, id, Intent(context, WidgetAccountConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
