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
 * Actividad del juego Multiplicar.
 * @author José López Mohedano
 */
public class MultiplicarActivity extends AppCompatActivity {

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

    private List<Integer> tablasSeleccionadas;
    private int nivelJuego = 1;
    private int aciertos = 0;
    private int fallos = 0;
    private int tiempoRestante = 60;
    private int tiempoTotalPartidaSegundos = 60;
    private CountDownTimer timer;

    private int respuestasCorrectasConsecutivas = 0;
    private int fallosConsecutivos = 0;
    private Dificultad dificultad = Dificultad.MEDIO;
    private int tier = 2;

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplicar);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        tier = getIntent().getIntExtra(ExerciseExtras.EXTRA_TIER, 2);
        dificultad = Dificultad.fromTier(tier);
        tiempoTotalPartidaSegundos = dificultad.getTiempoSegundos();

        // Restaurar tras LevelUpActivity u otra entrada
        nivelJuego = getIntent().getIntExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, 1);
        aciertos = getIntent().getIntExtra("aciertos", 0);
        fallos = getIntent().getIntExtra("fallos", 0);
        respuestasCorrectasConsecutivas = 0;
        fallosConsecutivos = 0;

        int minTabla = dificultad.getMinOperando();
        tablasSeleccionadas = getIntent().getIntegerArrayListExtra(ExerciseExtras.EXTRA_TABLAS);
        if (tablasSeleccionadas == null || tablasSeleccionadas.isEmpty()) {
            tablasSeleccionadas = new ArrayList<>();
            for (int i = minTabla; i <= 10; i++) {
                tablasSeleccionadas.add(i);
            }
        } else {
            java.util.Iterator<Integer> it = tablasSeleccionadas.iterator();
            while (it.hasNext()) {
                if (it.next() < minTabla) {
                    it.remove();
                }
            }
            if (tablasSeleccionadas.isEmpty()) {
                for (int i = minTabla; i <= 10; i++) {
                    tablasSeleccionadas.add(i);
                }
            }
        }

        juegoLayout = findViewById(R.id.juegoLayoutMultiplicar);
        juegoLayout.setBackgroundResource(
                FONDOS_EJERCICIO[new Random().nextInt(FONDOS_EJERCICIO.length)]);

        inicializarVistas();
        iniciarNivel();
        ButtonAnimUtils.addBounce(this, opcion1Button, opcion2Button, opcion3Button, opcion4Button,
                findViewById(R.id.btnVolverMultiplicar));
    }

    private void inicializarVistas() {
        preguntaTextView = findViewById(R.id.preguntaTextViewMultiplicar);
        timerTextView = findViewById(R.id.timerTextViewMultiplicar);
        scoreTextView = findViewById(R.id.scoreTextViewMultiplicar);
        nivelTextView = findViewById(R.id.nivelTextViewMultiplicar);
        opcion1Button = findViewById(R.id.opcion1ButtonMultiplicar);
        opcion2Button = findViewById(R.id.opcion2ButtonMultiplicar);
        opcion3Button = findViewById(R.id.opcion3ButtonMultiplicar);
        opcion4Button = findViewById(R.id.opcion4ButtonMultiplicar);

        nivelTextView.setText(String.format("Nivel: %d", nivelJuego));
    }

    private void iniciarNivel() {
        configurarPreguntas();
        iniciarTemporizador(tiempoTotalPartidaSegundos);
    }

    private void configurarPreguntas() {
        limpiarColoresBotones();
        Random random = new Random();

        int tabla = tablasSeleccionadas.get(random.nextInt(tablasSeleccionadas.size()));
        int minF = dificultad.getMinOperando();
        int maxFactor = dificultad.getMaxFactorMultiplicar();
        int num = minF + random.nextInt(maxFactor - minF + 1);
        int respuestaCorrecta = tabla * num;

        preguntaTextView.setText(String.format("%d x %d = ?", tabla, num));

        List<Integer> opciones = new ArrayList<>();
        opciones.add(respuestaCorrecta);

        int rangoOpciones = Math.max(100, respuestaCorrecta + 20);
        while (opciones.size() < 4) {
            int opcion = random.nextInt(rangoOpciones) + 1;
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
                        if (timer != null) {
                            timer.cancel();
                        }
                        irANivelSuperado();
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

    private void irANivelSuperado() {
        Intent intent = new Intent(MultiplicarActivity.this, LevelUpActivity.class);
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_MULTIPLICAR);
        intent.putExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        intent.putExtra("aciertos", aciertos);
        intent.putExtra("fallos", fallos);
        intent.putIntegerArrayListExtra(ExerciseExtras.EXTRA_TABLAS, new ArrayList<>(tablasSeleccionadas));
        startActivity(intent);
        finish();
    }

    private void terminarJuego() {
        if (timer != null) {
            timer.cancel();
        }

        guardarResultado();

        Intent intent = new Intent(MultiplicarActivity.this, InformeActivity.class);
        intent.putExtra("aciertos", aciertos);
        intent.putExtra("fallos", fallos);
        intent.putExtra("nivelMaximo", nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_MULTIPLICAR);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        startActivity(intent);
        finish();
    }

    private void guardarResultado() {
        if (sessionManager.isLoggedIn()) {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_USUARIO_ID, sessionManager.getUserId());
            values.put(DBHelper.COLUMN_TIPO_EJERCICIO, "multiplicar");
            values.put(DBHelper.COLUMN_ACIERTOS, aciertos);
            values.put(DBHelper.COLUMN_FALLOS, fallos);
            int transcurrido = Math.max(0, tiempoTotalPartidaSegundos - tiempoRestante);
            values.put(DBHelper.COLUMN_TIEMPO, transcurrido);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            values.put(DBHelper.COLUMN_FECHA, sdf.format(new java.util.Date()));

            db.insert(DBHelper.TABLE_RESULTADOS, null, values);
            db.close();
        }
    }

    public void onVolverClicked(View view) {
        if (timer != null) {
            timer.cancel();
        }
        MusicPlayer.stopMusic();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
