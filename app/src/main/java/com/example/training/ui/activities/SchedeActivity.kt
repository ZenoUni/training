package com.example.training.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.training.R
import com.example.training.data.Scheda
import com.example.training.ui.base.BottomNavFragment
import com.example.training.ui.fragments.CreateSchedaFragment
import com.example.training.ui.fragments.DeleteSchedaFragment
import com.example.training.ui.fragments.EditSchedaFragment
import com.example.training.viewmodel.AppViewModel

class SchedeActivity : AppCompatActivity() {

    private val actions by lazy {
        listOf(
            Pair(R.drawable.occhio, getString(R.string.azione_visualizza)),
            Pair(R.drawable.modifica, getString(R.string.azione_modifica)),
            Pair(R.drawable.add, getString(R.string.azione_crea)),
            Pair(R.drawable.delete, getString(R.string.azione_elimina))
        )
    }

    private val viewModel: AppViewModel by viewModels()

    private lateinit var actionIcon: ImageView
    private lateinit var actionText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var schedeContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schede)

        // Inserisci la BottomNavFragment (activeIndex = 1)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(1))
            .commitAllowingStateLoss()

        findViewById<TextView>(R.id.title_text).text = getString(R.string.le_tue_schede)

        val form = findViewById<LinearLayout>(R.id.form_action)
        actionIcon = findViewById(R.id.action_icon)
        actionText = findViewById(R.id.action_text)
        emptyStateText = findViewById(R.id.empty_state_text)
        schedeContainer = findViewById(R.id.schede_list_container)
        val actionArrow = findViewById<ImageView>(R.id.action_arrow)

        form.setOnClickListener { showActionsDialog() }
        actionArrow.setOnClickListener { showActionsDialog() }

        // osserva le schede dal ViewModel
        viewModel.schede.observe(this, Observer { list ->
            val visualizza = getString(R.string.azione_visualizza)

            // se siamo già in "Visualizza tutto" -> aggiorna la UI direttamente
            if (actionText.text == visualizza) {
                refreshSchedeViews(list)
                emptyStateText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            } else {
                // se eravamo su altro e ora la lista non è vuota -> torna su Visualizza tutto
                if (list.isNotEmpty()) {
                    selectAction(0)
                } else {
                    // se la lista è vuota e siamo su Visualizza, mostra empty state
                    if (actionText.text == visualizza) {
                        emptyStateText.visibility = View.VISIBLE
                    }
                }
            }
        })

        // Carica a startup
        viewModel.loadSchede()
    }

    override fun onResume() {
        super.onResume()
        // Default: "Visualizza tutto"
        selectAction(0)
    }

    /**
     * Seleziona l'azione. Rende public così i fragment possono richiamarla se necessario.
     */
    fun selectAction(index: Int) {
        actionIcon.setImageResource(actions[index].first)
        actionText.text = actions[index].second

        when (index) {
            0 -> { // Visualizza tutto
                viewModel.loadSchede()
            }
            1 -> {
                // Modifica
                val has = !viewModel.schede.value.isNullOrEmpty()
                if (has) {
                    // apri fragment per edit
                    EditSchedaFragment().show(supportFragmentManager, "edit_scheda")
                } else {
                    showNoSchedeDialog()
                }
            }
            3 -> {
                val hasSchede = !viewModel.schede.value.isNullOrEmpty()
                if (hasSchede) {
                    DeleteSchedaFragment().show(supportFragmentManager, "delete_scheda")
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setMessage(getString(R.string.no_schede))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                    // Torna subito su Visualizza tutto
                    selectAction(0)
                }
            }

            2 -> { // Crea scheda
                // Nascondi stato vuoto mentre si crea
                emptyStateText.visibility = View.GONE
                CreateSchedaFragment().show(supportFragmentManager, "create_scheda")
            }
        }
    }

    private fun showActionsDialog() {
        val labels = actions.map { it.second }

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.item_action,
            R.id.item_text,
            labels
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.item_action, parent, false)
                view.findViewById<ImageView>(R.id.item_icon).setImageResource(actions[position].first)
                view.findViewById<TextView>(R.id.item_text).text = actions[position].second
                return view
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setAdapter(adapter) { dialog, which ->
                selectAction(which)
                dialog.dismiss()
            }
            .show()
    }

    private fun showNoSchedeDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage(getString(R.string.no_schede))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun refreshSchedeViews(list: List<Scheda>) {
        schedeContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for (s in list) {
            val item = inflater.inflate(R.layout.item_scheda, schedeContainer, false)
            item.findViewById<TextView>(R.id.scheda_name).text = s.nome
            item.findViewById<TextView>(R.id.scheda_esercizi).text = s.esercizi
            schedeContainer.addView(item)
        }
    }
}
