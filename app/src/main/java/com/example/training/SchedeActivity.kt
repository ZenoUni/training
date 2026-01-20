package com.example.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SchedeActivity : BaseActivity(1) {

    // =========================
    // COSTANTI
    // =========================
    private val prefsName = "schede_prefs"
    private val schedeKey = "schede_list"
    private val gson = Gson()

    // =========================
    // AZIONI DISPONIBILI
    // =========================
    private val actions by lazy {
        listOf(
            Pair(R.drawable.occhio, getString(R.string.azione_visualizza)),
            Pair(R.drawable.modifica, getString(R.string.azione_modifica)),
            Pair(R.drawable.add, getString(R.string.azione_crea)),
            Pair(R.drawable.delete, getString(R.string.azione_elimina))
        )
    }

    // =========================
    // DATI
    // =========================
    private val schede: MutableList<Scheda> = mutableListOf()

    // =========================
    // UI
    // =========================
    private lateinit var actionIcon: ImageView
    private lateinit var actionText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var schedeContainer: LinearLayout

    override fun getLayoutId() = R.layout.activity_schede

    // =========================
    // LIFECYCLE
    // =========================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Titolo
        findViewById<TextView>(R.id.title_text).text =
            getString(R.string.le_tue_schede)

        // Bind UI
        val form = findViewById<LinearLayout>(R.id.form_action)
        actionIcon = findViewById(R.id.action_icon)
        actionText = findViewById(R.id.action_text)
        emptyStateText = findViewById(R.id.empty_state_text)
        schedeContainer = findViewById(R.id.schede_list_container)
        val actionArrow = findViewById<ImageView>(R.id.action_arrow)

        // Click form / freccia
        form.setOnClickListener { showActionsDialog() }
        actionArrow.setOnClickListener { showActionsDialog() }

        // Carica schede salvate
        loadSchede()
    }

    override fun onResume() {
        super.onResume()
        // Ogni volta che si entra → Visualizza tutto
        selectAction(0)
    }

    // =========================
    // AZIONI
    // =========================
    private fun selectAction(index: Int) {
        actionIcon.setImageResource(actions[index].first)
        actionText.text = actions[index].second

        when (index) {
            0 -> { // Visualizza tutto
                refreshSchedeViews()
                emptyStateText.visibility =
                    if (schede.isEmpty()) View.VISIBLE else View.GONE
            }

            1, 3 -> { // Modifica / Elimina
                if (schede.isEmpty()) {
                    showNoSchedeDialog()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.non_implementato_ancora),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            2 -> { // Crea scheda
                emptyStateText.visibility = View.GONE
                CreateSchedaFragment()
                    .show(supportFragmentManager, "create_scheda")
            }
        }
    }

    // =========================
    // DIALOG AZIONI
    // =========================
    private fun showActionsDialog() {
        val labels = actions.map { it.second }

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.item_action,
            R.id.item_text,
            labels
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                val view = convertView
                    ?: layoutInflater.inflate(R.layout.item_action, parent, false)

                view.findViewById<ImageView>(R.id.item_icon)
                    .setImageResource(actions[position].first)

                view.findViewById<TextView>(R.id.item_text).text =
                    actions[position].second

                return view
            }
        }

        AlertDialog.Builder(this)
            .setAdapter(adapter) { dialog, which ->
                selectAction(which)
                dialog.dismiss()
            }
            .show()
    }

    // =========================
    // EMPTY STATE
    // =========================
    private fun showNoSchedeDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.no_schede))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    // =========================
    // CRUD
    // =========================
    fun addScheda(scheda: Scheda) {
        schede.add(0, scheda)
        saveSchede()
        refreshSchedeViews()
        emptyStateText.visibility = View.GONE
    }

    // =========================
    // UI LISTA
    // =========================
    private fun refreshSchedeViews() {
        schedeContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)
        for (s in schede) {
            val item = inflater.inflate(
                R.layout.item_scheda,
                schedeContainer,
                false
            )

            item.findViewById<TextView>(R.id.scheda_name).text = s.nome
            item.findViewById<TextView>(R.id.scheda_esercizi).text = s.esercizi

            schedeContainer.addView(item)
        }
    }

    // =========================
    // PERSISTENZA
    // =========================
    private fun saveSchede() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        prefs.edit()
            .putString(schedeKey, gson.toJson(schede))
            .apply()
    }

    private fun loadSchede() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val json = prefs.getString(schedeKey, null)

        if (json != null) {
            val type = object : TypeToken<MutableList<Scheda>>() {}.type
            val savedSchede: MutableList<Scheda> =
                gson.fromJson(json, type)

            schede.clear()
            schede.addAll(savedSchede)
        }
    }
}
