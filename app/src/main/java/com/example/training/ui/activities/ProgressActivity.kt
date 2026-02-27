package com.example.training.ui.activities

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.training.R
import com.example.training.data.Training
import com.example.training.ui.base.BottomNavFragment
import com.example.training.viewmodel.AppViewModel
import java.util.concurrent.TimeUnit

/**
 * Progress screen activity.
 * Observes saved trainings and shows them grouped by date.
 */
class ProgressActivity : AppCompatActivity() {

    private val viewModel: AppViewModel by viewModels()

    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(3))
            .commitAllowingStateLoss()

        listContainer = findViewById(R.id.progress_list_container)

        viewModel.trainings.observe(this) { list ->
            renderTrainings(list)
        }

        viewModel.loadTrainings()
    }

    private fun renderTrainings(list: List<Training>) {

        listContainer.removeAllViews()

        if (list.isEmpty()) {
            val tv = TextView(this).apply {
                text = getString(R.string.no_trainings_yet)
                setTextAppearance(android.R.style.TextAppearance_Medium)
                gravity = Gravity.CENTER
            }
            listContainer.addView(tv)
            return
        }

        // 🔥 GROUP BY DATE
        val grouped = list.groupBy { it.dateFormatted }

        grouped.forEach { (date, trainingsOfDay) ->

            // ----- DATE HEADER (shown only once) -----
            val dateTv = TextView(this).apply {
                text = date
                setTextAppearance(android.R.style.TextAppearance_Medium)
                setPadding(0, 24, 0, 12)
            }
            listContainer.addView(dateTv)

            // ----- TRAININGS UNDER THAT DATE -----
            trainingsOfDay.forEach { t ->

                val h = TimeUnit.MILLISECONDS.toHours(t.durationMillis)
                val m = TimeUnit.MILLISECONDS.toMinutes(t.durationMillis) -
                        TimeUnit.HOURS.toMinutes(h)
                val s = TimeUnit.MILLISECONDS.toSeconds(t.durationMillis) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(t.durationMillis)
                        )

                val sb = StringBuilder()
                sb.append("${t.name}, ")
                sb.append("Durata = %d:%02d:%02d".format(h, m, s))

                if (t.distanceFormatted != null) {
                    sb.append(", Distanza = ${t.distanceFormatted} km")
                    val avg = t.avgKmPerMin ?: 0.0
                    sb.append(", Velocità media = ${"%.3f".format(avg)} km/min")
                }

                val detailTv = TextView(this).apply {
                    text = sb.toString()
                    setTextAppearance(android.R.style.TextAppearance_Small)
                    setPadding(16, 0, 0, 12)
                }

                listContainer.addView(detailTv)
            }
        }
    }
}