package com.sadsiyoon.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;

/**
 * Branded, non-blocking two-second splash. No network, catalog parsing or
 * button press is needed before opening the student's home screen.
 */
public class IntroActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navigating = false;
    private final Runnable openHome = this::enter;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        FrameLayout screen = new FrameLayout(this);
        screen.setBackground(Ui.gradient(this, 0xff162F58, Ui.BG, 0));
        ViewCompat.setOnApplyWindowInsetsListener(screen, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            screen.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        LinearLayout body = Ui.col(this);
        body.setGravity(Gravity.CENTER);
        Ui.pad(this, body, 24, 24);
        TextView eyebrow = Ui.txt(this, "S A D E S I O N", 12, Ui.MINT, true);
        eyebrow.setGravity(Gravity.CENTER);
        body.addView(eyebrow, Ui.lp(this, -1, 28));

        FrameLayout emblem = new FrameLayout(this);
        emblem.setBackground(Ui.stroke(this, 0xff1A355D, 32, 0xff365C8B));
        emblem.setClipToOutline(true);
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(
            Ui.dp(this, 116), Ui.dp(this, 116), Gravity.CENTER);
        emblem.addView(Ui.logo(this, 116, false), logoParams);
        LinearLayout.LayoutParams emblemParams = Ui.lp(this, 156, 156);
        emblemParams.gravity = Gravity.CENTER_HORIZONTAL;
        emblemParams.topMargin = Ui.dp(this, 12);
        body.addView(emblem, emblemParams);

        TextView brand = Ui.txt(this, "سادسيون", 34, Ui.WHITE, true);
        brand.setGravity(Gravity.CENTER);
        body.addView(brand, Ui.top(this, -1, 53, 15));

        TextView title = Ui.txt(this, "منصة السادس الإعدادي", 17, Ui.WHITE, true);
        title.setGravity(Gravity.CENTER);
        body.addView(title, Ui.top(this, -1, 35, 3));

        TextView subtitle = Ui.txt(this, "محاضراتك، وزارياتك، وتقدمك بمكان واحد", 13, Ui.MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        body.addView(subtitle, Ui.top(this, -1, 41, 3));

        View accent = new View(this);
        accent.setBackground(Ui.round(this, Ui.MINT, 3));
        LinearLayout.LayoutParams accentParams = Ui.lp(this, 64, 4);
        accentParams.gravity = Gravity.CENTER_HORIZONTAL;
        accentParams.topMargin = Ui.dp(this, 26);
        body.addView(accent, accentParams);

        FrameLayout.LayoutParams bodyParams = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        screen.addView(body, bodyParams);

        TextView footer = Ui.txt(this, "رحلة تعلم أوضح", 12, Ui.MUTED, false);
        footer.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams footerParams = new FrameLayout.LayoutParams(-1, Ui.dp(this, 50), Gravity.BOTTOM);
        footerParams.bottomMargin = Ui.dp(this, 12);
        screen.addView(footer, footerParams);

        setContentView(screen);
        ViewCompat.requestApplyInsets(screen);
        body.setAlpha(0f);
        body.setTranslationY(Ui.dp(this, 14));
        body.animate().alpha(1f).translationY(0f).setDuration(480).start();
        handler.postDelayed(openHome, 2000);
    }

    private void enter() {
        if (navigating || isFinishing() || isDestroyed()) return;
        navigating = true;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(openHome);
        super.onDestroy();
    }
}
