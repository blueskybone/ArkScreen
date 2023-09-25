package com.godot17.arksc.utils;

import static com.godot17.arksc.utils.HttpConnectionUtils.RequestMethod;
import static com.godot17.arksc.utils.HttpConnectionUtils.getResponse;
import static com.godot17.arksc.utils.HttpConnectionUtils.httpResponse;
import static com.godot17.arksc.utils.HttpConnectionUtils.httpResponseConnection;
import static com.godot17.arksc.utils.Utils.getAppVersionName;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

/**
 * 写的什么玩意
 */

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    private final static String grant_code_url = "https://as.hypergryph.com/user/oauth2/v2/grant";
    private final static String sign_url = "https://zonai.skland.com/api/v1/game/attendance";
    private final static String cred_code_url = "https://zonai.skland.com/api/v1/user/auth/generate_cred_by_code";
    private final static String game_info_url = "https://zonai.skland.com/api/v1/game/player/info";
    private final static String binding_url = "https://zonai.skland.com/api/v1/game/player/binding";
    private final static String app_version_url = "https://gitee.com/blueskybone/ArkScreen/raw/master/version.info";
    private final static String app_version_url_test = "https://gitee.com/blueskybone/ArkScreen/raw/master/version_test.info";
    private final static String logout_url = "https://as.hypergryph.com/user/info/v1/logout";
    private final static String app_code = "4ca99fa6b56cc2ba";

    private static Map<String, String> header = new HashMap<String, String>() {{
        put("cred", "");
        put("User-Agent", "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0");
        put("Accept-Encoding", "gzip");
        put("Connection", "close");
        put("Content-Type", "application/json");
        put("vName", "1.0.1");
        put("vCode", "100001014");
        put("dId", "de9759a5afaa634f");
        put("platform", "1");

    }};
    private static final Map<String, String> header_login = new HashMap<String, String>() {{
        put("User-Agent", "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0");
        put("Accept-Encoding", "gzip");
        put("Connection", "close");
        put("Content-Type", "application/json");
        put("vName", "1.0.1");
        put("vCode", "100001014");
        put("dId", "de9759a5afaa634f");
        put("platform", "1");
    }};

    public static HttpsURLConnection getAppVersion() throws MalformedURLException {
        URL url = new URL(app_version_url_test);

        return httpResponseConnection(url,null, RequestMethod.GET);
    }

    public static boolean logOutByToken(String token) throws IOException {
        URL url = new URL(logout_url);
        String jsonInputString = "{\"token\":\"" + token + "\"}";
        //Log.e(TAG, jsonInputString);
        String resp = httpResponse(url,jsonInputString,header_login,RequestMethod.POST);
        Log.e(TAG, resp);
        return (Objects.equals(getJsonContent(resp, "msg"), "OK"));
    }

    public static String getGrantCodeByTokenNew(String token) throws IOException {
        URL url = new URL(grant_code_url);
        String jsonInputString = "{\"appCode\":\"" + app_code + "\", \"token\":\"" + token + "\", \"type\":0}";
        //Log.e(TAG, jsonInputString);
        return httpResponse(url, jsonInputString, header_login, RequestMethod.POST);
    }

    public static String getCredByGrantNew(String grantCode) throws MalformedURLException {
        URL url = new URL(cred_code_url);
        String code = grantCode.replace("\"", "");
        String jsonInputString = "{\"code\":\"" + code + "\", \"kind\":1}";
        //Log.e(TAG, jsonInputString);
        RequestMethod method = RequestMethod.POST;
        return httpResponse(url, jsonInputString, header_login, method);
    }

    public static String getBindingJson(String cred) throws MalformedURLException {
        URL url = new URL(binding_url);
        header.replace("cred", cred.replace("\"", ""));
        return httpResponse(url, null, header, RequestMethod.GET);
    }

    public static HttpsURLConnection getGameInfoConnection(String cred, String uid) throws IOException {
        header.replace("cred", cred.replace("\"", ""));
        uid = uid.replace("\"", "");
        URL url = new URL(game_info_url + "?uid=" + uid);
        return httpResponseConnection(url, header, RequestMethod.GET);
    }


    public static String doAttendanceNew(String cred,
                                         String uid,
                                         String channelMasterId) throws MalformedURLException {
        URL url = new URL(sign_url);
        header.replace("cred", cred.replace("\"", ""));
        //Log.e(TAG, cred);
        uid = uid.replace("\"", "");
        channelMasterId = channelMasterId.replace("\"", "");
        String jsonInputString = "{\"uid\":\"" + uid + "\", \"gameId\":\""
                + channelMasterId + "\"}";
        return httpResponse(url,jsonInputString,header,RequestMethod.POST);
    }

    public static String getJsonContent(String jsonStr, String key) {

        if (jsonStr == null) {
            return null;
        }
        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(jsonStr);
            List<JsonNode> keys = tree.findValues(key);
            return keys.get(0).toString().replace("\"", "");
        } catch (Exception e) {
            return null;
        }
    }




    public static String checkAppVersion(Context context) throws MalformedURLException {
        URL url = new URL(app_version_url);
        InputStream is = getResponse(url);
        if (is == null) {
            return "解析错误.";
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
            return "解析xml错误.";
        }
        String oldVersion = getAppVersionName(context);
        if (oldVersion.compareTo(version) >= 0) {
            return "";
        } else {
            //showDialog
            //showAppUpdateDialog(context, link, content);

            return "检测到应用新版本, [下载链接](" + link + ")";
        }
    }


}
