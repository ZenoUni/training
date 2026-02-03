package com.example.training.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.training.R
import com.example.training.data.Scheda
import com.example.training.ui.base.BottomNavFragment
import com.example.training.viewmodel.AppViewModel
import java.util.concurrent.TimeUnit

class AttivitaActivity : AppCompatActivity() {

    private val viewModel: AppViewModel by viewModels()

    // UI: form & text
    private lateinit var startForm: LinearLayout
    private lateinit var startIcon: ImageView
    private lateinit var startText: TextView
    private lateinit var startArrow: ImageView

    // Timer text
    private lateinit var timerText: TextView

    // Buttons / sets
    private lateinit var btnAvvia: Button
    private lateinit var setRunningLocked: LinearLayout
    private lateinit var btnPausa: Button
    private lateinit var btnLock: ImageButton
    private lateinit var btnFine1: Button

    private lateinit var setPaused: LinearLayout
    private lateinit var btnRiprendi: Button
    private lateinit var btnUnlock: ImageButton
    private lateinit var btnFine2: Button

    // state
    private var selectedTrainingLabel: String? = null
    private var selectedTrainingIcon: Int? = null
    private var schedeList: List<Scheda> = emptyList()

    // lock local flag (UI only) - whether "lock" has been tapped to enable pause/fine
    private var unlockedForActions = false

    // timer ui updater
    private val handler = Handler(Looper.getMainLooper())
    private val uiRunnable = object : Runnable {
        override fun run() {
            val active = viewModel.trainingActive.value ?: false
            val paused = viewModel.trainingPaused.value ?: false
            val elapsed: Long = if (!active) {
                0L
            } else {
                if (paused) {
                    viewModel.trainingAccumulated.value ?: 0L
                } else {
                    val start = viewModel.trainingStart.value ?: System.currentTimeMillis()
                    System.currentTimeMillis() - start
                }
            }
            timerText.text = formatMillis(elapsed)
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attivita)

        // bottom nav
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(2))
            .commitAllowingStateLoss()

        // data text
        val dateText = findViewById<TextView>(R.id.date_text)
        val today = java.util.Calendar.getInstance().time
        val formatter = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault())
        dateText.text = formatter.format(today)

        // Bind UI
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

        btnAvvia.setBgColor(R.color.button_green)
        btnPausa.setBgColor(R.color.button_yellow)
        btnFine1.setBgColor(R.color.button_red)
        btnRiprendi.setBgColor(R.color.button_green)
        btnFine2.setBgColor(R.color.button_red)
        btnLock.setBgColor(R.color.button_dark_gray)
        btnUnlock.setBgColor(R.color.button_light_gray)


        // default label
        startText.text = getString(R.string.inizia_allenamento)
        startIcon.visibility = View.GONE

        // Click aprono il menu — ogni click ricarica schede fresche dal ViewModel
        startForm.setOnClickListener {
            if (viewModel.trainingActive.value == true && viewModel.trainingPaused.value == false) {
                Toast.makeText(this, getString(R.string.allenamento_in_corso), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.loadSchede()
            showStartSelector()
        }
        startArrow.setOnClickListener {
            if (viewModel.trainingActive.value == true && viewModel.trainingPaused.value == false) {
                Toast.makeText(this, getString(R.string.allenamento_in_corso), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.loadSchede()
            showStartSelector()
        }

        // Avvia (inizia allenamento)
        btnAvvia.setOnClickListener {
            val label = selectedTrainingLabel
            if (label.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.seleziona_prima_un_allenamento), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // start via viewmodel (persistente)
            viewModel.startTraining(label)
        }

        // LOCK button: when displayed in running-locked set; user must tap it to enable pause & finish
        btnLock.setOnClickListener {
            unlockedForActions = true
            // enable pausa and fine in running set
            btnPausa.isEnabled = true
            btnFine1.isEnabled = true
            // change visual: show locked -> unlocked icon/background; here we set icon to "open"
            btnLock.setImageResource(R.drawable.lucchetto_aperto)
            btnLock.backgroundTintList = getColorStateList(R.color.light_gray)
        }

        // In paused-set the unlock button is shown and should be already open (per your spec)
        btnUnlock.setOnClickListener {
            unlockedForActions = true
        }

        // Pausa: pause timer (but only if unlocked)
        btnPausa.setOnClickListener {
            if (!unlockedForActions) {
                Toast.makeText(this, getString(R.string.unlock_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.pauseTraining()
        }

        // Riprendi: resume timer
        btnRiprendi.setOnClickListener {
            viewModel.resumeTraining()
        }

        btnFine1.setOnClickListener {
            handleFinishTraining()
        }

        btnFine2.setOnClickListener {
            handleFinishTraining()
        }

        // Observers
        viewModel.schede.observe(this, Observer { list ->
            schedeList = list
        })

        viewModel.trainingActive.observe(this, Observer { active ->
            updateUiForTrainingState(active, viewModel.trainingPaused.value ?: false)
        })

        viewModel.trainingPaused.observe(this, Observer { paused ->
            updateUiForTrainingState(viewModel.trainingActive.value ?: false, paused)
        })

        viewModel.trainingLabel.observe(this, Observer { label ->
            // show label if active
            if (viewModel.trainingActive.value == true) {
                startText.text = label ?: getString(R.string.inizia_allenamento)
                selectedTrainingLabel = label
            }
        })

        // Start/restore state
        viewModel.loadSchede()
        viewModel.loadTrainingState()

        viewModel.trainingActive.observe(this) { active ->
            if (!active) {
                resetFormAfterStop()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // start runnable if training active
        handler.removeCallbacks(uiRunnable)
        handler.post(uiRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(uiRunnable)
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(uiRunnable)
    }

    private fun Button.setBgColor(colorRes: Int) {
        background.mutate().setTint(getColor(colorRes))
    }

    private fun ImageButton.setBgColor(colorRes: Int) {
        background.mutate().setTint(getColor(colorRes))
    }


    private fun handleFinishTraining() {
        val start = viewModel.trainingStart.value ?: return
        val elapsed = System.currentTimeMillis() - start

        val twentyMinutes = TimeUnit.MINUTES.toMillis(20)

        if (elapsed < twentyMinutes) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.hai_gia_finito))
                .setMessage(getString(R.string.popup_fine_anticipata))
                .setNegativeButton(getString(R.string.no_elimina)) { _, _ ->
                    viewModel.stopTraining()
                    // reset UI
                    resetFormAfterStop()
                }
                .setPositiveButton(getString(R.string.si_salva)) { _, _ ->
                    viewModel.stopTraining()
                    resetFormAfterStop()
                }
                .show()
        } else {
            viewModel.stopTraining()
            resetFormAfterStop()
        }
    }

    private fun resetFormAfterStop() {
        // UI cleanup after stop: torna al testo iniziale e nascondi icona
        selectedTrainingLabel = null
        selectedTrainingIcon = null
        startText.text = getString(R.string.inizia_allenamento)
        startIcon.visibility = View.GONE
    }

    private fun updateUiForTrainingState(active: Boolean, paused: Boolean) {

        fun showOnly(view: View) {
            btnAvvia.visibility = View.GONE
            setRunningLocked.visibility = View.GONE
            setPaused.visibility = View.GONE

            btnAvvia.layoutParams = (btnAvvia.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 0f
            }
            setRunningLocked.layoutParams = (setRunningLocked.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 0f
            }
            setPaused.layoutParams = (setPaused.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 0f
            }

            view.visibility = View.VISIBLE
            (view.layoutParams as LinearLayout.LayoutParams).weight = 1f
        }

        if (!active) {
            showOnly(btnAvvia)

            startForm.isClickable = true
            startForm.alpha = 1f
            startArrow.isEnabled = true

            unlockedForActions = false
            btnPausa.isEnabled = false
            btnFine1.isEnabled = false

            btnLock.setImageResource(R.drawable.lucchetto)
            btnLock.backgroundTintList = getColorStateList(R.color.dark_gray)

            // reset text/icon when not active
            startText.text = getString(R.string.inizia_allenamento)
            startIcon.visibility = View.GONE
            selectedTrainingLabel = null
            selectedTrainingIcon = null

        } else {
            startForm.isClickable = false
            startForm.alpha = 0.6f
            startArrow.isEnabled = false

            if (!paused) {
                showOnly(setRunningLocked)

                unlockedForActions = false
                btnPausa.isEnabled = false
                btnFine1.isEnabled = false

                btnLock.setImageResource(R.drawable.lucchetto)
                btnLock.backgroundTintList = getColorStateList(R.color.dark_gray)

            } else {
                showOnly(setPaused)

                unlockedForActions = true
                btnUnlock.setImageResource(R.drawable.lucchetto_aperto)
                btnUnlock.backgroundTintList = getColorStateList(R.color.light_gray)
            }
        }
    }

    private fun showStartSelector() {
        val items = mutableListOf<Pair<Int?, String>>()
        val schede = viewModel.schede.value ?: emptyList()
        for (s in schede) items.add(Pair(R.drawable.foglio, s.nome))
        items.add(Pair(R.drawable.corsa_colori, getString(R.string.corsa)))
        items.add(Pair(R.drawable.boxe_colori, getString(R.string.pugilato)))

        val labels = items.map { it.second }.toTypedArray()
        val adapter = object : ArrayAdapter<String>(this, R.layout.item_action, R.id.item_text, labels) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_action, parent, false)
                val iconView = view.findViewById<ImageView>(R.id.item_icon)
                val textView = view.findViewById<TextView>(R.id.item_text)
                val iconRes = items[position].first
                if (iconRes != null) iconView.setImageResource(iconRes) else iconView.setImageDrawable(null)
                textView.text = items[position].second
                return view
            }
        }

        AlertDialog.Builder(this)
            .setAdapter(adapter) { dialog, which ->
                val label = items[which].second
                val iconRes = items[which].first
                selectedTrainingLabel = label
                selectedTrainingIcon = iconRes
                startText.text = label
                if (iconRes != null) {
                    startIcon.setImageResource(iconRes)
                    startIcon.visibility = View.VISIBLE
                } else {
                    startIcon.visibility = View.GONE
                }
                timerText.text = formatMillis(0)
                dialog.dismiss()
            }
            .show()
    }

    private fun formatMillis(ms: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    }
}
