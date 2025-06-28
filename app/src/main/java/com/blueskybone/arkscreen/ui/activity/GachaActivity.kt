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
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.ActivityGachaBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.ui.recyclerview.GachaAdapter
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.viewmodel.GachaModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.getKoin

/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */

class GachaActivity : AppCompatActivity() {
    private val prefManager: PrefManager by getKoin().inject()
    private val model: GachaModel by viewModels()
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
        adapter = GachaAdapter(this)
        binding.RecyclerView.adapter = adapter
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_gacha_menu, menu)
        return true
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
        binding.NestedScrollView.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayErrorView(msg: String) {
        binding.Page.visibility = View.VISIBLE
        binding.NestedScrollView.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayView() {
        binding.Page.visibility = View.GONE
        binding.NestedScrollView.visibility = View.VISIBLE

        val account = prefManager.baseAccountGc.get()
        binding.NickName.text = account.nickName
        if (account.official) binding.Icon.setImageResource(R.drawable.hg_icon_80x80)
        else binding.Icon.setImageResource(R.drawable.bili_icon_75x71)

        binding.CountSum.text = getString(R.string.gacha_count, model.finalCountSum)
        binding.Rarity6.text = model.rarity6Count.toString()
        binding.AverageCount.text =
            if (model.rarity6Count == 0) "-" else getString(
                R.string.gacha_count,
                model.finalCountSum / model.rarity6Count
            )
        binding.NormalCount.text = getString(R.string.gacha_count, model.poolCountNormal)
        binding.FesCount.text = getString(R.string.gacha_count, model.poolCountFes)
        binding.CoreCount.text = getString(R.string.gacha_count, model.poolCountCore)

        binding.DateRange.text = getString(R.string.date_range, model.dateRange)
    }

    private fun registerLauncher() {
        launcherForTxt =
            registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
                uri?.let { model.exportTxt(uri) }
            }
        launcherForJson =
            registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                uri?.let { model.exportJson(uri) }
            }
        launcherForImport =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let {
                    model.importData(uri)
                }
            }
    }
}