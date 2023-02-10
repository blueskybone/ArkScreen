package com.example.arkscreen.activity;

import static com.example.arkscreen.Utils.ConfigUtils.THEME_AMIYA;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_KALT;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_PTILO;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_ROSMO;
import static com.example.arkscreen.Utils.ConfigUtils.THEME_SKADI;
import static com.example.arkscreen.Utils.ConfigUtils.getShared;
import static com.example.arkscreen.Utils.ConfigUtils.initialProperTies;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.arkscreen.R;

import io.noties.markwon.Markwon;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String CHANNEL_ID = "CHANNEL_CAPTURE";
    private final int OVERLAY_PERMISSION_REQUEST_CODE = 99;
    private Bundle savedInstanceState;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        setLayout();

        createNotificationChannel();
        getFloatWindowPermission(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "未授权悬浮窗权限",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setLayout();
    }
    private void setLayout(){
        initialProperTies(this);
        setContentView(R.layout.activity_main);
        CardView card_op =findViewById(R.id.card_operator);
        CardView card_instruct = findViewById(R.id.card_instruct);
        CardView card_check_update =findViewById(R.id.card_check_update);
        CardView card_about = findViewById(R.id.card_about);
        CardView card_setting = findViewById(R.id.card_setting);

        card_op.setOnClickListener(this);
        card_instruct.setOnClickListener(this);
        card_check_update.setOnClickListener(this);
        card_about.setOnClickListener(this);
        card_setting.setOnClickListener(this);
    }
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void getFloatWindowPermission(Bundle savedInstanceState){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            if(!Settings.canDrawOverlays(this)){
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE,savedInstanceState);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showDialog(CharSequence title, CharSequence content)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        View view= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog,null);
        TextView textView = view.findViewById(R.id.text_dialog);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(textView,content.toString());
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setTitle(title).setView(view).show();
    }
    @Override
    public void onClick(View v) {
        int getId = v.getId();
        if(getId == R.id.card_operator){
            Toast.makeText(this,"施工中...",Toast.LENGTH_SHORT).show();
        }
        else if(getId == R.id.card_instruct){
            showDialog(this.getResources().getText(R.string.instruct_use),
                    this.getResources().getText(R.string.instruct_desc));
        }
        else if(getId == R.id.card_check_update){
            Toast.makeText(this,"施工中...",Toast.LENGTH_SHORT).show();
           // showAbout();
        }
        else if(getId == R.id.card_setting){
            startActivityForResult(new Intent(this, SettingActivity.class)
                    ,OVERLAY_PERMISSION_REQUEST_CODE
                    ,MainActivity.this.savedInstanceState);
        }
        else if(getId == R.id.card_about){
            showDialog(this.getResources().getText(R.string.about_title),
                    this.getResources().getText(R.string.about_content));
        }
    }
}
