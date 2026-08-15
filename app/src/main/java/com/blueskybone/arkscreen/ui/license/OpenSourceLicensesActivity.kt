package com.blueskybone.arkscreen.ui.license

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.license.GeneratedLicenseCatalog
import com.blueskybone.arkscreen.databinding.ActivityOpenSourceLicensesBinding
import com.blueskybone.arkscreen.domain.model.license.OpenSourceLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OpenSourceLicensesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenSourceLicensesBinding
    private val adapter = OpenSourceLicenseAdapter { library ->
        startActivity(LicenseDetailActivity.createIntent(this, library.id))
    }
    private var libraries: List<OpenSourceLibrary> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenSourceLicensesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.LibraryList.adapter = adapter
        binding.SearchInput.doAfterTextChanged { filter(it?.toString().orEmpty()) }
        loadCatalog()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadCatalog() {
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    GeneratedLicenseCatalog(applicationContext).load()
                }
            }
            binding.Loading.visibility = View.GONE
            result.onSuccess {
                libraries = it.sortedBy(OpenSourceLibrary::name)
                filter(binding.SearchInput.text?.toString().orEmpty())
            }.onFailure {
                binding.EmptyText.setText(R.string.license_load_failed)
                binding.EmptyText.visibility = View.VISIBLE
            }
        }
    }

    private fun filter(query: String) {
        val keyword = query.trim()
        val filtered = if (keyword.isEmpty()) {
            libraries
        } else {
            libraries.filter { library ->
                library.name.contains(keyword, ignoreCase = true) ||
                    library.id.contains(keyword, ignoreCase = true) ||
                    library.licenses.any { it.name.contains(keyword, ignoreCase = true) }
            }
        }
        adapter.submitList(filtered)
        binding.EmptyText.setText(R.string.license_empty)
        binding.EmptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
