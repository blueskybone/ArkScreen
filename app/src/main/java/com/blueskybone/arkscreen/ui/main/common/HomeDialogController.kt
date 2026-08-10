package com.blueskybone.arkscreen.ui.main.common

import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.databinding.PopupAccountBinding
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.ui.account.adapter.AccountAdapter
import com.blueskybone.arkscreen.ui.main.MainModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster

/**
 * Created by blueskybone
 * Date: 2026/4/2
 */
class HomeDialogController(
    private val fragment: Fragment,
    private val viewModel: MainModel,
    private val accountAdapter: AccountAdapter,
    private val onLaunchWebLogin: () -> Unit
) {

    private var accountPopup: PopupWindow? = null


    fun launchWebLogin(){
        onLaunchWebLogin
    }
    fun showCookieLoginDialog() {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.EditText2.visibility = View.GONE
        dialogBinding.EditText1.hint = fragment.getString(R.string.import_cookie)

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.import_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.import_cookie) { _, _ ->
                val token = dialogBinding.EditText1.text.toString().trim()
                if (token.isNotEmpty()) {
                    Toaster.show(fragment.getString(R.string.getting_info))
                    viewModel.loginSkland(
                        SyncAccountSkUseCase.LoginWay.Token(token)
                    )
                } else {
                    Toaster.show(fragment.getString(R.string.wrong_format))
                }
            }
            .show()
    }

    fun showPasswordLoginDialog() {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.EditText1.hint = fragment.getString(R.string.phone_number)
        dialogBinding.EditText2.visibility = View.VISIBLE
        dialogBinding.EditText2.hint = fragment.getString(R.string.password)
        dialogBinding.EditText2.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.password_login)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.login) { _, _ ->
                val phone = dialogBinding.EditText1.text.toString()
                val password = dialogBinding.EditText2.text.toString()

                if (phone.isEmpty() || password.isEmpty()) {
                    Toaster.show("请输入手机号和密码")
                    return@setPositiveButton
                }

                Toaster.show(fragment.getString(R.string.logging_in))
                viewModel.loginSkland(
                    SyncAccountSkUseCase.LoginWay.PhoneAndPassword(phone, password)
                )
            }
            .show()
    }

    fun showEditLinkDialog(link: Link) {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.EditText1.setText(link.title)
        dialogBinding.EditText2.setText(link.url)

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.edit_site)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.EditText1.text.toString().trim()
                val url = dialogBinding.EditText2.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    viewModel.updateLink(link, title, url)
                }
            }
            .show()
    }

    fun showDeleteConfirm(link: Link) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteLink(link)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onAddButtonClick() {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.EditText2.setText(R.string.prefix)

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.add_site)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.EditText1.text.toString().trim()
                val url = dialogBinding.EditText2.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    viewModel.addLink(title, url)
                }
            }
            .show()
    }

    fun showAddLinkDialog() {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.EditText2.setText(R.string.prefix)
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.add_site)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.EditText1.text.toString().trim()
                val url = dialogBinding.EditText2.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    viewModel.addLink(title = title, url = url)
                }
            }.show()
    }

    fun showAccountPopup(anchor: View) {
        if (accountPopup?.isShowing == true) return

        val popupBinding =
            PopupAccountBinding.inflate(LayoutInflater.from(fragment.requireContext()))
        popupBinding.lvAccount.adapter = accountAdapter

        accountPopup = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(
                ContextCompat.getDrawable(fragment.requireContext(), android.R.color.transparent)
            )
            isOutsideTouchable = true
            animationStyle = R.style.PopupDownAnim
        }

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        accountPopup?.showAtLocation(
            anchor,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] + anchor.height
        )
    }


    fun release(){

    }

    fun dismissPopup() {
       accountPopup?.dismiss()
    }
}