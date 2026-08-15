package com.blueskybone.arkscreen.ui.license

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.license.GeneratedLicenseCatalog
import com.blueskybone.arkscreen.databinding.ActivityLicenseDetailBinding
import com.blueskybone.arkscreen.domain.model.license.OpenSourceLibrary
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LicenseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLicenseDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val libraryId = intent.getStringExtra(EXTRA_LIBRARY_ID)
        if (libraryId.isNullOrBlank()) {
            finish()
            return
        }
        lifecycleScope.launch {
            val library = runCatching {
                withContext(Dispatchers.IO) {
                    GeneratedLicenseCatalog(applicationContext)
                        .load()
                        .firstOrNull { it.id == libraryId }
                }
            }.getOrNull()
            if (library == null) {
                Toaster.show(getString(R.string.license_load_failed))
                finish()
            } else {
                render(library)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun render(library: OpenSourceLibrary) {
        binding.Loading.visibility = View.GONE
        binding.Content.visibility = View.VISIBLE
        binding.Toolbar.title = library.name.ifBlank { library.id.substringAfter(':') }
        binding.ModuleValue.text = library.id
        binding.VersionValue.text = library.version
        binding.LicenseValue.text = library.licenses
            .map { it.name.ifBlank { getString(R.string.license_unknown) } }
            .distinct()
            .joinToString("\n")
            .ifBlank { getString(R.string.license_unknown) }

        binding.ProjectButton.visibility =
            if (library.projectUrl.isBlank()) View.GONE else View.VISIBLE
        binding.ProjectButton.setOnClickListener { openUrl(library.projectUrl) }

        val licenseUrl = library.licenses.firstNotNullOfOrNull { it.url.takeIf(String::isNotBlank) }
        binding.LicenseButton.visibility = if (licenseUrl == null) View.GONE else View.VISIBLE
        binding.LicenseButton.setOnClickListener { licenseUrl?.let(::openUrl) }
    }

    private fun openUrl(url: String) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            .onFailure { Toaster.show(getString(R.string.illegal_url)) }
    }

    companion object {
        private const val EXTRA_LIBRARY_ID = "library_id"

        fun createIntent(context: Context, libraryId: String): Intent =
            Intent(context, LicenseDetailActivity::class.java)
                .putExtra(EXTRA_LIBRARY_ID, libraryId)
    }
}
