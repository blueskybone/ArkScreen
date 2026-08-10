package com.blueskybone.arkscreen.ui.main

/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.ActivityMainBinding
import com.blueskybone.arkscreen.platform.installer.ApkInstaller
import com.blueskybone.arkscreen.platform.notification.DownloadNotificationController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_PENDING_INSTALL_APK = "pending_install_apk"
        private const val KEY_CURRENT_PAGE = "current_page"
    }

    private val model: MainModel by viewModel()
    private lateinit var binding: ActivityMainBinding

    private val prefManager: InnerPrefManager by getKoin().inject()
    private val settingPrefManager: SettingPrefManager by getKoin().inject()
    private val apkInstaller: ApkInstaller by getKoin().inject()
    private val downloadNotificationController: DownloadNotificationController by getKoin().inject()
    private val pageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.navView.menu[position].isChecked = true
        }
    }
    private val intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
    }

    private var pendingInstallApkPath: String? = null

    override fun onResume() {
        super.onResume()
        val apkPath = pendingInstallApkPath
        if (apkPath != null && apkInstaller.canInstallUnknownApps()) {
            installApk(apkPath)
        }    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        pendingInstallApkPath = savedInstanceState?.getString(KEY_PENDING_INSTALL_APK)
        setContentView(binding.root)
        observeEvent()
        setUpNavigation(savedInstanceState?.getInt(KEY_CURRENT_PAGE) ?: 0)
        if (settingPrefManager.autoUpdateApp.get()) {
            model.checkAppUpdate()
        }
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

                        MainEvent.DownloadStarted -> downloadNotificationController.showStarted()
                        is MainEvent.DownloadProgress -> {
                            downloadNotificationController.updateProgress(event.percent)
                        }
                        is MainEvent.DownloadCompleted -> {
                            downloadNotificationController.showCompleted()
                            requestInstall(event.filePath)
                        }
                        is MainEvent.DownloadFailed -> {
                            downloadNotificationController.showFailed()
                            Toaster.show(event.message)
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

    private fun requestInstall(filePath: String) {
        if (!apkInstaller.canInstallUnknownApps()) {
            pendingInstallApkPath = filePath
            apkInstaller.openUnknownAppSourcesSettings()
            Toaster.show("请允许安装未知来源应用后继续安装")
            return
        }
        installApk(filePath)
    }

    private fun installApk(filePath: String) {
        runCatching { apkInstaller.install(filePath) }
            .onSuccess { pendingInstallApkPath = null }
            .onFailure { Toaster.show(it.message ?: "安装包打开失败") }
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

    private fun setUpNavigation(initialPage: Int) {
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
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
        viewPager.setCurrentItem(initialPage.coerceIn(0, 2), false)
    }

    override fun onDestroy() {
        binding.ViewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.ViewPager.adapter = null
        super.onDestroy()
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

    override fun onSaveInstanceState(outState: Bundle) {
        pendingInstallApkPath?.let { outState.putString(KEY_PENDING_INSTALL_APK, it) }
        outState.putInt(KEY_CURRENT_PAGE, binding.ViewPager.currentItem)
        super.onSaveInstanceState(outState)
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
