package com.example.training.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.training.R
import com.example.training.data.Scheda
import com.example.training.ui.activities.SchedeActivity
import com.example.training.viewmodel.AppViewModel

class EditSchedaFragment : DialogFragment() {

    private val viewModel: AppViewModel by activityViewModels()

    private lateinit var formContainer: LinearLayout
    private lateinit var selectLabel: TextView
    private lateinit var selectArrow: ImageView

    private lateinit var editContainer: LinearLayout
    private lateinit var nomeEdit: EditText
    private lateinit var eserciziEdit: EditText

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var selectedIndex: Int = -1
    private var schedeList: List<Scheda> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_edit_scheda, null)

        formContainer = view.findViewById(R.id.edit_form)
        selectLabel = view.findViewById(R.id.edit_select_label)
        selectArrow = view.findViewById(R.id.edit_select_arrow)

        editContainer = view.findViewById(R.id.edit_fields_container)
        nomeEdit = view.findViewById(R.id.edit_input_nome)
        eserciziEdit = view.findViewById(R.id.edit_input_esercizi)

        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)

        // inizializza
        schedeList = viewModel.schede.value ?: emptyList()
        selectedIndex = -1
        selectLabel.text = getString(R.string.seleziona_scheda_modificare)
        editContainer.visibility = View.GONE

        // Click su form o freccia -> mostra menu di selezione
        formContainer.setOnClickListener { showSchedeSelector() }
        selectArrow.setOnClickListener { showSchedeSelector() }

        btnSave.setOnClickListener {
            if (selectedIndex !in schedeList.indices) {
                Toast.makeText(requireContext(), getString(R.string.seleziona_prima_una_scheda), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newNome = nomeEdit.text.toString().trim()
            val newEsercizi = eserciziEdit.text.toString().trim()

            if (newNome.isEmpty() || newEsercizi.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.compila_tutti_campi), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Conferma prima di salvare
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.confirm_save_modifiche))
                .setPositiveButton(getString(R.string.si)) { _, _ ->
                    // Effettua update tramite ViewModel
                    viewModel.updateScheda(selectedIndex, Scheda(newNome, newEsercizi))
                    Toast.makeText(requireContext(), getString(R.string.modifiche_salvate), Toast.LENGTH_SHORT).show()
                    dismiss()
                    (activity as? SchedeActivity)?.selectAction(0)
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }

        btnCancel.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.tutte_modifiche_annullate))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    dismiss()
                    (activity as? SchedeActivity)?.selectAction(0)
                }
                .setNegativeButton(getString(R.string.annulla), null)
                .show()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

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
        editContainer.visibility = View.VISIBLE

        // popola i campi con i dati correnti
        nomeEdit.setText(s.nome)
        eserciziEdit.setText(s.esercizi)
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        (activity as? SchedeActivity)?.selectAction(0)
    }
}
