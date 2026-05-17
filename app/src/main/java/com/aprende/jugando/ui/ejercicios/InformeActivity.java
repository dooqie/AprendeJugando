package com.aprende.jugando.ui.ejercicios;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.ui.ejercicios.util.ExerciseLabels;
import com.aprende.jugando.ui.main.MainActivity;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.MusicPlayer;
import com.aprende.jugando.utils.SessionManager;

/**
 * Resultado al terminar una partida de cualquier ejercicio.
 * @author José López Mohedano
 */
public class InformeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe);

        mostrarResultados();
    }

    private void mostrarResultados() {
        SessionManager session = new SessionManager(this);
        String nombre = session.getUserName();

        int totalAciertos = getIntent().getIntExtra("aciertos", 0);
        int totalFallos = getIntent().getIntExtra("fallos", 0);
        int nivelMaximo = getIntent().getIntExtra("nivelMaximo", 1);
        String exerciseType = getIntent().getStringExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE);
        if (exerciseType == null) {
            exerciseType = ExerciseExtras.TYPE_MULTIPLICAR;
        }
        final String ejercicioFinal = exerciseType;

        TextView tituloUsuario = findViewById(R.id.informeTituloUsuario);
        TextView nombreEj = findViewById(R.id.informeNombreEjercicio);
        tituloUsuario.setText(getString(R.string.informe_buena, nombre));
        nombreEj.setText(ExerciseLabels.labelForExercise(this, ejercicioFinal));

        LinearLayout header = findViewById(R.id.informeHeader);
        header.setBackgroundResource(headerForExercise(ejercicioFinal));

        TextView nivelTv = findViewById(R.id.nivelAlcanzadoTextView);
        nivelTv.setText(getString(R.string.informe_nivel, nivelMaximo));

        int diferencia = totalAciertos - totalFallos;
        TextView aciertosTv = findViewById(R.id.aciertosTextView);
        TextView fallosTv = findViewById(R.id.fallosTextView);
        TextView difTv = findViewById(R.id.diferenciaTextView);
        aciertosTv.setText(getString(R.string.informe_aciertos_label) + ": " + totalAciertos);
        fallosTv.setText(getString(R.string.informe_fallos_label) + ": " + totalFallos);
        difTv.setText(getString(R.string.informe_diferencia_label) + ": " + diferencia);

        int estrellas = calcularEstrellas(totalAciertos, totalFallos);
        ImageView[] stars = {
                findViewById(R.id.star1),
                findViewById(R.id.star2),
                findViewById(R.id.star3),
                findViewById(R.id.star4),
                findViewById(R.id.star5)
        };
        for (int i = 0; i < 5; i++) {
            stars[i].setImageResource(i < estrellas ? R.drawable.estrella_on : R.drawable.estrella_off);
        }

        TextView mensaje = findViewById(R.id.mensajeMotivador);
        int suma = totalAciertos + totalFallos;
        if (suma == 0) {
            mensaje.setText(R.string.informe_mensaje_keep);
        } else {
            float ratio = totalAciertos / (float) suma;
            if (ratio >= 0.85f) {
                mensaje.setText(R.string.informe_mensaje_gold);
            } else if (ratio >= 0.5f) {
                mensaje.setText(R.string.informe_mensaje_ok);
            } else {
                mensaje.setText(R.string.informe_mensaje_keep);
            }
        }

        Button btnReiniciar = findViewById(R.id.btnReiniciar);
        Button btnSalir = findViewById(R.id.btnSalir);

        btnReiniciar.setOnClickListener(v -> {
            MusicPlayer.stopMusic();
            Intent intent = new Intent(InformeActivity.this, SeleccionNivelActivity.class);
            intent.putExtra(SeleccionNivelActivity.EXTRA_EXERCISE, ejercicioFinal);
            startActivity(intent);
            finish();
        });

        btnSalir.setOnClickListener(v -> {
            MusicPlayer.stopMusic();
            Intent intent = new Intent(InformeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        ButtonAnimUtils.addBounce(this, btnReiniciar, btnSalir);
    }

    private int calcularEstrellas(int aciertos, int fallos) {
        int suma = aciertos + fallos;
        if (suma == 0 || aciertos == 0) {
            return suma == 0 ? 0 : 1;
        }
        float ratio = aciertos / (float) suma;
        int stars = Math.round(ratio * 5f);
        return Math.max(1, Math.min(5, stars));
    }

    private int headerForExercise(String type) {
        switch (type) {
            case ExerciseExtras.TYPE_SUMAR:
                return R.drawable.header_gradient_green;
            case ExerciseExtras.TYPE_RESTAR:
                return R.drawable.header_gradient_pink;
            case ExerciseExtras.TYPE_DIVIDIR:
                return R.drawable.header_gradient_orange;
            case ExerciseExtras.TYPE_MIXTO:
                return R.drawable.header_gradient_yellow;
            case ExerciseExtras.TYPE_FIGURAS:
                return R.drawable.header_gradient_purple;
            case ExerciseExtras.TYPE_LOGICA:
                return R.drawable.header_gradient_cyan;
            case ExerciseExtras.TYPE_MEMORIA:
                return R.drawable.header_gradient_yellow;
            case ExerciseExtras.TYPE_ANIMALES:
                return R.drawable.header_gradient_animales;
            case ExerciseExtras.TYPE_MULTIPLICAR:
            default:
                return R.drawable.header_gradient_orange;
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
