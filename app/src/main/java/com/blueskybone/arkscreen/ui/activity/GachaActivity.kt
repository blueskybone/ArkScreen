package com.blueskybone.arkscreen.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.ActivityGachaBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.ui.fragment.CharNotOwn
import com.blueskybone.arkscreen.ui.fragment.CharOwn
import com.blueskybone.arkscreen.ui.fragment.Gacha
import com.blueskybone.arkscreen.ui.fragment.GachaStatis
import com.blueskybone.arkscreen.ui.fragment.GachaText
import com.blueskybone.arkscreen.ui.recyclerview.GachaAdapter
import com.blueskybone.arkscreen.ui.recyclerview.GachaTextAdapter
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.blueskybone.arkscreen.viewmodel.GachaModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin

/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */

class GachaActivity : AppCompatActivity() {
    private val prefManager: PrefManager by getKoin().inject()
    private val model: GachaModel by viewModels()
    private val modelBase: BaseModel by viewModels()
    private var adapter: GachaAdapter? = null

    private var _binding: ActivityGachaBinding? = null
    private val binding get() = _binding!!
    private var launcherForTxt: ActivityResultLauncher<String>? = null
    private var launcherForJson: ActivityResultLauncher<String>? = null
    private var launcherForImport: ActivityResultLauncher<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityGachaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObserver()
        setUpBinding()
        registerLauncher()
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

        vp.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                ta.selectTab(ta.getTabAt(position))
            }
        })
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
                0 -> Gacha()
                1 -> GachaStatis()
                else -> GachaText()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_export -> {
                MenuDialog(this)
                    .add(getString(R.string.file_txt)) {
                        launcherForTxt?.launch(prefManager.baseAccountGc.get().uid + "_gacha_records")
                    }.add(getString(R.string.file_json)) {
                        launcherForJson?.launch(prefManager.baseAccountGc.get().uid + "_gacha_records")
                    }.show()
                true
            }

            R.id.menu_import -> {
                Toaster.show("施工中...")
//                MaterialAlertDialogBuilder(this)
//                    .setTitle(getString(R.string.import_data))
//                    .setMessage(R.string.import_data_detail)
//                    .setPositiveButton(R.string.import_data) { _, _ ->
//                        val mimeTypes = arrayOf("text/plain", "application/json")
//                        launcherForImport?.launch(mimeTypes)
//                    }
//                    .setNegativeButton(R.string.cancel, null)
//                    .show()
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

    private fun setupObserver() {
        model.uiState.observe(this) { value ->
            when (value) {
                is DataUiState.Loading -> displayLoadingView(value.msg)
                is DataUiState.Error -> displayErrorView(value.msg)
                is DataUiState.Success -> displayView()
                else -> {}
            }
        }

        model.gachaData.observe(this) { value ->
            adapter?.submitList(value)
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

    private fun registerLauncher() {
        launcherForTxt =
            registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
                uri?.let {
                    model.exportTxt(uri)
                }
            }
        launcherForJson =
            registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                uri?.let {
                    model.exportJson(uri)
                }
            }
        launcherForImport =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let {
                    Toaster.show("施工中...")
//                    model.importData(uri)
                }
            }
    }
}