package com.example.training

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class CreateSchedaFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_create_scheda, null)

        val nomeInput = view.findViewById<EditText>(R.id.input_nome)
        val eserciziInput = view.findViewById<EditText>(R.id.input_esercizi)

        val btnSalva = view.findViewById<Button>(R.id.btn_salva)
        val btnAnnulla = view.findViewById<Button>(R.id.btn_annulla)

        btnSalva.setOnClickListener {
            val nome = nomeInput.text.toString().trim()
            val esercizi = eserciziInput.text.toString().trim()

            if (nome.isEmpty() || esercizi.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.compila_tutti_campi))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            } else {
                (activity as? SchedeActivity)?.addScheda(
                    Scheda(nome, esercizi)
                )

                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.scheda_salvata))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        dismiss()
                    }
                    .show()
            }
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

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}
