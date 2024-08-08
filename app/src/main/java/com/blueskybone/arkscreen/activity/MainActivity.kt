package com.blueskybone.arkscreen.activity

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.UpdateResource
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.data.AppUpdateInfo
import com.blueskybone.arkscreen.network.NetWorkTask.getAppUpdateInfo
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.util.getAppVersionName
import com.blueskybone.arkscreen.util.isInternetAvailable
import com.hjq.toast.Toaster
import io.noties.markwon.Markwon
import org.koin.android.ext.android.getKoin


/**
 *   Created by blueskybone
 *   Date: 2024/7/29
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
        updateResource()
        checkAppUpdate()
        loadingData()
        requestOverlayPermission(this)
    }


    private fun loadingData() {
        val prefManager: PrefManager by getKoin().inject()
        val textView1: TextView = findViewById(R.id.text_userid_1)
        val textView2: TextView = findViewById(R.id.text_userid_2)
        val baseAccount = prefManager.BaseAccount.get()
        if (baseAccount != null) {
            textView1.post {
                textView1.text = baseAccount.nickName
            }
            textView2.post {
                textView2.text = baseAccount.nickName
            }
        } else {
            textView1.post {
                textView1.text = "未登录"
            }
            textView2.post {
                textView2.text = "未登录"
            }
        }

    }

    private fun checkAppUpdate() {
        Thread {
            val prefManager: PrefManager by getKoin().inject()
            if (!prefManager.isAutoCheckUpdate.get()) return@Thread
            val updateInfo = getAppUpdateInfo()
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
            }
        }.start()
    }

    private fun showUpdateDialog(updateInfo: AppUpdateInfo) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val textView = TextView(this)
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
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

    private fun updateResource() {
        //拆开
        //资源更新，检查更新
        val thread = Thread {
            val textView = findViewById<TextView>(R.id.text_cal_update)
            val textViewLog = findViewById<TextView>(R.id.textView_log)
            try {
                if (!isInternetAvailable(this)) {
                    textViewLog.post {
                        textViewLog.text = "无网络"
                    }
                    return@Thread
                }
                textView.post {
                    textView.text = "检查更新中..."
                }
                val updateResource: UpdateResource by getKoin().inject()
                val remoteInfo = updateResource.update(UpdateResource.Resource.RecruitDb)
                textView.post {
                    textView.text = "最后更新：${remoteInfo.date}"
                }
            } catch (e: Exception) {
                textViewLog.post {
                    textViewLog.text = e.message
                }
            }
        }
        thread.start()
    }


    private fun setLayout() {
        setContentView(R.layout.activity_home)
        title = getString(R.string.app_name)

        val cardCal = findViewById<CardView>(R.id.card_cal)
        val cardSkland = findViewById<CardView>(R.id.card_skland)
        val cardAssets = findViewById<CardView>(R.id.card_operator_assets)
//        val cardMaterial = findViewById<CardView>(R.id.card_material)
        val cardManual = findViewById<CardView>(R.id.card_manual)
        val cardQuestion = findViewById<CardView>(R.id.card_question)
        val cardAccount = findViewById<CardView>(R.id.card_account)
        val cardSetting = findViewById<CardView>(R.id.card_setting)
        val cardAbout = findViewById<CardView>(R.id.card_about)

        cardCal.setOnClickListener(this)
        cardSkland.setOnClickListener(this)
        cardManual.setOnClickListener(this)
        cardAssets.setOnClickListener(this)
//        cardMaterial.setOnClickListener(this)
        cardQuestion.setOnClickListener(this)
        cardSetting.setOnClickListener(this)
        cardAccount.setOnClickListener(this)
        cardAbout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.card_cal -> startActivity(Intent(this, CalActivity::class.java))
            R.id.card_skland -> startActivity(Intent(this, RealTimeActivity::class.java))
            R.id.card_operator_assets -> startActivity(
                Intent(
                    this,
                    OpeAssetsActivity::class.java
                )
            )
//            R.id.card_material -> startActivity(Intent(this, MaterialActivity::class.java))
            R.id.card_setting -> startActivity(Intent(this, SettingActivity::class.java))
            R.id.card_account -> startActivity(Intent(this, AccountActivity::class.java))
            R.id.card_about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.card_manual -> {
                showDialog(getString(R.string.content_use), getString(R.string.manual))
            }

            R.id.card_question -> {
                showDialog(getString(R.string.content_query), getString(R.string.question))
            }
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


    override fun onResume() {
        loadingData()
        super.onResume()
    }

    private fun requestOverlayPermission(context: Context) {
        if (Settings.canDrawOverlays(context)) return
        Toaster.show(getString(R.string.acquire_overlay_permission_content))
        jumpToOverlayPermission()
    }

    private fun jumpToOverlayPermission() {
        val intentActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { _: ActivityResult ->
        }
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                "package:$packageName"
            )
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentActivityResultLauncher.launch(intent)
    }
}