package com.blueskybone.arkscreen.ui.main

/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blueskybone.arkscreen.AppUpdater
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin


class MainActivity : AppCompatActivity() {

    private val model: MainModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val prefManager: InnerPrefManager by getKoin().inject()
    private val intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
    }

    private lateinit var appUpdater: AppUpdater
    private var currentDownloadId: Long = -1L
    private var pendingInstallDownloadId: Long = -1L

    override fun onResume() {
        super.onResume()
        if (pendingInstallDownloadId != -1L && appUpdater.canInstallUnknownApps()) {
            val installed = appUpdater.installDownloadedApk(pendingInstallDownloadId)
            if (installed) {
                pendingInstallDownloadId = -1L
            }
        }
//        model.reloadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        appUpdater = AppUpdater(this)
        setContentView(binding.root)
        observeEvent()
        setUpNavigation()
        model.checkAppUpdate()
        requestOverlayPermission(this)
    }


    private fun observeEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.event.collect { event ->
                    when (event) {
                        is MainEvent.ShowUpdateDialog -> {
                            showUpdateDialog(
                                version = event.version,
                                changelog = event.changelog,
                                url = event.url
                            )
                        }

                        is MainEvent.StartAppDownload -> {
                            startAppDownload(event.url)
                        }

                        is MainEvent.ShowError -> {
                            Toaster.show(event.message)
                        }

                        is MainEvent.ShowToast -> {
                            Toaster.show(event.message)
                        }
                    }
                }
            }
        }
    }

    private fun startAppDownload(url: String) {
        if (!appUpdater.canInstallUnknownApps()) {
            pendingInstallDownloadId = -1L
            appUpdater.openUnknownAppSourcesSettings()
            Toaster.show("请先允许安装未知来源应用")
            return
        }
        currentDownloadId = appUpdater.downloadApk(url)
        Toaster.show("开始下载更新")
    }

    private fun showUpdateDialog(version: String, changelog: String, url: String) {
        MaterialAlertDialogBuilder(this).setTitle(version).setMessage(changelog)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(getString(R.string.download)) { _, _ ->
                try {
                    model.downloadApp(url)
                } catch (e: Exception) {
                    Toaster.show(getString(R.string.illegal_url))
                    e.printStackTrace()
                }
            }.show()
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (downloadId != currentDownloadId) return

            if (!appUpdater.canInstallUnknownApps()) {
                pendingInstallDownloadId = downloadId
                appUpdater.openUnknownAppSourcesSettings()
                Toaster.show("请允许安装未知来源应用后继续安装")
                return
            }

            val installed = appUpdater.installDownloadedApk(downloadId)
            if (!installed) {
                Toaster.show("安装包打开失败")
            }
        }
    }



    private fun setUpNavigation() {
        val viewPager = binding.ViewPager
        viewPager.offscreenPageLimit = 2
        val bottomNavigationView: BottomNavigationView = binding.navView
        viewPager.adapter = ViewPagerFragmentAdapter(this)

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_function -> {
                    viewPager.currentItem = 1
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_setting -> {
                    viewPager.currentItem = 2
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu[position].isChecked = true
            }
        })
    }


    class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> Home()
                1 -> Function()
                else -> Setting()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerDownloadReceiver()
    }

    override fun onStop() {
        unregisterReceiverSafe(downloadReceiver)
        super.onStop()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerDownloadReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(downloadReceiver, filter)
        }
    }

    private fun unregisterReceiverSafe(receiver: BroadcastReceiver) {
        runCatching { unregisterReceiver(receiver) }
    }


    private fun requestOverlayPermission(context: Context) {
        if (Settings.canDrawOverlays(context)) return
        if (!prefManager.warnOverlayPermission.get()) return
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.overlay_permission))
            .setMessage(getString(R.string.acquire_overlay_permission_content))
            .setNegativeButton(R.string.no_more_warn) { _, _ ->
                prefManager.warnOverlayPermission.set(false)
            }
            .setPositiveButton(getString(R.string.jump_to)) { _, _ -> jumpToPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION) }
            .show()
    }

    fun jumpToPermission(permission: String) {
        try {
            val intent = Intent(
                permission, "package:$packageName".toUri()
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentActivityResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toaster.show("无法打开页面，请手动设置")
        }
    }

    fun openNotificationSettings(context: Context) {
        try {
            // 尝试 Android 8.0+ 的方式
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                // 回退到应用详情页面
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                // 最终回退到系统设置主页
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toaster.show("无法打开电池优化设置页面")
        }
    }
}