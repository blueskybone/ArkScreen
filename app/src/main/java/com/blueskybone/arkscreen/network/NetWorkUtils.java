package com.blueskybone.arkscreen.network;

import static com.blueskybone.arkscreen.network.HttpConnectionUtils.httpResponse;
import static com.blueskybone.arkscreen.network.HttpConnectionUtils.httpResponseConnection;
import static com.blueskybone.arkscreen.network.SignUtils.generateSign;


import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.blueskybone.arkscreen.base.data.AccountSk;
import com.blueskybone.arkscreen.network.HttpConnectionUtils.RequestMethod;

import javax.net.ssl.HttpsURLConnection;

import com.blueskybone.arkscreen.util.TimeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NetWorkUtils {

    static class CredAndToken {
        public String cred;
        public String token;

        public CredAndToken(String cred, String token) {
            this.cred = cred;
            this.token = token;
        }
    }

    private final static String skland_url = "https://zonai.skland.com";
    private final static String grant_code_url = "https://as.hypergryph.com/user/oauth2/v2/grant";
    private final static String logout_url = "https://as.hypergryph.com/user/info/v1/logout";
    private final static String sign_api = "/api/v1/game/attendance";
    private final static String cred_code_api = "/api/v1/user/auth/generate_cred_by_code";
    private final static String game_info_api = "/api/v1/game/player/info";
    private final static String binding_api = "/api/v1/game/player/binding";
//    private final static String app_version_url = "https://gitee.com/blueskybone/ArkScreen/raw/master/version.info";
    //private final static String app_version_url_test = "https://gitee.com/blueskybone/ArkScreen/raw/master/version_test.info";
    private final static String app_version_url = "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/app_version.xml";
    private final static String app_code = "4ca99fa6b56cc2ba";

    private static final Map<String, String> headerLogin = new HashMap<>() {{
        put("User-Agent", "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0");
        put("Accept-Encoding", "gzip");
        put("Connection", "close");
        put("Content-Type", "application/json");
    }};

    private static final Map<String, String> headerSign = new HashMap<>() {{
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

    public static HttpsURLConnection getAppVersion() throws MalformedURLException {
        URL url = new URL(app_version_url);
        return httpResponseConnection(url, null, HttpConnectionUtils.RequestMethod.GET);
    }

    public static boolean logOutByToken(String token) throws Exception {
        URL url = new URL(logout_url);
        String jsonInputString = "{\"token\":\"" + token + "\"}";
        HttpConnectionUtils.Response resp = httpResponse(url, jsonInputString, headerLogin, RequestMethod.POST);
        return (Objects.equals(getJsonContent(resp.responseContent, "msg"), "OK"));
    }

    public static String getGrantByToken(String token) throws Exception {
        URL url = new URL(grant_code_url);
        String jsonInputString = "{\"appCode\":\"" + app_code + "\", \"token\":\"" + token + "\", \"type\":0}";
//        Log.e("getGrantByToken", jsonInputString);
        HttpConnectionUtils.Response resp = httpResponse(url, jsonInputString, headerLogin, RequestMethod.POST);
        if (resp.responseCode == 200) {
            try {
                JsonNode tree = new ObjectMapper().readTree(resp.responseContent);
                return tree.at("/data/code").asText();
            } catch (Exception e) {
                throw new Exception("json format wrong in getGrantByToken()");
            }
        } else {
            Log.e("getGrantByToken", resp.responseContent);
            if (resp.responseCode == 401) {
                throw new Exception("登录过期");
            }
            String msg = getJsonContent(resp.responseContent, "message");
            if (msg == null) {
                msg = getJsonContent(resp.responseContent, "msg");
                if (msg == null) {
                    throw new Exception(resp.responseCode + "");
                }
            }
            throw new Exception(msg);
        }
    }

    public static CredAndToken getCredByGrant(String grantCode) throws Exception {
        URL url = new URL(skland_url + cred_code_api);
        String jsonInputString = "{\"code\":\"" + grantCode + "\", \"kind\":1}";
        HttpConnectionUtils.Response resp = httpResponse(url, jsonInputString, headerLogin, RequestMethod.POST);
        if (resp.responseCode == 200) {
            try {
                JsonNode tree = new ObjectMapper().readTree(resp.responseContent);
                String cred = tree.at("/data/cred").asText();
                String token = tree.at("/data/token").asText();
                return new CredAndToken(cred, token);
            } catch (Exception e) {
                throw new Exception("json format wrong in getCredByGrant()");
            }
        } else {
            if (resp.responseCode == 401) {
                throw new Exception("登录过期");
            }
            String msg = getJsonContent(resp.responseContent, "message");
            if (msg == null) {
                msg = getJsonContent(resp.responseContent, "msg");
                if (msg == null) {
                    throw new Exception(resp.responseCode + "");
                }
            }
            throw new Exception(msg);
        }
    }

    public static List<AccountSk> createAccountSkList(String cred, String credToken, String token) throws Exception {
        URL url = new URL(skland_url + binding_api);
        long currentTs = TimeUtils.INSTANCE.getCurrentTs();
        String timeStamp = Long.toString(currentTs);
        String sign = generateSign(binding_api, "", credToken, timeStamp);
        headerSign.replace("cred", cred);
        headerSign.replace("sign", sign);
        headerSign.replace("timestamp", timeStamp);
        HttpConnectionUtils.Response resp = httpResponse(url, null, headerSign, RequestMethod.GET);
        if (resp.responseCode == 200) {
            try {
                Log.e("createAccountList", resp.responseContent);
                JsonNode list = new ObjectMapper().readTree(resp.responseContent).at("/data/list");
                return generateAccountSkList(list, token);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("json format wrong in createAccountSkList()");
            }
        } else {
            throw new Exception("createAccountSkList failed");
        }
    }

    private static List<AccountSk> generateAccountSkList(JsonNode node, String token) {
        List<AccountSk> accountSkList = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.get("appCode").asText().equals("arknights")) {
                JsonNode bindingList = item.get("bindingList");
                for (JsonNode user : bindingList) {
                    AccountSk account = new AccountSk(
                            token,
                            user.get("nickName").asText(),
                            user.get("channelMasterId").asText(),
                            user.get("uid").asText(),
                            user.get("isOfficial").asBoolean(),
                            false
                    );
                    accountSkList.add(account);
                }
            }
        }
        return accountSkList;
    }

    public static HttpsURLConnection getGameInfoConnection(String cred, String token, String uid) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        URL url = new URL(skland_url + game_info_api + "?uid=" + uid);
        long currentTs = TimeUtils.INSTANCE.getCurrentTs();
        String timeStamp = Long.toString(currentTs);
        String sign = generateSign(game_info_api, "uid=" + uid, token, timeStamp);
        headerSign.replace("cred", cred);
        headerSign.replace("sign", sign);
        headerSign.replace("timestamp", timeStamp);
        return httpResponseConnection(url, headerSign, RequestMethod.GET);
    }


    public static HttpConnectionUtils.Response logAttendance(String cred,
                                                             String token,
                                                             String uid,
                                                             String channelMasterId) throws Exception {
        URL url = new URL(skland_url + sign_api);
        long currentTs = TimeUtils.INSTANCE.getCurrentTs();
        String timeStamp = Long.toString(currentTs);
        Log.e("timestamp = ", timeStamp);
        String jsonBody = "{\"gameId\": " + channelMasterId + ", \"uid\": \"" + uid + "\"}";
        String sign = generateSign(sign_api, jsonBody, token, timeStamp);
        headerSign.replace("cred", cred);
        headerSign.replace("sign", sign);
        headerSign.replace("timestamp", timeStamp);
        //TODO：try get Result Back
        return httpResponse(url, jsonBody, headerSign, RequestMethod.POST);
    }

    public static Long getServerTs() throws Exception {
        URL url = new URL(skland_url + game_info_api);
        HttpConnectionUtils.Response resp = httpResponse(url, null, headerLogin, RequestMethod.GET);
        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode node = om.readTree(resp.responseContent);
            return node.get("timestamp").asLong();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getJsonContent(String jsonStr, String key) {
        if (jsonStr == null) {
            return null;
        }
        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(jsonStr);
            List<JsonNode> keys = tree.findValues(key);
            return keys.get(0).asText();
        } catch (Exception e) {
            return null;
        }
    }
}
