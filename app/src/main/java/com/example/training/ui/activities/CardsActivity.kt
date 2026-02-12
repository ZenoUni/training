package com.example.training.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.training.R
import com.example.training.data.Card
import com.example.training.ui.base.BottomNavFragment
import com.example.training.ui.fragments.CreateCardFragment
import com.example.training.ui.fragments.DeleteCardFragment
import com.example.training.ui.fragments.EditCardFragment
import com.example.training.viewmodel.AppViewModel

/**
 * Activity that manages training cards (view, create, edit, delete).
 * Uses AppViewModel for all repository interactions.
 */
class CardsActivity : AppCompatActivity() {

    // Available actions with icon + label
    private val actions by lazy {
        listOf(
            Pair(R.drawable.occhio, getString(R.string.azione_visualizza)),
            Pair(R.drawable.modifica, getString(R.string.azione_modifica)),
            Pair(R.drawable.add, getString(R.string.azione_crea)),
            Pair(R.drawable.delete, getString(R.string.azione_elimina))
        )
    }

    private val viewModel: AppViewModel by viewModels()

    // UI references
    private lateinit var actionIcon: ImageView
    private lateinit var actionText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var schedeContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards)

        // Insert Bottom Navigation (active index = 1)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottom_nav_container, BottomNavFragment.newInstance(1))
            .commitAllowingStateLoss()

        findViewById<TextView>(R.id.title_text).text = getString(R.string.le_tue_schede)

        val form = findViewById<LinearLayout>(R.id.form_action)
        val actionArrow = findViewById<ImageView>(R.id.action_arrow)

        actionIcon = findViewById(R.id.action_icon)
        actionText = findViewById(R.id.action_text)
        emptyStateText = findViewById(R.id.empty_state_text)
        schedeContainer = findViewById(R.id.schede_list_container)

        form.setOnClickListener { showActionsDialog() }
        actionArrow.setOnClickListener { showActionsDialog() }

        // Observer for schede list - update UI and empty state robustly
        viewModel.schede.observe(this) { list ->
            Log.d("CARDS_ACTIVITY", "Observer -> schede list size = ${list.size}")
            list.forEachIndexed { idx, c ->
                Log.d("CARDS_ACTIVITY", "  [$idx] name='${c.name}', exercises='${c.exercises?.take(60)}'")
            }

            val viewAll = getString(R.string.azione_visualizza)
            val isViewAll = actionText.text.toString() == viewAll

            // Always refresh the visual list
            refreshSchedeViews(list)

            // Show empty state only when user is on "View all" and list is empty
            val shouldShowEmpty = isViewAll && list.isEmpty()
            emptyStateText.visibility = if (shouldShowEmpty) View.VISIBLE else View.GONE
            Log.d("CARDS_ACTIVITY", "emptyState visibility = ${emptyStateText.visibility}")

            // ensure emptyStateText is on top (in case of overlay issues)
            if (shouldShowEmpty) {
                emptyStateText.post { emptyStateText.bringToFront() }
            }
        }

        // Initial load and ensure empty-state is correct if list already loaded
        viewModel.loadSchede()

        // Log current cached value immediately after loadSchede() call (may be empty until observer runs)
        val current = viewModel.schede.value ?: emptyList()
        Log.d("CARDS_ACTIVITY", "onCreate -> immediate schede value size = ${current.size}")
        current.forEachIndexed { idx, c ->
            Log.d("CARDS_ACTIVITY", "  [immediate $idx] name='${c.name}', exercises='${c.exercises?.take(60)}'")
        }

        // Initial empty-state fallback (observer will overwrite as soon as it runs)
        emptyStateText.visibility = if (current.isEmpty()) View.VISIBLE else View.GONE
        if (emptyStateText.visibility == View.VISIBLE) emptyStateText.post { emptyStateText.bringToFront() }
    }

    override fun onResume() {
        super.onResume()
        // Default action: View all
        selectAction(0)
    }

    /** Selects an action from the top form. */
    fun selectAction(index: Int) {
        actionIcon.setImageResource(actions[index].first)
        actionText.text = actions[index].second

        when (index) {
            0 -> { // View all
                viewModel.loadSchede()
            }

            1 -> { // Edit
                if (!viewModel.schede.value.isNullOrEmpty()) {
                    EditCardFragment().show(supportFragmentManager, "edit_card")
                } else {
                    showNoSchedeDialog()
                }
            }

            2 -> { // Create
                // hide empty state while creating
                emptyStateText.visibility = View.GONE
                CreateCardFragment().show(supportFragmentManager, "create_card")
            }

            3 -> { // Delete
                if (!viewModel.schede.value.isNullOrEmpty()) {
                    DeleteCardFragment().show(supportFragmentManager, "delete_card")
                } else {
                    showNoSchedeDialog()
                    selectAction(0)
                }
            }
        }
    }

    /** Shows action selector dialog. */
    private fun showActionsDialog() {
        val labels = actions.map { it.second }

        val adapter = object :
            ArrayAdapter<String>(this, R.layout.item_action, R.id.item_text, labels) {

            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    R.layout.item_action,
                    parent,
                    false
                )
                view.findViewById<ImageView>(R.id.item_icon)
                    .setImageResource(actions[position].first)
                view.findViewById<TextView>(R.id.item_text)
                    .text = actions[position].second
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

    /** Shows alert when no cards are available. */
    private fun showNoSchedeDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.no_schede))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    /** Refreshes cards UI list and also updates empty-state as a defensive measure. */
    private fun refreshSchedeViews(list: List<Card>) {
        schedeContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (card in list) {
            val item = inflater.inflate(R.layout.item_card, schedeContainer, false)
            item.findViewById<TextView>(R.id.scheda_name).text = card.name
            item.findViewById<TextView>(R.id.scheda_esercizi).text = card.exercises
            schedeContainer.addView(item)
        }

        // Defensive: if the list is empty, ensure the empty-state is visible (only if we are in view mode)
        val viewAll = getString(R.string.azione_visualizza)
        val isViewAll = actionText.text.toString() == viewAll
        val shouldShowEmpty = isViewAll && list.isEmpty()
        emptyStateText.visibility = if (shouldShowEmpty) View.VISIBLE else View.GONE
        if (shouldShowEmpty) emptyStateText.post { emptyStateText.bringToFront() }

        Log.d("CARDS_ACTIVITY", "refreshSchedeViews -> listSize=${list.size}, emptyVisible=${emptyStateText.visibility}")
    }
}
