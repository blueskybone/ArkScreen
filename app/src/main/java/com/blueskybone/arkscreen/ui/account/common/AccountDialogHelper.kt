package com.blueskybone.arkscreen.ui.account.common

import android.content.Context
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Created by blueskybone
 * Date: 2026/4/5
 */
class AccountDialogHelper(
    private val context: Context,
    private val inflater: LayoutInflater
) {

    fun showInfoDialog(titleRes: Int, messageRes: Int) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun showDeleteConfirm(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun showExportDialog(content: String, onCopy: () -> Unit) {
        val binding = DialogInputBinding.inflate(inflater).apply {
            EditText2.visibility = View.GONE
            EditText1.setText(content)
            EditText1.setSelection(content.length)
        }

        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setTitle(R.string.export_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.copy) { _, _ -> onCopy() }
            .show()
    }

    fun showCookieLoginDialog(onConfirm: (String) -> Unit) {
        val binding = DialogInputBinding.inflate(inflater).apply {
            EditText2.visibility = View.GONE
            EditText1.hint = context.getString(R.string.import_cookie)
        }

        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setTitle(R.string.import_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.import_cookie) { _, _ ->
                onConfirm(binding.EditText1.text.toString())
            }
            .show()
    }

    fun showPasswordLoginDialog(onConfirm: (phone: String, password: String) -> Unit) {
        val binding = DialogInputBinding.inflate(inflater).apply {
            EditText1.hint = context.getString(R.string.phone_number)
            EditText2.visibility = View.VISIBLE
            EditText2.hint = context.getString(R.string.password)
            EditText2.inputType =
                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            EditText2.keyListener = DigitsKeyListener.getInstance(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;:,.<>?/"
            )
        }

        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setTitle(R.string.password_login)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.login) { _, _ ->
                onConfirm(
                    binding.EditText1.text.toString().trim(),
                    binding.EditText2.text.toString()
                )
            }
            .show()
    }

    fun buildAccountMenu(
        account: Account,
        onCopyUid: () -> Unit,
        onCopyNickname: () -> Unit,
        onExportCookie: (() -> Unit)?,
        onDelete: () -> Unit
    ): MenuDialog {
        return MenuDialog(context)
            .add("复制UID") { onCopyUid() }
            .add("复制昵称") { onCopyNickname() }
            .apply {
                onExportCookie?.let {
                    add(context.getString(R.string.export_cookie)) { it() }
                }
            }
            .add(R.string.delete) { onDelete() }
    }
}