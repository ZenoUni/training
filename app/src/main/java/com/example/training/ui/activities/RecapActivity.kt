package com.example.training.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.training.R
import com.example.training.ui.base.BottomNavFragment

class RecapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recap)

        // Inserisci la BottomNavFragment (activeIndex = 4)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(4))
            .commitAllowingStateLoss()
    }
}
