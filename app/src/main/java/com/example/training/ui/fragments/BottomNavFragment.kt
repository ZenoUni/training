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
import com.example.training.ui.activities.TrainingActivity
import com.example.training.ui.activities.GoalsActivity
import com.example.training.ui.activities.ProgressActivity
import com.example.training.ui.activities.RecapActivity
import com.example.training.ui.activities.CardsActivity

class BottomNavFragment : Fragment() {

    companion object {

        private const val ARG_ACTIVE_INDEX = "arg_active_index"

        /** Creates a new BottomNavFragment with the selected active index */
        fun newInstance(activeIndex: Int) = BottomNavFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ACTIVE_INDEX, activeIndex)
            }
        }
    }

    private var activeIndex: Int = 0

    /** Retrieves the active navigation index from arguments */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeIndex = arguments?.getInt(ARG_ACTIVE_INDEX) ?: 0
    }

    /** Inflates the bottom navigation layout */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false)
    }

    /** Initializes navigation items and click listeners */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val bottomNav = view.findViewById<LinearLayout>(R.id.bottom_navigation)

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

        // Highlight the active navigation item
        icons.forEachIndexed { index, icon ->
            val color = if (index == activeIndex) R.color.black else R.color.gray
            icon.setColorFilter(requireContext().getColor(color))
            texts[index].setTextColor(requireContext().getColor(color))
        }

        val activities = listOf(
            GoalsActivity::class.java,
            CardsActivity::class.java,
            TrainingActivity::class.java,
            ProgressActivity::class.java,
            RecapActivity::class.java
        )

        // Assign click listeners to each navigation item
        navLayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                if (index != activeIndex) {
                    val intent = Intent(requireContext(), activities[index])
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    requireActivity().overridePendingTransition(0, 0)
                }
            }
        }
    }
}
