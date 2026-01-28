package com.example.training.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.training.R
import com.example.training.ui.base.BottomNavFragment
import java.text.SimpleDateFormat
import java.util.*

class AttivitaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attivita)

        // Mostra data corrente in alto
        val dateText = findViewById<TextView>(R.id.date_text)
        val today = Calendar.getInstance().time
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        dateText.text = formatter.format(today)

        // Inserisci la BottomNavFragment nel container
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(2))
            .commitAllowingStateLoss()
    }
}
