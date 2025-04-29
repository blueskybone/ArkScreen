package com.blueskybone.arkscreen.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
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

        binding.GroupChat.Layout.setOnClickListener {
            val groupId = getString(R.string.qq_group_num)
            try{
                val url = "mqqapi://card/show_pslcard?src_type=internal&card_type=group&uin=$groupId"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setPackage("com.tencent.mobileqq")
                startActivity(intent)
            }catch (e:Exception){
                copyToClipboard(this, groupId)
                Toaster.show(getString(R.string.copied_qq_num))
            }
        }
        binding.FeedBack.setOnClickListener {
            MenuDialog(this).add("github") {
                val github = "https://github.com/blueskybone/ArkScreen/issues"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(github)))
            }.add("bilibili") {
                val biliUid = "13957147"
                try {
                    val url = "bilibili://space/$biliUid"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("tv.danmaku.bili")
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    val bilibili = "https://space.bilibili.com/$biliUid"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bilibili)))
                }
            }.add("QQ") {
                try {
                    val qqNumber = "1980463469"
                    val url = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$qqNumber"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.tencent.mobileqq")
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    Toaster.show("未安装QQ")
                }
            }.show()
        }
    }

    private fun PreferenceBinding.setUp(textInfo: TextInfo) {
        Title.setText(textInfo.title)
        Value.setText(textInfo.subTitle)
    }
}