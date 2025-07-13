package com.blueskybone.arkscreen.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.databinding.ActivityRealTimeBinding
import com.blueskybone.arkscreen.databinding.InfoCardBinding
import com.blueskybone.arkscreen.playerinfo.RealTimeUi
import com.blueskybone.arkscreen.ui.bindinginfo.Campaign
import com.blueskybone.arkscreen.ui.bindinginfo.DataInfo
import com.blueskybone.arkscreen.ui.bindinginfo.Dormitories
import com.blueskybone.arkscreen.ui.bindinginfo.Labor
import com.blueskybone.arkscreen.ui.bindinginfo.Manufactures
import com.blueskybone.arkscreen.ui.bindinginfo.Meeting
import com.blueskybone.arkscreen.ui.bindinginfo.Recruit
import com.blueskybone.arkscreen.ui.bindinginfo.RecruitRefresh
import com.blueskybone.arkscreen.ui.bindinginfo.Tired
import com.blueskybone.arkscreen.ui.bindinginfo.Trading
import com.blueskybone.arkscreen.ui.bindinginfo.Train
import com.blueskybone.arkscreen.viewmodel.RealTimeModel
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
    }


    private fun setupObserver() {
        model.uiState.observe(this) { value ->
            when (value) {
                is DataUiState.Loading -> displayLoadingView(value.msg)
                is DataUiState.Error -> displayErrorView(value.msg)
                is DataUiState.Success -> displayView()
                else -> {}
            }
        }
    }

    private fun displayLoadingView(msg: String) {
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.Message.text = msg
    }

    private fun displayErrorView(msg: String) {
        binding.Page.visibility = View.VISIBLE
        binding.ScrollView.visibility = View.GONE
        binding.Message.text = msg
    }

    @SuppressLint("SetTextI18n")
    private fun displayView() {
        val data = model.realTimeUi
        if (data == null) {
            Toast.makeText(this, "LOADING FAILED: data null", Toast.LENGTH_LONG).show()
            return
        }
        binding.Ap.text = data.apNow
        binding.ApMax.text = data.apMax
        binding.Avatar.load(data.avatarUrl)
        binding.Level.text = data.level.toString()
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
                openAnotherApp("com.hypergryph.arknights")
            else
                openAnotherApp("com.hypergryph.arknights.bilibili")
        }
        binding.Page.visibility = View.GONE
        binding.ScrollView.visibility = View.VISIBLE

//        val intent = Intent(APP, WidgetReceiver::class.java)
//        intent.action = WidgetReceiver.MANUAL_UPDATE
//        APP.sendBroadcast(intent)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openAnotherApp(packageName: String) {
        val packageManager = packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toaster.show("未检测到游戏安装")
        }
    }
}