package com.blueskybone.arkscreen.activity


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.view.marginStart
import androidx.core.view.setMargins
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.network.NetWorkUtils.getServerTs
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import kotlin.concurrent.thread


/**
 *   Created by blueskybone
 *   Date: 2024/7/29
 */

class SettingActivity : AppCompatActivity() {

    private val prefManager: PrefManager by getKoin().inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
    }


    private fun setLayout() {
        setContentView(R.layout.activity_setting)
        title = getString(R.string.setting)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val cardShowMode = this.findViewById<CardView>(R.id.card_recruit_mode)
        val cardWidgetTheme = this.findViewById<CardView>(R.id.card_widget_theme)
        val switchAutoCheck = this.findViewById<SwitchCompat>(R.id.switch_skland_auto_attendance)
        val switchAutoCheckUpdate = this.findViewById<SwitchCompat>(R.id.switch_auto_check_update)

        val switchTimeCorrection = this.findViewById<SwitchCompat>(R.id.switch_time_correction)
        val buttonCorrection = this.findViewById<Button>(R.id.button_click_correction)
        setTextOption()

        cardShowMode.setOnClickListener {
            showModeDialog(prefManager.recruitShowMode.get())
        }

        cardWidgetTheme.setOnClickListener {
            showThemeDialog()
        }

        val textTimeCorrection = this.findViewById<TextView>(R.id.image_correct)
        textTimeCorrection.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.time_correction))
                .setMessage(getString(R.string.time_correction_detail)).create().show()
        }

        switchTimeCorrection.isChecked = prefManager.isSklandCorrectOn.get()
        switchAutoCheck.isChecked = prefManager.isAutoCheckOn.get()
        switchAutoCheckUpdate.isChecked = prefManager.isAutoCheckUpdate.get()

        buttonCorrection.visibility = if (switchTimeCorrection.isChecked) {
            Button.VISIBLE
        } else {
            Button.INVISIBLE
        }

        buttonCorrection.setOnClickListener {
            val thread = Thread {
                val nowTs = System.currentTimeMillis() / 1000
                val serverTs = getServerTs()
                if (serverTs == null) {
                    Toaster.show("发生错误")
                } else {
                    val delay = nowTs - serverTs
                    Toaster.show("delay: $delay")
                    prefManager.CorrectionTs.set(delay)
                }
            }
            thread.start()
        }

        switchTimeCorrection.setOnCheckedChangeListener { _, isChecked ->
            prefManager.isSklandCorrectOn.set(
                isChecked
            )
            buttonCorrection.visibility = if (isChecked) {
                Button.VISIBLE
            } else {
                Button.INVISIBLE
            }
        }
        switchAutoCheck.setOnCheckedChangeListener { _, isChecked ->
            prefManager.isAutoCheckOn.set(
                isChecked
            )
        }
        switchAutoCheckUpdate.setOnCheckedChangeListener { _, isChecked ->
            prefManager.isAutoCheckUpdate.set(
                isChecked
            )
        }

    }

    private fun showThemeDialog() {
        val thread = Thread {
            val prefManager: PrefManager by getKoin().inject()
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val linearLayout = LinearLayout(this)
            linearLayout.layoutParams = layoutParams
            linearLayout.orientation = LinearLayout.VERTICAL

            val layoutParams1 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutParams1.setMargins(10)

            val layoutParams2 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams2.setMargins(10)
            val radioGroup = RadioGroup(this)
            radioGroup.layoutParams = layoutParams1
            val listEnum = listOf(
                PrefManager.WidgetTheme.WHITE_ON_BLACK, PrefManager.WidgetTheme.BLACK_ON_WHITE
            )
            val listName = listOf(
                getString(R.string.black_on_white), getString(R.string.white_on_black)
            )
            for ((idx, item) in listName.withIndex()) {
                val rb = RadioButton(this)
                rb.text = item
                rb.layoutParams = layoutParams2
                rb.id = View.generateViewId()
                if (listEnum[idx] == prefManager.widgetThemePreference.get()) rb.isChecked = true
                radioGroup.addView(rb)
            }
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                prefManager.widgetThemePreference.set(listEnum[(checkedId - 1) % listEnum.size])
            }
            linearLayout.addView(radioGroup)
            val textView = TextView(this)
            textView.layoutParams = layoutParams2
            textView.text = getString(R.string.alpha)
            val seekBar = SeekBar(this).apply {
                max = 255 // 设置最大值
                progress = prefManager.widgetAlpha.get() // 设置初始值

                // 设置进度变化监听器
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        prefManager.widgetAlpha.set(progress)
                    }
                })
            }

            linearLayout.addView(textView)
            linearLayout.addView(seekBar)
            Handler(Looper.getMainLooper()).post {
                val builder = AlertDialog.Builder(this@SettingActivity)
                builder.setView(linearLayout).show()
            }
        }
        thread.start()
    }

    private fun setTextOption() {
        val textOption: TextView = findViewById(R.id.text_recruit_option)
        when (prefManager.recruitShowMode.get()) {
            PrefManager.ShowMode.FLOAT -> textOption.setText(R.string.float_win)
            PrefManager.ShowMode.TOAST -> textOption.setText(R.string.toast)
            PrefManager.ShowMode.AUTO -> textOption.setText(R.string.auto)
            else -> textOption.setText(R.string.text_default)
        }
    }

    private fun showModeDialog(showMode: PrefManager.ShowMode) {
        val thread = Thread {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val linearLayout = LinearLayout(this)
            linearLayout.layoutParams = layoutParams
            linearLayout.orientation = LinearLayout.VERTICAL

            val layoutParams1 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutParams1.setMargins(10)

            val layoutParams2 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams2.setMargins(10)
            val radioGroup = RadioGroup(this)
            radioGroup.layoutParams = layoutParams1
            val listEnum = listOf(
                PrefManager.ShowMode.FLOAT, PrefManager.ShowMode.TOAST, PrefManager.ShowMode.AUTO
            )
            val listName = listOf(
                getString(R.string.float_win), getString(R.string.toast), getString(R.string.auto)
            )
            for ((idx, item) in listName.withIndex()) {
                val rb = RadioButton(this)
                rb.text = item
                rb.layoutParams = layoutParams2
                rb.id = View.generateViewId()
                Log.e("rb_id", rb.id.toString())
                if (listEnum[idx] == showMode) rb.isChecked = true
                radioGroup.addView(rb)
            }
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                prefManager.recruitShowMode.set(listEnum[(checkedId - 1) % listEnum.size])
                setTextOption()
            }
            linearLayout.addView(radioGroup)
            Handler(Looper.getMainLooper()).post {
                val builder = AlertDialog.Builder(this@SettingActivity)
                builder.setView(linearLayout).show()
            }
        }
        thread.start()
    }
}