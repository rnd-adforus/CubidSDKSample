package com.adforus.cubidsdksample

import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class StatusView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs), LifecycleEventObserver {

    private val infoMark : TextView
    private val textStatus: TextView

    private lateinit var event : Lifecycle.Event

    var state = AdStatus.AD_DEFAULT

    init {
        inflate(context, R.layout.view_status, this)
        layoutParams = LinearLayout.LayoutParams(90.dp(), LayoutParams.WRAP_CONTENT)
        setPadding(8.dp(), 4.dp(), 8.dp(), 4.dp())

        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        setBackgroundResource(R.drawable.box_round)

        val colorBackgroundTint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.normal_gray))

        backgroundTintList = colorBackgroundTint

        context.getLifeCycle()?.addObserver(this)

        infoMark = findViewById<TextView>(R.id.info_mark)
        textStatus = findViewById<TextView>(R.id.text_status)

        val colorText = ContextCompat.getColor(context, R.color.dim_gray)
        val colorTextTint = ColorStateList.valueOf(colorText)
        textStatus.setTextColor(colorTextTint)
        infoMark.backgroundTintList = colorTextTint
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        this.event = event
    }

    fun setStatus(state: AdStatus) {
        this.state = state

        val rect = Rect()
        val visible = this.getGlobalVisibleRect(rect)
        val rectVisible = visible && rect.width() > 0 && rect.height() > 0

        textStatus.text = state.subText

        when(state) {
            AdStatus.FAILED, AdStatus.FAILED_EMPTY -> {
                setInfoColor(R.color.failed_red)
                setInfoBackgroundColor(R.color.failed_red_soft)
                elevation = 5f
            }
            AdStatus.AD_REQ -> {
                setInfoColor(R.color.interaction_amber)
                setInfoBackgroundColor(R.color.interaction_amber_soft)
                elevation = 0f
            }
            AdStatus.AD_DEFAULT -> {
                setInfoColor(R.color.dim_gray)
                setInfoBackgroundColor(R.color.normal_gray)
                elevation = 0f
            }
            else -> {
                setInfoColor(R.color.stable_blue)
                setInfoBackgroundColor(R.color.stable_blue_soft)
                elevation = 0f
            }
        }

        if(::event.isInitialized && (!rectVisible || event == Lifecycle.Event.ON_PAUSE)) {
            Toast.makeText(context, state.subText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setInfoColor(color: Int){
        val colorInt = ContextCompat.getColor(context, color)
        infoMark.backgroundTintList = ColorStateList.valueOf(colorInt)
        textStatus.setTextColor(colorInt)
    }

    private fun setInfoBackgroundColor(color: Int) {
        val colorInt = ContextCompat.getColor(context, color)
        backgroundTintList = ColorStateList.valueOf(colorInt)
    }

    private fun Int.dp() : Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun Context.getLifeCycle(): Lifecycle? {
        return when(this) {
            is LifecycleOwner -> this.lifecycle
            is ContextWrapper -> (baseContext as? LifecycleOwner)?.lifecycle
            else -> null
        }
    }
}