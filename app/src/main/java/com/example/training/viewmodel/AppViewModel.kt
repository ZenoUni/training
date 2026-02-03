package com.example.training.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.training.data.Scheda

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsName = "training_prefs"
    private val KEY_TRAINING_ACTIVE = "training_active"
    private val KEY_TRAINING_LABEL = "training_label"
    private val KEY_TRAINING_START = "training_start"
    private val KEY_TRAINING_ACC = "training_acc"
    private val KEY_TRAINING_PAUSED = "training_paused"

    private val prefs = application.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    // Schede
    private val _schede = MutableLiveData<List<Scheda>>(emptyList())
    val schede: LiveData<List<Scheda>> = _schede

    // Allenamento in corso (persistente)
    private val _trainingActive = MutableLiveData<Boolean>(false)
    val trainingActive: LiveData<Boolean> = _trainingActive

    private val _trainingLabel = MutableLiveData<String?>(null)
    val trainingLabel: LiveData<String?> = _trainingLabel

    // "start" is the reference timestamp (if paused=false)
    private val _trainingStart = MutableLiveData<Long>(0L)
    val trainingStart: LiveData<Long> = _trainingStart

    // accumulated millis before pause; when paused this is the elapsed time
    private val _trainingAccumulated = MutableLiveData<Long>(0L)
    val trainingAccumulated: LiveData<Long> = _trainingAccumulated

    private val _trainingPaused = MutableLiveData<Boolean>(false)
    val trainingPaused: LiveData<Boolean> = _trainingPaused

    init {
        loadSchede()
        loadTrainingState()
    }

    // -------------------
    // Schede CRUD (wrapper al repository se presente)
    // -------------------
    fun loadSchede() {
        try {
            val repo = com.example.training.ui.activities.AppRepository.getInstance(getApplication())
            _schede.value = repo.getSchede()
        } catch (e: Exception) {
            // fallback: keep current or empty
            _schede.value = _schede.value ?: emptyList()
        }
    }

    fun addScheda(scheda: Scheda) {
        try {
            val repo = com.example.training.ui.activities.AppRepository.getInstance(getApplication())
            repo.addScheda(scheda)
            loadSchede()
        } catch (e: Exception) {
            val list = _schede.value?.toMutableList() ?: mutableListOf()
            list.add(0, scheda)
            _schede.value = list
        }
    }

    fun updateScheda(index: Int, scheda: Scheda) {
        try {
            val repo = com.example.training.ui.activities.AppRepository.getInstance(getApplication())
            repo.updateScheda(index, scheda)
            loadSchede()
        } catch (e: Exception) {
            val list = _schede.value?.toMutableList() ?: mutableListOf()
            if (index in list.indices) {
                list[index] = scheda
                _schede.value = list
            }
        }
    }

    fun deleteScheda(index: Int) {
        try {
            val repo = com.example.training.ui.activities.AppRepository.getInstance(getApplication())
            repo.deleteScheda(index)
            loadSchede()
        } catch (e: Exception) {
            val list = _schede.value?.toMutableList() ?: mutableListOf()
            if (index in list.indices) {
                list.removeAt(index)
                _schede.value = list
            }
        }
    }

    // -------------------
    // Allenamento persistente: start / pause / resume / stop
    // -------------------
    fun loadTrainingState() {
        val active = prefs.getBoolean(KEY_TRAINING_ACTIVE, false)
        val paused = prefs.getBoolean(KEY_TRAINING_PAUSED, false)
        val label = prefs.getString(KEY_TRAINING_LABEL, null)
        val start = prefs.getLong(KEY_TRAINING_START, 0L)
        val acc = prefs.getLong(KEY_TRAINING_ACC, 0L)

        _trainingActive.value = active
        _trainingPaused.value = paused
        _trainingLabel.value = label
        _trainingStart.value = start
        _trainingAccumulated.value = acc
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
    }

    fun pauseTraining() {
        // store accumulated time and mark paused
        val now = System.currentTimeMillis()
        val start = _trainingStart.value ?: now
        val acc = now - start
        prefs.edit()
            .putLong(KEY_TRAINING_ACC, acc)
            .putBoolean(KEY_TRAINING_PAUSED, true)
            .apply()

        _trainingAccumulated.value = acc
        _trainingPaused.value = true
    }

    fun resumeTraining() {
        // compute a new start timestamp so that (now - start) + previously accumulated == total
        val acc = _trainingAccumulated.value ?: 0L
        val newStart = System.currentTimeMillis() - acc
        prefs.edit()
            .putLong(KEY_TRAINING_START, newStart)
            .putBoolean(KEY_TRAINING_PAUSED, false)
            .apply()

        _trainingStart.value = newStart
        _trainingPaused.value = false
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
    }
}
