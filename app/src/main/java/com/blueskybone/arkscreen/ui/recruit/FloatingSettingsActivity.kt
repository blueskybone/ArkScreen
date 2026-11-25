package com.blueskybone.arkscreen.ui.recruit

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.common.showChoicePopup
import com.blueskybone.arkscreen.ui.recruit.screenshot.FloatingAppearance
import com.blueskybone.arkscreen.ui.recruit.screenshot.FloatWindowController
import com.google.android.material.slider.Slider
import org.koin.android.ext.android.inject

class FloatingSettingsActivity : AppCompatActivity() {
    private val prefs: SettingPrefManager by inject()
    private var style = FloatingAppearance()
    private var mode = RecruitMode.FLOATWINDOW
    private lateinit var binding: com.blueskybone.arkscreen.databinding.ActivityFloatingSettingsBinding
    private lateinit var preview: FrameLayout
    private lateinit var ballPreview: FrameLayout
    private lateinit var modeHint: TextView
    private val swatches = mutableListOf<Pair<String, ImageView>>()
    private var popup: androidx.appcompat.widget.ListPopupWindow? = null
    private val renderer by lazy { FloatWindowController(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        style = FloatingAppearance.read(prefs)
        mode = prefs.recruitMode.get()
        savedInstanceState?.let {
            style = style.copy(width=it.getInt("w"), height=it.getInt("h"), font=it.getInt("f"),
                colorful=it.getBoolean("c"), ballSize=it.getInt("b"), ballColor=it.getString("ballColor") ?: style.ballColor)
            mode = it.getString("mode") ?: mode
        }
        binding = com.blueskybone.arkscreen.databinding.ActivityFloatingSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.Toolbar.setNavigationOnClickListener { finish() }
        preview = binding.Preview
        ballPreview = binding.BallPreview
        modeHint = binding.ModeHint
        modeHint.visibility = if (mode == RecruitMode.AUTO) View.VISIBLE else View.GONE
        choice(binding.ModeRow.root, "展示方式", RecruitMode.getEntries(this).toList(), RecruitMode.getEntryValues().indexOf(mode)) {
            mode = RecruitMode.getEntryValues()[it]
            modeHint.visibility = if (mode == RecruitMode.AUTO) View.VISIBLE else View.GONE
            if (mode == RecruitMode.TOAST) com.hjq.toast.Toaster.show("示例：召唤 · 4★\n梅尔、豆苗")
        }
        slider(binding.WidthSlider.root, style.width, "dp") { style=style.copy(width=it); render() }
        slider(binding.HeightSlider.root, style.height, "dp") { style=style.copy(height=it); render() }
        slider(binding.FontSlider.root, style.font, "%") { style=style.copy(font=it); render() }
        slider(binding.BallSlider.root, style.ballSize, "dp") { style=style.copy(ballSize=it); render() }
        colorPalette()
        binding.Save.setOnClickListener {
            style.save(prefs); prefs.recruitMode.set(mode)
            org.koin.mp.KoinPlatform.getKoin().get<com.blueskybone.arkscreen.platform.tile.RecruitFloatingBallController>().refreshAppearance()
            org.koin.mp.KoinPlatform.getKoin().get<FloatWindowController>().refreshAppearance()
            com.hjq.toast.Toaster.show("已保存")
        }
        preview.post { render() }
    }
    private fun render() {
        if (!::ballPreview.isInitialized) return
        val tags = listOf("召唤", "减速", "输出")
        val results = listOf(
            RecruitResult(listOf("召唤"), listOf(RecruitOpe("梅尔",5, emptyList(),""),RecruitOpe("豆苗",4, emptyList(),""))),
            RecruitResult(listOf("减速", "输出"), listOf(RecruitOpe("真理",5, emptyList(),""),RecruitOpe("夜烟",4, emptyList(),""))))
        preview.removeAllViews()
        val view = renderer.preview(tags,results,style)
        val width=dp(style.width).coerceAtMost((preview.width-dp(24)).coerceAtLeast(dp(140)))
        preview.layoutParams = preview.layoutParams.apply { height=dp(style.height+24) }
        preview.addView(view,FrameLayout.LayoutParams(width,dp(style.height),Gravity.CENTER))
        ballPreview.removeAllViews()
        ballPreview.addView(style.ball(this),FrameLayout.LayoutParams(-2,-2,Gravity.CENTER))
    }
    private fun colorPalette() {
        val flow = binding.Palette
        FloatingAppearance.colors.forEachIndexed { index,key ->
            val swatch=(layoutInflater.inflate(R.layout.item_floating_color, flow, false) as ImageView).apply {
                contentDescription=FloatingAppearance.colorNames[index]
                setOnClickListener { style=style.copy(ballColor=key); updateSwatches(); render() }
            }
            flow.addView(swatch)
            swatches.add(key to swatch)
        }
        updateSwatches()
    }
    private fun updateSwatches() {
        swatches.forEach { (key,view) ->
            val swatchStyle=style.copy(ballColor=key)
            val selected = key == style.ballColor
            val background = ContextCompat.getDrawable(this, R.drawable.bg_floating_color)!!.mutate() as android.graphics.drawable.LayerDrawable
            (background.findDrawableByLayerId(R.id.ColorRing) as android.graphics.drawable.GradientDrawable)
                .setStroke(dp(2), if(selected) swatchStyle.fillColor() else android.graphics.Color.TRANSPARENT)
            (background.findDrawableByLayerId(R.id.ColorFill) as android.graphics.drawable.GradientDrawable).setColor(swatchStyle.fillColor())
            view.background = background
            view.isSelected=key==style.ballColor
            view.setImageResource(if(view.isSelected) R.drawable.ic_ball_success else 0)
            view.setColorFilter(android.graphics.Color.rgb(25, 30, 28))
        }
    }
    private fun choice(row: View, title:String, values:List<String>, selected:Int, changed:(Int)->Unit) {
        row.findViewById<TextView>(R.id.RowTitle).text=title
        val value=row.findViewById<TextView>(R.id.RowValue); var index=selected.coerceAtLeast(0); value.text=values[index]
        row.setOnClickListener {
            popup?.dismiss(); popup=showChoicePopup(value,values,index) { index=it; value.text=values[it]; changed(it) }
        }
    }
    private fun slider(row: View, initial:Int, unit:String, changed:(Int)->Unit) {
        val value=row.findViewById<TextView>(R.id.SliderValue);value.text="$initial$unit"
        row.findViewById<Slider>(R.id.Slider).apply {
            this.value=initial.toFloat()
            setLabelFormatter { "${it.toInt()}$unit" }
            setCustomThumbDrawable(android.graphics.drawable.GradientDrawable().apply {
                cornerRadius=dp(3).toFloat();setColor(color(R.color.home_text_secondary));setSize(dp(6),dp(32))
            })
            addOnChangeListener { _,v,user -> if(user) { value.text="${v.toInt()}$unit";changed(v.toInt()) } }
        }
    }
    override fun onSaveInstanceState(out:Bundle) {
        super.onSaveInstanceState(out)
        out.putInt("w",style.width);out.putInt("h",style.height);out.putInt("f",style.font);out.putInt("b",style.ballSize)
        out.putBoolean("c",style.colorful);out.putString("ballColor",style.ballColor);out.putString("mode",mode)
    }
    override fun onDestroy() { popup?.dismiss();super.onDestroy() }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun color(id:Int)=ContextCompat.getColor(this,id)
}
