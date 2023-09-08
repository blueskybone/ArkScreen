package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.HttpConnectionUtils.getResponse;
import static com.godot17.arksc.utils.Utils.getAppVersionName;
import static com.godot17.arksc.utils.Utils.showToast;

import android.app.Activity;
import android.content.DialogInterface;
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
        CardView card_web = findViewById(R.id.card_web);
        CardView card_other = findViewById(R.id.card_other);

        card_update.setOnClickListener(this);
        card_3rd.setOnClickListener(this);
        card_web.setOnClickListener(this);
        card_other.setOnClickListener(this);
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

        } else if (id == R.id.card_web) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri uri = Uri.parse(getString(R.string.repositories));
            intent.setData(uri);
            startActivity(intent);
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
        URL url = new URL("https://gitee.com/blueskybone/ArkScreen/raw/master/version.info");
        InputStream is = getResponse(url);
        if (is == null) {
            showToast(this, "解析更新地址错误.");
            return;
        }
        String version = null;
        String link = null;
        String content = null;
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(is, "utf-8");
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (xmlPullParser.getName()) {
                        case "version":
                            xmlPullParser.next();
                            version = xmlPullParser.getText();
                            Log.e(TAG, "version " + version);
                            break;
                        case "link":
                            xmlPullParser.next();
                            link = xmlPullParser.getText();
                            Log.e(TAG, "link " + link);
                            break;
                        case "content":
                            xmlPullParser.next();
                            content = xmlPullParser.getText();
                            Log.e(TAG, "content " + content);
                            break;
                        default:
                            Log.e(TAG, "GETNOTHING");
                            break;
                    }
                }
                eventType = xmlPullParser.next();
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (version == null || link == null || content == null) {
            showToast(this, "解析xml错误.");
            return;
        }
        String oldVersion = getAppVersionName(this);
        if (oldVersion.compareTo(version) < 0) {
            String finalContent = content;
            String finalLink = link;
            mHandler.post(() -> {
                showUpdateDiaLog(finalContent, "检测到新版本", finalLink);
            });
            return;
        }
        showToast(this, "当前已是最新");
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
