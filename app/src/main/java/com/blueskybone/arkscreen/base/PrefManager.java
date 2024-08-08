package com.blueskybone.arkscreen.base;


import com.blueskybone.arkscreen.base.data.AccountSk;
import com.blueskybone.arkscreen.base.preference.Preference;
import com.blueskybone.arkscreen.base.preference.PreferenceStore;

import java.util.ArrayList;
import java.util.function.Function;

public class PrefManager {

    public PrefManager(PreferenceStore preferenceStore) {
        isAutoCheckOn = preferenceStore.getBoolean("is_auto_check_on", true);
        isAutoCheckUpdate = preferenceStore.getBoolean("is_auto_check_update", true);
        isSklandCorrectOn = preferenceStore.getBoolean("is_skland_correct_on", false);
        CorrectionTs = preferenceStore.getLong("ts_correction", 0L);
        LastAttendanceTs = preferenceStore.getLong("ts_last_attendance", 0L);
        enableDebug = preferenceStore.getBoolean("enable_debug", false);
        BaseAccount = preferenceStore.getObject("base_account", new AccountSk(), serializer(), deserializer());
        ListAccountSk = preferenceStore.getObject("list_account", new ArrayList<>(), serializerList(), deserializerList());
        recruitShowMode = preferenceStore.getEnum("recruit_show_mode", ShowMode.FLOAT);
        widgetAlpha = preferenceStore.getInt("widget_alpha", 0);
        widgetThemePreference = preferenceStore.getEnum("widget_theme", WidgetTheme.WHITE_ON_BLACK);
    }

    public Preference<Boolean> isAutoCheckOn;
    public Preference<Boolean> isAutoCheckUpdate;
    public Preference<Boolean> isSklandCorrectOn;
    public Preference<Long> CorrectionTs;
    public Preference<Long> LastAttendanceTs;
    public Preference<Boolean> enableDebug;
    public Preference<AccountSk> BaseAccount;
    public Preference<ArrayList<AccountSk>> ListAccountSk;
    public Preference<ShowMode> recruitShowMode;
    public Preference<Integer> widgetAlpha;
    public Preference<WidgetTheme> widgetThemePreference;

    //TODO: Account的序列化和反序列化可能出问题，包括Object Preference的处理。

    public enum WidgetTheme {
        WHITE_ON_BLACK,
        BLACK_ON_WHITE,

    }

    public enum ShowMode {
        FLOAT, TOAST, AUTO
    }

    Function<AccountSk, String> serializer() {
        return account -> account.token +
                "@" + account.nickName +
                "@" + account.channelMasterId +
                "@" + account.uid +
                "@" + account.isOfficial +
                "@" + account.isExpired;
    }

    Function<String, AccountSk> deserializer() {
        return string -> {
            try {
                String[] list = string.split("@");
                return new AccountSk(list[0], list[1], list[2], list[3], Boolean.parseBoolean(list[4]), Boolean.parseBoolean(list[5]));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    Function<ArrayList<AccountSk>, String> serializerList() {
        return list -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (AccountSk acc : list) {
                stringBuilder.append(serializer().apply(acc)).append("%");
            }
            return stringBuilder.toString();
        };
    }

    Function<String, ArrayList<AccountSk>> deserializerList() {
        return string -> {
            try {
                String[] list = string.split("%");
                ArrayList<AccountSk> listAccount = new ArrayList<>();
                for (String str : list) {
                    AccountSk accountSk = deserializer().apply(str);
                    if (accountSk != null)
                        listAccount.add(accountSk);
                }
                return listAccount;
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        };
    }
}