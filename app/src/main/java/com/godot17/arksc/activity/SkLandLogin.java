package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.NetWorkTask.OK;
import static com.godot17.arksc.utils.NetWorkTask.getBindingRoleInfo;
import static com.godot17.arksc.utils.NetWorkTask.getCredByToken;
import static com.godot17.arksc.utils.NetWorkTask.loadStatusInfoByCred;
import static com.godot17.arksc.utils.NetWorkTask.logOutMsg;
import static com.godot17.arksc.utils.PrefManager.getTokenChanged;
import static com.godot17.arksc.utils.PrefManager.getUserInfo;
import static com.godot17.arksc.utils.PrefManager.setChannelMasterId;
import static com.godot17.arksc.utils.PrefManager.setSignTs;
import static com.godot17.arksc.utils.PrefManager.setTokenChanged;
import static com.godot17.arksc.utils.PrefManager.setUserId;
import static com.godot17.arksc.utils.PrefManager.setUserInfo;
import static com.godot17.arksc.utils.Utils.showToast;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.godot17.arksc.R;
import com.godot17.arksc.datautils.RoleInfo;
import com.godot17.arksc.utils.LoadingDialog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;


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
            try {
                switchBindingRole();
            } catch (MalformedURLException | JsonProcessingException e) {
                e.printStackTrace();
            }
            //showAlertDialog();
        } else if (id == R.id.card_skland_login_web) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.card_skland_logout) {
            logOut();
        }
    }

    private void switchBindingRole() throws MalformedURLException, JsonProcessingException {
        //cred->bindinglist
        new Thread(() -> {
            List<RoleInfo> roleInfos = null;
            try {
                roleInfos = getBindingRoleInfo(this);
            } catch (MalformedURLException | JsonProcessingException e) {
                e.printStackTrace();
            }
            if (roleInfos == null) {
                mHandler.post(() -> {
                    Toast.makeText(this, "获取角色列表失败", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            String[] str = new String[roleInfos.size()];
            int cnt = 0;
            for (RoleInfo role : roleInfos) {
                str[cnt++] = role.userInfo;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            List<RoleInfo> finalRoleInfos = roleInfos;
            builder.setTitle("选择绑定角色")
                    .setItems(str, (dialog, which) -> {
                        RoleInfo roleInfo = finalRoleInfos.get(which);
                        setUserId(this, roleInfo.uid);
                        setChannelMasterId(this, roleInfo.channelMasterId);
                        setUserInfo(this, roleInfo.userInfo);
                        setSignTs(this, 0);

                        Intent intent = new Intent("MANUAL_UPDATE");
                        intent.setPackage(getPackageName());    //from Android 12 must setPackage
                        sendBroadcast(intent);
                        mHandler.post(() -> {
                            textView.setText(getUserInfo(this));
                        });
                    });
            mHandler.post(() -> builder.create().show());

        }).start();


    }

    @Override
    protected void onResume() {
        super.onResume();
        textView.setText(getUserInfo(this));
        if (getTokenChanged(this)) {
            setTokenChanged(this, false);
            loadingDialog.show();
            loginByToken();
        }
    }

    private void loginByToken() {
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
