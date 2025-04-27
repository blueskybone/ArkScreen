package com.blueskybone.arkscreen.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.AppUpdate
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.bindinginfo.CheckUpdate
import com.blueskybone.arkscreen.bindinginfo.GroupChat
import com.blueskybone.arkscreen.bindinginfo.TextInfo
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.ActivityAboutBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.util.copyToClipboard
import com.blueskybone.arkscreen.util.getAppVersionName
import com.blueskybone.arkscreen.util.saveDrawableToGallery
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
class AboutActivity : AppCompatActivity() {

    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
    private var _binding: ActivityAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAboutBinding.inflate(layoutInflater)
        setUpBinding()
        setContentView(binding.root)
    }

    private fun setUpBinding() {
        binding.CheckUpdate.setUp(CheckUpdate)
//        binding.Donate.setUp(DonateList)
        binding.GroupChat.setUp(GroupChat)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.CheckUpdate.Layout.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val info = AppUpdate.getUpdateInfo()
                getAppVersionName(APP)?.let {
                    if (it < info.version)
                        Handler(Looper.getMainLooper()).post {
                            MaterialAlertDialogBuilder(APP)
                                .setTitle(info.version)
                                .setMessage(info.content)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(getString(R.string.download)) { _, _ ->
                                    try {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(info.link)
                                            )
                                        )
                                    } catch (e: Exception) {
                                        Toaster.show(getString(R.string.illegal_url))
                                        e.printStackTrace()
                                    }
                                }.show()
                        }
                    else Toaster.show("当前是最新版本")
                }
            }
        }
        binding.OpenSourceLicense.setOnClickListener {
            val textView = TextView(this).apply {
                setPadding(80, 80, 80, 80) // 设置padding
            }
            val markwon = Markwon.create(this)
            markwon.setMarkdown(textView, getString(R.string.open_license_content))
            MaterialAlertDialogBuilder(this)
                .setView(textView)
                .show()
        }
//        binding.UpdateLog.setOnClickListener {
//
//        }
        binding.Donate.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.donate)
                .setMessage(R.string.donate_msg)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.donated) { _, _ -> Toaster.show(getString(R.string.thank_for_donate)) }
                .setPositiveButton(R.string.save_code) { _, _ ->
                    saveDrawableToGallery(this, R.drawable.wechat)
                    saveDrawableToGallery(this, R.drawable.zfb)
                    Toaster.show("已保存到本地")
                }.show()
        }
        binding.GroupChat.Layout.setOnClickListener {
            copyToClipboard(this, getString(R.string.qq_group_num))
            Toaster.show(getString(R.string.copied_qq_num))
        }
        binding.FeedBack.setOnClickListener {
            MenuDialog(this).add("github") {
                val github = "https://github.com/blueskybone/ArkScreen/issues"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(github)))
            }.add("bilibili") {
                val bilibili = "https://space.bilibili.com/13957147"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bilibili)))
            }.show()
        }
    }

    private fun PreferenceBinding.setUp(textInfo: TextInfo) {
        Title.setText(textInfo.title)
        Value.setText(textInfo.subTitle)
    }
}