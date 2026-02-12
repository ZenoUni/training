package com.example.training.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.training.R
import com.example.training.data.Card
import com.example.training.ui.activities.CardsActivity
import com.example.training.viewmodel.AppViewModel

class DeleteCardFragment : DialogFragment() {

    private val viewModel: AppViewModel by activityViewModels()

    // UI components
    private lateinit var formContainer: LinearLayout
    private lateinit var selectLabel: TextView
    private lateinit var selectArrow: ImageView
    private lateinit var previewContainer: LinearLayout
    private lateinit var previewTitle: TextView
    private lateinit var previewEsercizi: TextView
    private lateinit var btnDelete: Button
    private lateinit var btnCancel: Button

    private var selectedIndex: Int = -1
    private var schedeList: List<Card> = emptyList()

    /** Creates dialog and initializes UI */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_delete_card, null)

        formContainer = view.findViewById(R.id.delete_form)
        selectLabel = view.findViewById(R.id.delete_select_label)
        selectArrow = view.findViewById(R.id.delete_select_arrow)
        previewContainer = view.findViewById(R.id.delete_preview_container)
        previewTitle = view.findViewById(R.id.delete_preview_title)
        previewEsercizi = view.findViewById(R.id.delete_preview_esercizi)
        btnDelete = view.findViewById(R.id.btn_delete)
        btnCancel = view.findViewById(R.id.btn_cancel)

        previewContainer.visibility = View.GONE
        schedeList = viewModel.schede.value ?: emptyList()

        if (schedeList.isEmpty()) {
            selectLabel.text = getString(R.string.no_schede)
            btnDelete.isEnabled = false
            btnDelete.alpha = 0.5f
        } else {
            selectLabel.text = getString(R.string.seleziona_scheda_eliminare)
        }

        formContainer.setOnClickListener { showSchedeSelector() }
        selectArrow.setOnClickListener { showSchedeSelector() }

        btnDelete.setOnClickListener { handleDelete() }
        btnCancel.setOnClickListener {
            dismiss()
            (activity as? CardsActivity)?.selectAction(0)
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    /** Displays schede selector dialog */
    private fun showSchedeSelector() {
        schedeList = viewModel.schede.value ?: emptyList()
        if (schedeList.isEmpty()) return

        val names = schedeList.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setItems(names) { dialog, which ->
                selectScheda(which)
                dialog.dismiss()
            }
            .show()
    }

    /** Selects a scheda and displays preview */
    private fun selectScheda(index: Int) {
        if (index !in schedeList.indices) return

        selectedIndex = index
        val s = schedeList[index]

        selectLabel.text = s.name
        previewContainer.visibility = View.VISIBLE
        previewTitle.text = s.name
        previewEsercizi.text = s.exercises
    }

    /** Handles delete confirmation and removal */
    private fun handleDelete() {
        if (selectedIndex !in schedeList.indices) {
            Toast.makeText(requireContext(),
                getString(R.string.seleziona_prima_una_scheda),
                Toast.LENGTH_SHORT).show()
            return
        }

        val nome = schedeList[selectedIndex].name

        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.confirm_delete_scheda, nome))
            .setPositiveButton(getString(R.string.si)) { _, _ ->
                viewModel.deleteScheda(selectedIndex)
                Toast.makeText(requireContext(),
                    getString(R.string.scheda_eliminata),
                    Toast.LENGTH_SHORT).show()
                dismiss()
                (activity as? CardsActivity)?.selectAction(0)
            }
            .setNegativeButton(getString(R.string.annulla), null)
            .show()
    }

    /** Restores activity state when dialog is dismissed */
    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        (activity as? CardsActivity)?.selectAction(0)
    }
}
