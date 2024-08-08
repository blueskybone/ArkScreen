package com.blueskybone.arkscreen.activity

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.createLine
import com.blueskybone.arkscreen.common.createSpace
import com.blueskybone.arkscreen.common.opeTextView
import com.blueskybone.arkscreen.common.tagTextView
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.common.TagViewGroup
import com.blueskybone.arkscreen.common.getRarityColorId
import com.blueskybone.arkscreen.common.tagButtonView
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.util.getEleCombination
import com.hjq.toast.Toaster
import com.nex3z.flowlayout.FlowLayout
import org.koin.android.ext.android.getKoin


/**
 *   Created by blueskybone
 *   Date: 2024/7/29
 */
class CalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
    }

    private var tagCnt = 0

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
    }

    private val buttonViewList: ArrayList<Button> = ArrayList()
    private val flowLayoutList: ArrayList<FlowLayout> = ArrayList()


    private fun setLayout() {
        setContentView(R.layout.activity_calculate)
        setButtonLayout()
        title = getString(R.string.recruit_cal)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setButtonLayout() {
        val thread = Thread {
            val linearLayout = findViewById<LinearLayout>(R.id.linear_recruit_button)
            for (buttonList in buttonListAll) {
                val flowLayout = getFlowLayout()
                for (button in buttonList) {
                    val buttonView = tagButton(button)
                    buttonViewList.add(buttonView)
                    flowLayout.addView(buttonView)
                }
                flowLayoutList.add(flowLayout)
            }
            flowLayoutList.last().addView(resetButton("重置"))
            Handler(Looper.getMainLooper()).post {
                for (flowLayout in flowLayoutList) {
                    linearLayout.addView(flowLayout)
                }
            }
        }
        thread.start()
    }

    private fun tagButton(text: String): Button {
        val button = tagButtonView(this, text)
        val typedValue = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorSecondaryVariant, typedValue, true
        )
        button.setTextColor(typedValue.data)
        button.setBackgroundResource(R.drawable.button_tag)
        button.setOnClickListener {
            if (button.isSelected) {
                button.isSelected = false
                tagCnt--
            } else if (tagCnt < 6) {
                button.isSelected = true
                tagCnt++
            } else {
                Toaster.show("最多选择6个标签")
            }
            try {
                startCalculateRecruit()
            } catch (e: Exception) {
                e.printStackTrace()
                Toaster.show(e.message)
            }
        }
        return button
    }

    private fun resetButton(text: String): Button {
        val button = tagButtonView(this, text)
        val typedValue = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorSecondaryVariant, typedValue, true
        )
        button.setTextColor(typedValue.data)
        button.setBackgroundResource(R.drawable.button_reset)
        button.setOnClickListener {
            for (buttonView in buttonViewList) {
                buttonView.isSelected = false
            }
            tagCnt = 0
            try {
                startCalculateRecruit()
            } catch (e: Exception) {
                e.printStackTrace()
                Toaster.show(e.message)
            }
        }
        return button
    }

    private fun opeButtonView(context: Context, ope: RecruitManager.Operator): Button {
        val button = tagButtonView(context, ope.name)
        val colorId = getRarityColorId(ope.rare)
        val typedValue = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorSecondaryVariant, typedValue, true
        )
        button.setTextColor(typedValue.data)

        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(context, 2F)
            setColor(ContextCompat.getColor(context, colorId))
        }
        button.background = shapeDrawable
        return button
    }

    private fun resultTagButtonView(context: Context, text: String): Button {
        val button = tagButtonView(context, text)
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(context, 2F)
            setColor(ContextCompat.getColor(context, R.color.grey))
        }
        val typedValue = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorSecondaryVariant, typedValue, true
        )
        button.setTextColor(typedValue.data)
        button.isClickable = false
        button.background = shapeDrawable
        return button
    }


    private fun startCalculateRecruit() {
        val thread = Thread {
            val recruitManager: RecruitManager by getKoin().inject()
            val tags = ArrayList<String>()
            for (buttonView in buttonViewList) {
                if (buttonView.isSelected) {
                    tags.add(buttonView.text.toString())
                }
            }
            val tagsList = getEleCombination(tags)
            val recruitResultList = mutableListOf<RecruitManager.RecruitResult>()
            for (tagsCom in tagsList) {
                val recruitResult = recruitManager.getRecruitResult(tagsCom, false)
                if (recruitResult.operators.isNotEmpty()) {
                    recruitResult.sort()
                    recruitResultList.add(recruitResult)
                }
            }
            val finalList = recruitResultList.toList().sorted()
            showResult(finalList)
        }
        thread.start()
    }

    private fun showResult(resultList: List<RecruitManager.RecruitResult>) {
        val linearLayout = findViewById<LinearLayout>(R.id.linear_recruit_result)
        val view = LinearLayout(this)
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        view.layoutParams = layoutParams
        view.orientation = LinearLayout.VERTICAL

        val tagLayoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
        )
        tagLayoutParams.setMargins(0, 0, dpToPx(this, 5F).toInt(), dpToPx(this, 5F).toInt())
        for (result in resultList) {
            val tagLayout = getFlowLayout()
            val opeLayout = getFlowLayout()
            for (tag in result.tags) {
                val tagView = resultTagButtonView(this, tag)
                tagLayout.addView(tagView)
            }
            val rarityButton = resultTagButtonView(this, "${result.rare}★")
            tagLayout.addView(rarityButton)
            for (operator in result.operators) {
                val opeView = opeButtonView(this, operator)
                opeLayout.addView(opeView)
            }
            view.addView(tagLayout)
            view.addView(opeLayout)
            view.addView(createLine(this))
        }
        Handler(Looper.getMainLooper()).post {
            linearLayout.removeAllViews()
            linearLayout.addView(view)
        }
    }


    private fun getFlowLayout(): FlowLayout {
        val flowLayout = FlowLayout(this)
        flowLayout.childSpacingForLastRow = FlowLayout.SPACING_ALIGN
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        flowLayout.layoutParams = layoutParams
        return flowLayout
    }
}