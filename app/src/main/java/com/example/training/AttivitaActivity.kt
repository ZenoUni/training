package com.example.training

import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class AttivitaActivity : BaseActivity(2) {

    override fun getLayoutId() = R.layout.activity_attivita

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostra data corrente in alto
        val dateText = findViewById<TextView>(R.id.date_text)
        val today = Calendar.getInstance().time
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        dateText.text = formatter.format(today)

    }
}
