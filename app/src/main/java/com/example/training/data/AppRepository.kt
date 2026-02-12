package com.example.training.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Central repository for managing app persistent data.
 * Uses SharedPreferences + Gson for local storage.
 */
class AppRepository private constructor(private val context: Context) {

    private val prefsName = "app_repository_prefs"
    private val schedeKey = "schede_list"
    private val gson = Gson()

    companion object {

        @Volatile
        private var instance: AppRepository? = null

        /** Returns singleton instance of repository. */
        fun getInstance(context: Context): AppRepository {
            return instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /** Returns saved cards list from SharedPreferences. */
    fun getSchede(): MutableList<Card> {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(schedeKey, null)
        if (json.isNullOrEmpty()) {
            Log.d("APP_REPO", "getSchede => no data (null/empty). Returning empty list.")
            return mutableListOf()
        }

        try {
            val type = object : TypeToken<MutableList<Card>>() {}.type
            val list: MutableList<Card> = gson.fromJson(json, type) ?: mutableListOf()
            Log.d("APP_REPO", "getSchede => loaded ${list.size} items from prefs.")
            list.forEachIndexed { idx, c ->
                Log.d("APP_REPO", "  [$idx] name='${c.name}', exercises='${c.exercises?.take(60)}'")
            }
            return list
        } catch (e: Exception) {
            Log.e("APP_REPO", "getSchede => error parsing JSON, returning empty list", e)
            return mutableListOf()
        }
    }

    /** Saves the full cards list. */
    fun saveSchede(list: List<Card>) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = gson.toJson(list)
        prefs.edit().putString(schedeKey, json).apply()
        Log.d("APP_REPO", "saveSchede => saved ${list.size} items.")
    }

    /** Adds a new card at the top of the list. */
    fun addScheda(scheda: Card) {
        val current = getSchede()
        current.add(0, scheda)
        saveSchede(current)
        Log.d("APP_REPO", "addScheda => added '${scheda.name}'. New size: ${current.size}")
    }

    /** Updates a card at the specified index. */
    fun updateScheda(index: Int, scheda: Card) {
        val current = getSchede()
        if (index in current.indices) {
            current[index] = scheda
            saveSchede(current)
            Log.d("APP_REPO", "updateScheda => updated index $index with '${scheda.name}'.")
        } else {
            Log.w("APP_REPO", "updateScheda => index $index out of range (size=${current.size}).")
        }
    }

    /** Deletes a card at the specified index. */
    fun deleteScheda(index: Int) {
        val current = getSchede()
        if (index in current.indices) {
            val removed = current.removeAt(index)
            saveSchede(current)
            Log.d("APP_REPO", "deleteScheda => removed '${removed.name}' at index $index. New size: ${current.size}")
        } else {
            Log.w("APP_REPO", "deleteScheda => index $index out of range (size=${current.size}).")
        }
    }

    /** Clears all saved cards. */
    fun clearAllSchede() {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().remove(schedeKey).apply()
        Log.d("APP_REPO", "clearAllSchede => cleared saved schede.")
    }
}
