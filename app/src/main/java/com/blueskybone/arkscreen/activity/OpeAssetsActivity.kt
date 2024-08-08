package com.blueskybone.arkscreen.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.view.AccountAdapter
import com.blueskybone.arkscreen.activity.view.OperatorAdapter
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.data.AccountSk
import com.blueskybone.arkscreen.base.data.Operator
import com.blueskybone.arkscreen.base.data.PlayerData
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.util.LoadingDialog
import com.blueskybone.arkscreen.util.getOpeData
import com.blueskybone.arkscreen.util.getPlayerData
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import io.noties.markwon.Markwon
import org.koin.android.ext.android.getKoin
import org.koin.java.KoinJavaComponent
import java.lang.StringBuilder
import java.util.zip.GZIPInputStream

/**
 *   Created by blueskybone
 *   Date: 2024/8/6
 */
class OpeAssetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        setContentView(R.layout.activity_ope_assets)
        title = getString(R.string.operator_assets)

        loadingGameData()
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadingGameData() {
        //TODO: 黑幕加载动画
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        val thread = Thread {
            try {
                val prefManager: PrefManager by getKoin().inject()
                val baseAccount = prefManager.BaseAccount.get() ?: throw Exception("未登录")
                if (baseAccount.isExpired) {
                    throw Exception("登录过期")
                }
                val opeData = getOpeData(baseAccount)
                val opeInfo = statisticOpeInfo(opeData)
                showInfo(opeInfo)
                showResult(opeData)
                loadingDialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
                loadingDialog.dismiss()
                Toaster.show(e.message)
            }
        }
        thread.start()
    }

    private fun showInfo(opeInfo: OperatorAssetsInfo) {
        val opeInfoText = findViewById<TextView>(R.id.text_operator_info)
        val strBuilder = StringBuilder()
        strBuilder.append("干员总数\t\t").append(opeInfo.count).append("\n\n")
            .append("精二干员数\t\t").append(opeInfo.evolve2Count).append("\n\n")
            .append("专精三技能数\t\t").append(opeInfo.specializeCount).append("\n\n")
            .append("stage3模组数\t\t").append(opeInfo.equipStage3Count).append("\n\n")
        Handler(Looper.getMainLooper()).post {
            Markwon.create(this).setMarkdown(opeInfoText, strBuilder.toString())
        }
    }

    data class OperatorAssetsInfo(
        var count: Int = 0,
        var evolve2Count: Int = 0,
        var specializeCount: Int = 0,
        var equipStage3Count: Int = 0
    )

    private fun statisticOpeInfo(opeData: ArrayList<Operator>): OperatorAssetsInfo {
        val info = OperatorAssetsInfo()
        for (operator in opeData) {
            info.count++
            if (operator.evolvePhase == 2) info.evolve2Count++
            for (skill in operator.skills) {
                if (skill.specializeLevel == 3) info.specializeCount++
            }
            for (equip in operator.equips) {
                if (equip.level == 3) info.equipStage3Count++
            }
        }
        return info
    }

    private fun getOpeData(account: AccountSk): ArrayList<Operator> {
        val cn = NetWorkTask.getGameInfoInputConnection(account)
        val inputStream = cn.inputStream
        val gzip = GZIPInputStream(inputStream)
        val om = ObjectMapper()
        val result = getOpeData(om.readTree(gzip))
        gzip.close()
        inputStream.close()
        cn.disconnect()
        return result
    }

    private fun showResult(opeList: ArrayList<Operator>) {
        val opeRecycleView = findViewById<RecyclerView>(R.id.recycler_ope_assets)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = OperatorAdapter(opeList, this)
        Handler(Looper.getMainLooper()).post {
            opeRecycleView.layoutManager = linearLayoutManager
            opeRecycleView.adapter = adapter
        }
    }
}