package com.adforus.cubidsdksample;

import android.os.Build;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BaseSystemSetting {
    public static void setInsetDirection(View view) {
        if (Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout());

                int topInset = Math.max(systemInsets.top, cutoutInsets.top);
                int bottomInset = Math.max(systemInsets.bottom, cutoutInsets.bottom);

                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop() + topInset,
                        v.getPaddingRight(),
                        v.getPaddingBottom() + bottomInset
                );

                return WindowInsetsCompat.CONSUMED;
            });
        }
    }
}
