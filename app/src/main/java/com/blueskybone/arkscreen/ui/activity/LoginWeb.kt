package com.blueskybone.arkscreen.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

        private const val SKLAND = "skland"
        private const val GACHA_OFFICIAL = "gacha_official"
        private const val GACHA_BILI = "gacha_bili"

        private const val LOGIN_TYPE = "login_type"
        private fun convert(type: LoginType): String {
            return when (type) {
                LoginType.SKLAND -> SKLAND
                LoginType.GACHA_OFFICIAL -> GACHA_OFFICIAL
                else -> GACHA_BILI
            }
        }

        private fun convert(str: String): LoginType {
            return when (str) {
                SKLAND -> LoginType.SKLAND
                GACHA_OFFICIAL -> LoginType.GACHA_OFFICIAL
                else -> LoginType.GACHA_BILI
            }
        }

        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/117.0"

        private const val SKLAND_URL = "https://www.skland.com"
        private const val ARK_USER_URL = "https://ak.hypergryph.com/user/home"
//        private const val arkHomeBiliUrl = "https://ak.hypergryph.com/user/bilibili/login"

        private const val API_OFFICIAL = "https://web-api.skland.com/account/info/hg"
        private const val ARK_API_OFFICIAL = "https://web-api.hypergryph.com/account/info/hg"
//        private const val arkApiBili = "https://web-api.hypergryph.com/account/info/ak-b"

        fun start(context: Context, type: LoginType) {
            val intent = Intent(context, LoginWeb::class.java).apply {
                putExtra(LOGIN_TYPE, convert(type))
            }
            context.startActivity(intent)
        }

        fun startIntent(context: Context, type: LoginType): Intent {
            val intent = Intent(context, LoginWeb::class.java).apply {
                putExtra(LOGIN_TYPE, convert(type))
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
        settings.userAgentString = USER_AGENT
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

        when (convert(intent.getStringExtra(LOGIN_TYPE) ?: SKLAND)) {
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

        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("JavascriptInterface")
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                toolbar.title = view.title
                view.addJavascriptInterface(JsObject(), "Android")
                //自动检测登录js脚本
                val script =
                """(function() {
                    const pollCred = setInterval(function () {
                        const cred = localStorage.getItem("SK_OAUTH_CRED_KEY");
                        if (cred) {
                            const dId = SMSdk.getDeviceId(); 
                            Android.submitDeviceDid(dId);
                            clearInterval(pollCred);
                        }
                    }, 500);
                    })();
                """.trimIndent()
                //手动js脚本
                val manualScript = "(function() {const dId = SMSdk.getDeviceId(); Android.submitDeviceDid(dId);})();".trimIndent()
                view.evaluateJavascript(script, null)
                textButton.setOnClickListener {
                    view.evaluateJavascript(manualScript, null)
                }
            }
        }
        webView.loadUrl(SKLAND_URL)
    }


    private fun setArkOfficialWebView() {
        textButton.text = getString(R.string.text_web_ark)
        textButton.visibility = View.VISIBLE
        webView.apply {
            addJavascriptInterface(JsObjectArkOfficial(), "Android")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    toolbar.title = view.title
                    val manualScript = """
                    (function() {
                        const metaJson = localStorage.ONE_ACCOUNT_ROLE_META; 
                        Android.submitMetaJson(metaJson);
                    })();
                """.trimIndent()

                    val script = """
                    (function() {
                        localStorage.removeItem("ONE_ACCOUNT_ROLE_META");
                        console.log("Cleared ONE_ACCOUNT_ROLE_META from localStorage");
                        const pollCred = setInterval(function () {
                            const cred = localStorage.getItem("ONE_ACCOUNT_ROLE_META");
                            if (cred) {
                                Android.submitMetaJson(cred);
                                clearInterval(pollCred);
                            }
                        }, 500);
                    })();
                """.trimIndent()
                    //手动触发
                    textButton.setOnClickListener {
                        view.evaluateJavascript(manualScript, null)
                    }
                    //自动轮询
                    view.evaluateJavascript(script, null)
                }
            }
        }
        webView.loadUrl(ARK_USER_URL)
    }
    private fun setArkBilibiliWebView() {
        textButton.text = getString(R.string.text_web_ark)
        textButton.visibility = View.VISIBLE

        webView.apply {
            addJavascriptInterface(JsObjectArkBili(), "Android")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    toolbar.title = view.title
                    val manualScript = """
                    (function() {
                        const metaJson = localStorage.ONE_ACCOUNT_ROLE_META; 
                        Android.submitMetaJson(metaJson);
                    })();
                """.trimIndent()
                    val script = """
                    (function() {
                        localStorage.removeItem("ONE_ACCOUNT_ROLE_META");
                        console.log("Cleared ONE_ACCOUNT_ROLE_META from localStorage");
                        const pollCred = setInterval(function () {
                            const cred = localStorage.getItem("ONE_ACCOUNT_ROLE_META");
                            if (cred) {
                                Android.submitMetaJson(cred);
                                clearInterval(pollCred);
                            }
                        }, 500);
                    })();
                """.trimIndent()
                    //手动触发
                    textButton.setOnClickListener {
                        view.evaluateJavascript(manualScript, null)
                    }
                    //自动轮询
                    view.evaluateJavascript(script, null)
                }
            }
        }
        webView.loadUrl(ARK_USER_URL)
    }
    inner class JsObject {
        @JavascriptInterface
        @Throws(JsonProcessingException::class)
        fun submitDeviceDid(dId: String) {
            try {
                val token = getCookie(API_OFFICIAL, "ACCOUNT")
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

    inner class JsObjectArkOfficial {
        @JavascriptInterface
        fun submitMetaJson(metaJson: String) {
            try {
                println(metaJson)
                val jsonNode = jacksonObjectMapper().readTree(metaJson)
                val xrToken = jsonNode.get("token")?.asText()
                val token = getCookie(ARK_API_OFFICIAL, "ACCOUNT")
                val userCenter = getCookie(ARK_USER_URL, "ak-user-center")

                // 使用 runOnUiThread 确保 UI 操作在主线程
                runOnUiThread {
                    val returnIntent = Intent().apply {
                        putExtra("token", token)
                        putExtra("userCenter", userCenter)
                        putExtra("xrToken", xrToken)
                        putExtra("channelMasterId", 1)
                    }
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }
            } catch (e: Exception) {
                Timber.tag("submitMetaJson").w(e)
            }
        }
    }

    inner class JsObjectArkBili {
        @JavascriptInterface
        @Throws(JsonProcessingException::class)
        fun submitMetaJson(metaJson: String) {
            try {
                val jsonNode = jacksonObjectMapper() .readTree(metaJson)
                val xrToken = jsonNode.get("token")?.asText()
                val userCenter = getCookie(ARK_USER_URL, "ak-user-center")

                val returnIntent = Intent()

                returnIntent.putExtra("userCenter", userCenter)
                returnIntent.putExtra("xrToken", xrToken)
                returnIntent.putExtra("channelMasterId", 2)
                setResult(RESULT_OK, returnIntent)
                finish()
            } catch (e: Exception) {
                Timber.tag("submitMetaJson exception").w(e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}