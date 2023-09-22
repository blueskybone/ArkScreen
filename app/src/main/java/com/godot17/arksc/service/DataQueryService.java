package com.godot17.arksc.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.database.Database;
import com.godot17.arksc.database.EnCn;
import com.godot17.arksc.database.EnCnList;
import com.godot17.arksc.database.NewOpe;
import com.godot17.arksc.database.Operator;
import com.godot17.arksc.database.Update;
import com.godot17.arksc.datautils.FinalOpeList;
import com.godot17.arksc.datautils.OpeGroup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DataQueryService extends Service {
    private final String TAG = "DataQueryService";
    private static Database database_ope;
    private static EnCnList enCn_list;
    private static DataQueryService instance;
    private static List<Operator> lowList = new ArrayList<>();
    private static List<Operator> norList = new ArrayList<>();
    private static List<Operator> highList = new ArrayList<>();
    private static List<Operator> robotList = new ArrayList<>();

    private static final int range = 3;

    public static DataQueryService getInstance(){
        return instance;
    }
    public void initial() throws IOException {
        InputStream input_ope = new FileInputStream(getExternalCacheDir() + "/opedata.json");
        ObjectMapper mapper_db = new ObjectMapper();
        mapper_db.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        database_ope = mapper_db.readValue(input_ope, Database.class);

        lowList = database_ope.getOpeLowList();
        norList = database_ope.getOpeList();
        highList = database_ope.getOpeHighList();
        robotList = database_ope.getOpeRobotList();

        InputStream input_ec = new FileInputStream(getExternalCacheDir() + "/en_cn.json");
        ObjectMapper mapper_ec = new ObjectMapper();
        mapper_ec.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        enCn_list = mapper_ec.readValue(input_ec, EnCnList.class);
    }

    public NewOpe getNewOpe() {
        return database_ope.getNewOpe();
    }

    public Update getUpdate() {
        return database_ope.getUpdate();
    }

    public String getVersion() {
        return database_ope.getUpdate().getVersion();
    }

    public String getDate() {
        return database_ope.getUpdate().getDate();
    }

    private static String getCn(String enStr) {
        for (EnCn enCn : enCn_list.getEnCnList()) {
            if (enCn.getEn().equals(enStr)) {
                Log.e("En2cn", enCn.getCn());
                return enCn.getCn();
            }
        }
        return null;
    }



    private FinalOpeList getHighOpeList( String[] tags) {
        List<List<String>> tagsCom = getAllCombination(tags.length, Math.min(tags.length, range), tags);
        FinalOpeList finalOpeList = new FinalOpeList();
        finalOpeList.setTags(tags);
        for (List<String> tagList : tagsCom) {
            OpeGroup opeGroup = new OpeGroup();
            if (haveLowStar(tagList)) {
                StringBuilder str = new StringBuilder();
                for (String tag : tagList) str.append(tag).append(" ");
                continue;
            }
            // check high tag
            opeGroup.setTags(tagList);
            if (tagList.contains("高级资深干员")) {
                addOperator(highList, opeGroup);
            } else if (tagList.contains("支援机械")) {
                addOperator(robotList, opeGroup);
            } else {
                addOperator(norList, opeGroup);
            }
            finalOpeList.addOpeGroup(opeGroup);
        }
        return finalOpeList;
    }

    public FinalOpeList getAllOpeList(Context mContext, String[] tags) {
        List<List<String>> tagsCom = getAllCombination(tags.length, Math.min(tags.length, range), tags);
        FinalOpeList finalOpeList = new FinalOpeList();
        finalOpeList.setTags(tags);
        for (List<String> tagList : tagsCom) {
            // check senior_ope tag
            OpeGroup opeGroup = new OpeGroup();
            opeGroup.setTags(tagList);
            if (tagList.contains("高级资深干员")) {
                addOperator(highList, opeGroup);
            } else if (tagList.contains("支援机械")) {
                addOperator(robotList, opeGroup);
            } else {
                addOperator(norList, opeGroup);
                addOperator(lowList, opeGroup);
            }
            finalOpeList.addOpeGroup(opeGroup);
        }
        return finalOpeList;
    }


    public FinalOpeList getOpeListFromTile( String[] tags) {
        for (int i = 0; i < tags.length; i++) {
            tags[i] = getCn(tags[i]);
        }
        return getHighOpeList(tags);
    }


    private final IBinder dataLocalBinder = new dataLocalBinder();

    public class dataLocalBinder extends Binder {
        public DataQueryService getService() {
            return DataQueryService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return dataLocalBinder;
    }

    /*
    * private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }*/

    @Override
    public void onCreate() {
        instance = this;
        try {
            initial();
        } catch (IOException e) {
            Log.e(TAG,"Wrong with" + e);
            e.printStackTrace();
        }
        super.onCreate();
    }

    private static boolean haveLowStar(List<String> tag) {
        for (Operator operator : lowList) {
            if (operator.getTag().containsAll(tag)) {
                return true;
            }
        }
        return false;
    }

    private static void addOperator(List<Operator> checkList, OpeGroup opeGroup) {
        //opeGroup.setTags(tags);
        List<String> tags = opeGroup.tags;
        for (Operator operator : checkList) {
            if (operator.getTag().containsAll(tags)) {
                opeGroup.addOperator(operator);
            }
        }
    }

    private static List<List<String>> getAllCombination(int size, int range, String[] data) {
        List<List<String>> comListAll = new ArrayList<>();
        for (int i = range; i > 0; i--) {
            comListAll.addAll(getCombination(size, i, data));
        }
        return comListAll;
    }

    private static List<List<String>> getCombination(int size, int range, String[] data) {
        List<List<String>> comList = new ArrayList<>();
        for (int Str = (1 << size) - 1; Str >= 0; Str--) {
            int cnt = 0;
            int[] array = new int[10];
            List<String> strList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if ((Str & (1 << i)) != 0) {
                    array[cnt++] = i;
                }
            }
            if (cnt == range) {
                for (int i = range - 1; i >= 0; i--) {
                    strList.add(data[size - 1 - array[i]]);
                }
                comList.add(strList);
            }
        }
        return comList;
    }
}
