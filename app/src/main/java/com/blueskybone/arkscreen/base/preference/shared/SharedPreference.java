package com.blueskybone.arkscreen.base.preference.shared;

import android.content.SharedPreferences;

import com.blueskybone.arkscreen.base.preference.Preference;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import kotlin.Unit;
import kotlinx.coroutines.flow.Flow;

public abstract class SharedPreference<T> implements Preference<T> {
    private final SharedPreferences preferences;
    private final String key;
    private final T defaultValue;

    abstract T read(SharedPreferences preferences, String key, T defaultValue);

    abstract void write(SharedPreferences preferences, String key, T value);

    public SharedPreference(SharedPreferences preferences, String key, T defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }


    @Override
    public String key() {
        return key;
    }

    @Override
    public T get() {
        return read(preferences, key, defaultValue);
    }

    @Override
    public void set(T value) {
        write(preferences, key, value);
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    public Boolean isSet() {
        return preferences.contains(key);
    }

    @Override
    public void delete() {
        preferences.edit().remove(key).apply();
    }

    static class StringPrimitive extends SharedPreference<String> {
        public StringPrimitive(SharedPreferences preferences, String key, String defaultValue) {
            super(preferences, key, defaultValue);
        }

        String read(SharedPreferences preferences, String key, String defaultValue) {
            return preferences.getString(key, defaultValue);
        }

        void write(SharedPreferences preferences, String key, String value) {
            preferences.edit().putString(key, value).apply();
        }
//        Function<SharedPreferences.Editor, Unit> write(String key, String value) {
//            return editor -> {
//                editor.putString(key, value).apply();
//                return null; // Void 类型的返回值
//            };
//        }
    }


    static class LongPrimitive extends SharedPreference<Long> {
        public LongPrimitive(SharedPreferences preferences, String key, Long defaultValue) {
            super(preferences, key, defaultValue);
        }

        Long read(SharedPreferences preferences, String key, Long defaultValue) {
            return preferences.getLong(key, defaultValue);
        }

        //        Function<SharedPreferences.Editor, Unit> write(String key, Long value) {
//            return editor -> {
//                editor.putLong(key, value).apply();
//                return null; // Void 类型的返回值
//            };
//        }
        void write(SharedPreferences preferences, String key, Long value) {
            preferences.edit().putLong(key, value).apply();
        }
    }

    static class IntegerPrimitive extends SharedPreference<Integer> {
        public IntegerPrimitive(SharedPreferences preferences, String key, Integer defaultValue) {
            super(preferences, key, defaultValue);
        }

        Integer read(SharedPreferences preferences, String key, Integer defaultValue) {
            return preferences.getInt(key, defaultValue);
        }

        //        Function<SharedPreferences.Editor, Unit> write(String key, Integer value) {
//            return editor -> {
//                editor.putInt(key, value).apply();
//                return null; // Void 类型的返回值
//            };
//        }
        void write(SharedPreferences preferences, String key, Integer value) {
            preferences.edit().putInt(key, value).apply();
        }
    }

    static class FloatPrimitive extends SharedPreference<Float> {
        public FloatPrimitive(SharedPreferences preferences, String key, Float defaultValue) {
            super(preferences, key, defaultValue);
        }

        Float read(SharedPreferences preferences, String key, Float defaultValue) {
            return preferences.getFloat(key, defaultValue);
        }

        //        Function<SharedPreferences.Editor, Unit> write(String key, Float value) {
//            return editor -> {
//                editor.putFloat(key, value).apply();
//                return null; // Void 类型的返回值
//            };
//        }
        void write(SharedPreferences preferences, String key, Float value) {
            preferences.edit().putFloat(key, value).apply();
        }
    }

    static class BooleanPrimitive extends SharedPreference<Boolean> {
        public BooleanPrimitive(SharedPreferences preferences, String key, Boolean defaultValue) {
            super(preferences, key, defaultValue);
        }

        Boolean read(SharedPreferences preferences, String key, Boolean defaultValue) {
            return preferences.getBoolean(key, defaultValue);
        }

        //        Function<SharedPreferences.Editor, Unit> write(String key, Boolean value) {
//            return editor -> {
//                editor.putBoolean(key, value).apply();
//                return null; // Void 类型的返回值
//            };
//        }
        void write(SharedPreferences preferences, String key, Boolean value) {
            preferences.edit().putBoolean(key, value).apply();
        }

    }

    static class StringSetPrimitive extends SharedPreference<Set<String>> {
        public StringSetPrimitive(SharedPreferences preferences, String key, Set<String> defaultValue) {
            super(preferences, key, defaultValue);
        }

        Set<String> read(SharedPreferences preferences, String key, Set<String> defaultValue) {
            return preferences.getStringSet(key, defaultValue);
        }

        //        Function<SharedPreferences.Editor, Unit> write(String key, Set<String> value) {
//            return editor -> {
//                editor.putStringSet(key, value).apply();
//                return null;
//            };
//        }
        void write(SharedPreferences preferences, String key, Set<String> value) {
            preferences.edit().putStringSet(key, value).apply();
        }

    }


    static class Object<T> extends SharedPreference<T> {
        private final Function<T, String> serializer;
        private final Function<String, T> deserializer;

        public Object(SharedPreferences preferences, String key, T defaultValue, Function<T, String> serializer, Function<String, T> deserializer) {
            super(preferences, key, defaultValue);
            this.serializer = serializer;
            this.deserializer = deserializer;
        }

        T read(SharedPreferences preferences, String key, T defaultValue) {
            String str = preferences.getString(key, null);
            try {
                return deserializer.apply(str);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        //        Function<SharedPreferences.Editor, Unit> write(String key, T value) {
//            return editor -> {
//                editor.putString(key, serializer.apply(value)).apply();
//                return null;
//            };
//        }
        void write(SharedPreferences preferences, String key, T value) {
            preferences.edit().putString(key, serializer.apply(value)).apply();
        }
    }

}
