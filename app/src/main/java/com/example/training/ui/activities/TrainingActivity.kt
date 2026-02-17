package com.example.training.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.training.R
import com.example.training.data.Card
import com.example.training.ui.base.BottomNavFragment
import com.example.training.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Main activity used to start, pause, resume and stop trainings.
 * Shows a date header which updates when system date/time changes.
 * Also shows a "view card" hint when a saved scheda is selected.
 */
class TrainingActivity : AppCompatActivity() {

    private val viewModel: AppViewModel by viewModels()

    // UI - Date and Start form
    private lateinit var dateText: TextView
    private lateinit var startForm: LinearLayout
    private lateinit var startIcon: ImageView
    private lateinit var startText: TextView
    private lateinit var startArrow: ImageView
    private lateinit var timerText: TextView
    private lateinit var btnAvvia: Button

    // quick "view card" TextView (appears only for saved schede)
    private lateinit var tvViewCard: TextView

    // UI - Running state
    private lateinit var setRunningLocked: LinearLayout
    private lateinit var btnPausa: Button
    private lateinit var btnLock: ImageButton
    private lateinit var btnFine1: Button

    // UI - Paused state
    private lateinit var setPaused: LinearLayout
    private lateinit var btnRiprendi: Button
    private lateinit var btnUnlock: ImageButton
    private lateinit var btnFine2: Button

    // Selected training state
    private var selectedTrainingLabel: String? = null
    private var selectedTrainingIcon: Int? = null
    private var schedeList: List<Card> = emptyList()

    // If user selected a saved scheda from the list this holds it; null for built-ins (corsa/pugilato)
    private var selectedCard: Card? = null

    // Lock toggle for running state
    private var runningUnlocked = false

    // Timer handler
    private val handler = Handler(Looper.getMainLooper())
    private val uiRunnable = object : Runnable {
        override fun run() {
            val active = viewModel.trainingActive.value ?: false
            val paused = viewModel.trainingPaused.value ?: false

            val elapsed = when {
                !active -> 0L
                paused -> viewModel.trainingAccumulated.value ?: 0L
                else -> {
                    val start = viewModel.trainingStart.value ?: System.currentTimeMillis()
                    System.currentTimeMillis() - start
                }
            }

            timerText.text = formatMillis(elapsed)
            handler.postDelayed(this, 200)
        }
    }

    // BroadcastReceiver to update date when system date/time/ timezone changes
    private val dateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateDateText()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        // Insert Bottom Navigation (active index = 2)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(2))
            .commitAllowingStateLoss()

        bindUi()
        setupStyles()
        setupForm()
        setupButtons()
        setupObservers()

        viewModel.loadSchede()
        viewModel.loadTrainingState()

        // set initial date text
        updateDateText()
    }

    override fun onStart() {
        super.onStart()
        // register receiver to react to date/time changes while activity is visible
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(dateReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        handler.post(uiRunnable)

        // Reset form if no training is active
        if (viewModel.trainingActive.value != true) {
            resetForm()
        }

        // ensure date is up to date when returning to activity
        updateDateText()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(uiRunnable)
    }

    override fun onStop() {
        super.onStop()
        // unregister receiver to avoid leaks
        try {
            unregisterReceiver(dateReceiver)
        } catch (ignored: IllegalArgumentException) {
        }
    }

    // ---------- UI SETUP ----------

    private fun bindUi() {
        dateText = findViewById(R.id.date_text)
        startForm = findViewById(R.id.start_form)
        startIcon = findViewById(R.id.start_icon)
        startText = findViewById(R.id.start_text)
        startArrow = findViewById(R.id.start_arrow)
        timerText = findViewById(R.id.timer_text)
        btnAvvia = findViewById(R.id.btn_avvia)

        // new view-card TextView placed between form and timer
        tvViewCard = findViewById(R.id.tv_view_card)

        setRunningLocked = findViewById(R.id.set_running_locked)
        btnPausa = findViewById(R.id.btn_pausa)
        btnLock = findViewById(R.id.btn_lock)
        btnFine1 = findViewById(R.id.btn_fine_1)

        setPaused = findViewById(R.id.set_paused)
        btnRiprendi = findViewById(R.id.btn_riprendi)
        btnUnlock = findViewById(R.id.btn_unlock)
        btnFine2 = findViewById(R.id.btn_fine_2)

        startText.text = getString(R.string.inizia_allenamento)
        startIcon.visibility = View.GONE

        // underline the TextView (done in code to be safe) and set italic
        tvViewCard.paintFlags = tvViewCard.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvViewCard.setTypeface(tvViewCard.typeface, android.graphics.Typeface.ITALIC)
        tvViewCard.visibility = View.GONE

        // click shows card detail if selectedCard != null
        tvViewCard.setOnClickListener {
            selectedCard?.let { card -> showCardPopup(card) }
        }
    }

    private fun setupStyles() {
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
    }

    // ---------- FORM LOGIC ----------

    private fun setupForm() {
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
                Toast.makeText(this, R.string.seleziona_prima_un_allenamento, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            viewModel.startTraining(label)
        }
    }

    // ---------- BUTTONS ----------

    private fun setupButtons() {

        // Running lock toggle
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
    }

    // ---------- OBSERVERS ----------

    private fun setupObservers() {

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
    }

    // ---------- UI STATE ----------

    private fun updateUi(active: Boolean, paused: Boolean) {

        btnAvvia.visibility = View.GONE
        setRunningLocked.visibility = View.GONE
        setPaused.visibility = View.GONE

        if (!active) {
            btnAvvia.visibility = View.VISIBLE
            runningUnlocked = false

            // also hide view-card hint when no training active
            selectedCard = null
            updateCardPreviewVisibility()

            return
        }

        if (!paused) {
            setRunningLocked.visibility = View.VISIBLE
            runningUnlocked = false
            btnPausa.isEnabled = false
            btnFine1.isEnabled = false
            btnLock.setImageResource(R.drawable.lucchetto)

            // when running, paused-set buttons should be disabled
            btnRiprendi.isEnabled = false
            btnFine2.isEnabled = false
        } else {
            // paused - show second set
            setPaused.visibility = View.VISIBLE

            // paused set: riprendi and fine should be enabled (unlock always open)
            btnRiprendi.isEnabled = true
            btnFine2.isEnabled = true
            btnUnlock.setImageResource(R.drawable.lucchetto_aperto)
        }
    }

    // ---------- FINISH TRAINING ----------
    // When "FINE" is pressed (either set) handle end-of-training flows.
    private fun handleFinishTraining() {

        val start = viewModel.trainingStart.value
        val accumulated = viewModel.trainingAccumulated.value ?: 0L

        val elapsed = when {
            viewModel.trainingPaused.value == true -> accumulated
            start != null -> accumulated + (System.currentTimeMillis() - start)
            else -> accumulated
        }

        val underTwentyMinutes = elapsed < TimeUnit.MINUTES.toMillis(20)
        val isCorsa = selectedTrainingLabel == getString(R.string.corsa)

        // ---------- CORSA ----------
        if (isCorsa) {

            if (underTwentyMinutes) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.hai_gia_finito))
                    .setMessage(getString(R.string.popup_fine_anticipata))
                    .setNegativeButton(getString(R.string.no_elimina)) { d, _ ->
                        viewModel.stopTraining()
                        resetForm()

                        Toast.makeText(
                            this,
                            getString(R.string.training_deleted),
                            Toast.LENGTH_SHORT
                        ).show()

                        d.dismiss()
                    }
                    .setPositiveButton(getString(R.string.si_salva)) { d, _ ->
                        viewModel.stopTraining()
                        resetForm()
                        d.dismiss()

                        // show distance popup
                        showDistanceInputDialog()
                    }
                    .show()
            } else {
                viewModel.stopTraining()
                resetForm()
                showDistanceInputDialog()
            }
            return
        }

        // ---------- ALLENAMENTI NORMALI ----------
        if (underTwentyMinutes) {

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.hai_gia_finito))
                .setMessage(getString(R.string.popup_fine_anticipata))
                .setNegativeButton(getString(R.string.no_elimina)) { d, _ ->
                    viewModel.stopTraining()
                    resetForm()

                    Toast.makeText(
                        this,
                        getString(R.string.training_deleted),
                        Toast.LENGTH_SHORT
                    ).show()

                    d.dismiss()
                }
                .setPositiveButton(getString(R.string.si_salva)) { d, _ ->
                    viewModel.stopTraining()
                    resetForm()

                    Toast.makeText(
                        this,
                        getString(R.string.training_saved),
                        Toast.LENGTH_SHORT
                    ).show()

                    d.dismiss()
                }
                .show()

        } else {
            viewModel.stopTraining()
            resetForm()

            Toast.makeText(
                this,
                getString(R.string.training_saved),
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    private fun resetForm() {
        selectedTrainingLabel = null
        selectedTrainingIcon = null
        selectedCard = null
        startText.text = getString(R.string.inizia_allenamento)
        startIcon.visibility = View.GONE
        updateCardPreviewVisibility()
    }

    // ---------- SELECTOR ----------

    private fun showStartSelector() {

        // Build items with a parallel list that stores the Card reference (or null for built-ins)
        val items = mutableListOf<Pair<Int?, String>>()
        val itemsCardRef = mutableListOf<Card?>()
        val schede = viewModel.schede.value ?: emptyList()

        for (s in schede) {
            items.add(Pair(R.drawable.foglio, s.name))
            itemsCardRef.add(s)
        }

        // built-ins are added with null cardRef
        items.add(Pair(R.drawable.corsa_colori, getString(R.string.corsa)))
        itemsCardRef.add(null)
        items.add(Pair(R.drawable.boxe_colori, getString(R.string.pugilato)))
        itemsCardRef.add(null)

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
                // set selected label/icon
                selectedTrainingLabel = items[which].second
                selectedTrainingIcon = items[which].first
                startText.text = selectedTrainingLabel
                startIcon.setImageResource(selectedTrainingIcon ?: 0)
                startIcon.visibility = View.VISIBLE

                // set selectedCard: if the chosen item references a saved Card, store it; otherwise null
                selectedCard = itemsCardRef[which]
                updateCardPreviewVisibility()

                d.dismiss()
            }
            .show()
    }

    // Show/hide the "view card" TextView depending if selectedCard exists
    private fun updateCardPreviewVisibility() {
        if (selectedCard != null) {
            tvViewCard.visibility = View.VISIBLE
            tvViewCard.post { tvViewCard.bringToFront() }
        } else {
            tvViewCard.visibility = View.GONE
        }
    }

    // ---------- CARD POPUP ----------
    // Shows a professional dialog: title=name, separator, scrollable body with exercises
    private fun showCardPopup(card: Card) {
        // inflate a small container programmatically
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = resources.getDimensionPixelSize(R.dimen.content_padding_big)
            setPadding(pad, pad, pad, pad)
        }

        // divider
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.divider_height)
            ).apply { topMargin = resources.getDimensionPixelSize(R.dimen.divider_margin_top) }
            setBackgroundColor(ContextCompat.getColor(this@TrainingActivity, R.color.light_gray))
        }

        // scrollable text view for exercises
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.dialog_body_max_height)
            )
        }

        val tv = TextView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = card.exercises ?: ""
            textSize = resources.getDimension(R.dimen.text_size) / resources.displayMetrics.density
            setTextColor(ContextCompat.getColor(this@TrainingActivity, R.color.black))
            // allow long texts to scroll inside the ScrollView
            movementMethod = ScrollingMovementMethod()
        }

        scroll.addView(tv)

        // assemble: container -> divider + scroll
        container.addView(divider)
        container.addView(scroll)

        AlertDialog.Builder(this)
            .setTitle(card.name ?: getString(R.string.scheda_dettaglio))
            .setView(container)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    // ---------- DISTANCE INPUT DIALOG ----------
    // Shows a dialog that asks the user to enter the distance (km) using a numeric decimal input.
    private fun showDistanceInputDialog() {

        val pad = resources.getDimensionPixelSize(R.dimen.content_padding)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val message = TextView(this).apply {
            text = getString(R.string.insert_distance_message)
            setTextColor(ContextCompat.getColor(this@TrainingActivity, R.color.black))
            textSize = resources.getDimension(R.dimen.text_size) / resources.displayMetrics.density
        }

        val input = EditText(this).apply {
            hint = getString(R.string.distance_hint)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            isSingleLine = true
        }

        val btnSave = Button(this).apply {
            text = getString(R.string.salva)
            background = ContextCompat.getDrawable(
                this@TrainingActivity,
                R.drawable.bg_button_green
            )
            setTextColor(ContextCompat.getColor(this@TrainingActivity, R.color.white))
            isEnabled = false
        }

        container.addView(message)
        container.addView(input)
        container.addView(btnSave)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .create()

        val validRegex = Regex("^\\d+(?:[\\.,]\\d+)?$")

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val t = s?.toString()?.trim() ?: ""
                btnSave.isEnabled = validRegex.matches(t)
            }
        })

        btnSave.setOnClickListener {

            val raw = input.text.toString().trim()

            // Always save/display with comma
            val formattedDistance = raw.replace('.', ',')

            // TODO: save formattedDistance in next step

            dialog.dismiss()

            Toast.makeText(
                this,
                getString(R.string.run_training_saved),
                Toast.LENGTH_SHORT
            ).show()
        }

        dialog.show()
    }


    // ---------- UTIL ----------

    private fun formatMillis(ms: Long): String {
        val h = TimeUnit.MILLISECONDS.toHours(ms)
        val m = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(h)
        val s = TimeUnit.MILLISECONDS.toSeconds(ms) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))
        return String.format("%d:%02d:%02d", h, m, s)
    }

    // Update the date header text using device locale
    private fun updateDateText() {
        val today = Calendar.getInstance().time
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        dateText.text = formatter.format(today)
    }
}
