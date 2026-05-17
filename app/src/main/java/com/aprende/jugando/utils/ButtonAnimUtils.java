package com.aprende.jugando.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.aprende.jugando.R;

/**
 * Animación de pulsación tipo rebote para botones (misma sensación que el menú principal).
 * @author José López Mohedano
 */
public final class ButtonAnimUtils {

    private ButtonAnimUtils() {
    }

    /**
     * Añade un toque de animación al pulsar. {@code return false} en el listener
     * permite que el botón reciba el evento completo (clicks, pressed state del drawable).
     */
    public static void addBounce(Context context, View... views) {
        for (View v : views) {
            if (v == null) {
                continue;
            }
            v.setOnTouchListener((view, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.startAnimation(
                            AnimationUtils.loadAnimation(context, R.anim.bounce_scale));
                }
                return false;
            });
        }
    }
}
