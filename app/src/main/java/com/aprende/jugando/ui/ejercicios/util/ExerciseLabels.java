package com.aprende.jugando.ui.ejercicios.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aprende.jugando.R;

/**
 * Etiquetas legibles por tipo de ejercicio ({@link ExerciseExtras}),
 * reusadas en selección de nivel, informes y permisos padre/hijo.
 * @author José López Mohedano
 */
public final class ExerciseLabels {

    private ExerciseLabels() {}

    @NonNull
    public static String labelForExercise(Context ctx, String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case ExerciseExtras.TYPE_MULTIPLICAR:
                return ctx.getString(R.string.exercise_multiplicar);
            case ExerciseExtras.TYPE_SUMAR:
                return ctx.getString(R.string.exercise_sumar);
            case ExerciseExtras.TYPE_RESTAR:
                return ctx.getString(R.string.exercise_restar);
            case ExerciseExtras.TYPE_DIVIDIR:
                return ctx.getString(R.string.exercise_dividir);
            case ExerciseExtras.TYPE_MIXTO:
                return ctx.getString(R.string.exercise_mixto);
            case ExerciseExtras.TYPE_FIGURAS:
                return ctx.getString(R.string.exercise_figuras);
            case ExerciseExtras.TYPE_LOGICA:
                return ctx.getString(R.string.exercise_logica);
            case ExerciseExtras.TYPE_MEMORIA:
                return ctx.getString(R.string.exercise_memoria);
            case ExerciseExtras.TYPE_ANIMALES:
                return ctx.getString(R.string.exercise_animales);
            default:
                return type;
        }
    }
}
