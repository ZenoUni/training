package com.example.training.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.training.R
import com.example.training.data.Scheda
import com.example.training.viewmodel.AppViewModel

class CreateSchedaFragment : DialogFragment() {

    private val viewModel: AppViewModel by activityViewModels()

    private lateinit var nomeInput: EditText
    private lateinit var eserciziInput: EditText
    private lateinit var btnSalva: Button
    private lateinit var btnAnnulla: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_scheda, container, false)

        nomeInput = view.findViewById(R.id.input_nome)
        eserciziInput = view.findViewById(R.id.input_esercizi)
        btnSalva = view.findViewById(R.id.btn_salva)
        btnAnnulla = view.findViewById(R.id.btn_annulla)

        btnSalva.setOnClickListener {
            val nome = nomeInput.text.toString().trim()
            val esercizi = eserciziInput.text.toString().trim()

            if (nome.isEmpty() || esercizi.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.compila_tutti_campi), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addScheda(Scheda(nome, esercizi))
            Toast.makeText(requireContext(), getString(R.string.scheda_salvata), Toast.LENGTH_SHORT).show()
            dismiss()
        }

        btnAnnulla.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.conferma_annulla))
                .setPositiveButton(getString(R.string.si)) { _, _ ->
                    dismiss()
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
