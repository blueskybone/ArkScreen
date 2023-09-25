package com.godot17.arksc.activity;

import static com.godot17.arksc.utils.HttpConnectionUtils.downloadToLocal;
import static com.godot17.arksc.utils.HttpConnectionUtils.getResponse;
import static com.godot17.arksc.utils.HttpConnectionUtils.isNetConnected;
import static com.godot17.arksc.utils.NetWorkTask.getUpdateInfo;
import static com.godot17.arksc.utils.PrefManager.getUserInfo;
import static com.godot17.arksc.utils.Utils.getAssets2CacheDir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.godot17.arksc.R;
import com.godot17.arksc.datautils.UpdateInfo;
import com.godot17.arksc.service.DataQueryService;
import com.godot17.arksc.service.FloatTileService;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import io.noties.markwon.Markwon;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private final int OVERLAY_PERMISSION_REQUEST_CODE = 200;
    private final String TAG = "HomeActivity";
    private StringBuilder stringBuilder = new StringBuilder("");
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        requestFloatWinPermission(this);
        requestNotificationPermission();
        initialData();
        startService();
        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
        try {
            printLog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void setLayout() {
        setContentView(R.layout.activity_home);

        CardView card_cal = findViewById(R.id.card_cal);
        CardView card_skland = findViewById(R.id.card_skland);
        CardView card_use = findViewById(R.id.card_use);
        CardView card_question = findViewById(R.id.card_question);
        CardView card_setting = findViewById(R.id.card_setting);
        CardView card_about = findViewById(R.id.card_about);



        card_cal.setOnClickListener(this);
        card_skland.setOnClickListener(this);
        card_use.setOnClickListener(this);
        card_question.setOnClickListener(this);
        card_setting.setOnClickListener(this);
        card_about.setOnClickListener(this);
    }

    private void initialData() {
        getAssets2CacheDir(this, "target_std.dat");
        getAssets2CacheDir(this, "opedata.json");
        getAssets2CacheDir(this, "en_cn.json");
    }

    private void printLog() throws IOException {
        if (!isNetConnected(this)) {
            updateLog("无网络连接");
            return;
        }
        mHandler.post(()->{
            try {
                checkAppVersion();
                checkDatabaseVersion();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
//        new Thread(() -> {
//
//        }).start();
    }

    private void updateLog(String info) {
        stringBuilder.append(info);
        TextView text_log = findViewById(R.id.textView_log);
        text_log.post(() -> {
            text_log.setText(stringBuilder.toString());
            Markwon.create(this).setMarkdown(text_log, stringBuilder.toString());
        });
    }

    private void checkDatabaseVersion() throws MalformedURLException {

        TextView textViewDBUpdate = findViewById(R.id.text_cal_update);
        URL url = new URL("https://gitee.com/blueskybone/ArkScreen/raw/master/recruit_version.info");
        InputStream is = getResponse(url);
        if (is == null) {
            updateLog("数据库更新地址解析错误.\n\n");
            return;
        }
        String version = null;
        String link = null;
        String update = null;
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
                        case "update":
                            xmlPullParser.next();
                            update = xmlPullParser.getText();
                            Log.e(TAG, "update " + update);
                            break;
                        default:
                            Log.e(TAG, "getNothing");
                            break;
                    }
                }
                eventType = xmlPullParser.next();
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (version == null || link == null || update == null) {
            updateLog("解析数据库xml发生错误.\n\n");
            return;
        }
        String finalUpdate = "最后更新：" + update;
        textViewDBUpdate.post(()-> textViewDBUpdate.setText(finalUpdate));
        DataQueryService dataQueryService = DataQueryService.getInstance();
        if (dataQueryService == null) {
            updateLog("数据库初始化失败\n\n");
            return;
        }
        String oldVersion = dataQueryService.getVersion();
        if (oldVersion.compareTo(version) < 0) {
            updateLog("检测到公招池更新，正在更新数据库...\n\n");
            try {
                downloadToLocal(getExternalCacheDir() + "/opedata.json", link);
                dataQueryService.initial();
                Log.e(TAG, dataQueryService.getDate());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            String newOpe = dataQueryService.getNewOpe().getName().toString();
            updateLog("更新完成，公招池最后更新时间：" + update + "\n\n" + "新增干员：" + newOpe + "\n\n");
        }
    }

    private void checkAppVersion() throws MalformedURLException {

        if(getUpdateInfo(this)){
            updateLog("检测到应用新版本, [下载链接](" + UpdateInfo.link + ")\n\n");
            showUpdateDiaLog(UpdateInfo.content, UpdateInfo.link);
            //showDialog.
        }
    }

    private void requestFloatWinPermission(Context context) {
        if (Settings.canDrawOverlays(this)) return;
        ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (!Settings.canDrawOverlays(context)) {
                    Toast.makeText(context, R.string.no_permission_for_float_window, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, R.string.no_permission_for_float_window, Toast.LENGTH_SHORT).show();
            }
        });
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentActivityResultLauncher.launch(intent);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView textViewUserInfo = findViewById(R.id.text_userInfo_home);
        textViewUserInfo.setText(getUserInfo(this));
    }

    private void requestNotificationPermission() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.card_cal) {
            startActivity(new Intent(this, CalActivity.class));
        } else if (id == R.id.card_skland) {
            startActivity(new Intent(this, SklandActivity.class));
        } else if (id == R.id.card_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if (id == R.id.card_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.card_use) {
            showDialog(getString(R.string.content_use), getString(R.string.title_use));
        } else if (id == R.id.card_question) {
            showDialog(getString(R.string.content_query), getString(R.string.title_question));
        }
    }

    private void showDialog(String content, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_normal, null);
        TextView textView = view.findViewById(R.id.textView_dialog);
        Markwon.create(this).setMarkdown(textView, content);
        builder.setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.identify, (dialog, which) -> { })
                .create()
                .show();
    }

    private void showUpdateDiaLog(String content, String link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_normal, null);
        TextView textView = view.findViewById(R.id.textView_dialog);
        Markwon.create(this).setMarkdown(textView, content);
        builder.setView(view)
                .setTitle("检测到新版本")
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

    @Override
    protected void onDestroy() {
        FloatTileService floatTileService = FloatTileService.getInstance();
        if (floatTileService != null) {
            floatTileService.getQsTile().setState(Tile.STATE_INACTIVE);
            floatTileService.getQsTile().updateTile();
        }
        super.onDestroy();
    }

    private void startService() {
        this.startService(new Intent(this, DataQueryService.class));
    }
}
