package com.aprende.jugando.ui.ejercicios;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.MusicPlayer;

import java.util.ArrayList;

/**
 * Pantalla intermedia al subir de nivel dentro de un ejercicio.
 * @author José López Mohedano
 */
public class LevelUpActivity extends AppCompatActivity {

    private int nivelJuego;
    private int aciertosAcumulados;
    private int fallosAcumulados;
    private int tier;
    private String exerciseType;
    private ArrayList<Integer> tablasSeleccionadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        MusicPlayer.startMusic(this, R.raw.nivelsuperado);

        aciertosAcumulados = getIntent().getIntExtra("aciertos", 0);
        fallosAcumulados = getIntent().getIntExtra("fallos", 0);
        nivelJuego = getIntent().getIntExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, 1);
        tier = getIntent().getIntExtra(ExerciseExtras.EXTRA_TIER, 2);
        exerciseType = getIntent().getStringExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE);
        if (exerciseType == null) {
            exerciseType = ExerciseExtras.TYPE_MULTIPLICAR;
        }
        tablasSeleccionadas = getIntent().getIntegerArrayListExtra(ExerciseExtras.EXTRA_TABLAS);
        if (tablasSeleccionadas == null || tablasSeleccionadas.isEmpty()) {
            tablasSeleccionadas = new ArrayList<>();
            for (int i = 2; i <= 10; i++) {
                tablasSeleccionadas.add(i);
            }
        }

        Button botonContinuar = findViewById(R.id.botonContinuar);
        TextView etiquetaEnhorabuena = findViewById(R.id.etiquetaEnhorabuena);
        TextView explicacionSiguienteNivel = findViewById(R.id.explicacionSiguienteNivel);

        etiquetaEnhorabuena.setText(R.string.enhorabuena);
        explicacionSiguienteNivel.setText(R.string.explicacion_siguiente_nivel);

        botonContinuar.setOnClickListener(v -> {
            MusicPlayer.stopMusic();
            Intent intent = buildContinueIntent();
            startActivity(intent);
            finish();
        });
        ButtonAnimUtils.addBounce(this, botonContinuar);
    }

    private Intent buildContinueIntent() {
        Intent intent;
        switch (exerciseType) {
            case ExerciseExtras.TYPE_SUMAR:
                intent = new Intent(this, SumarActivity.class);
                break;
            case ExerciseExtras.TYPE_RESTAR:
                intent = new Intent(this, RestarActivity.class);
                break;
            case ExerciseExtras.TYPE_DIVIDIR:
                intent = new Intent(this, DividirActivity.class);
                break;
            case ExerciseExtras.TYPE_MIXTO:
                intent = new Intent(this, MixtoActivity.class);
                break;
            case ExerciseExtras.TYPE_FIGURAS:
                intent = new Intent(this, FigurasActivity.class);
                break;
            case ExerciseExtras.TYPE_LOGICA:
                intent = new Intent(this, LogicaActivity.class);
                break;
            case ExerciseExtras.TYPE_MEMORIA:
                intent = new Intent(this, MemoriaActivity.class);
                break;
            case ExerciseExtras.TYPE_ANIMALES:
                intent = new Intent(this, AnimalesActivity.class);
                break;
            case ExerciseExtras.TYPE_MULTIPLICAR:
            default:
                intent = new Intent(this, MultiplicarActivity.class);
                intent.putIntegerArrayListExtra(ExerciseExtras.EXTRA_TABLAS, new ArrayList<>(tablasSeleccionadas));
                break;
        }
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, exerciseType);
        intent.putExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        intent.putExtra("aciertos", aciertosAcumulados);
        intent.putExtra("fallos", fallosAcumulados);
        return intent;
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
