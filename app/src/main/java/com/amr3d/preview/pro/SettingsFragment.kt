package com.amr3d.preview.pro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view  = inflater.inflate(R.layout.fragment_settings, container, false)
        val prefs = requireContext().getSharedPreferences("amr3d_prefs", Context.MODE_PRIVATE)
        val ctx   = requireContext()

        // ══ وحدة القياس ══
        val unitGroup = view.findViewById<RadioGroup>(R.id.unitGroup)
        unitGroup?.let {
            when (prefs.getString("unit", "MM")) {
                "MM"   -> it.check(R.id.radioMM)
                "CM"   -> it.check(R.id.radioCM)
                "INCH" -> it.check(R.id.radioInch)
            }
            it.setOnCheckedChangeListener { _, id ->
                prefs.edit().putString("unit", when (id) {
                    R.id.radioMM   -> "MM"
                    R.id.radioCM   -> "CM"
                    R.id.radioInch -> "INCH"
                    else -> "MM"
                }).apply()
            }
        }

        // ══ اللغة ══
        val langGroup = view.findViewById<RadioGroup>(R.id.languageGroup)
        langGroup?.let {
            when (prefs.getString("language", "ar")) {
                "ar" -> it.check(R.id.radioArabic)
                "en" -> it.check(R.id.radioEnglish)
                "fr" -> it.check(R.id.radioFrench)
                "es" -> it.check(R.id.radioSpanish)
            }
            it.setOnCheckedChangeListener { _, id ->
                val lang = when (id) {
                    R.id.radioArabic  -> "ar"
                    R.id.radioEnglish -> "en"
                    R.id.radioFrench  -> "fr"
                    R.id.radioSpanish -> "es"
                    else -> "ar"
                }
                prefs.edit().putString("language", lang).apply()
                Toast.makeText(ctx, "✅ اللغة: $lang\nسيُطبَّق بعد إعادة تشغيل التطبيق", Toast.LENGTH_SHORT).show()
                requireActivity().recreate()
            }
        }

        // باقي الكود زي ما هو...
        setupThemeRow(view)

        view.findViewById<Button>(R.id.btnChangeName)?.setOnClickListener { /* ... */ }
        // كمل باقي الـ findViewById بنفس الطريقة

        refreshVersionText(view)
        return view
    }
    
    // باقي الفانكشنز زي ما هي
    private fun refreshVersionText(view: View) { /* ... */ }
    private fun setupThemeRow(view: View) { /* ... */ }
}
