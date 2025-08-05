package com.blueskybone.arkscreen.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ActivityLoginWebBinding
import com.blueskybone.arkscreen.util.getCookie
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hjq.toast.Toaster
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */


class LoginWeb : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    private lateinit var textButton: TextView
    private var _binding: ActivityLoginWebBinding? = null
    private val binding get() = _binding!!

    companion object {
        enum class LoginType {
            SKLAND, GACHA_OFFICIAL, GACHA_BILI
        }

        private const val skland = "skland"
        private const val gacha_official = "gacha_official"
        private const val gacha_bili = "gacha_bili"

        private const val loginType = "login_type"
        private fun convert(type: LoginType): String {
            return when (type) {
                LoginType.SKLAND -> skland
                LoginType.GACHA_OFFICIAL -> gacha_official
                else -> gacha_bili
            }
        }

        private fun convert(str: String): LoginType {
            return when (str) {
                skland -> LoginType.SKLAND
                gacha_official -> LoginType.GACHA_OFFICIAL
                else -> LoginType.GACHA_BILI
            }
        }

        private const val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/117.0"

        private const val sklandUrl = "https://www.skland.com"
        private const val arkHomeOfficialUrl = "https://ak.hypergryph.com/user/home"
        private const val arkHomeBiliUrl = "https://ak.hypergryph.com/user/bilibili/login"

        private const val apiOfficial = "https://web-api.skland.com/account/info/hg"
        private const val arkApiOfficial = "https://web-api.hypergryph.com/account/info/hg"
        private const val arkApiBili = "https://web-api.hypergryph.com/account/info/ak-b"


        fun start(context: Context, type: LoginType) {
            val intent = Intent(context, LoginWeb::class.java).apply {
                putExtra(loginType, convert(type))
            }
            context.startActivity(intent)
        }

        fun startIntent(context: Context, type: LoginType): Intent {
            val intent = Intent(context, LoginWeb::class.java).apply {
                putExtra(loginType, convert(type))
            }
            return intent
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginWebBinding.inflate(layoutInflater)
        setSupportActionBar(binding.ToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progressBar = binding.ProgressBar
        toolbar = binding.ToolBar
        webView = binding.WebView
        textButton = binding.TextButton

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setContentView(binding.root)

        webView.webViewClient = WebViewClient()

        val settings = webView.settings
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.cacheMode = WebSettings.LOAD_DEFAULT // 默认缓存模式
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true // 适应网页大小
        settings.userAgentString = userAgent
        settings.useWideViewPort = true
        settings.javaScriptEnabled = true
        settings.displayZoomControls = false
        settings.builtInZoomControls = false
        settings.allowFileAccess = true
        settings.loadsImagesAutomatically = true

        //cookie
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = ProgressBar.GONE
                } else {
                    progressBar.visibility = ProgressBar.VISIBLE
                }
            }
        }

        when (convert(intent.getStringExtra(loginType) ?: skland)) {
            LoginType.SKLAND -> setSklandWebView()
            LoginType.GACHA_OFFICIAL -> setArkOfficialWebView()
            LoginType.GACHA_BILI -> setArkBilibiliWebView()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    finish()
                }
            }
        })
    }


    private fun setSklandWebView() {
        textButton.text = getString(R.string.text_web_skland)
        textButton.visibility = View.VISIBLE

        class JsObject {
            @JavascriptInterface
            @Throws(JsonProcessingException::class)
            fun submitDeviceDid(dId: String) {
                try {
                    val token = getCookie(apiOfficial, "ACCOUNT")
                    val returnIntent = Intent()
                    returnIntent.putExtra("token", token)
                    returnIntent.putExtra("dId", dId)
                    setResult(RESULT_OK, returnIntent)
                    finish()
                } catch (e: Exception) {
                    Timber.tag("exception").w(e)
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("JavascriptInterface")
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                toolbar.title = view.title
                view.addJavascriptInterface(JsObject(), "Android")
                val script =
                    "(function() {const dId = SMSdk.getDeviceId(); Android.submitDeviceDid(dId);})();".trim { it <= ' ' }
                textButton.setOnClickListener {
                    view.evaluateJavascript(script, null)
                }
            }
        }
        webView.loadUrl(sklandUrl)

    }

    private fun setArkOfficialWebView() {
        textButton.text = getString(R.string.text_web_ark)
        textButton.visibility = View.VISIBLE
        class JsObject {
            @JavascriptInterface
            @Throws(JsonProcessingException::class)
            fun submitMetaJson(metaJson: String) {
                try {
                    val jsonNode = jacksonObjectMapper() .readTree(metaJson)
                    val xrToken = jsonNode.get("token")?.asText()
                    val token = getCookie(arkApiOfficial, "ACCOUNT")
                    val userCenter = getCookie(arkHomeOfficialUrl, "ak-user-center")

                    val returnIntent = Intent()

                    returnIntent.putExtra("token", token)
                    returnIntent.putExtra("userCenter", userCenter)
                    returnIntent.putExtra("xrToken", xrToken)
                    returnIntent.putExtra("channelMasterId", 1)
                    setResult(RESULT_OK, returnIntent)
                    finish()
                } catch (e: Exception) {
                    Timber.tag("exception").w(e)
                }
            }
        }
        webView.apply {
            // 1. 先配置 WebViewClient
            val script =
                "(function() {const metaJson = localStorage.ONE_ACCOUNT_ROLE_META; Android.submitMetaJson(metaJson);})();".trim { it <= ' ' }
            this.addJavascriptInterface(JsObject(), "Android")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    toolbar.title = view.title
                    // 3. 页面加载完成后执行JS
                    textButton.setOnClickListener {
                        Toaster.show("onclick")
                        view.evaluateJavascript(script, null)
                    }
                }
            }
        }
        webView.loadUrl(arkHomeOfficialUrl)
    }

    private fun setArkBilibiliWebView() {
        textButton.text = getString(R.string.text_web_ark)
        textButton.visibility = View.VISIBLE
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                toolbar.title = view.title
                textButton.setOnClickListener {
                    try {
                        val token = getCookie(arkApiBili, "ACCOUNT_AK_B")
                        val returnIntent = Intent()
                        returnIntent.putExtra("token", token)
                        returnIntent.putExtra("channelMasterId", 2)
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    } catch (e: Exception) {
                        Toaster.show(e.message)
                    }
                }
            }
        }
        webView.loadUrl(arkHomeBiliUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}