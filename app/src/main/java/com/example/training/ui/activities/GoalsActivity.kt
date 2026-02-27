package com.example.training.ui.activities

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.training.R
import com.example.training.data.Card
import com.example.training.ui.base.BottomNavFragment
import com.example.training.viewmodel.AppViewModel
import java.util.*

class GoalsActivity : AppCompatActivity() {

    private val vm: AppViewModel by viewModels()

    private lateinit var tvTitle: TextView
    private lateinit var tvEndOfMonth: TextView
    private lateinit var container: LinearLayout
    private lateinit var btnPrimary: Button
    private lateinit var btnReset: Button

    private val editors = mutableMapOf<String, EditText>()
    private val plusButtons = mutableMapOf<String, Button>()
    private val minusButtons = mutableMapOf<String, Button>()

    private var editing = false

    private val PREFS = "goals_prefs"
    private val KEY_LAST_SAVED_MONTH = "goals_last_saved_month"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(0))
            .commitAllowingStateLoss()

        tvTitle = findViewById(R.id.central_text)
        tvEndOfMonth = findViewById(R.id.end_of_month_text)
        container = findViewById(R.id.goals_list_container)
        btnPrimary = findViewById(R.id.btn_primary)
        btnReset = findViewById(R.id.btn_reset)

        // IMPORTANT: remove material tint
        btnPrimary.backgroundTintList = null
        btnReset.backgroundTintList = null

        tvTitle.text = getString(R.string.goals_title)
        tvEndOfMonth.text = getString(R.string.goals_valid_until, getEndOfMonthString())

        btnPrimary.setOnClickListener { onPrimaryClicked() }
        btnReset.setOnClickListener { onResetClicked() }

        vm.schede.observe(this) { schede ->
            vm.goals.observe(this) { goals ->

                val currentMonthKey = currentMonthKey()
                val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                val savedMonth = prefs.getString(KEY_LAST_SAVED_MONTH, null)

                if (savedMonth != null && savedMonth != currentMonthKey && goals.isNotEmpty()) {
                    vm.clearGoals()
                    prefs.edit().putString(KEY_LAST_SAVED_MONTH, currentMonthKey).apply()
                    vm.loadGoals()
                    return@observe
                }

                editing = goals.isEmpty()
                renderList(schede, goals)
                updateUIState(goals)
            }
            vm.loadGoals()
        }

        vm.loadSchede()
        vm.loadGoals()
    }

    override fun onResume() {
        super.onResume()

        // Ricarica SEMPRE i dati salvati
        vm.loadGoals()
    }

    private fun onPrimaryClicked() {

        if (!editing) {
            editing = true
            updateEditorsEnabled(true)

            setPrimaryButtonMode(
                R.string.save,
                R.drawable.bg_button_green,
                R.color.white
            )

            btnReset.visibility = View.VISIBLE
            return
        }

        val map = mutableMapOf<String, Int>()
        for ((name, et) in editors) {
            val value = et.text.toString().trim().toIntOrNull() ?: 0
            if (value > 0) map[name] = value
        }

        vm.saveGoals(map)
        vm.loadGoals()

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_SAVED_MONTH, currentMonthKey()).apply()

        editing = false
        updateEditorsEnabled(false)

        setPrimaryButtonMode(
            R.string.modify_goals,
            R.drawable.bg_button_yellow,
            R.color.black
        )

        btnReset.visibility = View.GONE

        Toast.makeText(this, getString(R.string.goals_saved_toast), Toast.LENGTH_SHORT).show()
    }

    private fun onResetClicked() {

        // NON tocchiamo il database
        // NON salviamo nulla
        // Reset solo temporaneo della UI

        editors.values.forEach { it.setText("0") }

        // Rimaniamo in modalità modifica
        editing = true
        updateEditorsEnabled(true)

        setPrimaryButtonMode(
            R.string.save,
            R.drawable.bg_button_green,
            R.color.white
        )

        btnReset.visibility = View.VISIBLE
    }

    private fun setPrimaryButtonMode(
        textResId: Int,
        backgroundDrawable: Int,
        textColorRes: Int
    ) {
        btnPrimary.text = getString(textResId)
        btnPrimary.setBackgroundResource(backgroundDrawable)
        btnPrimary.setTextColor(ContextCompat.getColor(this, textColorRes))
        btnPrimary.backgroundTintList = null
    }

    private fun updateEditorsEnabled(enabled: Boolean) {
        for (et in editors.values) {
            et.isEnabled = enabled
            et.isFocusable = enabled
            et.isFocusableInTouchMode = enabled
        }
        for (b in plusButtons.values) b.isEnabled = enabled
        for (b in minusButtons.values) b.isEnabled = enabled
    }

    private fun renderList(schede: List<Card>, goals: Map<String, Int>) {
        container.removeAllViews()
        editors.clear()
        plusButtons.clear()
        minusButtons.clear()

        val items = mutableListOf<String>()
        items.add(getString(R.string.corsa))
        items.add(getString(R.string.pugilato))
        for (s in schede) s.name?.let { items.add(it) }

        for (name in items) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvName = TextView(this).apply {
                text = name
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnMinus = Button(this).apply { text = "-" }
            val et = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText((goals[name] ?: 0).toString())
                gravity = Gravity.CENTER
            }
            val btnPlus = Button(this).apply { text = "+" }

            btnMinus.setOnClickListener {
                val v = et.text.toString().toIntOrNull() ?: 0
                et.setText((v - 1).coerceAtLeast(0).toString())
            }

            btnPlus.setOnClickListener {
                val v = et.text.toString().toIntOrNull() ?: 0
                et.setText((v + 1).toString())
            }

            row.addView(tvName)
            row.addView(btnMinus)
            row.addView(et)
            row.addView(btnPlus)

            container.addView(row)

            editors[name] = et
            plusButtons[name] = btnPlus
            minusButtons[name] = btnMinus
        }

        updateEditorsEnabled(editing)
    }

    private fun updateUIState(goals: Map<String, Int>) {

        if (goals.isEmpty()) {
            editing = true
            updateEditorsEnabled(true)

            setPrimaryButtonMode(
                R.string.save,
                R.drawable.bg_button_green,
                R.color.white
            )

            btnReset.visibility = View.VISIBLE
            btnReset.setBackgroundResource(R.drawable.bg_button_red)
            btnReset.backgroundTintList = null
            btnReset.setTextColor(ContextCompat.getColor(this, R.color.white))

        } else {
            editing = false
            updateEditorsEnabled(false)

            setPrimaryButtonMode(
                R.string.modify_goals,
                R.drawable.bg_button_yellow,
                R.color.black
            )

            btnReset.visibility = View.GONE
        }
    }

    private fun currentMonthKey(): String {
        val cal = Calendar.getInstance()
        return String.format(
            Locale.US,
            "%04d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1
        )
    }

    private fun getEndOfMonthString(): String {
        val cal = Calendar.getInstance()
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        return "$lastDay $monthName $year"
    }
}