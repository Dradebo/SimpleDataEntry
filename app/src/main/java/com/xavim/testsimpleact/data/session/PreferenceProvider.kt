package com.xavim.testsimpleact.data.session

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceProvider @Inject constructor(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setValue(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun setValue(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun setValue(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun setValue(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun getString(key: String, defaultValue: String?): String? {
        return preferences.getString(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean? {
        return if (preferences.contains(key)) preferences.getBoolean(key, defaultValue) else defaultValue
    }

    fun getInt(key: String, defaultValue: Int): Int? {
        return if (preferences.contains(key)) preferences.getInt(key, defaultValue) else defaultValue
    }

    fun getLong(key: String, defaultValue: Long): Long? {
        return if (preferences.contains(key)) preferences.getLong(key, defaultValue) else defaultValue
    }

    fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "testsimpleact_prefs"
    }
}