package com.blueskybone.arkscreen.network;

import static com.blueskybone.arkscreen.network.NetWorkUtils.createAccountSkList;
import static com.blueskybone.arkscreen.network.NetWorkUtils.getAppVersion;
import static com.blueskybone.arkscreen.network.NetWorkUtils.getCredByGrant;
import static com.blueskybone.arkscreen.network.NetWorkUtils.getGameInfoConnection;
import static com.blueskybone.arkscreen.network.NetWorkUtils.logAttendance;
import static com.blueskybone.arkscreen.network.NetWorkUtils.logOutByToken;

import android.util.Xml;

import com.blueskybone.arkscreen.UpdateResource;
import com.blueskybone.arkscreen.base.data.AccountSk;
import com.blueskybone.arkscreen.base.data.AppUpdateInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class NetWorkTask {

    public static List<AccountSk> createAccountList(String token) throws Exception {
        String grant = NetWorkUtils.getGrantByToken(token);
        NetWorkUtils.CredAndToken credAndToken = getCredByGrant(grant);
        return createAccountSkList(credAndToken.cred, credAndToken.token, token);
    }

    //缓存明天再说。
    public void downloadGameData(AccountSk accountSk) throws Exception {
        String grant = NetWorkUtils.getGrantByToken(accountSk.token);
        NetWorkUtils.CredAndToken credAndToken = getCredByGrant(grant);
        getGameInfoConnection(credAndToken.cred, credAndToken.token, accountSk.uid);
    }

    public static HttpsURLConnection getGameInfoInputConnection(AccountSk accountSk) throws Exception {
        String grant = NetWorkUtils.getGrantByToken(accountSk.token);
        NetWorkUtils.CredAndToken credAndToken = getCredByGrant(grant);
        return getGameInfoConnection(credAndToken.cred, credAndToken.token, accountSk.uid);
    }

    public static void sklandAttendance(String token, String uid, String channelMasterId) throws Exception {
        String grant = NetWorkUtils.getGrantByToken(token);
        NetWorkUtils.CredAndToken credAndToken = getCredByGrant(grant);
        HttpConnectionUtils.Response resp = logAttendance(credAndToken.cred, credAndToken.token, uid, channelMasterId);
        //TODO: analyze result
    }

    public static AppUpdateInfo getAppUpdateInfo() throws Exception {
        HttpsURLConnection cn = getAppVersion();
        AppUpdateInfo appUpdateInfo = new AppUpdateInfo(null, null, null);
        try {
            InputStream is = cn.getInputStream();
            if (is == null) {
                cn.disconnect();
                return null;
            } else {
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(is, "utf-8");
                int eventType = xmlPullParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        switch (xmlPullParser.getName()) {
                            case "version" -> {
                                xmlPullParser.next();
                                appUpdateInfo.version = xmlPullParser.getText();
                            }
                            case "link" -> {
                                xmlPullParser.next();
                                appUpdateInfo.link = xmlPullParser.getText();
                            }
                            case "content" -> {
                                xmlPullParser.next();
                                appUpdateInfo.content = xmlPullParser.getText();
                            }
                            default -> {
                            }
                        }
                    }
                    eventType = xmlPullParser.next();
                }
                is.close();
                cn.disconnect();
                return appUpdateInfo;
            }
        } catch (Exception e) {
            return null;
        }
    }



//    //官B共用token，不能登出
//    public boolean logOut(String token) throws Exception {
//        return logOutByToken(token);
//    }
}
