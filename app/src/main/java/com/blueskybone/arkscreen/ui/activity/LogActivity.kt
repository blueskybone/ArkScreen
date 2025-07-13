package com.blueskybone.arkscreen.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blueskybone.arkscreen.databinding.ActivityLogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LogActivity : AppCompatActivity() {


    private var _binding: ActivityLogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val filepath = intent.getStringExtra("log_filepath")
        println(filepath)
        if (filepath == null) {
            binding.LogText.text = "file path is null."
        } else {
            try {
                val file = File(filepath)
                if (file.length() > 1024 * 1024 * 5) throw Exception("file is too large: ${file.length() / 1024 / 1024} Mb")
                CoroutineScope(Dispatchers.IO).launch {
                    val text = file.readText(Charsets.UTF_8)
                    withContext(Dispatchers.Main) {
                        binding.LogText.text = text
                    }
                }
            } catch (e: Exception) {
                binding.LogText.text = e.message
            }
        }
    }

}