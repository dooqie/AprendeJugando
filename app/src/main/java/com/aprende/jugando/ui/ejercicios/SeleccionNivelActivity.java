package com.aprende.jugando.ui.ejercicios;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.ui.ejercicios.util.ExerciseLabels;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.MusicPlayer;

/**
 * Pantalla común para elegir dificultad (Fácil / Medio / Difícil) antes de un ejercicio.
 * @author José López Mohedano
 */
public class SeleccionNivelActivity extends AppCompatActivity {

    public static final String EXTRA_EXERCISE = ExerciseExtras.EXTRA_EXERCISE_TYPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_nivel);

        String exerciseType = getIntent().getStringExtra(EXTRA_EXERCISE);
        if (exerciseType == null) {
            exerciseType = ExerciseExtras.TYPE_MULTIPLICAR;
        }
        final String ejercicio = exerciseType;

        TextView tvNombre = findViewById(R.id.tvNombreEjercicio);
        tvNombre.setText(ExerciseLabels.labelForExercise(this, ejercicio));

        Button btnFacil = findViewById(R.id.btnFacil);
        Button btnMedio = findViewById(R.id.btnMedio);
        Button btnDificil = findViewById(R.id.btnDificil);
        Button btnAtras = findViewById(R.id.btnAtrasSeleccion);

        btnFacil.setOnClickListener(v -> startExercise(ejercicio, 1));
        btnMedio.setOnClickListener(v -> startExercise(ejercicio, 2));
        btnDificil.setOnClickListener(v -> startExercise(ejercicio, 3));
        btnAtras.setOnClickListener(v -> finish());

        ButtonAnimUtils.addBounce(this, btnFacil, btnMedio, btnDificil, btnAtras);
    }



    private void startExercise(String exerciseType, int tier) {
        Intent intent = targetIntent(exerciseType);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out);
    }

    private Intent targetIntent(String exerciseType) {
        switch (exerciseType) {
            case ExerciseExtras.TYPE_SUMAR:
                return new Intent(this, SumarActivity.class);
            case ExerciseExtras.TYPE_RESTAR:
                return new Intent(this, RestarActivity.class);
            case ExerciseExtras.TYPE_DIVIDIR:
                return new Intent(this, DividirActivity.class);
            case ExerciseExtras.TYPE_MIXTO:
                return new Intent(this, MixtoActivity.class);
            case ExerciseExtras.TYPE_FIGURAS:
                return new Intent(this, FigurasActivity.class);
            case ExerciseExtras.TYPE_LOGICA:
                return new Intent(this, LogicaActivity.class);
            case ExerciseExtras.TYPE_MEMORIA:
                return new Intent(this, MemoriaActivity.class);
            case ExerciseExtras.TYPE_ANIMALES:
                return new Intent(this, AnimalesActivity.class);
            case ExerciseExtras.TYPE_MULTIPLICAR:
            default:
                return new Intent(this, MultiplicarActivity.class);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicPlayer.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicPlayer.resumeMusic();
    }
}
