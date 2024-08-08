package com.blueskybone.arkscreen.activity

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.App
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.data.AccountSk
import com.blueskybone.arkscreen.base.data.PlayerData
import com.blueskybone.arkscreen.network.HttpConnectionUtils.writeToLocal
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.network.NetWorkTask.getGameInfoInputConnection
import com.blueskybone.arkscreen.util.LoadingDialog
import com.blueskybone.arkscreen.util.TimeUtils
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getDayNum
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import com.blueskybone.arkscreen.util.dpToPx
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import java.util.zip.GZIPInputStream

/**
 *   Created by blueskybone
 *   Date: 2024/7/29
 */

class RealTimeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
        loadingGameData()
    }

    private fun setLayout() {
        setContentView(R.layout.activity_real_time)
        title = getString(R.string.real_time_data)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.real_time_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.menu_real_time_info) {
            showInfoDialog(this)
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun showInfoDialog(context: Context) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.data_details))
            .setMessage(getString(R.string.real_time_data_details)).create().show()
    }

    private fun loadingGameData() {
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        val thread = Thread {
            try {
                val prefManager: PrefManager by getKoin().inject()
                val baseAccount = prefManager.BaseAccount.get() ?: throw Exception("未登录")
                if (baseAccount.isExpired) {
                    throw Exception("token过期")
                }
                val playerData = getPlayerData(baseAccount) ?: return@Thread
                showResult(playerData)
                showPlayerInfo(baseAccount)
                loadingDialog.dismiss()
            } catch (e: Exception) {
                loadingDialog.dismiss()
                Toaster.show(e.message)
            }
        }
        thread.start()
    }

    private fun showPlayerInfo(baseAccount: AccountSk) {
        val channelImageView = findViewById<ImageView>(R.id.channel_image)
        val nicknameButton = findViewById<Button>(R.id.button_nickname)
        val uidButton = findViewById<Button>(R.id.button_uid)
        Handler(Looper.getMainLooper()).post {
            if (!baseAccount.isOfficial) channelImageView.setImageResource(R.drawable.bili_icon_75x71)
            nicknameButton.text = baseAccount.nickName
            uidButton.text = baseAccount.uid
            nicknameButton.setOnClickListener {
                val clipboard: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nicknameButton.text)
                clipboard.setPrimaryClip(clip)
                Toaster.show("已复制用户名")
            }
            uidButton.setOnClickListener {
                val clipboard: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", uidButton.text)
                clipboard.setPrimaryClip(clip)
                Toaster.show("已复制UID")
            }
        }
    }


    private fun showResult(playerData: PlayerData) {
        val textRegister = findViewById<TextView>(R.id.text_register)
        val textLastOnline = findViewById<TextView>(R.id.text_last_online)

        val registerStr = getTimeStr(playerData.playerStatus.registerTs * 1000, "yyyy-MM-dd")
        val textLastOnlineStr =
            getTimeStr(playerData.playerStatus.lastOnlineTs * 1000, "yyyy-MM-dd")

        val lastOnlineStr =
            when (getDayNum(getCurrentTs()) - getDayNum(playerData.playerStatus.lastOnlineTs)) {
                0L -> "今天"
                1L -> "昨天"
                else -> textLastOnlineStr
            }


        val view = findViewById<ScrollView>(R.id.real_time_scrollview)
        val apValueText = findViewById<TextView>(R.id.text_ap)
        val apMaxText = findViewById<TextView>(R.id.text_ap_max)
        val apRecoverText = findViewById<TextView>(R.id.text_ap_recover)

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, dpToPx(this, 5F).toInt(), 0, dpToPx(this, 5F).toInt())
        val linearLayout1 = LinearLayout(this)
        linearLayout1.layoutParams = layoutParams
        linearLayout1.orientation = LinearLayout.HORIZONTAL
        linearLayout1.addView(itemRecruit(playerData.recruits))
        linearLayout1.addView(itemHire(playerData.hire))

        val linearLayout2 = LinearLayout(this)
        linearLayout2.layoutParams = layoutParams
        linearLayout2.orientation = LinearLayout.HORIZONTAL
        linearLayout2.addView(itemLabor(playerData.labor))
        linearLayout2.addView(itemMeeting(playerData.meeting))

        val linearLayout3 = LinearLayout(this)
        linearLayout3.layoutParams = layoutParams
        linearLayout3.orientation = LinearLayout.HORIZONTAL
        linearLayout3.addView(itemManu(playerData.manufactures))
        linearLayout3.addView(itemTrading(playerData.tradings))

        val linearLayout4 = LinearLayout(this)
        linearLayout4.layoutParams = layoutParams
        linearLayout4.orientation = LinearLayout.HORIZONTAL
        linearLayout4.addView(itemDormitories(playerData.dormitories))
        linearLayout4.addView(itemTired(playerData.tired))

        val linearLayout5 = LinearLayout(this)
        linearLayout5.layoutParams = layoutParams
        linearLayout5.orientation = LinearLayout.HORIZONTAL
        linearLayout5.addView(itemTrain(playerData.train))
        linearLayout5.addView(itemCampaign(playerData.routine))

        val changeTextView = TextView(this)
        layoutParams.setMargins(
            dpToPx(this, 5F).toInt(),
            dpToPx(this, 5F).toInt(),
            dpToPx(this, 5F).toInt(),
            dpToPx(this, 5F).toInt()
        )
        changeTextView.layoutParams = layoutParams
        val changeText = StringBuilder()
        var flag = false
        if (playerData.train.changeTime != -1L) {
            changeText.append("艾丽妮换班时间：${TimeUtils.getTimeStr(playerData.train.changeTime * 1000)}")
                .append("\n")
            flag = true
        }
        if (playerData.train.changeTimeLogos != -1L) {
            changeText.append("逻各斯换班时间：${TimeUtils.getTimeStr(playerData.train.changeTimeLogos * 1000)}")
            flag = true
        }
        changeTextView.text = changeText.toString()


        val linearView = findViewById<LinearLayout>(R.id.real_time_layout)
        Handler(Looper.getMainLooper()).post {
            textRegister.text = "入职日   ${registerStr}"
            textLastOnline.text = "上次登录 ${lastOnlineStr}"
            apValueText.text = playerData.apInfo.current.toString()
            apMaxText.text = "/" + playerData.apInfo.max.toString()
            apRecoverText.text = playerData.apInfo.remainSecsStr
            linearView.addView(linearLayout1)
            linearView.addView(linearLayout2)
            linearView.addView(linearLayout3)
            linearView.addView(linearLayout4)
            linearView.addView(linearLayout5)
            if (flag) linearView.addView(changeTextView)
            view.visibility = View.VISIBLE
        }
    }

    private fun itemRecruit(recruits: PlayerData.Recruits): LinearLayout {
        val text1 = getString(R.string.recruit)
        val text2 = "${recruits.complete}/${recruits.max}"
        val text3 = if (recruits.remainSecs == -1L) {
            "已完成招募"
        } else {
            getRemainTimeStr(recruits.remainSecs)
        }
        val color = R.color.blue_500
        return cardView(text1, text2, text3, color)
    }

    private fun itemHire(hire: PlayerData.Hire): LinearLayout {
        val text1 = getString(R.string.hire_refresh)
        val color = R.color.blue_500
        return if (hire.isNull) {
            val text2 = "暂无数据"
            val text3 = ""
            cardView(text1, text2, text3, color)
        } else {
            val text2 = "${hire.count}/3"
            val text3 = if (hire.remainSecs == -1L) {
                "已完成刷新"
            } else {
                getRemainTimeStr(hire.remainSecs)
            }
            cardView(text1, text2, text3, color)
        }


    }

    private fun itemLabor(labor: PlayerData.Labor): LinearLayout {
        val text1 = getString(R.string.labor)
        val text2 = "${labor.current}/${labor.max}"
        val text3 = if (labor.remainSecs == -1L) {
            ""
        } else {
            getRemainTimeStr(labor.remainSecs)
        }
        val color = R.color.purple
        return cardView(text1, text2, text3, color)
    }

    private fun itemMeeting(meeting: PlayerData.Meeting): LinearLayout {
        val text1 = getString(R.string.meeting)
        val color = R.color.blue_500
        return if (meeting.isNull) {
            val text2 = "暂无数据"
            val text3 = ""
            cardView(text1, text2, text3, color)
        } else {
            val text2 = "${meeting.current}/7"
            val text3 = if (meeting.remainSecs == -1L) {
                "收集完成"
            } else {
                getRemainTimeStr(meeting.remainSecs)
            }
            cardView(text1, text2, text3, color)
        }
    }

    private fun itemManu(manufacture: PlayerData.Manufactures): LinearLayout {
        val text1 = getString(R.string.manufactures)
        val text2 = "${manufacture.current}/${manufacture.max}"
        val text3 = ""
        val color = R.color.blue_500
        return cardView(text1, text2, text3, color)
    }

    private fun itemTrading(tradings: PlayerData.Tradings): LinearLayout {
        val text1 = getString(R.string.trading)
        val text2 = "${tradings.current}/${tradings.max}"
        val text3 = ""
        val color = R.color.blue_500
        return cardView(text1, text2, text3, color)
    }

    private fun itemDormitories(dormitories: PlayerData.Dormitories): LinearLayout {
        val text1 = getString(R.string.dormitories)
        val text2 = "${dormitories.current}/${dormitories.max}"
        val text3 = ""
        val color = R.color.blue_500
        return cardView(text1, text2, text3, color)
    }

    private fun itemTired(tired: PlayerData.Tired): LinearLayout {
        val text1 = getString(R.string.tired)
        val text2 = "${tired.current}"
        val text3 = ""
        val color = R.color.red
        return cardView(text1, text2, text3, color)
    }

    private fun itemTrain(train: PlayerData.Train): LinearLayout {
        val text1 = getString(R.string.train)
        val color = R.color.blue_500
        return if (train.isNull) {
            val text2 = "暂无数据"
            val text3 = ""
            cardView(text1, text2, text3, color)
        } else {
            val text2 = if (train.traineeIsNull) {
                "空闲中"
            } else {
                train.trainee
            }
            val text3 = when (train.remainSecs) {
                -1L -> "空闲中"
                0L -> "专精完成"
                else -> getRemainTimeStr(train.remainSecs)
            }
            cardView(text1, text2, text3, color)
        }
    }

    private fun itemCampaign(routine: PlayerData.Routine): LinearLayout {
        val text1 = getString(R.string.campaign)
        val text2 = "${routine.campaignCurrent}/${routine.campaignTotal}"
        val text3 = ""
        val color = R.color.red
        return cardView(text1, text2, text3, color)
    }


    private fun cardView(text1: String, text2: String, text3: String, color: Int): LinearLayout {
        val layoutParamsText2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        val layoutParamsText3 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        val textView1 = TextView(this)
        val textView2 = TextView(this)
        val textView3 = TextView(this)
        textView1.typeface = Typeface.DEFAULT_BOLD
        textView2.typeface = Typeface.DEFAULT_BOLD
        textView3.typeface = Typeface.DEFAULT_BOLD
        textView1.text = text1
        textView2.text = text2
        textView3.text = text3

        textView1.layoutParams = layoutParamsText3
        textView2.layoutParams = layoutParamsText2
        textView3.layoutParams = layoutParamsText2

        textView2.gravity = Gravity.END
        textView2.setTextColor(getColor(color))

        val linearLayout1 = LinearLayout(this)
        val layoutParamsLinear1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout1.orientation = LinearLayout.HORIZONTAL
        linearLayout1.layoutParams = layoutParamsLinear1
        linearLayout1.addView(textView1)
        linearLayout1.addView(textView2)
        val linearLayout2 = LinearLayout(this)
        val layoutParamsLinear2 =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        linearLayout2.orientation = LinearLayout.VERTICAL
        layoutParamsLinear2.setMargins(dpToPx(this, 15F).toInt())
        linearLayout2.layoutParams = layoutParamsLinear2
        linearLayout2.addView(linearLayout1)
        linearLayout2.addView(textView3)
        return linearLayout2
    }

    private fun getPlayerData(account: AccountSk): PlayerData {
        val cn = getGameInfoInputConnection(account) ?: throw Exception("连接失败")
        val inputStream = cn.inputStream
        //writeToLocal("${APP.externalCacheDir}/data.zip", inputStream)
        val gzip = GZIPInputStream(inputStream)
        val om = ObjectMapper()
        val result = com.blueskybone.arkscreen.util.getPlayerData(om.readTree(gzip))
        gzip.close()
        inputStream.close()
        cn.disconnect()
        return result
    }
}
