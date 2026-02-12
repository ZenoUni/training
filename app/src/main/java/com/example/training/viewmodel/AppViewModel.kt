package com.example.training.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.training.data.AppRepository
import com.example.training.data.Card

class AppViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences configuration
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
    // Persistent Training LiveData
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
        loadTrainingState()
    }

    /** Loads schede from repository or keeps current fallback list */
    fun loadSchede() {
        try {
            val repo = AppRepository.getInstance(getApplication())
            val list = repo.getSchede()
            _schede.value = list
            Log.d("APP_VM", "loadSchede => loaded ${list.size} items from repository.")
            list.forEachIndexed { idx, c ->
                Log.d("APP_VM", "  [$idx] name='${c.name}', exercises='${c.exercises?.take(60)}'")
            }
        } catch (e: Exception) {
            Log.e("APP_VM", "loadSchede => error loading schede", e)
            _schede.value = _schede.value ?: emptyList()
        }
    }

    /** Adds a new scheda */
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

    /** Updates a scheda at a specific index */
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

    /** Deletes a scheda at a specific index */
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

    /** Loads persisted training state from SharedPreferences */
    fun loadTrainingState() {
        _trainingActive.value = prefs.getBoolean(KEY_TRAINING_ACTIVE, false)
        _trainingPaused.value = prefs.getBoolean(KEY_TRAINING_PAUSED, false)
        _trainingLabel.value = prefs.getString(KEY_TRAINING_LABEL, null)
        _trainingStart.value = prefs.getLong(KEY_TRAINING_START, 0L)
        _trainingAccumulated.value = prefs.getLong(KEY_TRAINING_ACC, 0L)
        Log.d("APP_VM", "loadTrainingState => active=${_trainingActive.value}, paused=${_trainingPaused.value}, label=${_trainingLabel.value}")
    }

    /** Starts a new training session */
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

    /** Pauses the current training session */
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

    /** Resumes the training session */
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

    /** Stops the training and clears persisted data */
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
