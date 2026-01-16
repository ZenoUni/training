package com.example.training

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity(private val activeIndex: Int) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carica il layout specifico della Activity concreta
        setContentView(getLayoutId())

        // Setup della navbar
        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)
        setupNavbar(bottomNav)
    }

    private fun setupNavbar(bottomNav: LinearLayout) {

        // Lista delle icone
        val icons = listOf(
            bottomNav.findViewById<ImageView>(R.id.icon_obiettivi),
            bottomNav.findViewById(R.id.icon_schede),
            bottomNav.findViewById(R.id.icon_attivita),
            bottomNav.findViewById(R.id.icon_progressi),
            bottomNav.findViewById(R.id.icon_recap)
        )

        // Lista dei testi
        val texts = listOf(
            bottomNav.findViewById<TextView>(R.id.text_obiettivi),
            bottomNav.findViewById(R.id.text_schede),
            bottomNav.findViewById(R.id.text_attivita),
            bottomNav.findViewById(R.id.text_progressi),
            bottomNav.findViewById(R.id.text_recap)
        )

        // Lista dei layout cliccabili
        val navLayouts = listOf(
            bottomNav.findViewById<LinearLayout>(R.id.nav_obiettivi),
            bottomNav.findViewById(R.id.nav_schede),
            bottomNav.findViewById(R.id.nav_attivita),
            bottomNav.findViewById(R.id.nav_progressi),
            bottomNav.findViewById(R.id.nav_recap)
        )

        // Evidenzia la pagina corrente
        icons.forEachIndexed { i, icon ->
            val color = if (i == activeIndex) R.color.black else R.color.gray
            icon.setColorFilter(getColor(color))
            texts[i].setTextColor(getColor(color))
        }

        // Lista delle Activity concrete da aprire
        val activities = listOf(
            ObiettiviActivity::class.java,
            SchedeActivity::class.java,
            AttivitaActivity::class.java,
            ProgressiActivity::class.java,
            RecapActivity::class.java
        )

        // Click listener per cambiare pagina
        navLayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                if (index != activeIndex) {
                    val intent = Intent(this, activities[index])
                    // Gestione back stack per evitare duplicati
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
        }
    }

    /**
     * Metodo da sovrascrivere in ogni Activity concreta per restituire il layout corretto
     */
    open fun getLayoutId(): Int = R.layout.activity_attivita
}
