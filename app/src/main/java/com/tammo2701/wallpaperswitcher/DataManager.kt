package com.tammo2701.wallpaperswitcher

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataManager {
    private const val PREFS = "wp_prefs"
    private const val KEY = "folders_v2"
    private val gson = Gson()

    fun save(context: Context, folders: List<Folder>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, gson.toJson(folders)).apply()
    }

    fun load(context: Context): MutableList<Folder> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Folder>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
}