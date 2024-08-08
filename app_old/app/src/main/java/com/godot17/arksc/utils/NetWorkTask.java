package com.godot17.arksc.utils;

import static com.godot17.arksc.utils.NetworkUtils.getAppVersion;
import static com.godot17.arksc.utils.NetworkUtils.getBindingJson;
import static com.godot17.arksc.utils.NetworkUtils.getCredByGrant;
import static com.godot17.arksc.utils.NetworkUtils.getGameInfoConnection;
import static com.godot17.arksc.utils.NetworkUtils.getGrantCodeByToken;
import static com.godot17.arksc.utils.NetworkUtils.logAttendance;
import static com.godot17.arksc.utils.NetworkUtils.logOutByToken;
import static com.godot17.arksc.utils.PrefManager.getChannelMasterId;
import static com.godot17.arksc.utils.PrefManager.getCred;
import static com.godot17.arksc.utils.PrefManager.getCredToken;
import static com.godot17.arksc.utils.PrefManager.getSignTs;
import static com.godot17.arksc.utils.PrefManager.getToken;
import static com.godot17.arksc.utils.PrefManager.getUserId;
import static com.godot17.arksc.utils.PrefManager.setChannelMasterId;
import static com.godot17.arksc.utils.PrefManager.setCred;
import static com.godot17.arksc.utils.PrefManager.setCredToken;
import static com.godot17.arksc.utils.PrefManager.setSignTs;
import static com.godot17.arksc.utils.PrefManager.setToken;
import static com.godot17.arksc.utils.PrefManager.setUserId;
import static com.godot17.arksc.utils.PrefManager.setUserInfo;
import static com.godot17.arksc.utils.Utils.getAppVersionName;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.datautils.RoleInfo;
import com.godot17.arksc.datautils.UpdateInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class NetWorkTask {
    private final static String TAG = "NetWorkTask";
    public final static String TOKEN_UK_STATUS = "TOKEN_UK_STATUS";
    public final static String BAD_NETWORK = "网络异常";
    public final static String TOKEN_EXPIRED = "token过期";
    public final static String CRED_EXPIRED = "cred过期";
    public final static String GRANT_UK_RESPONSE = "GRANT_UK_RESPONSE";
    public final static String SERVER_WONDERING = "SERVER_WONDERING";
    public final static String REQUEST_ERROR = "请求异常";
    public final static String OK = "OK";

    /*
     * 访问权限请求： token -> grant -> cred
     * 中间请求：  cred->binding Info
     * 业务请求：  getGameStream(cred, uid(from binding info))
     *           doAttendance(cred, uid, channelId(from Binding info))
     *
     * 对外接口： token ->cred
     *  cred-> loadStatusInfo (binding Info)
     *  cred -> gameInfo
     *  cred -> doAttendance
     *
     * 应用场景：
     * cred->doWork
     * cred->unauthorized, token->cred->doWork
     * token ->unauthorized -> expired
     * */

    /*
     *TODO:
     * 其实还是从token开始请求的。但是不想写了。开摸
     * */
    public static String getCredByToken(Context context) throws IOException {
        String token = getToken(context);
        String respGrant = getGrantCodeByToken(token);
        Log.e(TAG,respGrant);
        String grantCode;
        if (respGrant == null) {
            return BAD_NETWORK;
        } else {
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(respGrant);
            if (tree.at("/status") != null) {
                int status = tree.get("status").asInt();
                switch (status) {
                    case 3:
                        return TOKEN_EXPIRED;
                    case 0:
                        grantCode = tree.at("/data/code").toString().replace("\"", "");
                        break;
                    default:
                        return TOKEN_UK_STATUS;
                }
            } else if (tree.at("/statusCode") != null) {
                return "GRANT:" + tree.get("error").toString().replace("\"", "");
            } else {
                return GRANT_UK_RESPONSE;
            }
        }
        //getCred
        String respCred = getCredByGrant(grantCode);
        Log.e(TAG, "respCred" + respCred);
        if (respCred == null) {
            return BAD_NETWORK;
        } else {
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(respCred);
            if (tree.at("/code") != null) {
                int code = tree.get("code").asInt();
                switch (code) {
                    case 10002:
                        return TOKEN_EXPIRED;
                    case 0:
                        setCred(context, tree.at("/data/cred").toString().replace("\"", ""));
                        setCredToken(context, tree.at("/data/token").toString().replace("\"", ""));
                        return OK;
                    case 10001:
                        return SERVER_WONDERING;  //鹰角服务器返回msg"服务器开小差"
                    default:
                        return TOKEN_UK_STATUS;
                }
            } else if (tree.at("/statusCode") != null) {
                return "err:" + tree.get("error").toString().replace("\"", "");
            } else {
                return GRANT_UK_RESPONSE;
            }
        }
    }

    public static String loadStatusInfoByCred(Context context) throws MalformedURLException, JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
        String cred = getCred(context);
        String credToken = getCredToken(context);
        String jsonBinding = getBindingJson(cred, credToken);
        JsonNode list;
        if (jsonBinding == null) {
            return BAD_NETWORK;
        } else {
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(jsonBinding);
            if (tree.at("/code") != null) {
                int code = tree.get("code").asInt();
                switch (code) {
                    case 10002:
                        return CRED_EXPIRED;
                    case 0:
                        list = tree.at("/data/list");
                        break;
                    case 10001:
                        return SERVER_WONDERING;  //鹰角服务器返回msg"服务器开小差"
                    case 10000:
                        return REQUEST_ERROR;
                    default:
                        return TOKEN_UK_STATUS;
                }
            } else if (tree.at("/statusCode") != null) {
                return "err:" + tree.get("error").toString().replace("\"", "");
            } else {
                return GRANT_UK_RESPONSE;
            }
        }
        int size = list.size();
        int idx;
        for (idx = 0; idx < size; idx++) {
            if (list.get(idx).get("appCode").toString().replace("\"", "").equals("arknights")) {
                break;
            }
        }
        //越界判断（？
        JsonNode arkList = list.get(idx);
        JsonNode bindingList = arkList.at("/bindingList");
        for (idx = 0; idx < bindingList.size(); idx++) {
            if (bindingList.get(idx).get("isDefault").asBoolean()) break;
        }
        if (idx == bindingList.size()) {
            String defaultUid = arkList.get("defaultUid").toString().replace("\"", "");
            Log.e(TAG, defaultUid);
            if (!defaultUid.equals("")) {
                for (idx = 0; idx < list.size(); idx++) {
                    if (arkList.at("/bindingList").get(idx).get("uid").toString().replace("\"", "").equals(defaultUid)) {
                        break;
                    }
                }
            } else {
                idx = 0;
            }
        }
        JsonNode info = arkList.at("/bindingList").get(idx);
        String uid = info.get("uid").toString().replace("\"", "");
        String channelMasterId = info.get("channelMasterId").toString().replace("\"", "");
        String userInfo = info.get("nickName").toString().replace("\"", "")
                + " | " + info.get("channelName").toString().replace("\"", "");
        setCred(context, cred);
        setUserId(context, uid);
        setChannelMasterId(context, channelMasterId);
        setUserInfo(context, userInfo);
        setSignTs(context, 0);
        return OK;
    }

    public static String doAttendance(Context context) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        int lastSignTs = getSignTs(context);
        int currentTs = (int) (System.currentTimeMillis() / 1000);
        int currentDay = Utils.convertTs2Day(currentTs);
        int lastDay = Utils.convertTs2Day(lastSignTs);
        if (currentDay == lastDay) {
            return "今日已签到";
        }

        String cred = getCred(context);
        String credToken = getCredToken(context);
        String uid = getUserId(context);
        String channelMasterId = getChannelMasterId(context);
        String resp = logAttendance(cred, credToken, uid, channelMasterId);
        if (resp == null) {
            return "签到失败：接口错误";
        } else {
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(resp);
            Log.e("net", resp);
            if (tree.get("code") == null) {
                return "签到失败";
            } else {
                int code = tree.get("code").asInt();
                switch (code) {
                    case 0:
                        setSignTs(context, currentTs);
                        return "签到成功：" + tree.at("/data/awards").get(0).at("/resource/name").toString().replace("\"", "")
                                + "×"
                                + tree.at("/data/awards").get(0).at("/count").asInt();
                    case 10001:
                        if(tree.at("/message").toString().replace("\"", "").equals("请勿重复签到！")) setSignTs(context, currentTs);
                        return "签到失败：" + tree.at("/message").toString().replace("\"", "");
                    case 10000:
                    case 10002:
                    case 10003:
                        default:
                        return "签到失败：" + tree.at("/message").toString().replace("\"", "");

                }
            }
        }
    }

    public static HttpsURLConnection getGameInfoInputConnection(Context context) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String cred = getCred(context);
        String credToken = getCredToken(context);
        String uid = getUserId(context);
        return getGameInfoConnection(cred, credToken, uid);
    }

    public static String logOutMsg(Context context) throws IOException {
        String token = getToken(context);
        String msg;
        if (token.equals("")) {
            msg = "未登录";
        } else {
            if (logOutByToken(token)) {
                msg = "登出成功";
            } else {
                msg = "登出发生问题，已清空数据";
            }
        }
        setToken(context, "");
        setCred(context, "");
        setUserId(context, "");
        setUserInfo(context, "未登录");
        setChannelMasterId(context, "");
        setSignTs(context, 0);

        return msg;
    }

    public static boolean getUpdateInfo(Context context) throws MalformedURLException {
        HttpsURLConnection cn = getAppVersion();
        try {
            InputStream is = cn.getInputStream();
            if (is == null) {
                cn.disconnect();
                Log.e(TAG, "get inputStream fails");
                return false;
            } else {
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(is, "utf-8");
                int eventType = xmlPullParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        switch (xmlPullParser.getName()) {
                            case "version":
                                xmlPullParser.next();
                                UpdateInfo.version = xmlPullParser.getText();
                                Log.e(TAG, "version " + UpdateInfo.version);
                                break;
                            case "link":
                                xmlPullParser.next();
                                UpdateInfo.link = xmlPullParser.getText();
                                Log.e(TAG, "link " + UpdateInfo.link);
                                break;
                            case "content":
                                xmlPullParser.next();
                                UpdateInfo.content = xmlPullParser.getText();
                                Log.e(TAG, "content " + UpdateInfo.content);
                                break;
                            default:
                                Log.e(TAG, "xml Err");
                                return false;
                        }
                    }
                    eventType = xmlPullParser.next();
                }
                is.close();
                cn.disconnect();

                if (UpdateInfo.version == null || UpdateInfo.link == null || UpdateInfo.content == null) {
                    return false;
                }
                String oldVersion = getAppVersionName(context);
                return oldVersion.compareTo(UpdateInfo.version) < 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "" + e);
            return false;
        }
    }

    public static List<RoleInfo> getBindingRoleInfo(Context context) throws MalformedURLException, JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
        String cred = getCred(context);
        String credToken = getCredToken(context);
        if (cred.equals("")) {
            return null;
        }
        String jsonBinding = getBindingJson(cred, credToken);
        JsonNode list;
        if (jsonBinding == null) {
            Log.e(TAG, "jsonBinding is null");
            return null;
        } else {
            Log.e(TAG, jsonBinding);
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(jsonBinding);
            if (tree.at("/code") != null) {
                int code = tree.get("code").asInt();
                switch (code) {
                    case 0:
                        list = tree.at("/data/list");
                        break;
                    case 10002:
                    case 10001:
                    case 10000:
                    default:
                        return null;
                }
            } else if (tree.at("/statusCode") != null) {
                return null;
            } else {
                return null;
            }
        }
        int size = list.size();
        int idx;
        for (idx = 0; idx < size; idx++) {
            if (list.get(idx).get("appCode").toString().replace("\"", "").equals("arknights")) {
                break;
            }
        }
        JsonNode arkList = list.get(idx);
        JsonNode bindingList = arkList.at("/bindingList");
        List<RoleInfo> roleInfoList = new ArrayList<>();
        for (idx = 0; idx < bindingList.size(); idx++) {
            RoleInfo roleInfo = new RoleInfo();
            JsonNode binding = bindingList.get(idx);
            roleInfo.uid = binding.get("uid").toString().replace("\"", "");
            roleInfo.nickName = binding.get("nickName").toString().replace("\"", "");
            roleInfo.channelName = binding.get("channelName").toString().replace("\"", "");
            roleInfo.channelMasterId = binding.get("channelMasterId").toString().replace("\"", "");
            roleInfo.userInfo = roleInfo.nickName + " | " + roleInfo.channelName;
            roleInfoList.add(roleInfo);
        }
        return roleInfoList;
    }

}
