package com.blueskybone.arkscreen.data.local.pref.preference.shared

import android.content.Context
import android.content.SharedPreferences
import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import com.blueskybone.arkscreen.data.local.pref.preference.PreferenceStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.function.Function

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
class SharedPreferenceStore(context: Context) : PreferenceStore {

    companion object {
        private lateinit var sharedPreferences: SharedPreferences
    }

    init {
        sharedPreferences = context.getSharedPreferences("context", Context.MODE_PRIVATE)
    }

    private val keyFlow = sharedPreferences.keyFlow

    override fun getString(key: String, value: String): Preference<String> {
        return SharedPreference.StringPrimitive(sharedPreferences, keyFlow, key, value)
    }

    override fun getInt(key: String, value: Int): Preference<Int> {
        return SharedPreference.IntegerPrimitive(sharedPreferences, keyFlow, key, value)
    }

    override fun getLong(key: String, value: Long): Preference<Long> {
        return SharedPreference.LongPrimitive(sharedPreferences, keyFlow, key, value)
    }

    override fun getFloat(key: String, value: Float): Preference<Float> {
        return SharedPreference.FloatPrimitive(sharedPreferences, keyFlow, key, value)
    }

    override fun getBoolean(key: String, value: Boolean): Preference<Boolean> {
        return SharedPreference.BooleanPrimitive(sharedPreferences, keyFlow, key, value)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: Function<T, String>,
        deserializer: Function<String, T>
    ): Preference<T> {
        return SharedPreference.Object(
            preferences = sharedPreferences,
            keyFlow = keyFlow,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }
}

private val SharedPreferences.keyFlow
    get() = callbackFlow {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? -> trySend(key) }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }