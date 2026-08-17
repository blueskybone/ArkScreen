package com.blueskybone.arkscreen.data.local.pref.preference.shared

import android.content.SharedPreferences
import androidx.core.content.edit
import com.blueskybone.arkscreen.data.local.pref.preference.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.util.function.Function

/**
 *   Created by blueskybone
 *   Date: 2025/1/7
 */
abstract class SharedPreference<T>(
    private val preferences: SharedPreferences,
    private val keyFlow: Flow<String?>,
    private val key: String,
    private val defaultValue: T
) : Preference<T> {

    abstract fun read(preferences: SharedPreferences, key: String, defaultValue: T): T
    abstract fun write(preferences: SharedPreferences, key: String, value: T?)

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return read(preferences, key, defaultValue)
    }

    override fun set(value: T) {
        write(preferences, key, value)
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun isSet(): Boolean {
        return preferences.contains(key)
    }

    override fun delete() {
        preferences.edit { remove(key) }
    }

    override fun flow(): Flow<T> {
        return keyFlow
            .filter { it == key || it == null }
            .onStart { emit("ignition") }
            .map {
                get()
            }.conflate().distinctUntilChanged()
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return flow().stateIn(scope, SharingStarted.Eagerly, get())
    }

    internal class StringPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: String
    ) : SharedPreference<String>(preferences, keyFlow, key, defaultValue) {

        override fun read(
            preferences: SharedPreferences,
            key: String,
            defaultValue: String
        ): String {
            return preferences.getString(key, defaultValue) ?: defaultValue
        }

        override fun write(preferences: SharedPreferences, key: String, value: String?) {
            preferences.edit()?.putString(key, value)?.apply()
        }
    }


    internal class LongPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Long
    ) : SharedPreference<Long>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Long): Long {
            return preferences.getLong(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Long?) {
            preferences.edit { putLong(key, value!!) }
        }
    }

    internal class IntegerPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Int
    ) :
        SharedPreference<Int>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Int): Int {
            return preferences.getInt(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Int?) {
            preferences.edit { putInt(key, value!!) }
        }
    }

    internal class FloatPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Float
    ) : SharedPreference<Float>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: Float): Float {
            return preferences.getFloat(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Float?) {
            preferences.edit { putFloat(key, value!!) }
        }
    }

    internal class BooleanPrimitive(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: Boolean
    ) : SharedPreference<Boolean>(preferences, keyFlow, key, defaultValue) {
        override fun read(
            preferences: SharedPreferences,
            key: String,
            defaultValue: Boolean
        ): Boolean {
            return preferences.getBoolean(key, defaultValue)
        }

        override fun write(preferences: SharedPreferences, key: String, value: Boolean?) {
            preferences.edit { putBoolean(key, value!!) }
        }

    }

    internal class Object<T>(
        preferences: SharedPreferences,
        keyFlow: Flow<String?>,
        key: String,
        defaultValue: T,
        private val serializer: Function<T, String>,
        private val deserializer: Function<String, T>
    ) : SharedPreference<T>(preferences, keyFlow, key, defaultValue) {
        override fun read(preferences: SharedPreferences, key: String, defaultValue: T): T {
            val str = preferences.getString(key, null)
            return try {
                deserializer.apply(str!!)
            } catch (e: Exception) {
                defaultValue!!
            }
        }

        override fun write(preferences: SharedPreferences, key: String, value: T?) {
            preferences.edit { putString(key, serializer.apply(value!!)) }
        }
    }
}

