package com.adforus.cubidsdksample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;

public class EtcInteraction {

    public static void setToggleViewInteraction(final ToggleButton toggleButton, final View view) {
        toggleButton.setChecked(true);
        setFadeAnimation(toggleButton.isChecked(), view);

        View parent = (View) toggleButton.getParent();
        if (parent != null) {
            parent.setOnClickListener(v -> toggleButton.setChecked(!toggleButton.isChecked()));
        }

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> setFadeAnimation(isChecked, view));
    }

    public static void popMessageDialog(Context context, String message) {
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setTitle("INFO")
                .setMessage(message)
                .setNeutralButton("확인", (dialogInterface, i) -> {})
                .setPositiveButton("복사 후 닫기", (dialogInterface, i) -> copyToClipboard(context, message))
                .create();

        dialog.show();

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        }
    }

    private static void copyToClipboard(Context context, String text) {
        new Handler(Looper.getMainLooper()).post(() -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Message", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show();
        });
    }

    private static void setFadeAnimation(boolean isIn, View view) {
        if (isIn) {
            fadeIn(view);
        } else {
            fadeOut(view);
        }
    }

    private static void fadeIn(View view) {
        view.setScaleY(0f);
        view.setPivotY(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .scaleY(1f)
                .setDuration(100)
                .setListener(null);
    }

    private static void fadeOut(View view) {
        view.setPivotY(0f);
        view.animate()
                .scaleY(0f)
                .setDuration(100)
                .withEndAction(() -> view.setVisibility(View.GONE));
    }
}