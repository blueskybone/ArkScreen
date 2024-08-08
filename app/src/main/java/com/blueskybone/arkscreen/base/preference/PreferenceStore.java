package com.blueskybone.arkscreen.base.preference;

/*
 * The code for data persistence refers to the following project
 * https://github.com/easybangumiorg/EasyBangumi
 * */

import java.util.Set;
import java.util.function.Function;

public interface PreferenceStore {
    Preference<String> getString(String key, String value);

    Preference<Integer> getInt(String key, int value);

    Preference<Long> getLong(String key, Long value);

    Preference<Float> getFloat(String key, Float value);

    Preference<Boolean> getBoolean(String key, Boolean value);

    Preference<Set<String>> getStringSet(String key, Set<String> set);

    <T> Preference<T> getObject(
            String key,
            T defaultValue,
            Function<T, String> serializer,
            Function<String, T> deserializer
    );

    default <T extends Enum<T>> Function<T, String> serializer() {
        return T::name;
    }

    default <T extends Enum<T>> Function<String, T> deserializer(T defaultValue) {
        return string -> {
            try {
                return Enum.valueOf((Class<T>) defaultValue.getDeclaringClass(), string);
            } catch (Exception e) {
                return defaultValue;
            }
        };
    }

    default <T extends Enum<T>> Preference<T> getEnum(String key, T defaultValue) {
        return getObject(key, defaultValue, serializer(), deserializer(defaultValue));
    }
}
