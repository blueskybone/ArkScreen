package com.blueskybone.arkscreen.activity


import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.line
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.viewmodel.RecruitModel
import com.nex3z.flowlayout.FlowLayout
import org.koin.android.ext.android.getKoin
import java.net.URLEncoder

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */
class RecruitActivity : AppCompatActivity() {
    private val model: RecruitModel by viewModels()
    private val prefManager: PrefManager by getKoin().inject()


    companion object {
        private val buttonList1 = listOf("高级资深干员", "资深干员", "新手")
        private val buttonList2 = listOf(
            "近卫干员",
            "狙击干员",
            "重装干员",
            "医疗干员",
            "辅助干员",
            "术师干员",
            "特种干员",
            "先锋干员"
        )
        private val buttonList3 = listOf("近战位", "远程位")
        private val buttonList4 = listOf(
            "支援机械",
            "控场",
            "爆发",
            "治疗",
            "支援",
            "费用回复",
            "输出",
            "生存",
            "群攻",
            "防护",
            "减速",
            "削弱",
            "快速复活",
            "位移",
            "召唤",
            "元素",
        )
        private val buttonListAll = listOf(buttonList1, buttonList2, buttonList3, buttonList4)
        private const val TAG_MAX = 6
    }

    private val buttonViewList: MutableList<Button> = ArrayList()
    private val flowLayoutList: MutableList<FlowLayout> = ArrayList()

    private var selectedTag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(R.layout.activity_recruit)
        val toolBar = findViewById<Toolbar>(R.id.Toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setButtonLayout()
        setObserver()
    }

    private lateinit var rarityValValues: List<String>
    private lateinit var rarityColorIds: List<Int>

    private fun initialize() {
        val rarityValues = resources.getStringArray(R.array.rarity_value)
        val rarityDrawable: TypedArray = resources.obtainTypedArray(R.array.rarity_draw)

        rarityValValues = rarityValues.toList()
        val list = ArrayList<Int>()
        for (idx in 0..rarityValValues.size) {
            list.add(rarityDrawable.getResourceId(idx, 1))
        }
        rarityColorIds = list
        rarityDrawable.recycle()
    }

    private fun setButtonLayout() {
        val linearLayout = findViewById<LinearLayout>(R.id.ButtonLayout)
        linearLayout.removeAllViews()

        for (buttonList in buttonListAll) {
            val flowLayout = getFlowLayout()
            for (button in buttonList) {
                val buttonView = tagButton(button)
                buttonViewList.add(buttonView)
                flowLayout.addView(buttonView)
            }
            flowLayoutList.add(flowLayout)
        }
        flowLayoutList.last().addView(resetButton())
        for (flowLayout in flowLayoutList) {
            linearLayout.addView(flowLayout)
        }
    }

    private fun getFlowLayout(): FlowLayout {
        val flowLayout = FlowLayout(this)
        flowLayout.childSpacingForLastRow = FlowLayout.SPACING_ALIGN
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        flowLayout.layoutParams = layoutParams
        return flowLayout
    }

    private fun Button.setTagLayout(text: String) {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, dpToPx(context, 5F).toInt(), dpToPx(context, 5F).toInt())
        this.layoutParams = layoutParams
        this.text = text
        this.minWidth = 0
        this.minHeight = 0
        this.minimumWidth = 0
        this.minimumHeight = 0
        this.setPadding(
            dpToPx(context, 10F).toInt(),
            dpToPx(context, 5F).toInt(),
            dpToPx(context, 10F).toInt(),
            dpToPx(context, 5F).toInt()
        )
        this.isEnabled = true
        this.isSelected = false
        this.isClickable = true
        this.stateListAnimator = null
        this.setTextColor(Color.WHITE)
    }

    private fun tagButton(text: String): Button {
        val button = Button(this)
        button.setTagLayout(text)
        button.setBackgroundResource(R.drawable.button_tag)
        button.setOnClickListener {
            if (button.isSelected) {
                button.isSelected = false
                selectedTag--
            } else if (selectedTag < TAG_MAX) {
                button.isSelected = true
                selectedTag++
            } else {
                Toast.makeText(this, "最多选择${TAG_MAX}个标签", Toast.LENGTH_SHORT).show()
            }
            try {
                val tags = ArrayList<String>()
                for (buttonView in buttonViewList) {
                    if (buttonView.isSelected) {
                        tags.add(buttonView.text.toString())
                    }
                }
                model.startCalculate(tags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return button
    }

    private fun resetButton(): Button {
        val button = Button(this)
        button.setTagLayout(getString(R.string.reset))
        button.setBackgroundResource(R.drawable.button_reset)
        button.setOnClickListener {
            for (buttonView in buttonViewList) {
                buttonView.isSelected = false
            }
            selectedTag = 0
            try {
                model.reset()
            } catch (e: Exception) {
                e.printStackTrace()
                // Toaster.show(e.message)
            }
        }
        return button
    }

    private fun opeButton(context: Context, chars: RecruitManager.Operator): Button {
        val button = Button(this)
        button.setTagLayout(chars.name)

        val rarityIdx = rarityValValues.indexOf((chars.rare).toString())
        val colorId = rarityColorIds[rarityIdx]
        button.setBackgroundColor(getColor(colorId))
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(context, 2F)
            setColor(ContextCompat.getColor(context, colorId))
        }
        button.background = shapeDrawable
        return button
    }

    private fun resultTagButton(context: Context, text: String): Button {
        val button = Button(context)
        button.setTagLayout(text)
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(context, 2F)
            setColor(ContextCompat.getColor(context, R.color.blue_500))
        }
        button.isClickable = false
        button.background = shapeDrawable
        return button
    }

    private fun displayCharDialog(operator: RecruitManager.Operator) {

        val builder = AlertDialog.Builder(this)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(20, 20, 20, 20)

        val flowLayout = getFlowLayout()
        for (tag in operator.tags) {
            val tagView = resultTagButton(this, tag)
            buttonViewList.add(tagView)
            flowLayout.addView(tagView)
        }
        flowLayoutList.add(flowLayout)

        linearLayout.addView(opeButton(this, operator))
        linearLayout.addView(flowLayout)

        builder.setView(linearLayout)
            .setPositiveButton(getString(R.string.goto_PRTS)) { _, _ ->
                val url = "https://prts.wiki/w/" + URLEncoder.encode(operator.name, "UTF-8")
                if (prefManager.useInnerWeb.get()) {
                    val intent = Intent(this, WebViewActivity::class.java)
                    intent.putExtra("url", url)
                    startActivity(intent)
                } else {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        builder.create().show()
    }

    private fun showResult(resultList: List<RecruitManager.RecruitResult>) {
        val linearLayout = findViewById<LinearLayout>(R.id.ResultLayout)
        val view = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        view.layoutParams = layoutParams
        view.orientation = LinearLayout.VERTICAL

        val tagLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tagLayoutParams.setMargins(0, 0, dpToPx(this, 5F).toInt(), dpToPx(this, 5F).toInt())
        println("resultSize" + resultList.size)
        for (result in resultList) {
            val tagLayout = getFlowLayout()
            val opeLayout = getFlowLayout()
            for (tag in result.tags) {
                val tagView = resultTagButton(this, tag)
                tagLayout.addView(tagView)
            }
            val rarityButton = resultTagButton(this, "${result.rare}★")
            tagLayout.addView(rarityButton)
            for (operator in result.operators) {
                val opeView = opeButton(this, operator)
                opeView.setOnClickListener {
                    displayCharDialog(operator)
                }
                opeLayout.addView(opeView)
            }
            view.addView(tagLayout)
            view.addView(opeLayout)
            view.addView(line(this))
        }
        Handler(Looper.getMainLooper()).post {
            linearLayout.removeAllViews()
            linearLayout.addView(view)
        }
    }

    private fun displayView() {
        val view = findViewById<ScrollView>(R.id.ScrollView)
        val page = findViewById<FrameLayout>(R.id.Page)
        view.visibility = View.VISIBLE
        page.visibility = View.GONE
    }

    private fun displayLoadingView(msg: String) {
        val view = findViewById<ScrollView>(R.id.ScrollView)
        val page = findViewById<FrameLayout>(R.id.Page)
        val text = findViewById<TextView>(R.id.Message)
        view.visibility = View.GONE
        page.visibility = View.VISIBLE
        text.text = msg
    }

    private fun setObserver() {

        model.uiState.observe(this) { value ->
            when (value) {
                is DataUiState.Success -> displayView()
                else -> displayLoadingView(value.msg)
            }
        }
        model.update.observe(this) { update ->
            val text = findViewById<TextView>(R.id.Update)
            text.text = getString(R.string.last_update, update)
        }
        model.result.observe(this) { result ->
            showResult(result)
        }
    }


}