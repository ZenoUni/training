package com.example.training.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.training.data.AppRepository
import com.example.training.data.Card
import com.example.training.data.Training

/**
 * Application ViewModel — orchestrates repository access for UI.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences configuration for training persistence (existing)
    private val prefsName = "training_prefs"
    private val KEY_TRAINING_ACTIVE = "training_active"
    private val KEY_TRAINING_LABEL = "training_label"
    private val KEY_TRAINING_START = "training_start"
    private val KEY_TRAINING_ACC = "training_acc"
    private val KEY_TRAINING_PAUSED = "training_paused"

    private val prefs = application.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    // ---------------------------
    // Schede LiveData
    // ---------------------------
    private val _schede = MutableLiveData<List<Card>>(emptyList())
    val schede: LiveData<List<Card>> = _schede

    // ---------------------------
    // Trainings LiveData
    // ---------------------------
    private val _trainings = MutableLiveData<List<Training>>(emptyList())
    val trainings: LiveData<List<Training>> = _trainings

    // ---------------------------
    // Goals LiveData (map name -> target count)
    // ---------------------------
    private val _goals = MutableLiveData<Map<String, Int>>(emptyMap())
    val goals: LiveData<Map<String, Int>> = _goals

    // ---------------------------
    // Persistent Training LiveData (existing)
    // ---------------------------
    private val _trainingActive = MutableLiveData(false)
    val trainingActive: LiveData<Boolean> = _trainingActive

    private val _trainingLabel = MutableLiveData<String?>(null)
    val trainingLabel: LiveData<String?> = _trainingLabel

    private val _trainingStart = MutableLiveData(0L)
    val trainingStart: LiveData<Long> = _trainingStart

    private val _trainingAccumulated = MutableLiveData(0L)
    val trainingAccumulated: LiveData<Long> = _trainingAccumulated

    private val _trainingPaused = MutableLiveData(false)
    val trainingPaused: LiveData<Boolean> = _trainingPaused

    init {
        loadSchede()
        loadTrainings()
        loadGoals()
        loadTrainingState()
    }

    // ---------------------------
    // Schede
    // ---------------------------
    fun loadSchede() {
        try {
            val repo = AppRepository.getInstance(getApplication())
            val list = repo.getSchede()
            _schede.value = list
            Log.d("APP_VM", "loadSchede => loaded ${list.size} items from repository.")
        } catch (e: Exception) {
            Log.e("APP_VM", "loadSchede => error loading schede", e)
            _schede.value = _schede.value ?: emptyList()
        }
    }

    fun addScheda(scheda: Card) {
        try {
            val repo = AppRepository.getInstance(getApplication())
            repo.addScheda(scheda)
            loadSchede()
            Log.d("APP_VM", "addScheda => delegated to repo and reloaded list.")
        } catch (e: Exception) {
            val list = _schede.value?.toMutableList() ?: mutableListOf()
            list.add(0, scheda)
            _schede.value = list
            Log.e("APP_VM", "addScheda => fallback in-memory add", e)
        }
    }

    fun updateScheda(index: Int, scheda: Card) {
        try {
            val repo = AppRepository.getInstance(getApplication())
            repo.updateScheda(index, scheda)
            loadSchede()
            Log.d("APP_VM", "updateScheda => delegated to repo and reloaded list.")
        } catch (e: Exception) {
            val list = _schede.value?.toMutableList() ?: mutableListOf()
            if (index in list.indices) {
                list[index] = scheda
                _schede.value = list
            }
            Log.e("APP_VM", "updateScheda => fallback update", e)
        }
    }

    fun deleteScheda(index: Int) {
        try {
            val repo = AppRepository.getInstance(getApplication())
            repo.deleteScheda(index)
            loadSchede()
            Log.d("APP_VM", "deleteScheda => delegated to repo and reloaded list.")
        } catch (e: Exception) {
            val list = _schede.value?.toMutableList() ?: mutableListOf()
            if (index in list.indices) {
                list.removeAt(index)
                _schede.value = list
            }
            Log.e("APP_VM", "deleteScheda => fallback delete", e)
        }
    }

    // ---------------------------
    // Trainings
    // ---------------------------
    fun loadTrainings() {
        try {
            val repo = AppRepository.getInstance(getApplication())
            val list = repo.getTrainings()
            _trainings.value = list
            Log.d("APP_VM", "loadTrainings => loaded ${list.size} trainings.")
        } catch (e: Exception) {
            Log.e("APP_VM", "loadTrainings => error loading trainings", e)
            _trainings.value = _trainings.value ?: emptyList()
        }
    }

    fun addTraining(training: Training) {
        try {
            val repo = AppRepository.getInstance(getApplication())
            repo.addTraining(training)
            loadTrainings()
            Log.d("APP_VM", "addTraining => delegated to repo and reloaded list.")
        } catch (e: Exception) {
            val list = _trainings.value?.toMutableList() ?: mutableListOf()
            list.add(0, training)
            _trainings.value = list
            Log.e("APP_VM", "addTraining => fallback in-memory add", e)
        }
    }

    // ---------------------------
    // Goals management
    // ---------------------------
    fun loadGoals() {
        try {
            val repo = AppRepository.getInstance(getApplication())
            val map = repo.getGoals()
            _goals.value = map
            Log.d("APP_VM", "loadGoals => loaded ${map.size} goals.")
        } catch (e: Exception) {
            Log.e("APP_VM", "loadGoals => error loading goals", e)
            _goals.value = _goals.value ?: emptyMap()
        }
    }

    fun saveGoals(map: Map<String, Int>) {
        try {
            val repo = AppRepository.getInstance(getApplication())
            repo.saveGoals(map)
            _goals.value = map
            Log.d("APP_VM", "saveGoals => saved ${map.size} goals.")
        } catch (e: Exception) {
            Log.e("APP_VM", "saveGoals => error saving goals", e)
        }
    }

    fun clearGoals() {
        try {
            val repo = AppRepository.getInstance(getApplication())
            repo.clearGoals()
            _goals.value = emptyMap()
            Log.d("APP_VM", "clearGoals => cleared goals.")
        } catch (e: Exception) {
            Log.e("APP_VM", "clearGoals => error clearing goals", e)
        }
    }

    // ---------------------------
    // Existing persistent training state helpers
    // ---------------------------
    fun loadTrainingState() {
        _trainingActive.value = prefs.getBoolean(KEY_TRAINING_ACTIVE, false)
        _trainingPaused.value = prefs.getBoolean(KEY_TRAINING_PAUSED, false)
        _trainingLabel.value = prefs.getString(KEY_TRAINING_LABEL, null)
        _trainingStart.value = prefs.getLong(KEY_TRAINING_START, 0L)
        _trainingAccumulated.value = prefs.getLong(KEY_TRAINING_ACC, 0L)
        Log.d("APP_VM", "loadTrainingState => active=${_trainingActive.value}, paused=${_trainingPaused.value}, label=${_trainingLabel.value}")
    }

    fun startTraining(label: String) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putBoolean(KEY_TRAINING_ACTIVE, true)
            .putString(KEY_TRAINING_LABEL, label)
            .putLong(KEY_TRAINING_START, now)
            .putLong(KEY_TRAINING_ACC, 0L)
            .putBoolean(KEY_TRAINING_PAUSED, false)
            .apply()

        _trainingActive.value = true
        _trainingLabel.value = label
        _trainingStart.value = now
        _trainingAccumulated.value = 0L
        _trainingPaused.value = false

        Log.d("APP_VM", "startTraining => started label='$label' at $now")
    }

    fun pauseTraining() {
        val now = System.currentTimeMillis()
        val start = _trainingStart.value ?: now
        val acc = now - start

        prefs.edit()
            .putLong(KEY_TRAINING_ACC, acc)
            .putBoolean(KEY_TRAINING_PAUSED, true)
            .apply()

        _trainingAccumulated.value = acc
        _trainingPaused.value = true

        Log.d("APP_VM", "pauseTraining => accumulated=$acc")
    }

    fun resumeTraining() {
        val acc = _trainingAccumulated.value ?: 0L
        val newStart = System.currentTimeMillis() - acc

        prefs.edit()
            .putLong(KEY_TRAINING_START, newStart)
            .putBoolean(KEY_TRAINING_PAUSED, false)
            .apply()

        _trainingStart.value = newStart
        _trainingPaused.value = false

        Log.d("APP_VM", "resumeTraining => newStart=$newStart")
    }

    fun stopTraining() {
        prefs.edit()
            .putBoolean(KEY_TRAINING_ACTIVE, false)
            .putBoolean(KEY_TRAINING_PAUSED, false)
            .remove(KEY_TRAINING_LABEL)
            .remove(KEY_TRAINING_START)
            .remove(KEY_TRAINING_ACC)
            .apply()

        _trainingActive.value = false
        _trainingPaused.value = false
        _trainingLabel.value = null
        _trainingStart.value = 0L
        _trainingAccumulated.value = 0L

        Log.d("APP_VM", "stopTraining => training stopped and cleared.")
    }
}