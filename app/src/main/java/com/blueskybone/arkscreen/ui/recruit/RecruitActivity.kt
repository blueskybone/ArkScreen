package com.blueskybone.arkscreen.ui.recruit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blueskybone.arkscreen.R

class RecruitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RecruitFragment())
                .commit()
        }
    }
}