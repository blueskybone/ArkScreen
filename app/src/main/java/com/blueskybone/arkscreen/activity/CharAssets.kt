package com.blueskybone.arkscreen.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityCharAssetsBinding
import com.blueskybone.arkscreen.fragment.CharNotOwn
import com.blueskybone.arkscreen.fragment.CharOwn
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.viewmodel.CharModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.noties.markwon.Markwon
import org.koin.android.ext.android.getKoin


/**
 *   Created by blueskybone
 *   Date: 2025/1/18
 */

class CharAssets : AppCompatActivity() {
    private val prefManager: PrefManager by getKoin().inject()

    private val model: CharModel by viewModels()
    private var _binding: ActivityCharAssetsBinding? = null
    private var launcherForTxt: ActivityResultLauncher<String>? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCharAssetsBinding.inflate(layoutInflater)
        setupBinding()
        setupObserver()
        registerLauncher()
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_char_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_export -> {
                launcherForTxt?.launch(prefManager.baseAccountGc.get().uid + "_char_assets")
                true
            }
            R.id.menu_statistics -> {
                val textView = TextView(this).apply {
                    setPadding(80, 80, 80, 80) // 设置padding
                }
                val markwon = Markwon.create(this)
                markwon.setMarkdown(textView, model.generateStatisticMarkDownText())
                MaterialAlertDialogBuilder(this)
                    .setView(textView)
                    .setTitle(getString(R.string.statistic))
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBinding() {
        val vp = binding.ViewPager
        val ta = binding.TabLayout
        vp.adapter = ViewPagerFragmentAdapter(this)

        ta.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                vp.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        vp.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                ta.selectTab(ta.getTabAt(position))
            }
        })
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupObserver() {
        model.uiState.observe(this) { value ->
            when (value) {
                is DataUiState.Loading -> displayLoadingView(value.msg)
                is DataUiState.Error -> displayErrorView(value.msg)
                is DataUiState.Success -> displayView()
            }
        }
    }

    private fun displayLoadingView(msg: String) {
        binding.Page.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayErrorView(msg: String) {
        binding.Page.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayView() {
        binding.Page.visibility = View.GONE
        binding.ViewPager.visibility = View.VISIBLE
    }

    inner class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CharOwn()
                else -> CharNotOwn()
            }
        }
    }

    private fun registerLauncher() {
        launcherForTxt =
            registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
                uri?.let { model.exportTxt(uri) }
            }
    }
}