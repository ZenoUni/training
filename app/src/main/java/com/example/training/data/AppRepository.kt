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
    private val trainingsKey = "trainings_list"
    private val goalsKey = "monthly_goals"    // NEW: goals storage
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

    // -----------------------
    // Schede (cards)
    // -----------------------
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

    fun saveSchede(list: List<Card>) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = gson.toJson(list)
        prefs.edit().putString(schedeKey, json).apply()
        Log.d("APP_REPO", "saveSchede => saved ${list.size} items.")
    }

    fun addScheda(scheda: Card) {
        val current = getSchede()
        current.add(0, scheda)
        saveSchede(current)
        Log.d("APP_REPO", "addScheda => added '${scheda.name}'. New size: ${current.size}")
    }

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

    fun clearAllSchede() {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().remove(schedeKey).apply()
        Log.d("APP_REPO", "clearAllSchede => cleared saved schede.")
    }

    // =======================
    // Trainings storage
    // =======================
    fun getTrainings(): MutableList<Training> {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(trainingsKey, null)
        if (json.isNullOrEmpty()) {
            Log.d("APP_REPO", "getTrainings => no data (null/empty). Returning empty list.")
            return mutableListOf()
        }
        try {
            val type = object : TypeToken<MutableList<Training>>() {}.type
            val list: MutableList<Training> = gson.fromJson(json, type) ?: mutableListOf()
            Log.d("APP_REPO", "getTrainings => loaded ${list.size} items from prefs.")
            return list
        } catch (e: Exception) {
            Log.e("APP_REPO", "getTrainings => error parsing JSON, returning empty list", e)
            return mutableListOf()
        }
    }

    fun saveTrainings(list: List<Training>) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = gson.toJson(list)
        prefs.edit().putString(trainingsKey, json).apply()
        Log.d("APP_REPO", "saveTrainings => saved ${list.size} trainings.")
    }

    fun addTraining(training: Training) {
        val current = getTrainings()
        current.add(0, training)
        saveTrainings(current)
        Log.d("APP_REPO", "addTraining => added '${training.name}' on ${training.dateFormatted}. New size: ${current.size}")
    }

    fun clearAllTrainings() {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().remove(trainingsKey).apply()
        Log.d("APP_REPO", "clearAllTrainings => cleared saved trainings.")
    }

    // =======================
    // Goals storage (monthly)
    // stored as Map<String, Int> serialized with Gson
    // =======================
    fun getGoals(): MutableMap<String, Int> {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(goalsKey, null)
        if (json.isNullOrEmpty()) {
            Log.d("APP_REPO", "getGoals => no goals saved, returning empty map.")
            return mutableMapOf()
        }
        return try {
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            val map: MutableMap<String, Int> = gson.fromJson(json, type) ?: mutableMapOf()
            Log.d("APP_REPO", "getGoals => loaded ${map.size} goals.")
            map
        } catch (e: Exception) {
            Log.e("APP_REPO", "getGoals => error parsing JSON, returning empty map", e)
            mutableMapOf()
        }
    }

    fun saveGoals(map: Map<String, Int>) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = gson.toJson(map)
        prefs.edit().putString(goalsKey, json).apply()
        Log.d("APP_REPO", "saveGoals => saved ${map.size} goals.")
    }

    fun clearGoals() {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().remove(goalsKey).apply()
        Log.d("APP_REPO", "clearGoals => removed saved goals.")
    }
}