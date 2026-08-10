package com.blueskybone.arkscreen.ui.realtime

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.blueskybone.arkscreen.util.launchApp
import com.blueskybone.arkscreen.databinding.ActivityRealTimeBinding
import com.blueskybone.arkscreen.databinding.InfoCardBinding
import com.blueskybone.arkscreen.ui.UiState
import com.blueskybone.arkscreen.ui.common.bindinginfo.Campaign
import com.blueskybone.arkscreen.ui.common.bindinginfo.DataInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.Dormitories
import com.blueskybone.arkscreen.ui.common.bindinginfo.Labor
import com.blueskybone.arkscreen.ui.common.bindinginfo.Manufactures
import com.blueskybone.arkscreen.ui.common.bindinginfo.Meeting
import com.blueskybone.arkscreen.ui.common.bindinginfo.Recruit
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitRefresh
import com.blueskybone.arkscreen.ui.common.bindinginfo.Tired
import com.blueskybone.arkscreen.ui.common.bindinginfo.Trading
import com.blueskybone.arkscreen.ui.common.bindinginfo.Train
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeUi
import com.hjq.toast.Toaster

/**
 *   Created by blueskybone
 *   Date: 2025/1/5
 */

class RealTimeActivity : AppCompatActivity() {

    private var _binding: ActivityRealTimeBinding? = null
    private val binding get() = _binding!!

    private val model: RealTimeModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRealTimeBinding.inflate(layoutInflater)
        setUpBinding()
        setupObserver()
        setContentView(binding.root)
    }

    private fun setUpBinding() {
        binding.Recruit.setUp(Recruit)
        binding.RecruitReFresh.setUp(RecruitRefresh)
        binding.Labor.setUp(Labor)
        binding.Meeting.setUp(Meeting)
        binding.Manufactures.setUp(Manufactures)
        binding.Trading.setUp(Trading)
        binding.Dormitories.setUp(Dormitories)
        binding.Tired.setUp(Tired)
        binding.Train.setUp(Train)
        binding.Campaign.setUp(Campaign)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun InfoCardBinding.setUp(dataInfo: DataInfo) {
        Title.text = getString(dataInfo.title)
        Value.setTextColor(getColor(dataInfo.color))
    }

    private fun InfoCardBinding.setUp(pairInfo: RealTimeUi.PairInfo) {
        Time.text = pairInfo.time
        Value.text = pairInfo.value
        Notify.visibility = if(pairInfo.notify) View.VISIBLE else View.GONE
    }

    private fun setupObserver() {
        model.uiState.observe(this) { value ->
            when (value) {
                is UiState.Loading -> displayLoadingView()
                is UiState.Error -> displayErrorView(value.message)
                is UiState.Success -> displayView()
                is UiState.Warning -> displayWarningView(value.message)
                else -> {}
            }
        }
    }

    private fun displayLoadingView() {
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.Message.text = "加载中..."
    }

    private fun displayErrorView(msg: String) {
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayWarningView(msg: String) {
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.Message.text = msg
    }

    @SuppressLint("SetTextI18n")
    private fun displayView() {
        val realtimeUi = model.realTimeUi
        val data = realtimeUi.value!!
        binding.Ap.text = data.apNow
        binding.ApMax.text = data.apMax
        binding.CircularProgressBar.apply {
            progress = data.apNow.toFloat()
            progressMax = data.apMax.substring(1).toFloat()
        }
        binding.Avatar.load(data.avatarUrl)
        binding.Level.text = data.level
        binding.ApResTime.text = data.apResTime
        binding.NickName.text = data.nickName
        binding.LastLogin.text = data.lastLogin
        binding.Recruit.setUp(data.recruit)
        binding.RecruitReFresh.setUp(data.recruitRefresh)
        binding.Labor.setUp(data.labor)
        binding.Meeting.setUp(data.meeting)
        binding.Manufactures.setUp(data.manufacture)
        binding.Trading.setUp(data.trading)
        binding.Dormitories.setUp(data.dormitories)
        binding.Tired.setUp(data.tired)
        binding.Train.setUp(data.train)
        binding.Campaign.setUp(data.campaign)

        if (data.displayChange) {
            binding.TrainChange.visibility = View.VISIBLE
        }
        if (data.logosChange.display) {
            binding.Logos.Layout.visibility = View.VISIBLE
            binding.Logos.Text.text = data.logosChange.text
        }
        if (data.ireneChange.display) {
            binding.Irene.Layout.visibility = View.VISIBLE
            binding.Irene.Text.text = data.ireneChange.text
        }
        binding.Starter.setOnClickListener {
            if (data.official)
                this.launchApp("com.hypergryph.arknights") { Toaster.show("未检测到官服游戏安装") }
            else
                this.launchApp("com.hypergryph.arknights.bilibili") { Toaster.show("未检测到b服游戏安装") }
        }
        binding.Page.visibility = View.GONE
        binding.ScrollView.visibility = View.VISIBLE

        //发送广播强制更新桌面组件
//        val intent = Intent(APP, WidgetReceiver::class.java)
//        intent.action = WidgetReceiver.MANUAL_UPDATE
//        APP.sendBroadcast(intent)
    }
}