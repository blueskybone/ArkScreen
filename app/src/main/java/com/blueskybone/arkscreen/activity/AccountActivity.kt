package com.blueskybone.arkscreen.activity


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.icu.number.IntegerWidth
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.view.AccountAdapter
import com.blueskybone.arkscreen.base.PrefManager
import com.blueskybone.arkscreen.base.data.AccountSk
import com.blueskybone.arkscreen.network.NetWorkTask.createAccountList
import com.blueskybone.arkscreen.util.LoadingDialog
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import io.noties.markwon.Markwon
import org.koin.android.ext.android.getKoin
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent


/**
 *   Created by blueskybone
 *   Date: 2024/7/29
 */
class AccountActivity : AppCompatActivity() {
    private var accountList: ArrayList<AccountSk> = ArrayList()
    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
        loadingDialog = LoadingDialog(this)
        loadAccountList()
    }

    private fun setLayout() {
        setContentView(R.layout.activity_account)
        title = getString(R.string.account_manage)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.account_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.menu_add_account) {
            if (accountList.size >= 5) {
                Toaster.show("账号数超过上限")
            } else {
                showLoginDialog(this)
            }
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun updateAccountView() {
        val accountRecycleView = findViewById<RecyclerView>(R.id.recycler_account)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = AccountAdapter(accountList)
        adapter.setOnDataRemoved { list ->
            refreshAccountList(list)
        }
        adapter.setOnItemClicked { pos ->
            val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
            val account = accountList[pos]
            prefManager.BaseAccount.set(account)
            Toaster.show("默认账号设置为 " + account.nickName)
        }
        Handler(Looper.getMainLooper()).post {
            accountRecycleView.layoutManager = linearLayoutManager
            accountRecycleView.adapter = adapter
        }
    }

    private fun setAccountView() {
        val accountRecycleView = findViewById<RecyclerView>(R.id.recycler_account)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = AccountAdapter(accountList)
        adapter.setOnDataRemoved { list ->
            refreshAccountList(list)
        }
        adapter.setOnItemClicked { pos ->
            val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
            val account = accountList[pos]
            prefManager.BaseAccount.set(account)
            Toaster.show("默认账号设置为 " + account.nickName)
        }
        Handler(Looper.getMainLooper()).post {
            accountRecycleView.layoutManager = linearLayoutManager
            accountRecycleView.adapter = adapter
        }
    }

    private fun loadAccountList() {
        val thread = Thread {
            val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()
            accountList = prefManager.ListAccountSk.get()
            setAccountView()
        }
        thread.start()
    }

    @SuppressLint("ResourceType")
    private fun showLoginDialog(context: Context) {
        val thread = Thread {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_login, null)
            val textView = view.findViewById<TextView>(R.id.textView_dialog_login_text)
            val editText = view.findViewById<EditText>(R.id.editText_token)
            Markwon.create(context).setMarkdown(textView, context.getString(R.string.login_content))
            builder.setView(view).setTitle(context.getString(R.string.add_account))
                .setPositiveButton(R.string.confirm) { _, _ ->
                    doLogin(editText.text.toString())
                }.setNegativeButton(R.string.paste) { _, _ -> doLoginFromPaste(context) }

            Handler(Looper.getMainLooper()).post {
                builder.create().show()
            }
        }
        thread.start()
    }

    private fun doLoginFromPaste(context: Context) {
        val clipboard: ClipboardManager =
            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData: ClipData? = clipboard.primaryClip
        if ((clipData != null) && (clipData.itemCount > 0)) {
            doLogin(clipData.getItemAt(0).text.toString())
        }
    }

    private fun doLogin(content: String?) {
        if (content == "") {
            return
        }
        val token: String
        try {
            val tree: JsonNode
            val om = ObjectMapper()
            tree = om.readTree(content)
            token = tree.at("/data/content").asText()
        } catch (e: JsonProcessingException) {
            Toaster.show("json格式错误")
            return
        }
        loadingDialog?.show()
        loginByToken(token)
    }

    private fun loginByToken(token: String) {
        Thread {
            try {
                val list = createAccountList(token)
                accountList.addAll(list)
                refreshAccountList(accountList)
                updateAccountView()
//                Handler(Looper.getMainLooper()).post {
//                    for (account in list)
//                        adapter.addData(account)
//                }
                Toaster.show("登录成功")
            } catch (e: Exception) {
                Toaster.show(e.message)
                e.printStackTrace()
            } finally {
                loadingDialog?.dismiss()
            }
        }.start()
    }

    private fun refreshAccountList(list: ArrayList<AccountSk>) {
        val prefManager: PrefManager by getKoin().inject()
        prefManager.ListAccountSk.set(list)
    }
}
