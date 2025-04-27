package com.blueskybone.arkscreen.activity

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.recyclerview.AccountAdapter
import com.blueskybone.arkscreen.recyclerview.ItemListener
import com.blueskybone.arkscreen.room.Account
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.Type
import com.blueskybone.arkscreen.util.copyToClipboard
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */

class AccountGc : ListActivity(Type.ACCOUNT_GC), ItemListener {

    private val model: BaseModel by viewModels()
    private var adapter: AccountAdapter? = null
    override fun setUpRecycler() {
        adapter = AccountAdapter(this, this)
        binding.RecyclerView.adapter = adapter
        model.accountGcList.observe(this) { value ->
            adapter?.submitList(value as List<Account>?)
        }
    }

    override fun onAddButtonClick() {

        MenuDialog(this)
            .add(getString(R.string.import_cookie)) {
                displayLoginDialog()
            }
            .add(R.string.web_login_official) {
                val intent = LoginWeb.startIntent(this, LoginWeb.Companion.LoginType.GACHA_OFFICIAL)
                activityResultLauncher?.launch(intent)
            }
            .add(R.string.web_login_bili) {
                val intent = LoginWeb.startIntent(this, LoginWeb.Companion.LoginType.GACHA_BILI)
                activityResultLauncher?.launch(intent)
            }
            .show()
    }

    private fun displayLoginDialog() {
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
                if (list.size == 2) {
                    try {
                        Toaster.show(getString(R.string.getting_info))
                        model.accountGcLogin(list[0], list[1].toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toaster.show(getString(R.string.wrong_format))
                }
            }.show()
    }



    override fun onClick(position: Int) {
        adapter?.currentList?.get(position)?.let { value ->
            model.setDefaultAccountGc(value as AccountGc)
            Toaster.show(getString(R.string.set_default_account, value.nickName))
        }
    }

    override fun onLongClick(position: Int) {
        adapter?.currentList?.get(position)?.let { value ->
            MenuDialog(this)
                .add(getString(R.string.export_cookie)) {
                    displayExportDialog("${value.token}@${(value as AccountGc).channelMasterId}")
                }
                .add(R.string.delete) { confirmDeletion(value as AccountGc) }
                .show()
        }
    }

    private fun confirmDeletion(value: AccountGc) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteAccountGc(value) }
            .setNegativeButton(R.string.cancel, null)
            .show()
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

    override fun onActivityResult(data: Intent?) {
        val token = data?.getStringExtra("token")
        val channelMasterId = data?.getIntExtra("channelMasterId", 1)
        if (token != null && channelMasterId != null) {
            Toaster.show(getString(R.string.getting_info))
            model.accountGcLogin(token, channelMasterId)
        } else {
            Toaster.show("null")
        }
    }


}