package com.godot17.arksc.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.godot17.arksc.database.Operator;
import com.godot17.arksc.datautils.OpeGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    private static final String TAG = "Utils";

    public static boolean writeToFile(Context context, String fileName, String content) {
        String filePath = context.getExternalCacheDir() + "/" + fileName;
        try {
            File file = new File(filePath);
            OutputStream os = new FileOutputStream(file);
            os.write(content.getBytes());
            os.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean fileExist(Context context, String fileName) {
        String filePath = context.getExternalCacheDir() + "/" + fileName;
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean mDeleteFile(Context context, String fileName) {
        String filePath = context.getExternalCacheDir() + "/" + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    //没有cache文件就从assets复制出来
    public static String getAssets2CacheDir(Context context, String fileName) {
        String filePath = context.getExternalCacheDir() + "/" + fileName;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return filePath;
            } else {
                File cacheFile = new File(context.getExternalCacheDir(), fileName);
                try {
                    try (InputStream inputStream = context.getAssets().open(fileName)) {
                        try (FileOutputStream outputStream = new FileOutputStream(cacheFile)) {
                            byte[] buf = new byte[4096];
                            int len;
                            while ((len = inputStream.read(buf)) > 0) {
                                outputStream.write(buf, 0, len);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return cacheFile.getAbsolutePath();
            }
        } catch (Exception e) {
            return filePath;
        }
    }

    public static String generateTagsText(String[] tags) {
        StringBuilder text = new StringBuilder();
        for (String tag : tags) text.append(tag).append(" ");
        return text.toString();
    }

    public static String getMarkDownText(List<OpeGroup> opeGroups) {
        StringBuilder text = new StringBuilder();
        text.append("");
        for (OpeGroup opeGroup : opeGroups) {
            text.append("` ");
            for (String tag : opeGroup.tags) {
                text.append(tag).append(" ");
            }
            text.append("`  \n");
            for (Operator ope : opeGroup.operators) {
                text.append(ope.getStar()).append("★").append("   ")
                        .append(ope.getName()).append("  \n");
            }
        }
        return text.toString();
    }

    public static String getMarkDownTextOnAct(List<OpeGroup> opeGroups) {
        StringBuilder text = new StringBuilder();
        for (OpeGroup opeGroup : opeGroups) {
            text.append("#### ");
            for (String tag : opeGroup.tags) {
                text.append(tag).append(" ");
            }
            text.append("\n");
            for (Operator ope : opeGroup.operators) {
                text.append(ope.getStar()).append("★").append("   ")
                        .append(ope.getName()).append("  \n");
            }
            text.append("\n---\n");
        }
        return text.toString();
    }

    public static String getAppVersionName(Context context) {
        int versioncode;
        String versionname = null;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versioncode = packageInfo.versionCode;
            versionname = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionname;
    }

    public static void showToast(Context context, String text) {
        Looper myLooper = Looper.myLooper();
        if (myLooper == null) {
            Looper.prepare();
            myLooper = Looper.myLooper();
        }
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        if (myLooper != null) {
            Looper.loop();
            myLooper.quit();
        }
    }

    public static String convertSec2DayHourMin(int Sec) {
        if (Sec < 0) return "-1分钟";
        String str = "";
        int min = Sec / 60;
        int hour = min / 60;
        int day = hour / 24;
        int cnt = 0;
        if (day > 0) {
            str = str + day + "天";
            hour = hour % 24;
            cnt++;
        }
        if (hour > 0) {
            str = str + hour + "小时";
            min = min % 60;
            cnt++;
        }
        if (cnt < 2) {
            str = str + min + "分";
        }
        return str;

    }

    public static int convertTs2Day(int Sec) {
        if (Sec < 0) return -1;
        return (Sec + 28800) / 86400;
    }


    public static byte[] calculateHmacSha256(String message, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);
        return hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    public static String calculateMD5(String message) throws NoSuchAlgorithmException {
        byte[] md5 = null;
        md5 = MessageDigest.getInstance("md5").digest(message.getBytes(StandardCharsets.UTF_8));
        BigInteger no = new BigInteger(1, md5);
        StringBuilder hashText = new StringBuilder(no.toString(16));
        while (hashText.length() < 32) {
            hashText.insert(0, "0");
        }
        return hashText.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    //
    //
    public static String generateSign(String api, String params, String key, String timeStamp) throws NoSuchAlgorithmException, InvalidKeyException {

        Log.e(TAG, key);
        String jsonargs = "{\"platform\":\"\",\"timestamp\":\"" + timeStamp + "\",\"dId\":\"\",\"vName\":\"\"}";
        String data = api + params + timeStamp + jsonargs;
        Log.e(TAG, data);
        byte[] hmacData = calculateHmacSha256(data, key);
        String hmacSha256Hex = bytesToHex(hmacData);
        Log.e(TAG, hmacSha256Hex);
        String sign = calculateMD5(hmacSha256Hex);
        Log.e(TAG, sign);
        return sign;
    }

//    public List<List<String>> getEleCombination(List<String> list, int range ) {
//
////        val combList = mutableListOf < List < String >> ()
////        for (idx in range downTo 1){
////            combList.addAll(getOneCombination(list, idx))
////        }
////        return combList
//    }

}
