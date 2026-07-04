package com.amr3d.preview.pro

import android.content.Context
import android.graphics.Color

/**
 * نظام الثيمات — يحفظ ويطبّق لون التطبيق المختار بشكل ديناميكي.
 * كل ثيم له لون أساسي (accent) ولون خلفية (يبقى داكن دائماً للحفاظ على القراءة).
 */
object AppTheme {

    enum class ThemeColor(val id: String, val nameAr: String, val accent: Int, val accentDark: Int) {
        ORANGE ("orange", "🟠  برتقالي (افتراضي)", Color.parseColor("#FF8A1E"), Color.parseColor("#C46800")),
        BLUE   ("blue",   "🔵  أزرق",              Color.parseColor("#3D8BFF"), Color.parseColor("#1A5FCC")),
        GREEN  ("green",  "🟢  أخضر",              Color.parseColor("#3DDC84"), Color.parseColor("#1FA85C")),
        PURPLE ("purple", "🟣  بنفسجي",            Color.parseColor("#A855F7"), Color.parseColor("#7C2FD6")),
        RED    ("red",    "🔴  أحمر",              Color.parseColor("#FF4757"), Color.parseColor("#CC1F2E")),
        GOLD   ("gold",   "🟡  ذهبي",              Color.parseColor("#FFD23F"), Color.parseColor("#D4A018"));

        companion object {
            fun fromId(id: String): ThemeColor = values().find { it.id == id } ?: ORANGE
        }
    }

    private const val PREFS = "amr3d_prefs"
    private const val KEY_THEME = "app_theme_color"

    fun getCurrent(context: Context): ThemeColor {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, "orange") ?: "orange"
        return ThemeColor.fromId(id)
    }

    fun setCurrent(context: Context, theme: ThemeColor) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, theme.id).apply()
    }

    /** يطبّق لون الثيم على عناصر واجهة شائعة (نصوص، أزرار) برمجياً */
    fun applyToTextView(tv: android.widget.TextView, context: Context) {
        tv.setTextColor(getCurrent(context).accent)
    }
}
