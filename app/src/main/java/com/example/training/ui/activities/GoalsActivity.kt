package com.example.training.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.training.R
import com.example.training.ui.base.BottomNavFragment

/**
 * Goals screen activity.
 */
class GoalsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // Insert Bottom Navigation (active index = 0)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(0))
            .commitAllowingStateLoss()
    }
}
