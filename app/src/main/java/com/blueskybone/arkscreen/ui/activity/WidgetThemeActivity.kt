package com.blueskybone.arkscreen.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.CustomRadioGroup
import com.blueskybone.arkscreen.common.CustomRadioGroup.OnCheckedChangeListener
import com.blueskybone.arkscreen.common.bgImageButton
import com.blueskybone.arkscreen.common.getCustomRadioGroup
import com.blueskybone.arkscreen.common.getRadioButton
import com.blueskybone.arkscreen.databinding.ActivityWidgetSettingBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceRadioBinding
import com.blueskybone.arkscreen.databinding.PreferenceSeekbarBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.receiver.WidgetReceiver
import com.blueskybone.arkscreen.ui.bindinginfo.ListInfo
import com.blueskybone.arkscreen.ui.bindinginfo.SeekBarInfo
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetAlpha
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetContent
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetTextColor
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.getKoin

class WidgetThemeActivity : AppCompatActivity() {

    private var _binding: ActivityWidgetSettingBinding? = null
    private val binding get() = _binding!!
    private val prefManager: PrefManager by getKoin().inject()

    private val bgList =
        listOf(
            R.drawable.widget_bg_black,
            R.drawable.widget_bg_white,
            R.drawable.bg_1,
            R.drawable.bg_2,
            R.drawable.bg_3,
            R.drawable.bg_4,
            R.drawable.bg_5,
            R.drawable.bg_6,
            R.drawable.bg_7,
            R.drawable.bg_8,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.WidgetAlpha.setUp(WidgetAlpha, prefManager.widgetAlpha)

        bindImageRadioGroup()

        setUpBinding()

    }

    private fun setUpBinding() {
        binding.TextSize1x1.setUp(WidgetSize, prefManager.widget1Size, null)
        binding.TextSize1x2.setUp(WidgetSize, prefManager.widget2Size, null)
        binding.TextSize2x2.setUp(WidgetSize, prefManager.widget3Size, null)
        binding.TextSize2x3.setUp(WidgetSize, prefManager.widget4Size, null)

        binding.Context1x1.setUp(WidgetContent, prefManager.widget1Content, null)
        binding.Context1x2.setUp(WidgetContent, prefManager.widget2Content, null)
        binding.Context2x21.setUp(WidgetContent, prefManager.widget3Content1, null)
        binding.Context2x22.setUp(WidgetContent, prefManager.widget3Content2, null)

        binding.TextColor.setUp(WidgetTextColor, prefManager.widgetTextColor)
        binding.RecruitCheckBox.setup(prefManager.widget4ShowRecruit)
        binding.ApLaborCheckBox.setup(prefManager.widget4ShowDatabase)
        binding.TrainCheckBox.setup(prefManager.widget4ShowTrain)
        bindSwitchView(binding.ShowStarter,prefManager.widget4ShowStarter)

        binding.Apply.setOnClickListener{
            val intent = Intent(APP, WidgetReceiver::class.java)
            intent.action = WidgetReceiver.MANUAL_UPDATE
            intent.putExtra("msg", "组件刷新中...")
            APP.sendBroadcast(intent)
        }
    }

    private fun PreferenceSeekbarBinding.setUp(
        seekBarInfo: SeekBarInfo, pref: Preference<Int>
    ) {
        Title.setText(seekBarInfo.title)

        Slider.valueTo = seekBarInfo.max.toFloat()
        Slider.valueFrom = seekBarInfo.min.toFloat()

        Slider.value = pref.get().toFloat()
        Slider.stepSize = seekBarInfo.step.toFloat()
        Slider.addOnChangeListener { _, value, _ ->
            pref.set(value.toInt())
        }
    }


    private fun bindImageRadioGroup() {
        val radioGroup = getCustomRadioGroup(this)
        for (bg in bgList) {
            val imageButton = bgImageButton(this, bg).apply {
                if (prefManager.widgetBg.get() == bg) {
                    isSelected = true
                }
            }
            radioGroup.addView(imageButton)
        }

        radioGroup.setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(group: CustomRadioGroup, checkedId: Int) {
                val selectedButton = group.findViewById<ImageButton>(checkedId)
                if (selectedButton != null) {
                    val bg = selectedButton.tag as? Int ?: R.drawable.widget_bg_black
                    prefManager.widgetBg.set(bg)
                }
            }
        })
        binding.ImageGroupView.addView(radioGroup)
    }

    private fun PreferenceBinding.setUp(
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        Title.setText(listInfo.title)
        val entries = listInfo.getEntries(this@WidgetThemeActivity)
        val entryValues = listInfo.getEntryValues()
        var checked = entryValues.indexOf(pref.get())
        val displayValue = entries[checked]
        Value.text = displayValue
        root.setOnClickListener {
            MaterialAlertDialogBuilder(this@WidgetThemeActivity)
                .setTitle(listInfo.title)
                .setSingleChoiceItems(entries, checked) { dialog, which ->
                    dialog.cancel()
                    pref.set(entryValues[which])
                    Value.text = entries[which]
                    checked = which
                    onClick?.invoke()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun PreferenceRadioBinding.setUp(listInfo: ListInfo, pref: Preference<String>) {
        this.Title.text = getString(listInfo.title)
        val entries = listInfo.getEntries(this@WidgetThemeActivity)
        val entryValues = listInfo.getEntryValues()
        this.RadioGroup.removeAllViews()
        entries.forEachIndexed { index, entry ->
            val button = getRadioButton(this@WidgetThemeActivity).apply {
                id = View.generateViewId() // 为每个按钮生成唯一ID
                text = entry
                isChecked = (pref.get() == entryValues[index])
            }
            this.RadioGroup.addView(button)
        }
        this.RadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedIndex = group.indexOfChild(group.findViewById(checkedId))
            if (checkedIndex >= 0) {
                pref.set(entryValues[checkedIndex])
            }
        }
    }

    private fun CheckBox.setup(pref: Preference<Boolean>) {
        if (pref.get()) {
            this.isChecked = true
        }
        this.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                pref.set(true)
            } else {
                pref.set(false)
            }
        }
    }
    private fun bindSwitchView(switch: SwitchCompat, pref: Preference<Boolean>) {
        switch.isChecked = pref.get()
        switch.setOnCheckedChangeListener { _, isChecked -> pref.set(isChecked) }
    }
}