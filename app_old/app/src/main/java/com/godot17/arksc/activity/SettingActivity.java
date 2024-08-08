package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.NetWorkTask.OK;
import static com.godot17.arksc.utils.NetWorkTask.getBindingRoleInfo;
import static com.godot17.arksc.utils.NetWorkTask.getCredByToken;
import static com.godot17.arksc.utils.NetWorkTask.loadStatusInfoByCred;
import static com.godot17.arksc.utils.NetWorkTask.logOutMsg;
import static com.godot17.arksc.utils.PrefManager.getAutoSign;
import static com.godot17.arksc.utils.PrefManager.getShowMode;
import static com.godot17.arksc.utils.PrefManager.getUserInfo;
import static com.godot17.arksc.utils.PrefManager.getWidgetBgAlpha;
import static com.godot17.arksc.utils.PrefManager.getWidgetBgColor;
import static com.godot17.arksc.utils.PrefManager.getWidgetTextColor;
import static com.godot17.arksc.utils.PrefManager.getWidgetTextSize;
import static com.godot17.arksc.utils.PrefManager.setAutoSign;
import static com.godot17.arksc.utils.PrefManager.setChannelMasterId;
import static com.godot17.arksc.utils.PrefManager.setSignTs;
import static com.godot17.arksc.utils.PrefManager.setToken;
import static com.godot17.arksc.utils.PrefManager.setUserId;
import static com.godot17.arksc.utils.PrefManager.setUserInfo;
import static com.godot17.arksc.utils.PrefManager.setWidgetBgAlpha;
import static com.godot17.arksc.utils.PrefManager.setWidgetBgColor;
import static com.godot17.arksc.utils.PrefManager.setWidgetTextColor;
import static com.godot17.arksc.utils.PrefManager.setWidgetTextSize;
import static com.godot17.arksc.utils.Utils.showToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.R;
import com.godot17.arksc.datautils.RoleInfo;
import com.godot17.arksc.utils.LoadingDialog;
import com.godot17.arksc.utils.PrefManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.noties.markwon.Markwon;

public class SettingActivity extends Activity implements View.OnClickListener {
    private SettingActivity instance;
    private LoadingDialog loadingDialog;
    private Handler mHandler;
    private TextView textView;

    private void setLayout() {
        setContentView(R.layout.activity_setting);
        CardView card_option = this.findViewById(R.id.card_option);

        CardView card_bind_role = this.findViewById(R.id.card_bind_role);
        CardView card_login = this.findViewById(R.id.card_skland_login);
        CardView card_logout = this.findViewById(R.id.card_skland_logout);

        CardView card_widget = this.findViewById(R.id.card_widget);
        SwitchCompat switch_auto = this.findViewById(R.id.switch_auto_sign);
        setTextOption();

        textView = findViewById(R.id.text_binding_role);

        card_option.setOnClickListener(this);
        card_bind_role.setOnClickListener(this);
        card_login.setOnClickListener(this);
        card_logout.setOnClickListener(this);
        card_widget.setOnClickListener(this);

        switch_auto.setChecked(getAutoSign(this));
        switch_auto.setOnCheckedChangeListener((buttonView, isChecked) -> setAutoSign(this, isChecked));
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        textView.setText(getUserInfo(this));
    }

    private void setTextOption() {
        TextView text_option = this.findViewById(R.id.textView_option);
        PrefManager.funcMode showMode = getShowMode(this);
        if (showMode == PrefManager.funcMode.FLOAT_WIN) {
            text_option.setText(R.string.radio_float);
        } else if (showMode == PrefManager.funcMode.TOAST_MSG) {
            text_option.setText(R.string.radio_toast);
        } else if (showMode == PrefManager.funcMode.FLEX) {
            text_option.setText(R.string.radio_flex);
        } else {
            text_option.setText(R.string.text_default);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setLayout();
        mHandler = new Handler();
        loadingDialog = new LoadingDialog(this);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        TextView textView = findViewById(R.id.textView_userInfo);
//        textView.setText(getUserInfo(this));
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.card_option) {
            showDialogForShowMode();
        } else if (id == R.id.card_bind_role) {
            try {
                switchBindingRole();
            } catch (MalformedURLException | JsonProcessingException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.card_skland_login) {
            showLoginDialog();
        } else if (id == R.id.card_skland_logout) {
            logOut();
        } else if (id == R.id.card_widget) {
            showDialogWidget(this);
        }
    }


    private void switchBindingRole() throws MalformedURLException, JsonProcessingException {
        //cred->bindinglist
        new Thread(() -> {
            List<RoleInfo> roleInfos = null;
            try {
                roleInfos = getBindingRoleInfo(this);
            } catch (MalformedURLException | JsonProcessingException | InvalidKeyException |
                     NoSuchAlgorithmException e) {
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
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
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
                        mHandler.post(() -> textView.setText(getUserInfo(this)));
                    });
            mHandler.post(() -> builder.create().show());
        }).start();
    }


    @SuppressLint("ResourceType")
    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //String content = "te";
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_login, null);
        TextView textView = view.findViewById(R.id.textView_dialog_login_text);
        EditText editText = view.findViewById(R.id.editText_token);
        Markwon.create(this).setMarkdown(textView, getString(R.string.login_content));
        builder.setView(view)
                .setTitle(getString(R.string.button_login))
                //从输入框导入
                .setPositiveButton(R.string.identify, (dialog, which) -> doLogin(editText.getText().toString()))
                //从剪切板导入
                .setNegativeButton(R.string.paste, ((dialog, which) -> doLoginFromPaste()))
                .create()
                .show();
    }

    private void doLoginFromPaste() {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            doLogin(clipData.getItemAt(0).getText().toString());
        }
    }

    //尝试用json读取/data/content内容,失败则返回格式错误
    private void doLogin(String content) {
        Log.e("Content", content);
        if (null == content || content.equals("")) {
            return;
        }
        ObjectMapper om;
        JsonNode tree;
        try {
            om = new ObjectMapper();
            tree = om.readTree(content);
        } catch (JsonProcessingException e) {
            showToast(this, "json格式错误");
            return;
        }
        if (tree.at("/data/content") == null) {
            showToast(this, "content字段丢失");
            return;
        }
        String token = tree.at("/data/content").toString();
        loadingDialog.show();
        setToken(this, token.replace("\"", ""));
        LoginByToken();
    }

    private void LoginByToken() {
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

    private void showDialogWidget(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        View view = LayoutInflater.from(SettingActivity.this)
                .inflate(R.layout.dialog_edit, null);

        SeekBar seekBarBg = view.findViewById(R.id.seekBar_bg);
        SeekBar seekBarSize = view.findViewById(R.id.seekBar_size);

        RadioGroup radioGroupBgColor = view.findViewById(R.id.radioGroup_bg_color);
        RadioButton radioButtonBgBlack = view.findViewById(R.id.radioButton_bg_color_black);
        RadioButton radioButtonBgWhite = view.findViewById(R.id.radioButton_bg_color_white);
        RadioGroup radioGroupTextColor = view.findViewById(R.id.radioGroup_text_color);
        RadioButton radioButtonTextBlack = view.findViewById(R.id.radioButton_text_color_black);
        RadioButton radioButtonTextWhite = view.findViewById(R.id.radioButton_text_color_white);

        int alpha = getWidgetBgAlpha(this);
        int size = getWidgetTextSize(this);

        int colorIdBg = getWidgetBgColor(this);
        int colorIdText = getWidgetTextColor(this);

        seekBarBg.setProgress(alpha);
        seekBarSize.setProgress(size);
        if (colorIdBg == R.color.black) {
            radioButtonBgBlack.setChecked(true);
        } else {
            radioButtonBgWhite.setChecked(true);
        }
        if (colorIdText == R.color.black) {
            radioButtonTextBlack.setChecked(true);
        } else {
            radioButtonTextWhite.setChecked(true);
        }

        builder.setTitle("设置组件外观")
                .setView(view)
                .setPositiveButton(R.string.identify, (dialog, which) -> {
                    setWidgetBgAlpha(context, seekBarBg.getProgress());
                    setWidgetTextSize(context, seekBarSize.getProgress());
                    int id1 = radioGroupBgColor.getCheckedRadioButtonId();
                    if (id1 == R.id.radioButton_bg_color_black) {
                        setWidgetBgColor(context, R.color.black);
                    } else {
                        setWidgetBgColor(context, R.color.white);
                    }
                    int id2 = radioGroupTextColor.getCheckedRadioButtonId();
                    if (id2 == R.id.radioButton_text_color_black) {
                        setWidgetTextColor(context, R.color.black);
                    } else {
                        setWidgetTextColor(context, R.color.white);
                    }
                    //updateWidget
                    Intent intent = new Intent("MANUAL_UPDATE");
                    intent.setPackage(getPackageName());    //from Android 12 must setPackage
                    sendBroadcast(intent);
                    Log.e("SETTING", "broadcast");
                })
                .setNegativeButton(R.string.close, (dialog, which) -> {
                }).create()
                .show();

    }

    @SuppressLint("NonConstantResourceId")
    private void showDialogForShowMode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        View view = LayoutInflater.from(SettingActivity.this)
                .inflate(R.layout.dialog_show_mode, null);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup_showMode);
        RadioButton rb1 = view.findViewById(R.id.radio_float);
        RadioButton rb2 = view.findViewById(R.id.radio_toast);
        RadioButton rb3 = view.findViewById(R.id.radio_flex);

        CheckBox cb1 = view.findViewById(R.id.checkBox1);
        CheckBox cb2 = view.findViewById(R.id.checkBox2);
        CheckBox cb3 = view.findViewById(R.id.checkBox3);
        CheckBox cb4 = view.findViewById(R.id.checkBox4);
        CheckBox cb5 = view.findViewById(R.id.checkBox5);
        CheckBox cb6 = view.findViewById(R.id.checkBox6);

        CheckBox[] cbs = {cb1, cb2, cb3, cb4, cb5, cb6};

        for (CheckBox cb : cbs) {
            boolean status = PrefManager.getCheckBoxStatus(this, cb.getText().toString());
            cb.setChecked(status);
            cb.setVisibility(View.INVISIBLE);
        }
        TextView textView = view.findViewById(R.id.text_flex_desc);
        textView.setVisibility(View.INVISIBLE);

        PrefManager.funcMode showMode = getShowMode(this);
        if (showMode == PrefManager.funcMode.FLOAT_WIN) {
            rb1.setChecked(true);

        } else if (showMode == PrefManager.funcMode.TOAST_MSG) {
            rb2.setChecked(true);

        } else if (showMode == PrefManager.funcMode.FLEX) {
            rb3.setChecked(true);
            for (CheckBox cb : cbs) {
                cb.setVisibility(View.VISIBLE);
            }
            textView.setVisibility(View.VISIBLE);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_float) {
                rb1.setChecked(true);
                PrefManager.setShowMode(this, PrefManager.funcMode.FLOAT_WIN);
                for (CheckBox cb : cbs) {
                    cb.setVisibility(View.INVISIBLE);
                }
                textView.setVisibility(View.INVISIBLE);
            } else if (checkedId == R.id.radio_toast) {
                rb2.setChecked(true);
                PrefManager.setShowMode(this, PrefManager.funcMode.TOAST_MSG);
                for (CheckBox cb : cbs) {
                    cb.setVisibility(View.INVISIBLE);
                }
                textView.setVisibility(View.INVISIBLE);
            } else if (checkedId == R.id.radio_flex) {
                rb3.setChecked(true);
                PrefManager.setShowMode(this, PrefManager.funcMode.FLEX);
                for (CheckBox cb : cbs) {
                    cb.setVisibility(View.VISIBLE);
                }
                textView.setVisibility(View.VISIBLE);
            }
//            switch (checkedId) {
//                case R.id.radio_float:
//                    rb1.setChecked(true);
//                    PrefManager.setShowMode(this, PrefManager.funcMode.FLOAT_WIN);
//                    for (CheckBox cb : cbs) {
//                        cb.setVisibility(View.INVISIBLE);
//                    }
//                    textView.setVisibility(View.INVISIBLE);
//                    break;
//                case R.id.radio_toast:
//                    rb2.setChecked(true);
//                    PrefManager.setShowMode(this, PrefManager.funcMode.TOAST_MSG);
//                    for (CheckBox cb : cbs) {
//                        cb.setVisibility(View.INVISIBLE);
//                    }
//                    textView.setVisibility(View.INVISIBLE);
//                    break;
//                case R.id.radio_flex:
//                    rb3.setChecked(true);
//                    PrefManager.setShowMode(this, PrefManager.funcMode.FLEX);
//                    for (CheckBox cb : cbs) {
//                        cb.setVisibility(View.VISIBLE);
//                    }
//                    textView.setVisibility(View.VISIBLE);
//                    break;
//                default:
//                    break;
//            }
            setTextOption();
        });

        for (CheckBox cb : cbs) {
            cb.setOnClickListener(v -> PrefManager.setCheckBoxStatus(instance, cb.isChecked(), cb.getText().toString()));
        }
        builder.setView(view).show();
    }

}
