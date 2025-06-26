package com.adforus.cubidsdksample

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.adforus.cubidsdksample.EtcInteraction.fadeIn
import com.adforus.cubidsdksample.EtcInteraction.fadeOut

object EtcInteraction {
    fun setToggleViewInteraction(toggleButton: ToggleButton, view: View) {
        toggleButton.isChecked = true
        setFadeAnimation(toggleButton.isChecked, view)

        toggleButton.parent?.let {toggleContainer ->
            (toggleContainer as? View)?.setOnClickListener {
                toggleButton.isChecked = !toggleButton.isChecked
            }
        }

        toggleButton.setOnCheckedChangeListener { compoundButton, isCheck ->
            setFadeAnimation(isCheck, view)
        }
    }

    fun popMessageDialog(context: Context, message: String) {
        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setTitle("INFO")
            .setMessage(message)
            .setNeutralButton("확인") { dialog, _->
            }.setPositiveButton("복사 후 닫기"){ dialog, _->
                copyToClipBoard(context, message)
            }.create()

        dialog.show()
        val message = dialog.findViewById<TextView>(android.R.id.message)
        message?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
    }


    private fun copyToClipBoard(context: Context, text: String){
        Handler(Looper.getMainLooper()).post {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Message", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFadeAnimation(isIn: Boolean, view: View){
        if(isIn) { view.fadeIn() }else { view.fadeOut() }
    }

    private fun View.fadeIn(duration: Long = 100){
        scaleY = 0f
        pivotY = 0f
        visibility = View.VISIBLE
        animate()
            .scaleY(1f)
            .setDuration(duration)
            .setListener(null)
    }

    private fun View.fadeOut(duration: Long = 100){
        pivotY = 0f
        animate()
            .scaleY(0f)
            .setDuration(duration)
            .withEndAction { this.visibility = View.GONE }
    }
}