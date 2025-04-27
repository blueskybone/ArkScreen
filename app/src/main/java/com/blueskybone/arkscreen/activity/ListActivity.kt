package com.blueskybone.arkscreen.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityListBinding
import com.blueskybone.arkscreen.room.Type

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
abstract class ListActivity(private val type: Type) : AppCompatActivity() {

    open lateinit var binding: ActivityListBinding
    open var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBinding()
        setUpListener()
        setUpRecycler()
        registerLauncher()
    }

    abstract fun setUpRecycler()

    private fun setUpBinding() {

        val title: String = when (type) {
            Type.ACCOUNT_SK -> getString(R.string.skland_account_manage)
            Type.ACCOUNT_GC -> getString(R.string.gacha_account_manage)
            Type.LINK -> getString(R.string.home_3rd_link_manage)
        }
        binding.Toolbar.title = title

        when (type) {
            Type.ACCOUNT_SK -> binding.InfoText.text = getString(R.string.list_content_sk)
            Type.ACCOUNT_GC -> binding.InfoText.text = getString(R.string.list_content_gc)
            Type.LINK -> binding.InfoCard.visibility = View.GONE
        }
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpListener() {
        binding.Add.setOnClickListener {
            onAddButtonClick()
        }
    }

    abstract fun onAddButtonClick()
    abstract fun onActivityResult(data: Intent?)


    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    onActivityResult(data)
                }
            }
        }
    }
}