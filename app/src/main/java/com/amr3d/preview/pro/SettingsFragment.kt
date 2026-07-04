package com.amr3d.preview.pro

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(requireContext()).apply {
            text = "اللغة / Language"
            textSize = 18f
        }
        layout.addView(title)

        val radioGroup = RadioGroup(requireContext()).apply { id = View.generateViewId() }

        val radioArabic = RadioButton(requireContext()).apply { text = "العربية" }
        val radioEnglish = RadioButton(requireContext()).apply { text = "English" }
        val radioFrench = RadioButton(requireContext()).apply { text = "Français" }
        val radioSpanish = RadioButton(requireContext()).apply { text = "Español" }

        radioGroup.addView(radioArabic)
        radioGroup.addView(radioEnglish)
        radioGroup.addView(radioFrench)
        radioGroup.addView(radioSpanish)
        layout.addView(radioGroup)

        // نحدد المختار من الـ prefs
        when (prefs.getString("language", "en")) {
            "ar" -> radioArabic.isChecked = true
            "en" -> radioEnglish.isChecked = true
            "fr" -> radioFrench.isChecked = true
            "es" -> radioSpanish.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val lang = when (checkedId) {
                radioArabic.id -> "ar"
                radioEnglish.id -> "en"
                radioFrench.id -> "fr"
                radioSpanish.id -> "es"
                else -> "en"
            }
            prefs.edit().putString("language", lang).apply()
            Toast.makeText(context, "سيتم تطبيق اللغة عند اعادة التشغيل", Toast.LENGTH_SHORT).show()
        }

        return layout
    }
}
