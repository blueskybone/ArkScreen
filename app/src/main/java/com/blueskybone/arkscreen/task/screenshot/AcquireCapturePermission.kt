package com.blueskybone.arkscreen.task.screenshot

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.blueskybone.arkscreen.task.CapturePermission

/**
 *   Created by blueskybone
 *   Date: 2025/1/25
 */
class AcquireCapturePermission : AppCompatActivity() {

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
    }

    override fun onStart() {
        super.onStart()
        (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)?.apply {
            val intent = createScreenCaptureIntent()
            activityResultLauncher?.launch(intent)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                println("测试-1")
                val data = result.data
                CapturePermission.intent = data
                val intent = Intent(this, RecruitService::class.java)
                intent.putExtra("setPermission", true)
                startForegroundService(intent)
                println("测试0")
                finish()
            }
        }
    }
}