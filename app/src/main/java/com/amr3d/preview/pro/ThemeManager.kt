package com.amr3d.preview.pro

import android.app.Activity
import android.content.res.ColorStateList
import android.widget.ProgressBar
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ToggleButton
import android.view.ViewGroup

/**
 * ThemeManager — تطبيق الثيم على كل عناصر التطبيق بوحدة مركزية
 */
object ThemeManager {
    
    fun applyToActivity(activity: AppCompatActivity) {
        val theme = AppTheme.getCurrent(activity) // دي بترجع ThemeColor
        
        // تطبيق على BottomNavigationView
        activity.findViewById<BottomNavigationView?>(R.id.bottomNav)?.let {
            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            )
            val colors = intArrayOf(theme.accent, 0xFF888.toInt())
            val csl = ColorStateList(states, colors)
            it.itemIconTintList = csl
            it.itemTextColor = csl
        }
        
        // تطبيق الألوان على كل عناصر الواجهة
        val rootView = activity.window.decorView.findViewById<android.view.View>(android.R.id.content)
        applyThemeRecursive(rootView, activity, theme)
    }
    
    // هنا كان الغلط: غيرت AppTheme.Theme -> AppTheme.ThemeColor
    private fun applyThemeRecursive(view: android.view.View?, activity: Activity, theme: AppTheme.ThemeColor) {
        if (view == null) return
        
        when (view) {
            is ProgressBar -> {
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_enabled)
                )
                val colors = intArrayOf(theme.accent, 0xFF666.toInt())
                view.progressTintList = ColorStateList(states, colors)
            }
            is Button -> {
                view.setTextColor(theme.accent)
            }
            is ToggleButton -> {
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val colors = intArrayOf(theme.accent, 0xFF888.toInt())
                view.setTextColor(ColorStateList(states, colors))
            }
            is ImageButton -> {
                view.imageTintList = ColorStateList.valueOf(theme.accent)
            }
            is AppCompatImageButton -> {
                view.imageTintList = ColorStateList.valueOf(theme.accent)
            }
        }
        
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyThemeRecursive(view.getChildAt(i), activity, theme)
            }
        }
    }
}
