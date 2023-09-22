package com.godot17.arksc.service;

import static com.godot17.arksc.utils.Utils.generateTagsText;
import static com.godot17.arksc.utils.Utils.getMarkDownText;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.godot17.arksc.datautils.FinalOpeList;
import com.godot17.arksc.datautils.OpeGroup;
import com.godot17.arksc.utils.PrefManager;

import java.util.List;

public class DataProcessService extends Service {
    private final String TAG = "dataProcessService";
    private static DataProcessService instance;
    private DataQueryService dataQueryService;
    private FinalOpeList finalOpeList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static DataProcessService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        dataQueryService = DataQueryService.getInstance();
        if (dataQueryService == null) {
            Context context = getApplicationContext();
            context.startService(new Intent(context, DataQueryService.class));
            dataQueryService = DataQueryService.getInstance();
        }
        if (dataQueryService == null) {
            //Toast.makeText(this, "can't startServiceDataQuery", Toast.LENGTH_SHORT).show();
        }

        String tagSort = intent.getStringExtra("TAG_SORT");
        String tagText = intent.getStringExtra("TAG_TEXT");

        if (tagSort.equals("RECRUIT")) {
            String[] data = tagText.split("_");
            finalOpeList = dataQueryService.getOpeListFromTile(data);
            int star = finalOpeList.getStar();
            String showMode = getShowMode();
            if (showMode.equals("FLOAT_WIN")) {
                String tagTextFW = generateTagsText(finalOpeList.tags);
                String infoTextFW;
                if (finalOpeList.opeGroups.size() == 0) infoTextFW = "无高稀有度组合";
                else infoTextFW = "可锁" + star + "★";
                String listTextFW = getMarkDownText(finalOpeList.opeGroups);
                FloatWindowService floatWindowService = FloatWindowService.getInstance();
                floatWindowService.openAndUpdateFloatWindow(tagTextFW, infoTextFW, listTextFW);

            } else if (showMode.equals("TOAST_MSG")) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(generateTagsText(finalOpeList.tags)).append("\n");
                if (finalOpeList.opeGroups.size() == 0) stringBuilder.append("无高稀有度组合");
                else {
                    OpeGroup opeGroup = finalOpeList.opeGroups.get(0);
                    List<String> tags = opeGroup.tags;
                    String[] arr = new String[tags.size()];
                    String opeName = opeGroup.operators.get(0).getName();
                    stringBuilder.append("可锁").append(star).append("★")
                            .append(generateTagsText(tags.toArray(arr)))
                            .append("->")
                            .append(opeName);
                }
                Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Wrong with showMode", Toast.LENGTH_SHORT).show();
            }
        } else {
            //可能弹出主页
            Toast.makeText(getApplication(), "Unknown item", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private String getShowMode() {
        PrefManager.funcMode showMode = PrefManager.getShowMode(this);
        if (showMode == PrefManager.funcMode.FLOAT_WIN) return "FLOAT_WIN";
        else if (showMode == PrefManager.funcMode.TOAST_MSG) return "TOAST_MSG";
        else if (showMode == PrefManager.funcMode.FLEX) {
            int star = finalOpeList.getStar();
            if (PrefManager.getCheckBoxStatus(this, Integer.toString(star))) return "FLOAT_WIN";
            else return "TOAST_MSG";
        }
        return "UNKNOWN";
    }

}
