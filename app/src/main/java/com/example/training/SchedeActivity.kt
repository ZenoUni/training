package com.example.training

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog

class SchedeActivity : BaseActivity(1) {

    // Lista azioni: icona + testo
    private val actions by lazy {
        listOf(
            Pair(R.drawable.occhio, getString(R.string.azione_visualizza)),
            Pair(R.drawable.modifica, getString(R.string.azione_modifica)),
            Pair(R.drawable.add, getString(R.string.azione_crea)),
            Pair(R.drawable.delete, getString(R.string.azione_elimina))
        )
    }

    // Lista schede (per ora vuota)
    private val schede = emptyList<String>()

    // Componenti UI
    private lateinit var actionIcon: ImageView
    private lateinit var actionText: TextView
    private lateinit var emptyStateText: TextView

    override fun getLayoutId() = R.layout.activity_schede

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Titolo
        findViewById<TextView>(R.id.title_text).text = getString(R.string.le_tue_schede)

        val form = findViewById<LinearLayout>(R.id.form_action)
        actionIcon = findViewById(R.id.action_icon)
        actionText = findViewById(R.id.action_text)
        emptyStateText = findViewById(R.id.empty_state_text)
        val actionArrow = findViewById<ImageView>(R.id.action_arrow)

        // Click listener per aprire il menu
        form.setOnClickListener { showActionsDialog() }
        actionArrow.setOnClickListener { showActionsDialog() }
    }

    override fun onResume() {
        super.onResume()
        // Ogni volta che l'utente torna qui, imposta default "Visualizza tutto"
        selectAction(0)
    }

    // ===== FUNZIONI MEMBRO =====

    // Seleziona un'azione
    private fun selectAction(index: Int) {
        actionIcon.setImageResource(actions[index].first)
        actionText.text = actions[index].second

        when (index) {
            0 -> {
                // Visualizza tutto
                emptyStateText.visibility = if (schede.isEmpty()) TextView.VISIBLE else TextView.GONE
            }
            1, 3 -> {
                // Modifica o Elimina
                if (schede.isEmpty()) showNoSchedeDialog()
            }
            2 -> {
                // Crea scheda → nascondi testo grigio
                emptyStateText.visibility = TextView.GONE
            }
        }
    }

    // Mostra popup "Non hai ancora schede salvate"
    private fun showNoSchedeDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.no_schede))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    // Mostra menu a tendina con icona + testo
    private fun showActionsDialog() {
        val labels = actions.map { it.second }
        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.item_action,
            R.id.item_text,
            labels
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup) =
                (convertView ?: layoutInflater.inflate(R.layout.item_action, parent, false)).apply {
                    findViewById<ImageView>(R.id.item_icon).setImageResource(actions[position].first)
                    findViewById<TextView>(R.id.item_text).text = actions[position].second
                }
        }

        AlertDialog.Builder(this)
            .setAdapter(adapter) { dialog, which ->
                selectAction(which)
                dialog.dismiss()
            }
            .show()
    }
}
