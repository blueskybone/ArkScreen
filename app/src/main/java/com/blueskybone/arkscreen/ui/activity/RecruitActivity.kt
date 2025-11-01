package com.blueskybone.arkscreen.ui.activity

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import coil.load
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.BottomDialog
import com.blueskybone.arkscreen.common.getFlowLayout
import com.blueskybone.arkscreen.common.line
import com.blueskybone.arkscreen.common.space
import com.blueskybone.arkscreen.common.tagButton
import com.blueskybone.arkscreen.databinding.ChipRecruitBinding
import com.blueskybone.arkscreen.network.avatarUrl
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.util.copyToClipboard
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.util.openLink
import com.blueskybone.arkscreen.viewmodel.RecruitModel
import com.hjq.toast.Toaster
import com.nex3z.flowlayout.FlowLayout
import org.koin.android.ext.android.getKoin
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLEncoder

/**
 * 公招计算器界面
 */
class RecruitActivity : AppCompatActivity() {
    private val model: RecruitModel by viewModels()
    private val prefManager: PrefManager by getKoin().inject()

    companion object {
        // 标签分类列表
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
        private const val TAG_MAX = 6 // 最大选择标签数
    }

    private val buttonViewList: MutableList<Button> = ArrayList()
    private val flowLayoutList: MutableList<FlowLayout> = ArrayList()
    private var selectedTag = 0 // 当前选中的标签数量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            initialize()
            setContentView(R.layout.activity_recruit)
            // 设置工具栏
            val toolBar = findViewById<Toolbar>(R.id.Toolbar)
            setSupportActionBar(toolBar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setButtonLayout()
            setObserver()
        } catch (e: Exception) {
            // 错误处理
            val stringWriter = StringWriter()
            e.printStackTrace(PrintWriter(stringWriter))
            val errorMsg = stringWriter.toString()
            Toaster.show("初始化失败")
            BottomDialog(this)
                .setButtonText("复制错误信息")
                .setText(errorMsg)
                .setButtonOnclick {
                    copyToClipboard(this, errorMsg)
                    Toaster.show("已复制到剪贴板")
                }
                .show()
        }
    }

    private lateinit var rarityValValues: List<String>
    private lateinit var rarityColorIds: List<Int>

    // 初始化稀有度相关数据
    private fun initialize() {
        val rarityValues = resources.getStringArray(R.array.rarity_value)
        val rarityDrawable: TypedArray = resources.obtainTypedArray(R.array.rarity_draw)

        rarityValValues = rarityValues.toList()
        val list = ArrayList<Int>()
        for (idx in rarityValValues.indices) {
            list.add(rarityDrawable.getResourceId(idx, 1))
        }
        rarityColorIds = list
        rarityDrawable.recycle()
    }

    // 设置标签按钮布局
    private fun setButtonLayout() {
        val linearLayout = findViewById<LinearLayout>(R.id.ButtonLayout)
        linearLayout.removeAllViews()

        for ((index, buttonList) in buttonListAll.withIndex()) {
            val flowLayout = getFlowLayout(this)
            for (button in buttonList) {
                val buttonView = tagButton(this, button)
                buttonView.setup()
                buttonViewList.add(buttonView)
                flowLayout.addView(buttonView)
            }
            // 最后一组添加重置按钮，其他组添加分隔线
            if (index == buttonListAll.size - 1) {
                flowLayout.addView(resetButton())
            } else {
                flowLayout.addView(line(this))
            }
            flowLayoutList.add(flowLayout)
        }
        for (flowLayout in flowLayoutList) {
            linearLayout.addView(flowLayout)
        }
    }

    // 设置按钮点击事件
    private fun Button.setup() {
        this.setOnClickListener {
            if (this.isSelected) {
                this.isSelected = false
                selectedTag--
            } else if (selectedTag < TAG_MAX) {
                this.isSelected = true
                selectedTag++
            } else {
                Toaster.show("最多选择${TAG_MAX}个标签")
            }
            try {
                // 收集选中的标签并开始计算
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
    }

    // 创建重置按钮
    private fun resetButton(): Button {
        val button = tagButton(this, getString(R.string.reset))
        button.setBackgroundResource(R.drawable.button_reset)
        button.setOnClickListener {
            // 重置所有标签选择
            for (buttonView in buttonViewList) {
                buttonView.isSelected = false
            }
            selectedTag = 0
            try {
                model.reset()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return button
    }

    // 创建干员信息视图
    private fun opeButton(context: Context, chars: RecruitManager.Operator): View {
        val binding = ChipRecruitBinding.inflate(LayoutInflater.from(context))
        // 加载头像
        binding.Avatar.load("$avatarUrl${chars.skin}%231.png") {
            crossfade(true)
            crossfade(300)
        }
        binding.Name.text = chars.name
        binding.Rare.text = " ${chars.rare}★ "

        // 设置稀有度背景色
        val rarityIdx = rarityValValues.indexOf((chars.rare).toString())
        val colorId = rarityColorIds[rarityIdx]
        binding.Rare.setBackgroundColor(getColor(colorId))

        return binding.root
    }

    // 创建结果标签按钮
    private fun resultTagButton(context: Context, text: String): Button {
        val button = tagButton(this, text)
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(context, 2F)
            setColor(ContextCompat.getColor(context, R.color.blue_500))
        }
        button.isClickable = false
        button.background = shapeDrawable
        return button
    }

    // 显示干员详情对话框
    private fun displayCharDialog(operator: RecruitManager.Operator) {
        val builder = AlertDialog.Builder(this)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(80, 80, 80, 80)

        val flowLayout = getFlowLayout(this)
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
                // 跳转到PRTS维基
                val url = "https://prts.wiki/w/" + URLEncoder.encode(operator.name, "UTF-8")
                openLink(this, url, prefManager)
            }
        builder.create().show()
    }

    // 显示计算结果
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

        for (result in resultList) {
            view.addView(line(this))
            val tagLayout = getFlowLayout(this)
            val opeLayout = getFlowLayout(this)
            // 添加稀有度标签
            val rarityButton = resultTagButton(this, "${result.rare}★")
            tagLayout.addView(rarityButton)
            // 添加标签
            for (tag in result.tags) {
                val tagView = resultTagButton(this, tag)
                tagLayout.addView(tagView)
            }
            // 添加干员
            for (operator in result.operators) {
                val opeView = opeButton(this, operator)
                opeView.setOnClickListener {
                    displayCharDialog(operator)
                }
                opeLayout.addView(opeView)
            }
            view.addView(tagLayout)
            view.addView(opeLayout)
            view.addView(space(this, 5F))
        }
        // 在主线程更新UI
        Handler(Looper.getMainLooper()).post {
            linearLayout.removeAllViews()
            linearLayout.addView(view)
        }
    }

    // 显示结果视图
    private fun displayView() {
        val view = findViewById<ScrollView>(R.id.ScrollView)
        val page = findViewById<FrameLayout>(R.id.Page)
        view.visibility = View.VISIBLE
        page.visibility = View.GONE
    }

    // 显示加载视图
    private fun displayLoadingView(msg: String) {
        val view = findViewById<ScrollView>(R.id.ScrollView)
        val page = findViewById<FrameLayout>(R.id.Page)
        val text = findViewById<TextView>(R.id.Message)
        view.visibility = View.GONE
        page.visibility = View.VISIBLE
        text.text = msg
    }

    // 设置数据观察者
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