package com.blueskybone.arkscreen.ui.main.common

import android.view.View
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.ui.account.adapter.AccountAdapter
import com.blueskybone.arkscreen.ui.account.common.AccountPickerPopup
import com.blueskybone.arkscreen.ui.common.view.configureLinkInput
import com.blueskybone.arkscreen.ui.common.view.configurePasswordLogin
import com.blueskybone.arkscreen.ui.common.view.configureSingleInput
import com.blueskybone.arkscreen.ui.main.MainModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.blueskybone.arkscreen.ui.common.showDestructiveConfirmation
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

    private var accountPopup: AccountPickerPopup? = null


    fun launchWebLogin() {
        onLaunchWebLogin()
    }
    fun showCookieLoginDialog() {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.configureSingleInput(
            fragment.getString(R.string.import_cookie),
            sensitive = true,
        )

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.import_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.import_cookie) { _, _ ->
                val token = dialogBinding.EditText1.text.toString().trim()
                if (token.isNotEmpty()) {
                    Toaster.show(fragment.getString(R.string.getting_info))
                    viewModel.loginSkland(
                        SyncAccountSkUseCase.LoginWay.Cookie(token)
                    )
                } else {
                    Toaster.show(fragment.getString(R.string.wrong_format))
                }
            }
            .show()
    }

    fun showPasswordLoginDialog() {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.configurePasswordLogin(
            phoneHint = fragment.getString(R.string.phone_number),
            passwordHint = fragment.getString(R.string.password),
        )

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.password_login)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.login) { _, _ ->
                val phone = dialogBinding.EditText1.text.toString()
                val password = dialogBinding.EditText2.text.toString()

                if (phone.isEmpty() || password.isEmpty()) {
                    Toaster.show(fragment.getString(R.string.enter_phone_and_password))
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
        dialogBinding.configureLinkInput(
            titleHint = fragment.getString(R.string.title),
            urlHint = fragment.getString(R.string.site_url),
        )
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
        fragment.requireContext().showDestructiveConfirmation(
            titleRes = R.string.delete,
            message = fragment.getString(R.string.confirm_delete_link_detail),
        ) {
            viewModel.deleteLink(link)
        }
    }

    fun showAddLinkDialog() {
        val dialogBinding = DialogInputBinding.inflate(fragment.layoutInflater)
        dialogBinding.configureLinkInput(
            titleHint = fragment.getString(R.string.title),
            urlHint = fragment.getString(R.string.site_url),
        )
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
        accountPopup = AccountPickerPopup(fragment.requireActivity(), accountAdapter).also {
            it.show(anchor)
        }
    }


    fun release() {
        accountPopup?.dismiss()
        accountPopup = null
    }

    fun dismissPopup() {
       accountPopup?.dismiss()
    }
}
