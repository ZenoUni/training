package com.example.training.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.training.R
import com.example.training.ui.activities.AttivitaActivity
import com.example.training.ui.activities.ObiettiviActivity
import com.example.training.ui.activities.ProgressiActivity
import com.example.training.ui.activities.RecapActivity
import com.example.training.ui.activities.SchedeActivity

class BottomNavFragment : Fragment() {

    companion object {
        private const val ARG_ACTIVE_INDEX = "arg_active_index"
        fun newInstance(activeIndex: Int) = BottomNavFragment().apply {
            arguments = Bundle().apply { putInt(ARG_ACTIVE_INDEX, activeIndex) }
        }
    }

    private var activeIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeIndex = arguments?.getInt(ARG_ACTIVE_INDEX) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate fragment layout which contains the nav
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Trova il layout della nav all'interno del fragment_bottom_nav
        val bottomNav = view.findViewById<LinearLayout>(R.id.bottom_navigation)

        // icone
        val icons = listOf(
            bottomNav.findViewById<ImageView>(R.id.icon_obiettivi),
            bottomNav.findViewById<ImageView>(R.id.icon_schede),
            bottomNav.findViewById<ImageView>(R.id.icon_attivita),
            bottomNav.findViewById<ImageView>(R.id.icon_progressi),
            bottomNav.findViewById<ImageView>(R.id.icon_recap)
        )

        val texts = listOf(
            bottomNav.findViewById<TextView>(R.id.text_obiettivi),
            bottomNav.findViewById<TextView>(R.id.text_schede),
            bottomNav.findViewById<TextView>(R.id.text_attivita),
            bottomNav.findViewById<TextView>(R.id.text_progressi),
            bottomNav.findViewById<TextView>(R.id.text_recap)
        )

        val navLayouts = listOf(
            bottomNav.findViewById<LinearLayout>(R.id.nav_obiettivi),
            bottomNav.findViewById<LinearLayout>(R.id.nav_schede),
            bottomNav.findViewById<LinearLayout>(R.id.nav_attivita),
            bottomNav.findViewById<LinearLayout>(R.id.nav_progressi),
            bottomNav.findViewById<LinearLayout>(R.id.nav_recap)
        )

        // evidenzia
        icons.forEachIndexed { i, icon ->
            val color = if (i == activeIndex) R.color.black else R.color.gray
            icon.setColorFilter(requireContext().getColor(color))
            texts[i].setTextColor(requireContext().getColor(color))
        }

        // attività da aprire
        val activities = listOf(
            ObiettiviActivity::class.java,
            SchedeActivity::class.java,
            AttivitaActivity::class.java,
            ProgressiActivity::class.java,
            RecapActivity::class.java
        )

        navLayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                if (index != activeIndex) {
                    val intent = Intent(requireContext(), activities[index])
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    requireActivity().overridePendingTransition(0, 0)
                }
            }
        }
    }
}
