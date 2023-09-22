package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.PrefManager.setToken;
import static com.godot17.arksc.utils.PrefManager.setTokenChanged;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.utils.PrefManager;


//重新考虑凭证存储的数据结构。
//保存 token
//保存 cred
//获取数据首先使用 cred，失效时使用token，token失效清除登录信息，提示重新登录
public class LoginActivity extends ComponentActivity {
    String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/117.0";
    String skLandUrl = "https://www.skland.com";
    boolean mutex;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginView(skLandUrl);
        mutex = true;
    }

    private void setTokenToLocal(String token){
        setToken(this, token);
        setTokenChanged(this, true);
    }
    /*不知道cred过期时间是多久*/
    @SuppressLint("SetJavaScriptEnabled")
    private void LoginView(String url) {

        class JsObject{
            @JavascriptInterface
            public void submitSkToken(String cred, String usr_token) throws JsonProcessingException {
                /**
                 * onPageFinished() does not guarantee the load finish and can be called more than once.
                 * see: https://stackoverflow.com/questions/3149216/how-to-listen-for-a-webview-finishing-loading-a-url/5172952#5172952
                 * */
                if(mutex){
                    mutex = false;
                    ObjectMapper om = new ObjectMapper();
                    Log.e("login usr_token",usr_token);
                    Log.e("login cred",cred);
                    JsonNode tree = om.readTree(usr_token);
                    String token = tree.at("/value").toString().replace("\"","");
                    setTokenToLocal(token);
                    showDialogAlert();
                }
            }
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);

        WebView webView = new WebView(this);
        webView.loadUrl(url);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(USER_AGENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressLint("JavascriptInterface")
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.scrollBy(1024, 0);
                setContentView(view);
                view.addJavascriptInterface(new JsObject(),"Android");
                String script = "(function() { const pollCred = setInterval(function () {const usr_token = sessionStorage.getItem(\"__hg_user_token\");const cred = localStorage.getItem(\"SK_OAUTH_CRED_KEY\"); if (usr_token!=null && cred!=null) { Android.submitSkToken(cred,usr_token);clearInterval(pollCred);  } }, 500); })();".trim();
                view.evaluateJavascript(script,null);
            }
        });
    }

    private void showDialogAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("已获取凭证")
                .setMessage("确定后返回")
                .setPositiveButton("确定", (dialog, id) -> finish()).create()
                .show();
    }
}
