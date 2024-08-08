package com.blueskybone.arkscreen.base.preference.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.blueskybone.arkscreen.base.preference.Preference;
import com.blueskybone.arkscreen.base.preference.PreferenceStore;

import java.util.Set;
import java.util.function.Function;

/*
 * The code for data persistence refers to the following project
 * https://github.com/easybangumiorg/EasyBangumi
 * */

public class SharedPreferenceStore implements PreferenceStore {

    public SharedPreferenceStore(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private final SharedPreferences sharedPreferences;

    @Override
    public Preference<String> getString(String key, String value) {
        return new SharedPreference.StringPrimitive(sharedPreferences, key, value);
    }

    @Override
    public Preference<Integer> getInt(String key, int value) {
        return new SharedPreference.IntegerPrimitive(sharedPreferences, key, value);
    }

    @Override
    public Preference<Long> getLong(String key, Long value) {
        return new SharedPreference.LongPrimitive(sharedPreferences, key, value);
    }

    @Override
    public Preference<Float> getFloat(String key, Float value) {
        return new SharedPreference.FloatPrimitive(sharedPreferences, key, value);
    }

    @Override
    public Preference<Boolean> getBoolean(String key, Boolean value) {
        return new SharedPreference.BooleanPrimitive(sharedPreferences, key, value);
    }

    @Override
    public <T> Preference<T> getObject(String key, T defaultValue, Function<T, String> serializer, Function<String, T> deserializer) {
        return new SharedPreference.Object<>(sharedPreferences, key, defaultValue, serializer, deserializer);
    }

    @Override
    public Preference<Set<String>> getStringSet(String key, Set<String> set) {
        return new SharedPreference.StringSetPrimitive(sharedPreferences, key, set);
    }
}
