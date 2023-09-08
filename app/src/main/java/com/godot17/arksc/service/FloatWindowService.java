package com.godot17.arksc.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.godot17.arksc.R;
import com.godot17.arksc.utils.PrefManager;
import com.godot17.arksc.utils.ScreenUtils;

import java.util.concurrent.atomic.AtomicInteger;

import io.noties.markwon.Markwon;

public class FloatWindowService extends Service {
    private final String TAG = "FloatWindowService";
    private static FloatWindowService instance;
    private ConstraintLayout touchLayout;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private Handler mHandler;
    private boolean isAdd;

    private TextView tagsTextView;
    private TextView infoTextView;
    private TextView opeListTextView;

    private int statusBarHeight = -1;

    public static FloatWindowService getInstance(){
        return instance;
    }
    private void setParamsConfig() {
        SharedPreferences shared = PrefManager.getShared(this);
        params.alpha = shared.getFloat("floatWindowAlpha", PrefManager.DEFAULT_ALPHA);
        params.width = shared.getInt("floatWindowWidth", PrefManager.DEFAULT_WIDTH);
        params.height = shared.getInt("floatWindowHeight", PrefManager.DEFAULT_HEIGHT);
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    private void createTouch() {
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 50;
        params.y = 150;
        setParamsConfig();

        LayoutInflater inflater = LayoutInflater.from(getApplication());

        touchLayout = (ConstraintLayout) inflater.inflate(R.layout.window_float, null);

        TextView button = touchLayout.findViewById(R.id.textView_close);

        tagsTextView = touchLayout.findViewById(R.id.textView_tags);
        infoTextView = touchLayout.findViewById(R.id.textView_info);
        opeListTextView = touchLayout.findViewById(R.id.textView_list);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        button.setOnClickListener(v -> {
            touchLayout.performClick();
            windowManager.removeView(touchLayout);
            isAdd = false;
        });

        AtomicInteger LastX = new AtomicInteger();
        AtomicInteger LastY = new AtomicInteger();
        touchLayout.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            int mWidth = ScreenUtils.getRealWidth(this);
            int mHeight = ScreenUtils.getRealHeight(this);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    LastX.set((int) event.getX());
                    LastY.set((int) event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mWidth > mHeight) {
                        params.x = (int) event.getRawX() - LastX.get() - statusBarHeight;
                        //params.x = (mWidth - (int) event.getRawX()) - (v.getWidth() - LastX.get());
                        params.y = (int) event.getRawY() - LastY.get();
                        windowManager.updateViewLayout(touchLayout, params);
                    } else {
                        params.x = (int) event.getRawX() - LastX.get();
                        //params.x = (mWidth - (int) event.getRawX()) - (v.getWidth() - LastX.get());
                        params.y = (int) event.getRawY() - statusBarHeight - LastY.get();
                        windowManager.updateViewLayout(touchLayout, params);
                    }
            }
            return false;
        });
        isAdd = false;
    }

    public void openAndUpdateFloatWindow(String tags, String info, String list) {
        mHandler.post(() -> {
            tagsTextView.setText(tags);
            infoTextView.setText(info);
            Markwon.create(this).setMarkdown(opeListTextView, list);
        });
        showFloatWindow();
    }

    private void showFloatWindow() {
        mHandler.post(() -> {
            if (!isAdd) {
                windowManager.addView(touchLayout, params);
                isAdd = true;
            } else {
                windowManager.updateViewLayout(touchLayout, params);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onstartCommand");
//        String tagsText = intent.getStringExtra("tagsText");
//        String infoText = intent.getStringExtra("infoText");
//        String listText = intent.getStringExtra("listText");

        new Thread(() -> {
            while (mHandler == null) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //openAndUpdateFloatWindow(tagsText, infoText, listText);
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createTouch();
        instance = this;

        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isAdd) {
            windowManager.removeView(touchLayout);
        }
        stopForeground(true);
    }

}
