package com.godot17.arksc.utils;

import static com.godot17.arksc.utils.NetworkUtils.doAttendanceNew;
import static com.godot17.arksc.utils.NetworkUtils.getBindingJson;
import static com.godot17.arksc.utils.NetworkUtils.getCredByGrantNew;
import static com.godot17.arksc.utils.NetworkUtils.getGameInfoStream;
import static com.godot17.arksc.utils.NetworkUtils.getGrantCodeByTokenNew;
import static com.godot17.arksc.utils.NetworkUtils.logOutByToken;
import static com.godot17.arksc.utils.PrefManager.getChannelMasterId;
import static com.godot17.arksc.utils.PrefManager.getCred;
import static com.godot17.arksc.utils.PrefManager.getSignTs;
import static com.godot17.arksc.utils.PrefManager.getToken;
import static com.godot17.arksc.utils.PrefManager.getUserId;
import static com.godot17.arksc.utils.PrefManager.setChannelMasterId;
import static com.godot17.arksc.utils.PrefManager.setCred;
import static com.godot17.arksc.utils.PrefManager.setSignTs;
import static com.godot17.arksc.utils.PrefManager.setToken;
import static com.godot17.arksc.utils.PrefManager.setUserId;
import static com.godot17.arksc.utils.PrefManager.setUserInfo;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class NetWorkTask {

    public final static String TOKEN_UK_STATUS = "TOKEN_UK_STATUS";
    public final static String BAD_NETWORK = "网络异常";
    public final static String TOKEN_EXPIRED = "登录已过期";
    public final static String CRED_EXPIRED = "登录过期";
    public final static String GRANT_UK_RESPONSE = "GRANT_UK_RESPONSE";
    public final static String SERVER_WONDERING = "SERVER_WONDERING";
    public final static String REQUEST_ERROR = "请求异常";
    public final static String OK = "OK";
    //访问权限请求： token -> grant -> cred
    //中间请求： cred->binding Info
    //业务请求：  getGameStream(cred, uid(from binding info))
    //          doAttendance(cred, uid, channelId(from Binding info))
    //访问凭证：token ->cred

    // 对外接口： token ->cred
    // cred-> loadStatusInfo (binding Info)
    // cred -> gameInfo
    // cred -> doAttendance


    //应用场景：
    //cred->do
    //cred->unauthorized, token->cred->do
    //token ->authorized -> expired

    public static String getCredByToken(Context context) throws IOException {
        String token = getToken(context);
        String respGrant = getGrantCodeByTokenNew(token);
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
        //getGrantCode
        String respCred = getCredByGrantNew(grantCode);
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

    public static String loadStatusInfoByCred(Context context) throws MalformedURLException, JsonProcessingException {
        String cred = getCred(context);
        String jsonBinding = getBindingJson(cred);
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
        String defaultUid = arkList.get("defaultUid").toString().replace("\"", "");
        if (!defaultUid.equals("")) {
            for (idx = 0; idx < list.size(); idx++) {
                if (arkList.at("/bindingList").get(idx).get("uid").toString().replace("\"", "").equals(defaultUid)) {
                    break;
                }
            }
        } else {
            idx = 0;
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
        setSignTs(context,0);
        return OK;
    }

    public static String doAttendance(Context context) throws MalformedURLException, JsonProcessingException {
        int lastSignTs = getSignTs(context);
        int currentTs = (int) (System.currentTimeMillis() / 1000);
        Log.e("NET", currentTs + "");
        Log.e("NET", lastSignTs + "");

        int lastDay = Utils.convertSec2Day(currentTs);
        int currentDay = Utils.convertSec2Day(lastSignTs);
        Log.e("NET", lastDay + "");
        Log.e("NET", currentDay + "");

        if (currentDay == lastDay) {
            return "今日已签到";
        }
        String cred = getCred(context);
        String uid = getUserId(context);
        String channelMasterId = getChannelMasterId(context);
        String resp = doAttendanceNew(cred, uid, channelMasterId);
        if (resp == null) {
            return "签到失败：接口错误";
        } else {
            ObjectMapper om = new ObjectMapper();
            JsonNode tree = om.readTree(resp);
            Log.e("net",resp);
            if (tree.get("code") == null) {
                return "签到失败";
            } else {
                if (tree.get("code").toString().equals("0")) {
                    setSignTs(context, currentTs);
                    return "签到成功" + tree.at("/data/awards").get(0).at("/resource/name").toString().replace("\"", "")
                            + "×"
                            + tree.at("/data/awards").get(0).at("/count").asInt();
                } else {
                    setSignTs(context, currentTs);
                    return "签到失败：" + tree.at("/message").toString().replace("\"", "");
                }
            }
        }
    }

    public static InputStream getGameInfoInputStream(Context context) throws IOException {
        String cred = getCred(context);
        String uid = getUserId(context);
        return getGameInfoStream(cred, uid);
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
}
