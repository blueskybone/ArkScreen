package com.blueskybone.arkscreen.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Audio.Radio
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.CustomRadioGroup
import com.blueskybone.arkscreen.common.CustomRadioGroup.OnCheckedChangeListener
import com.blueskybone.arkscreen.common.bgImageButton
import com.blueskybone.arkscreen.common.getCustomRadioGroup
import com.blueskybone.arkscreen.common.getFlowRadioGroup
import com.blueskybone.arkscreen.common.getRadioButton
import com.blueskybone.arkscreen.databinding.ActivityWidgetSettingBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceRadioBinding
import com.blueskybone.arkscreen.databinding.PreferenceSeekbarBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.ui.bindinginfo.ListInfo
import com.blueskybone.arkscreen.ui.bindinginfo.SeekBarInfo
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetAlpha
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetContent
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetTextColor
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin

class WidgetThemeActivity : AppCompatActivity() {

    private var _binding: ActivityWidgetSettingBinding? = null
    private val binding get() = _binding!!
    private val prefManager: PrefManager by getKoin().inject()

    private val bgList =
        listOf(
            R.drawable.widget_background,
            R.drawable.widget_bg_white,
            R.drawable.activity,
            R.drawable.act_1,
            R.drawable.act_2,
            R.drawable.act_3
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
                    val bg = selectedButton.tag as? Int ?: R.drawable.widget_background
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

        // 先清除所有已有视图
        this.RadioGroup.removeAllViews()

        // 添加RadioButtons
        entries.forEachIndexed { index, entry ->
            val button = getRadioButton(this@WidgetThemeActivity).apply {
                id = View.generateViewId() // 为每个按钮生成唯一ID
                text = entry
                isChecked = (pref.get() == entryValues[index])
            }
            this.RadioGroup.addView(button)
        }

        // 设置RadioGroup的监听器
        this.RadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedIndex = group.indexOfChild(group.findViewById(checkedId))
            if (checkedIndex >= 0) {
                pref.set(entryValues[checkedIndex])
            }
        }
    }
}