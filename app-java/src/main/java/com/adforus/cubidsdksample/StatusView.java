package com.adforus.cubidsdksample;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class StatusView extends LinearLayout implements LifecycleEventObserver {

    private TextView infoMark;
    private TextView textStatus;
    private Lifecycle.Event event = null;
    private AdStatus state = AdStatus.AD_DEFAULT;

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_status, this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(90),
                LayoutParams.WRAP_CONTENT
        );
        setLayoutParams(params);

        setPadding(dp(8), dp(4), dp(8), dp(4));

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        setBackgroundResource(R.drawable.box_round);
        int backgroundColor = ContextCompat.getColor(context, R.color.normal_gray);
        setBackgroundTintList(ColorStateList.valueOf(backgroundColor));

        Lifecycle lifecycle = getLifeCycle(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }

        int textColor = ContextCompat.getColor(context, R.color.dim_gray);
        ColorStateList textColorTint = ColorStateList.valueOf(textColor);
        infoMark = findViewById(R.id.info_mark);
        textStatus = findViewById(R.id.text_status);

        infoMark.setBackgroundTintList(textColorTint);
        textStatus.setTextColor(textColor);
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        this.event = event;
    }

    public void setState(AdStatus state) {
        this.state = state;

        Rect rect = new Rect();
        boolean visible = getGlobalVisibleRect(rect);
        boolean rectVisible = visible && rect.width() > 0 && rect.height() > 0;

        textStatus.setText(state.getSubText());

        switch (state) {
            case FAILED:
            case FAILED_EMPTY:
                setInfoColor(R.color.failed_red);
                setInfoBackgroundColor(R.color.failed_red_soft);
                setElevation(5f);
                break;
            case AD_REQ:
                setInfoColor(R.color.interaction_amber);
                setInfoBackgroundColor(R.color.interaction_amber_soft);
                setElevation(0f);
                break;
            case AD_DEFAULT:
                setInfoColor(R.color.dim_gray);
                setInfoBackgroundColor(R.color.normal_gray);
                setElevation(0f);
                break;
            default:
                setInfoColor(R.color.stable_blue);
                setInfoBackgroundColor(R.color.stable_blue_soft);
                setElevation(0f);
                break;
        }

        if ( event != null && (!rectVisible || event == Lifecycle.Event.ON_PAUSE)) {
            Toast.makeText(getContext(), state.getSubText(), Toast.LENGTH_SHORT).show();
        }
    }

    public AdStatus getState()  {
        return state;
    }

    private void setInfoColor(int colorResId) {
        int colorInt = ContextCompat.getColor(getContext(), colorResId);
        infoMark.setBackgroundTintList(ColorStateList.valueOf(colorInt));
        textStatus.setTextColor(colorInt);
    }

    private void setInfoBackgroundColor(int colorResId) {
        int colorInt = ContextCompat.getColor(getContext(), colorResId);
        setBackgroundTintList(ColorStateList.valueOf(colorInt));
    }

    private int dp(int value) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (value * density);
    }

    private Lifecycle getLifeCycle(Context context) {
        if (context instanceof LifecycleOwner) {
            return ((LifecycleOwner) context).getLifecycle();
        } else if (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (baseContext instanceof LifecycleOwner) {
                return ((LifecycleOwner) baseContext).getLifecycle();
            }
        }
        return null;
    }
}