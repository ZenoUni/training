package com.example.training.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.training.R
import com.example.training.data.Scheda
import com.example.training.ui.base.BottomNavFragment
import com.example.training.viewmodel.AppViewModel
import java.util.concurrent.TimeUnit

class AttivitaActivity : AppCompatActivity() {

    private val viewModel: AppViewModel by viewModels()

    // UI
    private lateinit var startForm: LinearLayout
    private lateinit var startIcon: ImageView
    private lateinit var startText: TextView
    private lateinit var startArrow: ImageView
    private lateinit var timerText: TextView

    private lateinit var btnAvvia: Button

    // RUNNING
    private lateinit var setRunningLocked: LinearLayout
    private lateinit var btnPausa: Button
    private lateinit var btnLock: ImageButton
    private lateinit var btnFine1: Button

    // PAUSED
    private lateinit var setPaused: LinearLayout
    private lateinit var btnRiprendi: Button
    private lateinit var btnUnlock: ImageButton
    private lateinit var btnFine2: Button

    // state
    private var selectedTrainingLabel: String? = null
    private var selectedTrainingIcon: Int? = null
    private var schedeList: List<Scheda> = emptyList()

    /** toggle SOLO per il lucchetto in RUNNING */
    private var runningUnlocked = false

    // timer
    private val handler = Handler(Looper.getMainLooper())
    private val uiRunnable = object : Runnable {
        override fun run() {
            val active = viewModel.trainingActive.value ?: false
            val paused = viewModel.trainingPaused.value ?: false

            val elapsed = if (!active) {
                0L
            } else if (paused) {
                viewModel.trainingAccumulated.value ?: 0L
            } else {
                val start = viewModel.trainingStart.value ?: System.currentTimeMillis()
                System.currentTimeMillis() - start
            }

            timerText.text = formatMillis(elapsed)
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attivita)

        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(2))
            .commitAllowingStateLoss()

        // bind UI
        startForm = findViewById(R.id.start_form)
        startIcon = findViewById(R.id.start_icon)
        startText = findViewById(R.id.start_text)
        startArrow = findViewById(R.id.start_arrow)
        timerText = findViewById(R.id.timer_text)

        btnAvvia = findViewById(R.id.btn_avvia)

        setRunningLocked = findViewById(R.id.set_running_locked)
        btnPausa = findViewById(R.id.btn_pausa)
        btnLock = findViewById(R.id.btn_lock)
        btnFine1 = findViewById(R.id.btn_fine_1)

        setPaused = findViewById(R.id.set_paused)
        btnRiprendi = findViewById(R.id.btn_riprendi)
        btnUnlock = findViewById(R.id.btn_unlock)
        btnFine2 = findViewById(R.id.btn_fine_2)

        // stile pulsanti
        fun style(b: Button, bg: Int, txt: Int) {
            b.background = ContextCompat.getDrawable(this, bg)
            b.backgroundTintList = null
            b.setTextColor(ContextCompat.getColor(this, txt))
        }

        style(btnAvvia, R.drawable.bg_button_green, R.color.white)
        style(btnPausa, R.drawable.bg_button_yellow, R.color.black)
        style(btnFine1, R.drawable.bg_button_red, R.color.white)
        style(btnRiprendi, R.drawable.bg_button_green, R.color.white)
        style(btnFine2, R.drawable.bg_button_red, R.color.white)

        btnLock.background = ContextCompat.getDrawable(this, R.drawable.bg_lock_rect)
        btnUnlock.background = ContextCompat.getDrawable(this, R.drawable.bg_lock_rect)

        // stato iniziale form
        startText.text = getString(R.string.inizia_allenamento)
        startIcon.visibility = View.GONE

        // ---- FORM ----
        startForm.setOnClickListener {
            if (viewModel.trainingActive.value == true &&
                viewModel.trainingPaused.value == false
            ) {
                Toast.makeText(this, R.string.allenamento_in_corso, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.loadSchede()
            showStartSelector()
        }

        startArrow.setOnClickListener { startForm.performClick() }

        btnAvvia.setOnClickListener {
            val label = selectedTrainingLabel
            if (label.isNullOrBlank()) {
                Toast.makeText(
                    this,
                    R.string.seleziona_prima_un_allenamento,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.startTraining(label)
        }

        // ---- LUCCCHETTI ----

        // RUNNING → toggle infinito
        btnLock.setOnClickListener {
            runningUnlocked = !runningUnlocked
            btnPausa.isEnabled = runningUnlocked
            btnFine1.isEnabled = runningUnlocked
            btnLock.setImageResource(
                if (runningUnlocked) R.drawable.lucchetto_aperto else R.drawable.lucchetto
            )
        }

        btnPausa.setOnClickListener {
            if (!runningUnlocked) return@setOnClickListener
            viewModel.pauseTraining()
        }

        btnRiprendi.setOnClickListener {
            viewModel.resumeTraining()
        }

        btnFine1.setOnClickListener { handleFinishTraining() }
        btnFine2.setOnClickListener { handleFinishTraining() }

        // observers
        viewModel.schede.observe(this) { schedeList = it }

        viewModel.trainingActive.observe(this) {
            updateUi(it, viewModel.trainingPaused.value ?: false)
        }

        viewModel.trainingPaused.observe(this) {
            updateUi(viewModel.trainingActive.value ?: false, it)
        }

        viewModel.trainingLabel.observe(this) {
            if (viewModel.trainingActive.value == true) {
                startText.text = it
                selectedTrainingLabel = it
                selectedTrainingIcon?.let { icon ->
                    startIcon.setImageResource(icon)
                    startIcon.visibility = View.VISIBLE
                }
            }
        }

        viewModel.loadSchede()
        viewModel.loadTrainingState()
    }

    override fun onResume() {
        super.onResume()
        handler.post(uiRunnable)

        // reset form se NON c'è allenamento attivo
        if (viewModel.trainingActive.value != true) {
            selectedTrainingLabel = null
            selectedTrainingIcon = null
            startText.text = getString(R.string.inizia_allenamento)
            startIcon.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(uiRunnable)
    }

    private fun updateUi(active: Boolean, paused: Boolean) {

        btnAvvia.visibility = View.GONE
        setRunningLocked.visibility = View.GONE
        setPaused.visibility = View.GONE

        if (!active) {
            btnAvvia.visibility = View.VISIBLE
            runningUnlocked = false
            return
        }

        if (!paused) {
            setRunningLocked.visibility = View.VISIBLE
            runningUnlocked = false
            btnPausa.isEnabled = false
            btnFine1.isEnabled = false
            btnLock.setImageResource(R.drawable.lucchetto)
        } else {
            setPaused.visibility = View.VISIBLE
            btnUnlock.setImageResource(R.drawable.lucchetto_aperto)
        }
    }

    private fun handleFinishTraining() {

        // calcolo elapsed PRIMA dello stop
        val start = viewModel.trainingStart.value
        val accumulated = viewModel.trainingAccumulated.value ?: 0L

        val elapsed = if (viewModel.trainingPaused.value == true) {
            accumulated
        } else if (start != null) {
            accumulated + (System.currentTimeMillis() - start)
        } else {
            accumulated
        }

        val underTwentyMinutes = elapsed < TimeUnit.MINUTES.toMillis(20)

        // l'allenamento finisce SEMPRE
        viewModel.stopTraining()
        resetFormAfterStop()

        if (underTwentyMinutes) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.hai_gia_finito))
                .setMessage(getString(R.string.popup_fine_anticipata))
                .setNegativeButton(getString(R.string.no_elimina)) { d, _ -> d.dismiss() }
                .setPositiveButton(getString(R.string.si_salva)) { d, _ -> d.dismiss() }
                .show()
        }
    }

    private fun resetFormAfterStop() {
        selectedTrainingLabel = null
        selectedTrainingIcon = null
        startText.text = getString(R.string.inizia_allenamento)
        startIcon.visibility = View.GONE
    }

    private fun showStartSelector() {
        val items = mutableListOf<Pair<Int?, String>>()
        val schede = viewModel.schede.value ?: emptyList()

        for (s in schede) items.add(Pair(R.drawable.foglio, s.nome))
        items.add(Pair(R.drawable.corsa_colori, getString(R.string.corsa)))
        items.add(Pair(R.drawable.boxe_colori, getString(R.string.pugilato)))

        val labels = items.map { it.second }.toTypedArray()

        val adapter = object :
            ArrayAdapter<String>(this, R.layout.item_action, R.id.item_text, labels) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_action, parent, false)
                view.findViewById<TextView>(R.id.item_text).text = items[position].second
                view.findViewById<ImageView>(R.id.item_icon)
                    .setImageResource(items[position].first ?: 0)
                return view
            }
        }

        AlertDialog.Builder(this)
            .setAdapter(adapter) { d, which ->
                selectedTrainingLabel = items[which].second
                selectedTrainingIcon = items[which].first
                startText.text = selectedTrainingLabel
                startIcon.setImageResource(selectedTrainingIcon ?: 0)
                startIcon.visibility = View.VISIBLE
                d.dismiss()
            }
            .show()
    }

    private fun formatMillis(ms: Long): String {
        val h = TimeUnit.MILLISECONDS.toHours(ms)
        val m = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(h)
        val s = TimeUnit.MILLISECONDS.toSeconds(ms) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))
        return String.format("%d:%02d:%02d", h, m, s)
    }
}
