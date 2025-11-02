package com.blueskybone.arkscreen.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.ActivityAccountMngBinding
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.room.Account
import com.blueskybone.arkscreen.ui.recyclerview.AccountAdapter
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.util.copyToClipboard
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/11/1
 */

class AccountMngActivity : AppCompatActivity() {
    private val model: BaseModel by viewModels()
    lateinit var binding: ActivityAccountMngBinding

    private var adapter: AccountAdapter? = null
    private var adapterGc: AccountAdapter? = null
    private lateinit var activityResultLauncherSk: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherGc: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountMngBinding.inflate(layoutInflater)
        initialize()

        setContentView(binding.root)
        setUpBinding()
    }

    private fun initialize() {
        adapter = AccountAdapter(this, adapterSkListener)
        binding.RecyclerView.adapter = adapter
        model.accountSkList.observe(this) { value ->
            adapter?.submitList(value as List<Account>?)
        }

        adapterGc = AccountAdapter(this, adapterGcListener)
        binding.RecyclerViewGc.adapter = adapterGc
        model.accountGcList.observe(this) { value ->
            adapterGc?.submitList(value as List<Account>?)
        }

        activityResultLauncherSk = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // 处理返回结果
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // 解析返回的数据
                val token = data?.getStringExtra("token")
                val dId = data?.getStringExtra("dId")
                if (token != null && dId != null) {
                    Toaster.show(getString(R.string.getting_info))
                    try {
                        model.accountSkLogin(token, dId)
                    }catch (e:Exception){
                        Toaster.show("登录出现意外：${e.message}")
                    }
                } else {
                    Toaster.show("failed：获取token失败")
                }
            }
        }

        activityResultLauncherGc = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // 处理返回结果
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // 解析返回的数据
                val token = data?.getStringExtra("token") ?: "null"
                val xrToken = data?.getStringExtra("xrToken")
                val userCenter = data?.getStringExtra("userCenter")
                val channelMasterId = data?.getIntExtra("channelMasterId", 1)
                if (xrToken != null && userCenter != null) {
                    Toaster.show(getString(R.string.getting_info))
                    try {
                        model.accountGcLogin(token, channelMasterId!!, userCenter, xrToken)
                    }catch (e:Exception){
                        Toaster.show("登录出现意外：${e.message}")
                    }
                } else {
                    Toaster.show("failed：获取token失败")
                }
            }
        }
    }

    private fun setUpBinding() {

        binding.AddAccountSk.setOnClickListener {
            MenuDialog(this)
                .add(getString(R.string.import_cookie)) {
                    displayLoginDialog(1)
                }
                .add(R.string.web_login) {
                    val intent =
                        LoginWeb.startIntent(this, LoginWeb.Companion.LoginType.SKLAND)
                    activityResultLauncherSk.launch(intent)
                }
                .show()
        }

        binding.AddAccountGc.setOnClickListener {
            MenuDialog(this)
                .add(getString(R.string.import_cookie)) {
                    displayLoginDialog(2)
                }
                .add(R.string.web_login_official) {
                    val intent =
                        LoginWeb.startIntent(
                            this,
                            LoginWeb.Companion.LoginType.GACHA_OFFICIAL
                        )
                    activityResultLauncherGc.launch(intent)
                }.add(R.string.web_login_bili) {
                    val intent =
                        LoginWeb.startIntent(
                            this,
                            LoginWeb.Companion.LoginType.GACHA_BILI
                        )
                    activityResultLauncherGc.launch(intent)
                }
                .show()
        }

        binding.GcInfo.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.account_info)
                .setMessage(R.string.gc_account_info)
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.SkInfo.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.account_info)
                .setMessage(R.string.sk_account_info)
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun displayLoginDialog(type: Int) {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText2.visibility = View.GONE
        dialogBinding.EditText1.hint = getString(R.string.import_cookie)
        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle(R.string.import_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.import_cookie) { _, _ ->
                val str = dialogBinding.EditText1.text.toString()
                val list = str.split("@")
                if (type == 1) {
                    if (list.size == 2) {
                        try {
                            Toaster.show(getString(R.string.getting_info))
                            model.accountSkLogin(list[0], list[1])
                        } catch (e: Exception) {
                            Toaster.show("cookie登录失败：${e.message}")
                            Timber.e("cookie登录失败：${e.message}")
                        }
                    } else {
                        Toaster.show(getString(R.string.wrong_format))
                    }
                } else if (type == 2) {
                    if (list.size == 3) {
                        try {
                            Toaster.show(getString(R.string.getting_info))
                            model.accountGcLogin(list[0], 1, list[1], list[2])
                        } catch (e: Exception) {
                            Toaster.show("cookie登录失败：${e.message}")
                            Timber.e("cookie登录失败：${e.message}")
                        }
                    } else {
                        Toaster.show(getString(R.string.wrong_format) + list.size)
                    }
                }

            }.show()
    }

    private val adapterSkListener = object : ItemListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(position: Int) {
            adapter?.currentList?.get(position)?.let { value ->
                model.setDefaultAccountSk(value as com.blueskybone.arkscreen.room.AccountSk)
                Toaster.show(getString(R.string.set_default_account, value.nickName))
                adapter?.notifyDataSetChanged()
            }
        }

        override fun onLongClick(position: Int) {
            adapter?.currentList?.get(position)?.let { value ->
                MenuDialog(this@AccountMngActivity)
                    .add(getString(R.string.export_cookie)) {
                        displayExportDialog("${value.token}@${(value as com.blueskybone.arkscreen.room.AccountSk).dId}")
                    }
                    .add(R.string.delete) { confirmDeletion(value) }
                    .show()
            }
        }
    }

    private val adapterGcListener = object : ItemListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(position: Int) {
            adapterGc?.currentList?.get(position)?.let { value ->
                model.setDefaultAccountGc(value as com.blueskybone.arkscreen.room.AccountGc)
                Toaster.show(getString(R.string.set_default_account, value.nickName))
                adapterGc?.notifyDataSetChanged()
            }
        }

        override fun onLongClick(position: Int) {
            adapterGc?.currentList?.get(position)?.let { value ->
                MenuDialog(this@AccountMngActivity)
                    .add(getString(R.string.export_cookie)) {
                        val account = value as com.blueskybone.arkscreen.room.AccountGc
                        displayExportDialog("${account.token}@${account.akUserCenter}@${account.xrToken}")
                    }
                    .add(R.string.delete) { confirmDeletion(value) }
                    .show()
            }
        }
    }

    private fun displayExportDialog(key: String) {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText2.visibility = View.GONE
        dialogBinding.EditText1.setText(key)
        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle(R.string.export_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.copy) { _, _ ->
                copyToClipboard(this, key)
            }.show()
    }

    private fun confirmDeletion(value: Account) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteAccount(value) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
