package com.example.training.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.training.ui.activities.AppRepository
import com.example.training.data.Scheda

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository.getInstance(application)

    // Schede
    private val _schede = MutableLiveData<List<Scheda>>(emptyList())
    val schede: LiveData<List<Scheda>> = _schede

    init {
        loadSchede()
    }

    fun loadSchede() {
        _schede.value = repo.getSchede()
    }

    fun addScheda(scheda: Scheda) {
        repo.addScheda(scheda)
        loadSchede()
    }

    fun updateScheda(index: Int, scheda: Scheda) {
        repo.updateScheda(index, scheda)
        loadSchede()
    }

    fun deleteScheda(index: Int) {
        repo.deleteScheda(index)
        loadSchede()
    }

}
