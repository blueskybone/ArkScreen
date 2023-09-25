package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.HttpConnectionUtils.getResponse;
import static com.godot17.arksc.utils.NetWorkTask.getUpdateInfo;
import static com.godot17.arksc.utils.Utils.getAppVersionName;
import static com.godot17.arksc.utils.Utils.showToast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.godot17.arksc.R;
import com.godot17.arksc.datautils.UpdateInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import io.noties.markwon.Markwon;

public class AboutActivity extends Activity implements View.OnClickListener {
    private final String TAG = "AboutActivity";
    private Handler mHandler;

    private void setLayout() {
        setContentView(R.layout.activity_about);
        CardView card_update = findViewById(R.id.card_update);
        CardView card_3rd = findViewById(R.id.card_3rd);
        CardView card_other = findViewById(R.id.card_other);

        card_update.setOnClickListener(this);
        card_3rd.setOnClickListener(this);
        card_other.setOnClickListener(this);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.card_update) {
            new Thread(() -> {
                try {
                    checkAppVersion();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }).start();
        } else if (id == R.id.card_3rd) {
            showDialog(getString(R.string.open_licence_title),
                    getString(R.string.open_source_license));

        } else if (id == R.id.card_other) {
            showDialog(getString(R.string.title_other), getString(R.string.info_other));
        }
    }

    private void showDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_normal, null);
        TextView textView = view.findViewById(R.id.textView_dialog);
        if (textView == null) {
            Log.e(TAG, "textview == null");
            return;
        }
        Markwon.create(this).setMarkdown(textView, content);
        builder.setView(view)
                .setTitle(title)
                .setNegativeButton(R.string.identify, (dialog, which) -> {

                })
                .create()
                .show();
    }

    private void checkAppVersion() throws MalformedURLException {

        if (getUpdateInfo(this)) {
            mHandler.post(() -> showUpdateDiaLog(UpdateInfo.content, "检测到新版本", UpdateInfo.link));
        } else {
            showToast(this, "当前已是最新");
        }
    }

    private void showUpdateDiaLog(String content, String title, String link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_normal, null);
        TextView textView = view.findViewById(R.id.textView_dialog);
        Markwon.create(this).setMarkdown(textView, content);
        builder.setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.download, (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .create().
                show();

    }
}
