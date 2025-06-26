package com.adforus.cubidsdksample

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

object BaseSystemSetting {
    fun setInsetDirection(view: View){
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
                val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val topInset = max(systemInsets.top, cutoutInsets.top)
                val bottomInset = max(systemInsets.bottom, cutoutInsets.bottom)

                view.setPadding(view.paddingLeft, view.paddingTop + topInset, view.paddingRight, view.paddingBottom + bottomInset)
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}