package com.blueskybone.arkscreen.ui.common

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.core.logger.LogRepository
import com.blueskybone.arkscreen.databinding.ActivityLogManagerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogManagerBinding
    private lateinit var repository: LogRepository
    private val adapter = LogFileAdapter(::openLog, ::showEntryMenu)
    private var entries: List<LogRepository.Entry> = emptyList()
    private var selectedType: LogRepository.Type? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = LogRepository(applicationContext)

        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.LogList.adapter = adapter
        binding.FilterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedType = when (checkedIds.firstOrNull()) {
                R.id.FilterApp -> LogRepository.Type.APP
                R.id.FilterNetwork -> LogRepository.Type.NETWORK
                else -> null
            }
            render()
        }
        loadLogs()
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) loadLogs()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_log_manager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        onToolbarMenuItem(item) || super.onOptionsItemSelected(item)

    private fun loadLogs() {
        lifecycleScope.launch {
            entries = withContext(Dispatchers.IO) { repository.list() }
            render()
        }
    }

    private fun render() {
        if (!::binding.isInitialized) return
        val visibleEntries = selectedType?.let { type ->
            entries.filter { it.type == type }
        } ?: entries
        adapter.submitList(visibleEntries)
        binding.EmptyText.isVisible = visibleEntries.isEmpty()
        binding.FileCount.text = entries.size.toString()
        binding.TotalSize.text =
            Formatter.formatShortFileSize(this, entries.sumOf(LogRepository.Entry::size))
    }

    private fun openLog(entry: LogRepository.Entry) {
        startActivity(LogActivity.createIntent(this, entry.type, entry.file.name))
    }

    private fun showEntryMenu(anchor: View, entry: LogRepository.Entry) {
        PopupMenu(this, anchor).apply {
            menu.add(R.string.share)
            menu.add(R.string.delete)
            setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    getString(R.string.share) -> share(listOf(entry))
                    getString(R.string.delete) -> confirmDelete(entry)
                }
                true
            }
        }.show()
    }

    private fun confirmDelete(entry: LogRepository.Entry) {
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.confirm_delete_log, entry.file.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.delete(entry) }
                    loadLogs()
                }
            }
            .show()
    }

    private fun confirmClear() {
        if (entries.isEmpty()) return
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirm_clear_logs)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    val count = withContext(Dispatchers.IO) { repository.clear() }
                    Toaster.show(getString(R.string.logs_cleared_count, count))
                    loadLogs()
                }
            }
            .show()
    }

    private fun onToolbarMenuItem(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_clear_logs -> {
            confirmClear()
            true
        }
        else -> false
    }

    private fun share(logs: List<LogRepository.Entry>) {
        if (logs.isEmpty()) {
            Toaster.show(getString(R.string.log_empty))
            return
        }
        val uris = ArrayList(logs.mapNotNull(::contentUri))
        if (uris.isEmpty()) return

        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.first())
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                uris,
            )
        }.apply {
            type = "text/plain"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, "ArkScreen logs", uris.first()).apply {
                uris.drop(1).forEach { addItem(ClipData.Item(it)) }
            }
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_logs)))
    }

    private fun contentUri(entry: LogRepository.Entry): Uri? = runCatching {
        FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            entry.file,
        )
    }.getOrNull()
}
