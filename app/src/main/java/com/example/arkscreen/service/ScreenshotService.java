package com.example.arkscreen.service;

import static com.example.arkscreen.Utils.ConfigUtils.SHOW_MODE_SIMPLE;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import com.example.arkscreen.R;
import com.example.arkscreen.Utils.ConfigUtils;
import com.example.arkscreen.Utils.OpeData;
import com.example.arkscreen.Utils.ScreenCapture;
import com.example.arkscreen.Utils.ScreenUtils;
import com.example.arkscreen.adapter.OpeListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenshotService extends Service {
    private int mResultCode;
    private Intent mResultData;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private String CHANNEL_ID = "CHANNEL_CAPTURE";
    private int notificationId = 100;

    private ConstraintLayout touchLayout;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private Handler mHandler;
    private TextView button;
    private TextView tagsResult;
    private TextView captureResultInfo;
    private TextView noHighLevelResult;
    private TextView simpleText;
    private ListView lvOpeResult;
    private OpeListAdapter opeListAdapter;
    private ExecutorService executorService;
    private List<OpeData> opeDataList = new ArrayList<>();
    private boolean isAdd = false;
    int statusBarHeight = -1;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void setParamsConfig(){
        SharedPreferences shared = ConfigUtils.getShared(this);
        shared.getInt("floatWindowWidth", SHOW_MODE_SIMPLE);
        params.alpha = shared.getFloat("floatWindowAlpha",ConfigUtils.DEFAULT_ALPHA);
        params.width = shared.getInt("floatWindowWidth",ConfigUtils.DEFAULT_WIDTH);
        params.height = shared.getInt("floatWindowHeight",ConfigUtils.DEFAULT_HEIGHT);
    }

    @Override
    public void onCreate(){
//        params = new WindowManager.LayoutParams();
//        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
//        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        params.format = PixelFormat.RGBA_8888;
//        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        params.gravity = Gravity.END| Gravity.TOP;
//        params.x = 50;
//        params.y = 100;
//        setParamsConfig();
//
//        executorService = Executors.newFixedThreadPool(10);
//
//        LayoutInflater inflater = LayoutInflater.from(getApplication());
//
//        touchLayout = (ConstraintLayout) inflater.inflate(R.layout.window_float,null);
//        lvOpeResult = touchLayout.findViewById(R.id.lv_operator);
//        tagsResult = touchLayout.findViewById(R.id.text_tags);
//        captureResultInfo = touchLayout.findViewById(R.id.text_result_info);
//        captureResultInfo.setText("testtesttesttest");
//        noHighLevelResult = touchLayout.findViewById(R.id.text_no_high_level_Info);
//        simpleText = touchLayout.findViewById(R.id.simple_result);
//        button = touchLayout.findViewById(R.id.close_text);
//        opeListAdapter = new OpeListAdapter(this,opeDataList);
//        lvOpeResult.setAdapter(opeListAdapter);
//
//        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
//        if (resourceId > 0) {
//            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
//        }
//
//        button.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                touchLayout.performClick();
//                windowManager.removeView(touchLayout);
//                isAdd = false;
//            }
//        });
//
//        AtomicInteger LastX = new AtomicInteger();
//        AtomicInteger LastY = new AtomicInteger();
//        touchLayout.setOnTouchListener((v, event) ->  {
//            int action = event.getAction();
//            int mWidth = ScreenUtils.getRealWidth(this);
//            int mHeight = ScreenUtils.getRealHeight(this);
//            switch (action){
//                case MotionEvent.ACTION_DOWN:
//                    LastX.set((int) event.getX());
//                    LastY.set((int) event.getY());
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    if(mWidth > mHeight){
//                        params.x = (mWidth - (int)event.getRawX())-(v.getWidth() - LastX.get());
//                        params.y = (int)event.getRawY() - LastY.get();
//                        windowManager.updateViewLayout(touchLayout,params);
//                    }else {
//                        params.x = (mWidth - (int)event.getRawX())-(v.getWidth() - LastX.get());
//                        params.y = (int)event.getRawY() - statusBarHeight - LastY.get();
//                        windowManager.updateViewLayout(touchLayout,params);
//                    }
//            }
//            return false;
//        });
//
//        isAdd = false;
//
        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
    }

    public void showFloatWindow() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!isAdd){
                    windowManager.addView(touchLayout,params);
                    isAdd = true;
                }
            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("scservice","scservice");
        createNotificationChannel();
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        mProjectionManager = (MediaProjectionManager)getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mProjectionManager.getMediaProjection(
                mResultCode, Objects.requireNonNull(mResultData));
        if(mMediaProjection!=null) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new ScreenCapture(this, mMediaProjection).startProjection();
            //showFloatWindow();
        }
        else {
            Toast.makeText(this,"error in Mediaprojection",Toast.LENGTH_SHORT).show();
        }
        stopFore();
        //stopForeground(true);
        //return START_REDELIVER_INTENT;
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        String textTitle = getString(R.string.channel_name);
        String textContent = getString(R.string.channel_description);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tile)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTimeoutAfter(3000)
                .setAutoCancel(true);
        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        startForeground(notificationId, builder.build());
    }

    private void stopFore(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                   stopForeground(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
    stopForeground(true);
    super.onDestroy();
    }
}
