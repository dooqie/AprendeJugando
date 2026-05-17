package com.aprende.jugando.ui.ejercicios;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.ui.ejercicios.util.Dificultad;
import com.aprende.jugando.ui.ejercicios.util.ExerciseAnswerFeedbackHelper;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.MusicPlayer;
import com.aprende.jugando.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Actividad del juego Operaciones : (Suma, Resta, Multiplicación, División).
 * @author José López Mohedano
 */

public class MixtoActivity extends AppCompatActivity {

    private static final int[] FONDOS_EJERCICIO = {
            R.drawable.fondo_figuras,
            R.drawable.fondo_estrellas,
            R.drawable.fondo_nubes
    };

    private static final int[] OPCION_FONDOS = {
            R.drawable.button_rounded_purple,
            R.drawable.button_rounded_pink,
            R.drawable.button_rounded_yellow,
            R.drawable.button_rounded_cyan
    };

    private TextView preguntaTextView, timerTextView, scoreTextView, nivelTextView;
    private Button opcion1Button, opcion2Button, opcion3Button, opcion4Button;
    private RelativeLayout juegoLayout;

    private int aciertos = 0;
    private int fallos = 0;
    private int respuestasCorrectasConsecutivas = 0;
    private int fallosConsecutivos = 0;
    private int tiempoRestante;
    private int tiempoTotalPartidaSegundos;
    private CountDownTimer timer;

    private int nivelJuego = 1;
    private int tier = 2;
    private Dificultad dificultad = Dificultad.MEDIO;

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixto);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        tier = getIntent().getIntExtra(ExerciseExtras.EXTRA_TIER, 2);
        dificultad = Dificultad.fromTier(tier);
        tiempoTotalPartidaSegundos = dificultad.getTiempoSegundos();

        nivelJuego = getIntent().getIntExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, 1);
        aciertos = getIntent().getIntExtra("aciertos", 0);
        fallos = getIntent().getIntExtra("fallos", 0);
        respuestasCorrectasConsecutivas = 0;
        fallosConsecutivos = 0;

        juegoLayout = findViewById(R.id.juegoLayoutMixto);
        juegoLayout.setBackgroundResource(
                FONDOS_EJERCICIO[new Random().nextInt(FONDOS_EJERCICIO.length)]);

        inicializarVistas();
        configurarPreguntas();
        iniciarTemporizador(tiempoTotalPartidaSegundos);
        ButtonAnimUtils.addBounce(this, opcion1Button, opcion2Button, opcion3Button, opcion4Button,
                findViewById(R.id.btnVolverMixto));
    }

    private void inicializarVistas() {
        preguntaTextView = findViewById(R.id.preguntaTextViewMixto);
        timerTextView = findViewById(R.id.timerTextViewMixto);
        scoreTextView = findViewById(R.id.scoreTextViewMixto);
        nivelTextView = findViewById(R.id.nivelTextViewMixto);
        opcion1Button = findViewById(R.id.opcion1ButtonMixto);
        opcion2Button = findViewById(R.id.opcion2ButtonMixto);
        opcion3Button = findViewById(R.id.opcion3ButtonMixto);
        opcion4Button = findViewById(R.id.opcion4ButtonMixto);

        nivelTextView.setText(String.format("Nivel: %d", nivelJuego));
    }

    private void configurarPreguntas() {
        limpiarColoresBotones();
        Random random = new Random();
        int minOp = dificultad.getMinOperando();
        int maxSuma = Math.max(minOp, dificultad.getMaxOperandoSuma());
        int maxF = dificultad.getMaxFactorMultiplicar();

        int tipo = random.nextInt(4);
        String pregunta;
        int respuestaCorrecta;
        switch (tipo) {
            case 0: {
                int num1 = minOp + random.nextInt(maxSuma - minOp + 1);
                int num2 = minOp + random.nextInt(maxSuma - minOp + 1);
                respuestaCorrecta = num1 + num2;
                pregunta = String.format("%d + %d = ?", num1, num2);
                break;
            }
            case 1: {
                int num1 = minOp + random.nextInt(maxSuma - minOp + 1);
                int num2 = minOp + random.nextInt(num1 - minOp + 1);
                respuestaCorrecta = num1 - num2;
                pregunta = String.format("%d − %d = ?", num1, num2);
                break;
            }
            case 2: {
                int tabla = minOp + random.nextInt(maxF - minOp + 1);
                int num = minOp + random.nextInt(maxF - minOp + 1);
                respuestaCorrecta = tabla * num;
                pregunta = String.format("%d × %d = ?", tabla, num);
                break;
            }
            default: {
                int divisor = minOp + random.nextInt(maxF - minOp + 1);
                int cociente = minOp + random.nextInt(maxF - minOp + 1);
                int dividendo = divisor * cociente;
                respuestaCorrecta = cociente;
                pregunta = String.format("%d ÷ %d = ?", dividendo, divisor);
                break;
            }
        }

        preguntaTextView.setText(pregunta);

        List<Integer> opciones = new ArrayList<>();
        opciones.add(respuestaCorrecta);

        int rangoOpciones = Math.max(maxSuma * 2,
                Math.max(respuestaCorrecta + 30, maxF * maxF + 20));

        while (opciones.size() < 4) {
            int opcion = random.nextInt(Math.max(rangoOpciones, 1)) + 1;
            if (!opciones.contains(opcion) && opcion != respuestaCorrecta) {
                opciones.add(opcion);
            }
        }
        Collections.shuffle(opciones);

        opcion1Button.setText(String.valueOf(opciones.get(0)));
        opcion2Button.setText(String.valueOf(opciones.get(1)));
        opcion3Button.setText(String.valueOf(opciones.get(2)));
        opcion4Button.setText(String.valueOf(opciones.get(3)));

        configurarBotonRespuesta(opcion1Button, opciones.get(0), respuestaCorrecta);
        configurarBotonRespuesta(opcion2Button, opciones.get(1), respuestaCorrecta);
        configurarBotonRespuesta(opcion3Button, opciones.get(2), respuestaCorrecta);
        configurarBotonRespuesta(opcion4Button, opciones.get(3), respuestaCorrecta);

        actualizarPuntuacion();
    }

    private void configurarBotonRespuesta(Button boton, int valor, int respuestaCorrecta) {
        boton.setOnClickListener(v -> {
            deshabilitarBotones();

            if (valor == respuestaCorrecta) {
                aciertos++;
                respuestasCorrectasConsecutivas++;
                fallosConsecutivos = 0;
                MusicPlayer.startMusic(this, R.raw.si);
                boton.setBackgroundResource(R.drawable.button_feedback_correct);
                boton.setAlpha(1.0f);

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (respuestasCorrectasConsecutivas >= dificultad.getRachaParaSubirNivel()) {
                        nivelJuego++;
                        nivelTextView.setText(String.format("Nivel: %d", nivelJuego));
                        respuestasCorrectasConsecutivas = 0;
                        if (timer != null) timer.cancel();
                        irNivelSuperado();
                    } else {
                        restaurarBotones();
                        configurarPreguntas();
                    }
                }, 1500);
            } else {
                fallos++;
                respuestasCorrectasConsecutivas = 0;
                fallosConsecutivos++;
                MusicPlayer.startMusic(this, R.raw.no);
                boton.setBackgroundResource(R.drawable.button_feedback_incorrect);
                boton.setAlpha(1.0f);

                ExerciseAnswerFeedbackHelper.highlightCorrectNumeric(
                        respuestaCorrecta, opcion1Button, opcion2Button, opcion3Button, opcion4Button);

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (fallosConsecutivos >= 3) {
                        terminarJuego();
                    } else {
                        restaurarBotones();
                        configurarPreguntas();
                    }
                }, 1500);
            }
        });
    }

    private void irNivelSuperado() {
        Intent intent = new Intent(this, LevelUpActivity.class);
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_MIXTO);
        intent.putExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        intent.putExtra("aciertos", aciertos);
        intent.putExtra("fallos", fallos);
        startActivity(intent);
        finish();
    }

    private void limpiarColoresBotones() {
        opcion1Button.setBackgroundTintList(null);
        opcion2Button.setBackgroundTintList(null);
        opcion3Button.setBackgroundTintList(null);
        opcion4Button.setBackgroundTintList(null);
        opcion1Button.setBackgroundResource(OPCION_FONDOS[0]);
        opcion2Button.setBackgroundResource(OPCION_FONDOS[1]);
        opcion3Button.setBackgroundResource(OPCION_FONDOS[2]);
        opcion4Button.setBackgroundResource(OPCION_FONDOS[3]);
    }

    private void deshabilitarBotones() {
        opcion1Button.setEnabled(false);
        opcion1Button.setAlpha(0.5f);
        opcion2Button.setEnabled(false);
        opcion2Button.setAlpha(0.5f);
        opcion3Button.setEnabled(false);
        opcion3Button.setAlpha(0.5f);
        opcion4Button.setEnabled(false);
        opcion4Button.setAlpha(0.5f);
    }

    private void restaurarBotones() {
        opcion1Button.setEnabled(true);
        opcion1Button.setAlpha(1.0f);
        opcion2Button.setEnabled(true);
        opcion2Button.setAlpha(1.0f);
        opcion3Button.setEnabled(true);
        opcion3Button.setAlpha(1.0f);
        opcion4Button.setEnabled(true);
        opcion4Button.setAlpha(1.0f);
    }

    private void iniciarTemporizador(int tiempo) {
        tiempoRestante = tiempo;
        timer = new CountDownTimer(tiempo * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestante = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.format("Tiempo: %d", tiempoRestante));
            }

            @Override
            public void onFinish() {
                terminarJuego();
            }
        }.start();
    }

    private void actualizarPuntuacion() {
        scoreTextView.setText(String.format("Aciertos: %d | Fallos: %d", aciertos, fallos));
    }

    private void terminarJuego() {
        if (timer != null) timer.cancel();
        guardarResultado();

        Intent intent = new Intent(this, InformeActivity.class);
        intent.putExtra("aciertos", aciertos);
        intent.putExtra("fallos", fallos);
        intent.putExtra("nivelMaximo", nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_MIXTO);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        startActivity(intent);
        finish();
    }

    private void guardarResultado() {
        if (sessionManager.isLoggedIn()) {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_USUARIO_ID, sessionManager.getUserId());
            values.put(DBHelper.COLUMN_TIPO_EJERCICIO, "mixto");
            values.put(DBHelper.COLUMN_ACIERTOS, aciertos);
            values.put(DBHelper.COLUMN_FALLOS, fallos);
            values.put(DBHelper.COLUMN_TIEMPO, Math.max(0, tiempoTotalPartidaSegundos - tiempoRestante));

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            values.put(DBHelper.COLUMN_FECHA, sdf.format(new java.util.Date()));

            db.insert(DBHelper.TABLE_RESULTADOS, null, values);
            db.close();
        }
    }

    public void onVolverClicked(View view) {
        if (timer != null) timer.cancel();
        MusicPlayer.stopMusic();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
