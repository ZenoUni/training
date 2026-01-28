package com.example.training.ui.activities

import android.content.Context
import com.example.training.data.Scheda
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Repository centrale per l'app.

class AppRepository private constructor(private val context: Context) {

    private val prefsName = "app_repository_prefs"
    private val schedeKey = "schede_list"
    private val gson = Gson()

    // singleton per repository (richiede context application)
    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // Schede
    fun getSchede(): MutableList<Scheda> {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(schedeKey, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Scheda>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveSchede(list: List<Scheda>) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().putString(schedeKey, gson.toJson(list)).apply()
    }

    fun addScheda(scheda: Scheda) {
        val current = getSchede()
        current.add(0, scheda)
        saveSchede(current)
    }

    fun updateScheda(index: Int, scheda: Scheda) {
        val current = getSchede()
        if (index in current.indices) {
            current[index] = scheda
            saveSchede(current)
        }
    }

    fun deleteScheda(index: Int) {
        val current = getSchede()
        if (index in current.indices) {
            current.removeAt(index)
            saveSchede(current)
        }
    }

    fun clearAllSchede() {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().remove(schedeKey).apply()
    }

}
