package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.NetworkUtils.getBindingInfoWith;
import static com.godot17.arksc.utils.NetworkUtils.getCredByGrant;
import static com.godot17.arksc.utils.NetworkUtils.getGrantCodeByToken;
import static com.godot17.arksc.utils.NetworkUtils.getJsonContent;
import static com.godot17.arksc.utils.NetworkUtils.logOutByToken;
import static com.godot17.arksc.utils.PrefManager.getToken;
import static com.godot17.arksc.utils.PrefManager.setToken;
import static com.godot17.arksc.utils.PrefManager.setUserInfo;
import static com.godot17.arksc.utils.Utils.showToast;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.godot17.arksc.R;
import com.godot17.arksc.utils.LoadingDialog;

import java.io.IOException;

import io.noties.markwon.Markwon;

public class SklandLoginActivity extends Activity implements View.OnClickListener {
    private final String TAG = "SklandLoginActivity";
    private Handler mHandler;
    private LoadingDialog loadingDialog;
    private EditText editText;
    private TextView textView_1;
    private TextView textView_2;
    private TextView textView_3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(this);
        setLayout();
        mHandler = new Handler();
    }

    private void setLayout() {
        setContentView(R.layout.activity_login);
        TextView textView = findViewById(R.id.textView_login_content);
        editText = findViewById(R.id.edit_token);
        textView_1 = findViewById(R.id.text_clear);
        textView_2 = findViewById(R.id.text_identify);
        textView_3 = findViewById(R.id.text_paste);
        Markwon.create(this).setMarkdown(textView, getString(R.string.title_skland_login_content));
        if (!getToken(this).equals("")) {
            setLoginVisible(View.INVISIBLE);
        }
    }

    private void setLoginVisible(int visible) {
        textView_1.setVisibility(visible);
        textView_2.setVisibility(visible);
        textView_3.setVisibility(visible);
        editText.setVisibility(visible);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        EditText token_text = findViewById(R.id.edit_token);
        if (id == R.id.text_identify) {
            String token = getJsonContent(token_text.getText().toString(), "content");
            loginByToken(token);
        } else if (id == R.id.text_logout) {
            logOut();
        } else if (id == R.id.text_clear) {
            token_text.setText("");
        } else if (id == R.id.text_paste) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                token_text.setText(clipboard.getPrimaryClip().getItemAt(0).getText());
            }
        }
    }


    private void loginByToken(String token) {
        loadingDialog.show();
        new Thread(() -> {
            try {
                String code = getGrantCodeByToken(token);
                if (code == null) {
                    loadingDialog.dismiss();
                    showToast(this, "error with getGrantCode");
                    finish();
                    return;
                } else if (code.equals("UNAUTHORIZED")) {
                    loadingDialog.dismiss();
                    showToast(this, "Token已过期，请重新登录");
                    finish();
                    return;
                }
                String cred = getCredByGrant(code);
                if (cred == null) {
                    loadingDialog.dismiss();
                    showToast(this, "error with getCred");
                    finish();
                    return;
                }
                String userInfo = getBindingInfoWith(cred, "userInfo");
                if (userInfo == null) {
                    loadingDialog.dismiss();
                    showToast(this, "error with get BindingList");
                    finish();
                    return;
                }
                setUserInfo(this, userInfo);
                setToken(this, token);
                loadingDialog.dismiss();
                mHandler.post(() -> setLoginVisible(View.INVISIBLE));
                showToast(this, "登录成功，token已保存");
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }).start();
    }

    private void logOut() {
        loadingDialog.show();
        new Thread(() -> {
            try {
                String token = getToken(this);
                Log.e(TAG, token);
                if (token.equals("")) {
                    loadingDialog.dismiss();
                    showToast(this, "token为空");
                    finish();
                    return;
                }
                if (logOutByToken(token)) {
                    setToken(this, "");
                    setUserInfo(this, "未登录状态");
                    loadingDialog.dismiss();
                    mHandler.post(() -> setLoginVisible(View.VISIBLE));
                    showToast(this, "登出成功");

                } else {
                    setToken(this, "");
                    setUserInfo(this, "未登录状态");
                    loadingDialog.dismiss();
                    mHandler.post(() -> setLoginVisible(View.VISIBLE));

                    showToast(this, "登出发生问题，已清空数据");
                }
                setToken(this, "");
                setUserInfo(this, "未登录状态");
                //mDeleteFile(this, "binding_list.json");
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }).start();
    }
}
