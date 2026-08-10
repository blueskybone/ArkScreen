package com.blueskybone.arkscreen.ui.widget

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.databinding.ActivityWidgetSettingBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceRadioBinding
import com.blueskybone.arkscreen.databinding.PreferenceSeekbarBinding
import com.blueskybone.arkscreen.ui.common.bindinginfo.ListInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.SeekBarInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetAlpha
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetContent
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetTextColor
import com.blueskybone.arkscreen.ui.common.view.CustomRadioGroup
import com.blueskybone.arkscreen.ui.common.view.CustomRadioGroup.OnCheckedChangeListener
import com.blueskybone.arkscreen.ui.common.view.bgImageButton
import com.blueskybone.arkscreen.ui.common.view.getCustomRadioGroup
import com.blueskybone.arkscreen.ui.common.view.getRadioButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.getKoin

class WidgetThemeActivity : AppCompatActivity() {

    private var _binding: ActivityWidgetSettingBinding? = null
    private val binding get() = _binding!!
    private val prefManager: SettingPrefManager by getKoin().inject()

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

        binding.WidgetAlpha.setUp(
            seekBarInfo = WidgetAlpha,
            pref = prefManager.widgetAlpha,
            onChange = ::updatePreview,
        )

        bindImageRadioGroup()

        setUpBinding()
        updatePreview()

    }

    private fun setUpBinding() {
        binding.NewWidgetExperiment.apply {
            Title.setText(R.string.widget_template_lab)
            Icon.setImageResource(R.drawable.ic_widget_appearance)
            Layout.setOnClickListener {
                startActivity(
                    Intent(this@WidgetThemeActivity, WidgetTemplateLabActivity::class.java)
                )
            }
        }

        binding.TextSize1x1.setUp(null, WidgetSize, prefManager.widget1Size, null)
        binding.TextSize1x2.setUp(null, WidgetSize, prefManager.widget2Size, null)
        binding.TextSize2x2.setUp(null, WidgetSize, prefManager.widget3Size, null)
        binding.TextSize2x3.setUp(null, WidgetSize, prefManager.widget4Size, null)

        binding.Context1x1.setUp(null, WidgetContent, prefManager.widget1Content, null)
        binding.Context1x2.setUp(null, WidgetContent, prefManager.widget2Content, null)
        binding.Context2x21.setUp(null, WidgetContent, prefManager.widget3Content1, null)
        binding.Context2x22.setUp(null, WidgetContent, prefManager.widget3Content2, null)

        binding.TextColor.setUp(
            listInfo = WidgetTextColor,
            pref = prefManager.widgetTextColor,
            onChange = ::updatePreview,
        )
        binding.RecruitCheckBox.setup(prefManager.widget4ShowRecruit)
        binding.ApLaborCheckBox.setup(prefManager.widget4ShowDatabase)
        binding.TrainCheckBox.setup(prefManager.widget4ShowTrain)
        bindSwitchView(binding.ShowStarter, prefManager.widget4ShowStarter)

        binding.Apply.setOnClickListener {
            WidgetReceiver.renderCached(this)
        }
    }

    private fun PreferenceSeekbarBinding.setUp(
        seekBarInfo: SeekBarInfo,
        pref: Preference<Int>,
        onChange: (() -> Unit)? = null,
    ) {
        Title.setText(seekBarInfo.title)

        Slider.valueTo = seekBarInfo.max.toFloat()
        Slider.valueFrom = seekBarInfo.min.toFloat()

        Slider.value = pref.get().toFloat()
        Slider.stepSize = seekBarInfo.step.toFloat()
        Slider.addOnChangeListener { _, value, _ ->
            pref.set(value.toInt())
            onChange?.invoke()
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
                    updatePreview()
                }
            }
        })
        binding.ImageGroupView.addView(radioGroup)
    }

    private fun PreferenceBinding.setUp(
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        if (icon == null) {
            Icon.visibility = View.GONE
        } else Icon.setImageResource(icon)
        Title.setText(listInfo.title)
        val entries = listInfo.getEntries(this@WidgetThemeActivity)
        val entryValues = listInfo.getEntryValues()
        var checked = entryValues.indexOf(pref.get()).coerceAtLeast(0)
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

    private fun PreferenceRadioBinding.setUp(
        listInfo: ListInfo,
        pref: Preference<String>,
        onChange: (() -> Unit)? = null,
    ) {
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
                onChange?.invoke()
            }
        }
    }

    private fun updatePreview() {
        val textColor = WidgetTextColor.getColorInt(prefManager.widgetTextColor.get())
        binding.PreviewBackground.setImageResource(prefManager.widgetBg.get())
        binding.PreviewBackground.imageAlpha = prefManager.widgetAlpha.get()
        binding.PreviewValue.setTextColor(textColor)
        binding.PreviewSecondary.setTextColor(textColor)
        binding.PreviewIcon.setColorFilter(textColor)
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
