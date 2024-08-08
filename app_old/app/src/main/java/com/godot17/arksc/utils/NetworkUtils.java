package com.godot17.arksc.utils;

import static com.godot17.arksc.utils.HttpConnectionUtils.RequestMethod;
import static com.godot17.arksc.utils.HttpConnectionUtils.httpResponse;
import static com.godot17.arksc.utils.HttpConnectionUtils.httpResponseConnection;
import static com.godot17.arksc.utils.Utils.generateSign;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    private final static String skland_url = "https://zonai.skland.com";
    private final static String grant_code_url = "https://as.hypergryph.com/user/oauth2/v2/grant";
    private final static String logout_url = "https://as.hypergryph.com/user/info/v1/logout";
    private final static String sign_api = "/api/v1/game/attendance";
    private final static String cred_code_api = "/api/v1/user/auth/generate_cred_by_code";
    private final static String game_info_api = "/api/v1/game/player/info";
    private final static String binding_api = "/api/v1/game/player/binding";
    private final static String app_version_url = "https://gitee.com/blueskybone/ArkScreen/raw/master/version.info";
    private final static String app_version_url_test = "https://gitee.com/blueskybone/ArkScreen/raw/master/version_test.info";
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
//        put("vName", "1.0.1");
//        put("vCode", "100001014");
//        put("dId", "de9759a5afaa634f");
//        put("platform", "1");
    }};

    private static final Map<String, String> header_base = new HashMap<String, String>() {{
        put("User-Agent", "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0");
        put("Accept-Encoding", "gzip");
        put("Connection", "close");
        put("Content-Type", "application/json");
    }};

    private static Map<String, String> header_sign = new HashMap<String, String>() {{
        put("cred", "");
        put("User-Agent", "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0");
        put("Accept-Encoding", "gzip");
        put("Connection", "close");
        put("Content-Type", "application/json");
        put("sign", "");
        put("platform", "");
        put("timestamp", "");
        put("dId", "");
        put("vName", "");
    }};

    private static Map<String, String> header_sign_login = new HashMap<String, String>() {{
        put("User-Agent", "Skland/1.4.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0");
        put("Accept-Encoding", "gzip");
        put("Connection", "close");
        put("vName", "1.4.1");
        put("vCode", "100401001");
    }};

    public static HttpsURLConnection getAppVersion() throws MalformedURLException {
        URL url = new URL(app_version_url_test);
        return httpResponseConnection(url, null, RequestMethod.GET);
    }

    public static boolean logOutByToken(String token) throws IOException {
        URL url = new URL(logout_url);
        String jsonInputString = "{\"token\":\"" + token + "\"}";
        String resp = httpResponse(url, jsonInputString, header_login, RequestMethod.POST);
        return (Objects.equals(getJsonContent(resp, "msg"), "OK"));
    }

    public static String getGrantCodeByToken(String token) throws IOException {
        URL url = new URL(grant_code_url);
        String jsonInputString = "{\"appCode\":\"" + app_code + "\", \"token\":\"" + token + "\", \"type\":0}";
        return httpResponse(url, jsonInputString, header_login, RequestMethod.POST);
    }

    public static String getCredByGrant(String grantCode) throws MalformedURLException {
        URL url = new URL(skland_url + cred_code_api);
        String code = grantCode.replace("\"", "");
        String jsonInputString = "{\"code\":\"" + code + "\", \"kind\":1}";
        Log.e(TAG,jsonInputString);
        return httpResponse(url, jsonInputString, header_login, RequestMethod.POST);
    }

    public static String getBindingJson(String cred, String token) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException {
        URL url = new URL(skland_url + binding_api);
        String timeStamp = (int) (System.currentTimeMillis() / 1000) + "";
        String sign = generateSign(binding_api, "", token, timeStamp);
        header_sign.replace("cred", cred.replace("\"", ""));
        header_sign.replace("sign", sign.replace("\"", ""));
        header_sign.replace("timestamp", timeStamp);
        return httpResponse(url, null, header_sign, RequestMethod.GET);
    }

    public static HttpsURLConnection getGameInfoConnection(String cred, String token, String uid) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        uid = uid.replace("\"", "");
        URL url = new URL(skland_url + game_info_api + "?uid=" + uid);
        String timeStamp = (int) (System.currentTimeMillis() / 1000) + "";
        String sign = generateSign(game_info_api, "uid=" + uid, token, timeStamp);
        header_sign.replace("cred", cred.replace("\"", ""));
        header_sign.replace("sign", sign.replace("\"", ""));
        header_sign.replace("timestamp", timeStamp);
        return httpResponseConnection(url, header_sign, RequestMethod.GET);
    }
   
   
    public static String logAttendance(String cred,
                                      String token,
                                      String uid,
                                      String channelMasterId) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException {
        uid = uid.replace("\"", "");
        URL url = new URL(skland_url + sign_api);
        channelMasterId = channelMasterId.replace("\"", "");
        String timeStamp = (int) (System.currentTimeMillis() / 1000) + "";
        Log.e("timestamp = ",timeStamp);
        String jsonBody = "{\"gameId\": " + channelMasterId + ", \"uid\": \"" + uid + "\"}";
        String sign = generateSign(sign_api, jsonBody, token, timeStamp);
        header_sign.replace("cred", cred);
        header_sign.replace("sign", sign);
        header_sign.replace("timestamp", timeStamp);
        Log.e("header_sign_timestamp",header_sign.get("timestamp"));
        Log.e("header_sign_cred",header_sign.get("cred"));
        Log.e("header_sign_sign",header_sign.get("sign"));
        Log.e("jsonBody",jsonBody);
        return httpResponse(url, jsonBody, header_sign, RequestMethod.POST);
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
}
