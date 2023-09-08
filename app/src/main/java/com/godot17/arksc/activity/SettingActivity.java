package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.PrefManager.getAutoSign;
import static com.godot17.arksc.utils.PrefManager.getShowMode;
import static com.godot17.arksc.utils.PrefManager.getUserInfo;
import static com.godot17.arksc.utils.PrefManager.setAutoSign;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.godot17.arksc.R;
import com.godot17.arksc.utils.PrefManager;

public class SettingActivity extends Activity implements View.OnClickListener {
    private SettingActivity instance;

    private void setLayout() {
        setContentView(R.layout.activity_setting);
        CardView card_option = this.findViewById(R.id.card_option);
        CardView card_login = this.findViewById(R.id.card_skland_login);
        SwitchCompat switch_auto = this.findViewById(R.id.switch_auto_sign);
        setTextOption();
        card_option.setOnClickListener(this);
        card_login.setOnClickListener(this);
        switch_auto.setChecked(getAutoSign(this));
        switch_auto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setAutoSign(this, isChecked);
        });
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView textView = findViewById(R.id.textView_userInfo);
        textView.setText(getUserInfo(this));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.card_option) {
            showDialogForShowMode();
        } else if (id == R.id.card_skland_login) {
            startActivity(new Intent(this, SklandLoginActivity.class));
        }
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
            //PrefManager.setShowMode(this, PrefManager.funcMode.FLOAT_WIN);
        } else if (showMode == PrefManager.funcMode.TOAST_MSG) {
            rb2.setChecked(true);
            //PrefManager.setShowMode(this, PrefManager.funcMode.TOAST_MSG);
        } else if (showMode == PrefManager.funcMode.FLEX) {
            rb3.setChecked(true);
            //PrefManager.setShowMode(this, PrefManager.funcMode.FLEX);
            for (CheckBox cb : cbs) {
                cb.setVisibility(View.VISIBLE);
            }
            textView.setVisibility(View.VISIBLE);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_float:
                    rb1.setChecked(true);
                    PrefManager.setShowMode(this, PrefManager.funcMode.FLOAT_WIN);
                    for (CheckBox cb : cbs) {
                        cb.setVisibility(View.INVISIBLE);
                    }
                    textView.setVisibility(View.INVISIBLE);
                    break;
                case R.id.radio_toast:
                    rb2.setChecked(true);
                    PrefManager.setShowMode(this, PrefManager.funcMode.TOAST_MSG);
                    for (CheckBox cb : cbs) {
                        cb.setVisibility(View.INVISIBLE);
                    }
                    textView.setVisibility(View.INVISIBLE);
                    break;
                case R.id.radio_flex:
                    rb3.setChecked(true);
                    PrefManager.setShowMode(this, PrefManager.funcMode.FLEX);
                    for (CheckBox cb : cbs) {
                        cb.setVisibility(View.VISIBLE);
                    }
                    textView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            setTextOption();
        });

        for (CheckBox cb : cbs) {
            cb.setOnClickListener(v -> {
                if (cb.isChecked()) {
                    PrefManager.setCheckBoxStatus(instance, true, cb.getText().toString());
                } else {
                    PrefManager.setCheckBoxStatus(instance, false, cb.getText().toString());
                }
            });
        }
        builder.setView(view).show();
    }

}
