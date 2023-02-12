package com.example.arkscreen.service;

import static com.example.arkscreen.Utils.ConfigUtils.SHOW_MODE_DEFAULT;
import static com.example.arkscreen.Utils.ConfigUtils.SHOW_MODE_SIMPLE;
import static com.example.arkscreen.Utils.DataSearchUtils.getFinalList;
import static com.example.arkscreen.Utils.DataSearchUtils.splitTag;
import static com.example.arkscreen.Utils.DataSearchUtils.tagsEnToCh;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import com.example.arkscreen.R;
import com.example.arkscreen.Utils.ConfigUtils;
import com.example.arkscreen.Utils.OpeData;
import com.example.arkscreen.Utils.ScreenUtils;
import com.example.arkscreen.Utils.Utils;
import com.example.arkscreen.adapter.OpeListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.noties.markwon.Markwon;

public class ResultWindowService extends Service {
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
    private String CHANNEL_ID = "CHANNEL_SERVICE";
    private int notificationId = 102;
    private boolean isAdd = false;
    int statusBarHeight = -1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(isAdd){
            removeFloatWindow();
        }
        String result_data = intent.getStringExtra("result_data");
        Log.e("result",result_data);
        String[] data=result_data.split(",");

        if(data.length == 1) {
            printErrInfo(this);
        }
        else if(data.length == 2){
            printScrErrInfo(this,data[1]);
        }
        else {
            printOpeList(this,data);
        }
        return super.onStartCommand(intent ,flags, startId);
       // return START_REDELIVER_INTENT;
    }
    @Override
    public void onCreate(){
        createTouch();
    }

    private void setParamsConfig(){
        SharedPreferences shared = ConfigUtils.getShared(this);
        shared.getInt("floatWindowWidth", SHOW_MODE_SIMPLE);
        params.alpha = shared.getFloat("floatWindowAlpha",ConfigUtils.DEFAULT_ALPHA);
        params.width = shared.getInt("floatWindowWidth",ConfigUtils.DEFAULT_WIDTH);
        params.height = shared.getInt("floatWindowHeight",ConfigUtils.DEFAULT_HEIGHT);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void createTouch(){
        Log.e("create touch","create touchlayout");
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.END| Gravity.TOP;
        params.x = 50;
        params.y = 100;
        setParamsConfig();

        executorService = Executors.newFixedThreadPool(10);

        LayoutInflater inflater = LayoutInflater.from(getApplication());

        touchLayout = (ConstraintLayout) inflater.inflate(R.layout.window_float,null);
        lvOpeResult = touchLayout.findViewById(R.id.lv_operator);
        tagsResult = touchLayout.findViewById(R.id.text_tags);
        captureResultInfo = touchLayout.findViewById(R.id.text_result_info);
        noHighLevelResult = touchLayout.findViewById(R.id.text_no_high_level_Info);
        simpleText = touchLayout.findViewById(R.id.simple_result);
        button = touchLayout.findViewById(R.id.close_text);
        opeListAdapter = new OpeListAdapter(this,opeDataList);
        lvOpeResult.setAdapter(opeListAdapter);

        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                touchLayout.performClick();
                windowManager.removeView(touchLayout);
                isAdd = false;
                //stopSelf();
            }
        });

        AtomicInteger LastX = new AtomicInteger();
        AtomicInteger LastY = new AtomicInteger();
        touchLayout.setOnTouchListener((v, event) ->  {
            int action = event.getAction();
            int mWidth = ScreenUtils.getRealWidth(this);
            int mHeight = ScreenUtils.getRealHeight(this);
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                       LastX.set((int) event.getX());
                       LastY.set((int) event.getY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(mWidth > mHeight){
                            params.x = (mWidth - (int)event.getRawX())-(v.getWidth() - LastX.get());
                            params.y = (int)event.getRawY() - LastY.get();
                            windowManager.updateViewLayout(touchLayout,params);
                        }else {
                            params.x = (mWidth - (int)event.getRawX())-(v.getWidth() - LastX.get());
                            params.y = (int)event.getRawY() - statusBarHeight - LastY.get();
                            windowManager.updateViewLayout(touchLayout,params);
                        }
                }
            return false;
        });

        isAdd = false;

        new Thread(() -> {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }).start();
    }

    @Override
    public void onDestroy() {
        if(isAdd) {
            windowManager.removeView(touchLayout);
        }
        stopForeground(true);
        super.onDestroy();
    }

    private void printErrInfo(Context context){
        executorService.submit(new Runnable() {
            @Override
            public void run(){
//                String[] dataTest = {"90_高级资深干员","08_治疗","50_狙击干员","17_削弱","07_生存"};
//                opeDataList.addAll(getFinalList(dataTest,context));
               // noHighLevelResult.setHeight(0);

//                SharedPreferences shared = ConfigUtils.getShared(context);
//                shared.getInt("showMode",ConfigUtils.SHOW_MODE_DEFAULT);
//
//                switch (ConfigUtils.SHOW_MODE){
//                    case ConfigUtils.SHOW_MODE_DEFAULT:
//                        simpleText.setText("");
//                        opeListAdapter = new OpeListAdapter(context,opeDataList);
//                        break;
//                    case SHOW_MODE_SIMPLE:
//                        String text = Utils.getMarkDownText(opeDataList);
//                        Markwon markwon = Markwon.create(context);
//                        markwon.setMarkdown(simpleText,text);
//                        break;
//                    default:break;
//                }
                setViewEmpty(context);
                setParamsConfig();
                showFloatWindow();
            }
        });
    }
    private void setViewEmpty(Context context){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                simpleText.setText("");
                noHighLevelResult.setText("");
                tagsResult.setText(context.getResources().getText(R.string.info_default));
                captureResultInfo.setText(context.getResources().getText(R.string.get_wrong_tag_number));
                opeDataList.clear();
                opeListAdapter = null;
                lvOpeResult.setAdapter(null);
            }
        });
    }

private void printScrErrInfo(Context context,String info){
    executorService.submit(new Runnable() {
        @Override
        public void run(){
            opeDataList.clear();
            opeListAdapter = null;
            lvOpeResult.setAdapter(null);

            initialTextErr(context, info);

            setParamsConfig();
            showFloatWindow();
        }
    });

}

    private void initialTextErr(Context context,String info){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                captureResultInfo.setText(info);
                tagsResult.setText(context.getResources().getText(R.string.info_default));
                noHighLevelResult.setText("");
                simpleText.setText("");
            }
        });
    }

    private void initialText(Context context,String tags){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                captureResultInfo.setText(context.getResources().getText(R.string.get_right_result));
                tagsResult.setText(tags.toString());
                noHighLevelResult.setText("");
                simpleText.setText("");
            }
        });
    }

    private void setMarkDownText(Context context, String text){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Markwon markwon = Markwon.create(context);
                markwon.setMarkdown(simpleText,text);
            }
        });
    }

    private void printOpeList(Context context, String[] data){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String[] newData = tagsEnToCh(data,context);
                String[] cutData = splitTag(newData);

                StringBuilder tags = new StringBuilder();
                for (String cutDatum : cutData) {
                    tags.append(cutDatum).append(" ");
                }
                initialText(context, tags.toString());

                opeDataList.clear();
                opeDataList.addAll(getFinalList(newData,context));

                opeListAdapter = null;

                if( 0 == opeDataList.size()){
                    noHighLevelResult.setText(context.getResources().getText(R.string.no_high_level));
                    lvOpeResult.setAdapter(null);
                }
                else {
                    SharedPreferences shared = ConfigUtils.getShared(context);
                    int userMode = shared.getInt("showMode", SHOW_MODE_DEFAULT);
                    switch (userMode){
                        case SHOW_MODE_DEFAULT:
                            opeListAdapter = new OpeListAdapter(context,opeDataList);
                            break;
                        case SHOW_MODE_SIMPLE:
                            String text = Utils.getMarkDownText(opeDataList);
                            setMarkDownText(context,text);
                            break;
                        default:break;
                    }
                }
                lvOpeResult.setAdapter(opeListAdapter);
                setParamsConfig();
                showFloatWindow();
            }
        });
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
    public void removeFloatWindow(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(isAdd){
                    windowManager.removeView(touchLayout);
                    isAdd = false;
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    }