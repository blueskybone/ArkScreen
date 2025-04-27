package com.blueskybone.arkscreen.preference.preference.shared

import android.content.Context
import android.content.SharedPreferences
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.preference.preference.PreferenceStore
import java.util.function.Function

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
class SharedPreferenceStore(context: Context) : PreferenceStore {

    companion object{
        private lateinit var sharedPreferences: SharedPreferences
    }
    init{
        sharedPreferences = context.getSharedPreferences("context",Context.MODE_PRIVATE)
    }

    override fun getString(key: String, value: String): Preference<String> {
        return SharedPreference.StringPrimitive(sharedPreferences, key, value)
    }

    override fun getInt(key: String, value: Int): Preference<Int> {
        return SharedPreference.IntegerPrimitive(sharedPreferences, key, value)
    }

    override fun getLong(key: String, value: Long): Preference<Long> {
        return SharedPreference.LongPrimitive(sharedPreferences, key, value)
    }

    override fun getFloat(key: String, value: Float): Preference<Float> {
        return SharedPreference.FloatPrimitive(sharedPreferences, key, value)
    }

    override fun getBoolean(key: String, value: Boolean): Preference<Boolean> {
        return SharedPreference.BooleanPrimitive(sharedPreferences, key, value)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: Function<T, String>,
        deserializer: Function<String, T>
    ): Preference<T> {
        return SharedPreference.Object(
            sharedPreferences,
            key,
            defaultValue,
            serializer,
            deserializer
        )
    }


}