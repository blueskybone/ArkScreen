package com.blueskybone.arkscreen.base.data;

public class AccountSk {
    public String token = "";
    public String nickName = "";
    public String channelMasterId = "";
    public String uid = "";
    public Boolean isOfficial = true;
    public Boolean isExpired = false;

    public AccountSk() {

    }

    public AccountSk(String token, String nickName, String channelMasterId, String uid, Boolean isOfficial, Boolean isExpired) {
        this.token = token;
        this.nickName = nickName;
        this.channelMasterId = channelMasterId;
        this.uid = uid;
        this.isOfficial = isOfficial;
        this.isExpired = isExpired;
    }

}
