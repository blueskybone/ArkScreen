package com.example.arkscreen.activity;

import static com.example.arkscreen.Utils.ConfigUtils.DEFAULT_ALPHA;
import static com.example.arkscreen.Utils.ConfigUtils.DEFAULT_HEIGHT;
import static com.example.arkscreen.Utils.ConfigUtils.DEFAULT_WIDTH;
import static com.example.arkscreen.Utils.ConfigUtils.SHOW_MODE_DEFAULT;
import static com.example.arkscreen.Utils.ConfigUtils.SHOW_MODE_SIMPLE;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_AMIYA;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_KALT;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_PTILO;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_ROSMO;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_SKADI;
import static com.example.arkscreen.Utils.ConfigUtils.getShared;
import static com.example.arkscreen.Utils.ConfigUtils.initialProperTies;
import static com.example.arkscreen.Utils.ConfigUtils.resetConfig;
import static com.example.arkscreen.Utils.ConfigUtils.setProperTiesAlpha;
import static com.example.arkscreen.Utils.ConfigUtils.setProperTiesHeight;
import static com.example.arkscreen.Utils.ConfigUtils.setProperTiesMode;
import static com.example.arkscreen.Utils.ConfigUtils.setProperTiesTheme;
import static com.example.arkscreen.Utils.ConfigUtils.setProperTiesWidth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.arkscreen.R;
import com.example.arkscreen.Utils.ConfigUtils;

public class SettingActivity extends Activity implements View.OnClickListener {

    private TextView text_float;
    private TextView text_show_mode;
    private TextView text_theme;
    private TextView text_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setLayout();
        super.onCreate(savedInstanceState);

    }
    private void setLayout(){
        initialProperTies(this);
        setContentView(R.layout.activity_setting);
        CardView card_float = findViewById(R.id.card_float_setting);
        CardView card_show_mode = findViewById(R.id.card_show_mode_setting);

        text_float = findViewById(R.id.text_float_config);
        text_show_mode = findViewById(R.id.text_show_mode_config);
        text_reset = findViewById(R.id.text_reset);

        card_float.setOnClickListener(this);
        card_show_mode.setOnClickListener(this);
        text_reset.setOnClickListener(this);

        setConfigText();
    }
//    @Override
//    protected void onRestart() {
//
//        super.onRestart();
//        setLayout();
//    }
    private void setConfigText() {
        SharedPreferences shared = ConfigUtils.getShared(SettingActivity.this);
        int width = shared.getInt("floatWindowWidth", DEFAULT_WIDTH);
        int height = shared.getInt("floatWindowHeight", DEFAULT_HEIGHT);
        float alpha = shared.getFloat("floatWindowAlpha", DEFAULT_ALPHA);
        String text = "宽度:" + width + "   " + "高度:" + height + "   " + "透明度:" + alpha;
        text_float.setText(text);
        switch (shared.getInt("showMode", SHOW_MODE_DEFAULT)) {
            case SHOW_MODE_DEFAULT:
                text = "默认";
                break;
            case SHOW_MODE_SIMPLE:
                text = "简洁";
                break;
            default:
                break;
        }
        text_show_mode.setText(text);
    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        if (getId == R.id.card_float_setting) {
            showDialogFloatW(this.getResources().getText(R.string.float_window));
        } else if (getId == R.id.card_show_mode_setting) {
            showDialogMode(this.getResources().getText(R.string.show_mode));
        } else if (getId == R.id.text_reset) {
            resetConfig(this);
            initialProperTies(this);
            setConfigText();
        }
    }

    private void showDialogFloatW(CharSequence title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        View view = LayoutInflater.from(SettingActivity.this)
                .inflate(R.layout.dialog_float_w, null);
        RadioGroup radioGroupW = view.findViewById(R.id.radio_group_w);
        RadioButton rb1W = view.findViewById(R.id.rb_w_600);
        RadioButton rb2W = view.findViewById(R.id.rb_w_800);
        RadioButton rb3W = view.findViewById(R.id.rb_w_1000);

        RadioGroup radioGroupH = view.findViewById(R.id.radio_group_h);
        RadioButton rb1H = view.findViewById(R.id.rb_h_600);
        RadioButton rb2H = view.findViewById(R.id.rb_h_800);
        RadioButton rb3H = view.findViewById(R.id.rb_h_1000);

        SeekBar seekBar = view.findViewById(R.id.seekBar);

        SharedPreferences shared = ConfigUtils.getShared(SettingActivity.this);
        int width = shared.getInt("floatWindowWidth", DEFAULT_WIDTH);
        int height = shared.getInt("floatWindowHeight", DEFAULT_HEIGHT);
        float alpha = shared.getFloat("floatWindowAlpha", DEFAULT_ALPHA);

        switch (width) {
            case 600:
                rb1W.setChecked(true);
                break;
            case 800:
                rb2W.setChecked(true);
                break;
            case 1000:
                rb3W.setChecked(true);
                break;
            default:
                break;
        }
        switch (height) {
            case 600:
                rb1H.setChecked(true);
                break;
            case 800:
                rb2H.setChecked(true);
                break;
            case 1000:
                rb3H.setChecked(true);
                break;
            default:
                break;
        }
        seekBar.setProgress((int) (alpha * 10));

        radioGroupW.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_w_600:
                        rb1W.setChecked(true);
                        setProperTiesWidth(SettingActivity.this, 600);
                        break;
                    case R.id.rb_w_800:
                        rb2W.setChecked(true);
                        setProperTiesWidth(SettingActivity.this, 800);
                        break;
                    case R.id.rb_w_1000:
                        rb3W.setChecked(true);
                        setProperTiesWidth(SettingActivity.this, 1000);
                    default:
                        break;
                }
                initialProperTies(SettingActivity.this);
                setConfigText();
            }
        });
        radioGroupH.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_h_600:
                        rb1H.setChecked(true);
                        setProperTiesHeight(SettingActivity.this, 600);
                        break;
                    case R.id.rb_h_800:
                        rb2H.setChecked(true);
                        setProperTiesHeight(SettingActivity.this, 800);
                        break;
                    case R.id.rb_h_1000:
                        rb3H.setChecked(true);
                        setProperTiesHeight(SettingActivity.this, 1000);
                    default:
                        break;
                }
                initialProperTies(SettingActivity.this);
                setConfigText();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                int nAlpha = seekBar.getProgress();
                setProperTiesAlpha(SettingActivity.this, (float) nAlpha / 10);
                initialProperTies(SettingActivity.this);
                setConfigText();
            }
        });
        builder.setTitle(title).setView(view).show();

    }

    private void showDialogMode(CharSequence title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        View view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.dialog_mode, null);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        RadioButton rb1 = view.findViewById(R.id.rb_default);
        RadioButton rb2 = view.findViewById(R.id.rb_simple);
        SharedPreferences shared = ConfigUtils.getShared(SettingActivity.this);
        int userMode = shared.getInt("showMode", SHOW_MODE_DEFAULT);
        switch (userMode) {
            case SHOW_MODE_DEFAULT:
                rb1.setChecked(true);
                break;
            case SHOW_MODE_SIMPLE:
                rb2.setChecked(true);
                break;
            default:
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_default:
                        rb1.setChecked(true);
                        setProperTiesMode(SettingActivity.this,
                                SHOW_MODE_DEFAULT);
                        break;
                    case R.id.rb_simple:
                        rb2.setChecked(true);
                        setProperTiesMode(SettingActivity.this,
                                SHOW_MODE_SIMPLE);
                        break;
                    default:
                        break;
                }
                initialProperTies(SettingActivity.this);
                setConfigText();
            }
        });

//        Markwon markwon = Markwon.create(this);
//        markwon.setMarkdown(textView,content.toString());
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setTitle(title).setView(view).show();
    }
}
