package com.blueskybone.arkscreen.ui.activity

/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.bindinginfo.AppTheme
import com.blueskybone.arkscreen.databinding.ActivityMainBinding
import com.blueskybone.arkscreen.ui.fragment.Function
import com.blueskybone.arkscreen.ui.fragment.Home
import com.blueskybone.arkscreen.ui.fragment.Setting
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.util.getAppVersionName
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.java.KoinJavaComponent.getKoin


class MainActivity : AppCompatActivity() {

    private val model: BaseModel by viewModels()
    lateinit var binding: ActivityMainBinding

    private val prefManager: PrefManager by getKoin().inject()
    private val intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
    }

    override fun onResume() {
        super.onResume()
        model.reloadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpNavigation()
        checkAppUpdate(this)
        requestOverlayPermission(this)
    }

    private fun checkAppUpdate(context: Context) {
        model.appUpdateInfo.observe(this) { info ->
            getAppVersionName(context).let {
                if (it < info.version.toString())
                    Handler(Looper.getMainLooper()).post {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(info.version.toString())
                            .setMessage(info.content)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(getString(R.string.download)) { _, _ ->
                                try {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.link)))
                                } catch (e: Exception) {
                                    Toaster.show(getString(R.string.illegal_url))
                                    e.printStackTrace()
                                }
                            }.show()
                    }
            }
        }
    }

    private fun setUpNavigation() {
        val viewPager = binding.ViewPager
        val bottomNavigationView: BottomNavigationView = binding.navView
        viewPager.adapter = ViewPagerFragmentAdapter(this)

//        viewPager.isUserInputEnabled = false

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
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
    }


    inner class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
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


    private fun requestOverlayPermission(context: Context) {
        if (Settings.canDrawOverlays(context)) return
        if (!prefManager.warnOverlayPermission.get()) return
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.jump_to_overlay_permission))
            .setMessage(getString(R.string.acquire_overlay_permission_content))
            .setNegativeButton(R.string.no_more_warn) { _, _ ->
                prefManager.warnOverlayPermission.set(false)
            }
            .setPositiveButton(getString(R.string.jump_to)) { _, _ -> jumpToOverlayPermission() }
            .show()
    }

    fun jumpToOverlayPermission() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                    "package:$packageName"
                )
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentActivityResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
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