package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.NetWorkTask.OK;
import static com.godot17.arksc.utils.NetWorkTask.getCredByToken;
import static com.godot17.arksc.utils.NetWorkTask.loadStatusInfoByCred;
import static com.godot17.arksc.utils.NetWorkTask.logOutMsg;
import static com.godot17.arksc.utils.PrefManager.getTokenChanged;
import static com.godot17.arksc.utils.PrefManager.getUserInfo;
import static com.godot17.arksc.utils.PrefManager.setTokenChanged;
import static com.godot17.arksc.utils.Utils.showToast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.godot17.arksc.R;
import com.godot17.arksc.utils.LoadingDialog;

import java.io.IOException;


public class SkLandLogin extends Activity implements View.OnClickListener {
    private final String TAG = "SklandLogin";
    private Handler mHandler;
    private LoadingDialog loadingDialog;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        mHandler = new Handler();
        loadingDialog = new LoadingDialog(this);
    }

    private void setLayout() {
        setContentView(R.layout.activity_skland_login);
        CardView cardView_1 = findViewById(R.id.card_bind_role);
        CardView cardView_2 = findViewById(R.id.card_skland_login_web);
        CardView cardView_3 = findViewById(R.id.card_skland_logout);

        textView = findViewById(R.id.text_binding_role);

        cardView_1.setOnClickListener(this);
        cardView_2.setOnClickListener(this);
        cardView_3.setOnClickListener(this);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.card_bind_role) {
            //switchBindingRole();
            showAlertDialog();
        } else if (id == R.id.card_skland_login_web) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.card_skland_logout) {
            logOut();
        }
    }

    private void showAlertDialog() {
        String content = getString(R.string.content_switch_role);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(content)
                .setTitle("切换绑定角色")
                .setPositiveButton(R.string.identify, (dialog, which) -> {
                })
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        textView.setText(getUserInfo(this));
        if (getTokenChanged(this)) {
            setTokenChanged(this, false);
            loadingDialog.show();
            loginByTokenNew();
            //loginByToken(getToken(this));
        }
    }

    private void loginByTokenNew() {
        new Thread(() -> {
            try {
                String resp = getCredByToken(this);
                if (!resp.equals(OK)) {
                    loadingDialog.dismiss();
                    showToast(this, resp);
                    finish();
                    return;
                }
                resp = loadStatusInfoByCred(this);
                if (!resp.equals(OK)) {
                    loadingDialog.dismiss();
                    showToast(this, resp);
                    finish();
                    return;
                }
                loadingDialog.dismiss();
                String userInfo = getUserInfo(this);
                textView.post(() -> textView.setText(userInfo));
                showToast(this, "登录成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

//    private void loginByToken(String token) {
//        new Thread(() -> {
//            //loadingDialog.show();
//            try {
//                String code = getGrantCodeByToken(token);
//                if (code == null) {
//                    //loadingDialog.dismiss();
//                    showToast(this, "error with getGrantCode");
//                    finish();
//                    return;
//                } else if (code.equals("UNAUTHORIZED")) {
//                    // loadingDialog.dismiss();
//                    showToast(this, "Token已过期，请重新登录");
//                    finish();
//                    return;
//                }
//                String cred = getCredByGrant(code);
//                if (cred == null) {
//                    //loadingDialog.dismiss();
//                    showToast(this, "error with getCred");
//                    finish();
//                    return;
//                }
//                String userInfo = getBindingInfoWith(cred, "userInfo");
//                if (userInfo == null) {
//                    // loadingDialog.dismiss();
//                    showToast(this, "error with get BindingList");
//                    finish();
//                    return;
//                }
//                textView.post(() -> textView.setText(userInfo));
//                setUserInfo(this, userInfo);
//                setToken(this, token);
//                showToast(this, "token已保存");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            finish();
//        }).start();
//    }

    private void logOut() {
        new Thread(() -> {
            try {
                textView.post(() -> textView.setText("未登录"));
                String msg = logOutMsg(this);
                showToast(this, msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }).start();
    }
}
