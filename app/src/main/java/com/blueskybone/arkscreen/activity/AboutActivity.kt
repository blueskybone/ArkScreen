package com.blueskybone.arkscreen.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.data.AppUpdateInfo
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.util.getAppVersionName
import com.hjq.toast.Toaster
import io.noties.markwon.Markwon
import org.koin.android.ext.android.getKoin

/**
 *   Created by blueskybone
 *   Date: 2024/7/29
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        setContentView(R.layout.activity_about)
        title = getString(R.string.about)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val versionText = this.findViewById<TextView>(R.id.text_version)
        val cardCheckUpdate = this.findViewById<CardView>(R.id.card_update)
        val cardOpenLicense = this.findViewById<CardView>(R.id.card_3rd)
        val cardGroup = this.findViewById<CardView>(R.id.card_group)

        versionText.text = getAppVersionName(this)

        cardCheckUpdate.setOnClickListener {
            checkAppUpdate()
        }
        cardOpenLicense.setOnClickListener {
            showDialog(getString(R.string.open_source_license), getString(R.string.open_license))
        }
        cardGroup.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", getString(R.string.qq_group_num))
            clipboard.setPrimaryClip(clip)
            Toaster.show("已复制群号")
        }
    }

    private fun showDialog(content: String, title: String) {
        Thread {
            val builder = AlertDialog.Builder(this)
            val view: View = LayoutInflater.from(this)
                .inflate(R.layout.dialog_content, null)
            val textView = view.findViewById<TextView>(R.id.text_dialog)
            Markwon.create(this).setMarkdown(textView, content)
            builder.setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.confirm) { _, _ -> }
            Handler(Looper.getMainLooper()).post {
                builder.create().show()
            }
        }.start()
    }


    private fun checkAppUpdate() {
        Thread {
            val updateInfo = NetWorkTask.getAppUpdateInfo()
            if (updateInfo == null) {
                Toaster.show("检查更新失败")
                return@Thread
            }
            val oldVersion = getAppVersionName(this)
            if (oldVersion == null) {
                Toaster.show("getAppVersion failed")
                return@Thread
            }
            if (oldVersion < updateInfo.version) {
                showUpdateDialog(updateInfo)
            } else {
                Toaster.show("当前已是最新版本")
            }
        }.start()
    }

    private fun showUpdateDialog(updateInfo: AppUpdateInfo) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val textView = TextView(this)
        val layoutParams = android.app.ActionBar.LayoutParams(
            android.app.ActionBar.LayoutParams.MATCH_PARENT,
            android.app.ActionBar.LayoutParams.WRAP_CONTENT
        )
        val size = dpToPx(this, 20F).toInt()
        textView.setPadding(size, size, size, size)
        textView.text = updateInfo.content
        textView.textSize = 15F
        textView.layoutParams = layoutParams
        builder.setView(textView).setTitle("发现新版本" + updateInfo.version)
            .setPositiveButton(R.string.download) { _, _ ->
                this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.link)))
            }
        Handler(Looper.getMainLooper()).post {
            builder.create().show()
        }
    }

}