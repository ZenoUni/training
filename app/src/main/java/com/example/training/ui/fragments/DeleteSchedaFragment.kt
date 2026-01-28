package com.example.training.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.training.R
import com.example.training.data.Scheda
import com.example.training.ui.activities.SchedeActivity
import com.example.training.viewmodel.AppViewModel

class DeleteSchedaFragment : DialogFragment() {

    private val viewModel: AppViewModel by activityViewModels()

    private lateinit var formContainer: LinearLayout
    private lateinit var selectLabel: TextView
    private lateinit var selectArrow: ImageView
    private lateinit var previewContainer: LinearLayout
    private lateinit var previewTitle: TextView
    private lateinit var previewEsercizi: TextView
    private lateinit var btnDelete: Button
    private lateinit var btnCancel: Button

    private var selectedIndex: Int = -1
    private var schedeList: List<Scheda> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_delete_scheda, null)

        formContainer = view.findViewById(R.id.delete_form)
        selectLabel = view.findViewById(R.id.delete_select_label)
        selectArrow = view.findViewById(R.id.delete_select_arrow)
        previewContainer = view.findViewById(R.id.delete_preview_container)
        previewTitle = view.findViewById(R.id.delete_preview_title)
        previewEsercizi = view.findViewById(R.id.delete_preview_esercizi)
        btnDelete = view.findViewById(R.id.btn_delete)
        btnCancel = view.findViewById(R.id.btn_cancel)
        selectedIndex = -1
        previewContainer.visibility = View.GONE
        selectLabel.text = getString(R.string.seleziona_scheda_eliminare)


        // inizializza lista
        schedeList = viewModel.schede.value ?: emptyList()

        // se non ci sono schede, mostra testo e disabilita elimina
        if (schedeList.isEmpty()) {
            selectLabel.text = getString(R.string.no_schede)
            btnDelete.isEnabled = false
            btnDelete.alpha = 0.5f
        } else {
            selectLabel.text = getString(R.string.seleziona_scheda_eliminare)
            btnDelete.isEnabled = true
            btnDelete.alpha = 1f
        }

        // click su form o freccia -> mostra menu di selezione
        val openSelector: (View) -> Unit = let@{ _ ->
            if (schedeList.isEmpty()) return@let
        }

        formContainer.setOnClickListener { showSchedeSelector() }
        selectArrow.setOnClickListener { showSchedeSelector() }

        btnDelete.setOnClickListener {
            if (selectedIndex in schedeList.indices) {
                val nome = schedeList[selectedIndex].nome
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.confirm_delete_scheda, nome))
                    .setPositiveButton(getString(R.string.si)) { _, _ ->
                        // elimina dal repository tramite ViewModel
                        viewModel.deleteScheda(selectedIndex)
                        Toast.makeText(requireContext(), getString(R.string.scheda_eliminata), Toast.LENGTH_SHORT).show()
                        // dopo eliminazione chiudi fragment e forza refresh su SchedeActivity
                        dismiss()
                        (activity as? SchedeActivity)?.selectAction(0)
                    }
                    .setNegativeButton(getString(R.string.annulla), null)
                    .show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.seleziona_prima_una_scheda), Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
            (activity as? SchedeActivity)?.selectAction(0)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()


        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }


    // Mostra il selettore con i nomi delle schede
    private fun showSchedeSelector() {
        schedeList = viewModel.schede.value ?: emptyList()
        if (schedeList.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_schede), Toast.LENGTH_SHORT).show()
            return
        }

        val names = schedeList.map { it.nome }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setItems(names) { dialog, which ->
                selectScheda(which)
                dialog.dismiss()
            }
            .show()
    }

    private fun selectScheda(index: Int) {
        schedeList = viewModel.schede.value ?: emptyList()
        if (index !in schedeList.indices) return

        selectedIndex = index
        val s = schedeList[index]

        selectLabel.text = s.nome
        previewContainer.visibility = View.VISIBLE
        previewTitle.text = s.nome
        previewEsercizi.text = s.esercizi
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        // quando chiude il fragment, torna la SchedeActivity a Visualizza tutto
        (activity as? SchedeActivity)?.selectAction(0)
    }
}
