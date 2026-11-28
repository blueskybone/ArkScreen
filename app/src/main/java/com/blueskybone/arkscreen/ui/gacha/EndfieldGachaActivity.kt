package com.blueskybone.arkscreen.ui.gacha

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityEndfieldGachaBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EndfieldGachaActivity : AppCompatActivity() {
    private val model: EndfieldGachaViewModel by viewModel()
    private lateinit var binding: ActivityEndfieldGachaBinding
    private var mediator: com.google.android.material.tabs.TabLayoutMediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Endfield)
        super.onCreate(savedInstanceState)
        binding = ActivityEndfieldGachaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        model.selectCategory(savedInstanceState?.getString("type") ?: model.category.value)
        binding.CharButton.isCheckable = true
        binding.WeaponButton.isCheckable = true
        fun select(type: String) {
            model.selectCategory(type)
            binding.CharButton.isChecked = type == "char"
            binding.WeaponButton.isChecked = type == "weapon"
        }
        binding.CharButton.setOnClickListener { select("char") }
        binding.WeaponButton.setOnClickListener { select("weapon") }
        select(model.category.value)
        binding.ViewPager.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int) = EndfieldGachaPageFragment().apply {
                arguments = Bundle().apply { putInt("page", position) }
            }
        }
        mediator = com.google.android.material.tabs.TabLayoutMediator(binding.ViewTabs, binding.ViewPager) { tab, position ->
            tab.text = listOf(getString(R.string.gacha_pool_details), getString(R.string.gacha_report), getString(R.string.gacha_raw_records))[position]
        }.also { it.attach() }
        binding.ViewPager.setCurrentItem(savedInstanceState?.getInt("viewMode") ?: 0, false)
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { model.state.collect { invalidateOptionsMenu() } } }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("type", model.category.value)
        outState.putInt("viewMode", binding.ViewPager.currentItem)
        super.onSaveInstanceState(outState)
    }
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menu.add(0, 1, 0, R.string.refresh).setIcon(
            androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_function_refresh)?.mutate()?.apply {
                setTint(androidx.core.content.ContextCompat.getColor(this@EndfieldGachaActivity, R.color.app_text_primary))
            })
            .setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }
    override fun onPrepareOptionsMenu(menu: android.view.Menu): Boolean {
        menu.findItem(1)?.isEnabled = model.state.value.selectedUid != null && !model.state.value.loading
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == 1) { model.refresh(); return true }
        return super.onOptionsItemSelected(item)
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
    override fun onDestroy() {
        mediator?.detach()
        binding.ViewPager.adapter = null
        super.onDestroy()
    }
}
