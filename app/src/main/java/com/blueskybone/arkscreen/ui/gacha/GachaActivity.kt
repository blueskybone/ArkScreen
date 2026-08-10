package com.blueskybone.arkscreen.ui.gacha

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityGachaBinding
import com.blueskybone.arkscreen.ui.UiStatus
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch

/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */

class GachaActivity : AppCompatActivity() {
    private val model: GachaModel by viewModel()

    private var _binding: ActivityGachaBinding? = null
    private val binding get() = _binding!!
    private var launcherForTxt: ActivityResultLauncher<String>? = null
    private var launcherForJson: ActivityResultLauncher<String>? = null
    private var launcherForImport: ActivityResultLauncher<Array<String>>? = null
    private val pageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.TabLayout.selectTab(binding.TabLayout.getTabAt(position))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityGachaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBinding()
        registerLauncher()
        observeState()
        observeEvents()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect { state ->
                    val hasCachedRecords = state.gachaUiSnapshot?.records?.isNotEmpty() == true
                    when (val status = state.status) {
                        is UiStatus.Loading -> {
                            if (hasCachedRecords) showContent()
                            else showStatus(status.message ?: "加载中...")
                        }
                        is UiStatus.Empty -> showStatus(status.message)
                        is UiStatus.Error -> {
                            if (hasCachedRecords) showContent()
                            else showStatus(status.message)
                        }
                        is UiStatus.Success -> showContent()
                        UiStatus.Idle -> Unit
                    }
                }
            }
        }
    }

    private fun showStatus(message: String) {
        binding.Message.text = message
        binding.Page.visibility = View.VISIBLE
        binding.TabLayout.visibility = View.GONE
        binding.ViewPager.visibility = View.GONE
    }

    private fun showContent() {
        binding.Page.visibility = View.GONE
        binding.TabLayout.visibility = View.VISIBLE
        binding.ViewPager.visibility = View.VISIBLE
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.event.collect { event ->
                    when (event) {
                        is GachaEvent.ShowError -> Toaster.show(event.message)
                        is GachaEvent.ShowMessage -> Toaster.show(event.message)
                    }
                }
            }
        }
    }

    private fun setUpBinding() {

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

        vp.registerOnPageChangeCallback(pageChangeCallback)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_gacha_menu, menu)
        return true
    }

    inner class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> GachaFragment()
                1 -> GachaStatsFragment()
                else -> GachaTextFragment()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_export -> {
                MenuDialog(this)
                    .add(getString(R.string.file_txt)) {
                        launcherForTxt?.launch(model.exportFileBaseName())
                    }.add(getString(R.string.file_json)) {
                        launcherForJson?.launch(model.exportFileBaseName())
                    }.show()
                true
            }

            R.id.menu_import -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.import_data))
                    .setMessage(R.string.import_data_detail)
                    .setPositiveButton(R.string.import_data) { _, _ ->
                        val mimeTypes = arrayOf("text/plain", "application/json")
                        launcherForImport?.launch(mimeTypes)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }

            R.id.menu_clear -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.clear_data))
                    .setMessage(R.string.confirm_clear_data)
                    .setPositiveButton(R.string.clear) { _, _ -> model.deleteRecords() }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }

            R.id.gacha_correct -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.gacha_correct))
                    .setMessage(R.string.gacha_correct_detail)
                    .setPositiveButton(R.string.confirm) { _, _ -> model.correctUnCateRecord() }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun registerLauncher() = Unit

    override fun onDestroy() {
        binding.ViewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.ViewPager.adapter = null
        _binding = null
        super.onDestroy()
    }
}
