package com.blueskybone.arkscreen.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.AccountGc
import com.blueskybone.arkscreen.activity.AccountSk
import com.blueskybone.arkscreen.activity.CharAssets
import com.blueskybone.arkscreen.activity.GachaActivity
import com.blueskybone.arkscreen.activity.LinkMng
import com.blueskybone.arkscreen.activity.RecruitActivity
import com.blueskybone.arkscreen.databinding.DialogInfoBinding
import com.blueskybone.arkscreen.databinding.FragmentFunctionBinding
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
class Function : Fragment() {
    private val model: BaseModel by activityViewModels()
    private var _binding: FragmentFunctionBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFunctionBinding.inflate(inflater)
        setUpBinding()
        return binding.root
    }

    private fun setUpBinding() {
        binding.SklandAccountManage.Title.text = getString(R.string.skland_account_manage)
        binding.GachaAccountManage.Title.text = getString(R.string.gacha_account_manage)
        binding.OperatorAssets.Title.text = getString(R.string.operator_assets)
//        binding.GameInfo.Title.text = getString(R.string.game_info)
        binding.GachaStatistics.Title.text = getString(R.string.gacha_statistics)
        binding.Link3rdManage.Title.text = getString(R.string.home_3rd_link_manage)
        binding.AttendanceClick.Title.text = getString(R.string.attendance_click)
        binding.RecruitCalculator.Title.text = getString(R.string.recruit_cal)
        binding.TimeCorrectButton.Title.text = getString(R.string.time_correction)
//        binding.DebugLog.Title.text = getString(R.string.debug_log)

        binding.SklandAccountManage.Card.setOnClickListener {
            startActivity(Intent(requireContext(), AccountSk::class.java))
        }
        binding.GachaAccountManage.Card.setOnClickListener {
            startActivity(Intent(requireContext(), AccountGc::class.java))
        }
        binding.OperatorAssets.Card.setOnClickListener {
            startActivity(Intent(requireContext(), CharAssets::class.java))
        }
        binding.RecruitCalculator.Card.setOnClickListener {
            startActivity(Intent(requireContext(), RecruitActivity::class.java))
        }
        binding.Link3rdManage.Card.setOnClickListener {
            startActivity(Intent(requireContext(), LinkMng::class.java))
        }
        binding.AttendanceClick.Card.setOnClickListener {
            displayDialog()
        }
        binding.GachaStatistics.Card.setOnClickListener {
            startActivity(Intent(requireContext(), GachaActivity::class.java))

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun displayDialog() {

        val dialogBinding = DialogInfoBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.attendance_click)
            .setPositiveButton(R.string.confirm, null).show()

        model.testTextRun()
        model.testText.observe(viewLifecycleOwner) {
            dialogBinding.Text.text = it
        }
    }
}