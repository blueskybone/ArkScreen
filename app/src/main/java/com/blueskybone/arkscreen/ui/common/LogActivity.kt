package com.blueskybone.arkscreen.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.core.logger.LogRepository
import com.blueskybone.arkscreen.databinding.ActivityLogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

class LogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val file = resolveLogFile()
        if (file == null) {
            binding.LogText.setText(R.string.invalid_log_file)
            return
        }
        title = file.name
        lifecycleScope.launch {
            binding.LogText.text = runCatching {
                withContext(Dispatchers.IO) { readTail(file, MAX_DISPLAY_BYTES) }
            }.getOrElse { error ->
                getString(R.string.read_log_failed, error.message.orEmpty())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun resolveLogFile(): File? {
        val type = LogRepository.Type.fromValue(
            intent.getStringExtra(EXTRA_LOG_TYPE),
        ) ?: return null
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return null
        return LogRepository(applicationContext)
            .resolve(type, fileName)
            ?.takeIf { it.length() <= MAX_FILE_BYTES }
    }

    private fun readTail(file: File, maxBytes: Long): String {
        RandomAccessFile(file, "r").use { input ->
            val start = (input.length() - maxBytes).coerceAtLeast(0)
            input.seek(start)
            val bytes = ByteArray((input.length() - start).toInt())
            input.readFully(bytes)
            val prefix = if (start > 0) "…仅显示最后 ${maxBytes / 1024} KB…\n" else ""
            return prefix + bytes.toString(Charsets.UTF_8)
        }
    }

    companion object {
        private const val EXTRA_LOG_TYPE = "log_type"
        private const val EXTRA_FILE_NAME = "file_name"
        private const val MAX_DISPLAY_BYTES = 512L * 1024L
        private const val MAX_FILE_BYTES = 2L * 1024L * 1024L

        fun createIntent(
            context: Context,
            type: LogRepository.Type,
            fileName: String,
        ): Intent =
            Intent(context, LogActivity::class.java)
                .putExtra(EXTRA_LOG_TYPE, type.value)
                .putExtra(EXTRA_FILE_NAME, fileName)
    }
}
