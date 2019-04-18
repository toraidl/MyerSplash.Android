package com.juniperphoton.myersplash.contract

import android.content.Context
import com.juniperphoton.myersplash.utils.LocalSettingHelper

@Suppress("unused")
class PreferenceRepo(private val context: Context) {
    fun getBoolean(key: String, def: Boolean = false): Boolean {
        return LocalSettingHelper.getBoolean(context, key, def)
    }

    fun getString(key: String): String {
        return LocalSettingHelper.getString(context, key)
    }

    fun getInt(key: String, def: Int = 0): Int {
        return LocalSettingHelper.getInt(context, key, def)
    }
}