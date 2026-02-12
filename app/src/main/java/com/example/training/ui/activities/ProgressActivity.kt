package com.example.training.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.training.R
import com.example.training.ui.base.BottomNavFragment

/**
 * Progress screen activity.
 */
class ProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Insert Bottom Navigation (active index = 3)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(3))
            .commitAllowingStateLoss()
    }
}
