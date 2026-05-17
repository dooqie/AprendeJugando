package com.aprende.jugando.ui.ejercicios;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
 * Actividad del juego Identificación de Animales.
 * @author José López Mohedano
 */
public class AnimalesActivity extends AppCompatActivity {

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

    private static final int[] ANIMAL_DRAWABLES = {
            R.drawable.animal_vaca,
            R.drawable.animal_pajaro,
            R.drawable.animal_cerdo,
            R.drawable.animal_caballo,
            R.drawable.animal_buho,
            R.drawable.animal_lobo,
            R.drawable.animal_perro,
            R.drawable.animal_gato
    };

    private TextView preguntaTextView, timerTextView, scoreTextView, nivelTextView;
    private ImageView animalImageView;
    private Button[] opcionesButtons = new Button[4];
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

    private int indiceAnterior = -1;

    private String[] nombresAnimales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animales);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        tier = getIntent().getIntExtra(ExerciseExtras.EXTRA_TIER, 2);
        dificultad = Dificultad.fromTier(tier);
        tiempoTotalPartidaSegundos = dificultad.getTiempoSegundos();

        nombresAnimales = new String[]{
                getString(R.string.animal_vaca),
                getString(R.string.animal_pajaro),
                getString(R.string.animal_cerdo),
                getString(R.string.animal_caballo),
                getString(R.string.animal_buho),
                getString(R.string.animal_lobo),
                getString(R.string.animal_perro),
                getString(R.string.animal_gato)
        };

        nivelJuego = getIntent().getIntExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, 1);
        aciertos = getIntent().getIntExtra("aciertos", 0);
        fallos = getIntent().getIntExtra("fallos", 0);
        respuestasCorrectasConsecutivas = 0;
        fallosConsecutivos = 0;

        juegoLayout = findViewById(R.id.juegoLayoutAnimales);
        juegoLayout.setBackgroundResource(
                FONDOS_EJERCICIO[new Random().nextInt(FONDOS_EJERCICIO.length)]);

        inicializarVistas();
        configurarPreguntas();
        iniciarTemporizador(tiempoTotalPartidaSegundos);
        ButtonAnimUtils.addBounce(this, opcionesButtons[0], opcionesButtons[1], opcionesButtons[2],
                opcionesButtons[3], findViewById(R.id.btnVolverAnimales));
    }

    private void inicializarVistas() {
        preguntaTextView = findViewById(R.id.preguntaTextViewAnimales);
        timerTextView = findViewById(R.id.timerTextViewAnimales);
        scoreTextView = findViewById(R.id.scoreTextViewAnimales);
        nivelTextView = findViewById(R.id.nivelTextViewAnimales);
        animalImageView = findViewById(R.id.animalImageView);

        opcionesButtons[0] = findViewById(R.id.opcion1ButtonAnimales);
        opcionesButtons[1] = findViewById(R.id.opcion2ButtonAnimales);
        opcionesButtons[2] = findViewById(R.id.opcion3ButtonAnimales);
        opcionesButtons[3] = findViewById(R.id.opcion4ButtonAnimales);

        nivelTextView.setText(String.format("Nivel: %d", nivelJuego));
    }

    private void configurarPreguntas() {
        limpiarColoresBotones();
        Random random = new Random();

        int indiceCorrecto;
        do {
            indiceCorrecto = random.nextInt(nombresAnimales.length);
        } while (indiceCorrecto == indiceAnterior && nombresAnimales.length > 1);
        indiceAnterior = indiceCorrecto;

        String animalCorrecto = nombresAnimales[indiceCorrecto];

        animalImageView.setImageResource(ANIMAL_DRAWABLES[indiceCorrecto]);

        float rot = (random.nextFloat() * 50f) - 25f;
        animalImageView.setRotation(rot);
        float scale = 0.85f + random.nextFloat() * 0.75f;
        animalImageView.setScaleX(scale);
        animalImageView.setScaleY(scale);

        preguntaTextView.setText(R.string.pregunta_animales);

        List<Integer> indices = new ArrayList<>();
        indices.add(indiceCorrecto);
        while (indices.size() < 4) {
            int idx = random.nextInt(nombresAnimales.length);
            if (!indices.contains(idx)) {
                indices.add(idx);
            }
        }
        Collections.shuffle(indices);

        for (int i = 0; i < 4; i++) {
            final String respuesta = nombresAnimales[indices.get(i)];
            final Button botonActual = opcionesButtons[i];
            botonActual.setText(respuesta);
            final String correcta = animalCorrecto;
            botonActual.setOnClickListener(v -> {
                deshabilitarBotones();
                if (respuesta.equals(correcta)) {
                    aciertos++;
                    respuestasCorrectasConsecutivas++;
                    fallosConsecutivos = 0;
                    MusicPlayer.startMusic(this, R.raw.si);
                    botonActual.setBackgroundResource(R.drawable.button_feedback_correct);
                    botonActual.setAlpha(1.0f);

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
                    botonActual.setBackgroundResource(R.drawable.button_feedback_incorrect);
                    botonActual.setAlpha(1.0f);

                    ExerciseAnswerFeedbackHelper.highlightCorrectText(correcta, opcionesButtons);

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

        actualizarPuntuacion();
    }

    private void irNivelSuperado() {
        Intent intent = new Intent(this, LevelUpActivity.class);
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_ANIMALES);
        intent.putExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        intent.putExtra("aciertos", aciertos);
        intent.putExtra("fallos", fallos);
        startActivity(intent);
        finish();
    }

    private void limpiarColoresBotones() {
        for (int i = 0; i < opcionesButtons.length; i++) {
            Button btn = opcionesButtons[i];
            btn.setBackgroundTintList(null);
            btn.setBackgroundResource(OPCION_FONDOS[i]);
        }
    }

    private void deshabilitarBotones() {
        for (Button btn : opcionesButtons) {
            btn.setEnabled(false);
            btn.setAlpha(0.5f);
        }
    }

    private void restaurarBotones() {
        for (Button btn : opcionesButtons) {
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
        }
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
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_ANIMALES);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        startActivity(intent);
        finish();
    }

    private void guardarResultado() {
        if (sessionManager.isLoggedIn()) {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_USUARIO_ID, sessionManager.getUserId());
            values.put(DBHelper.COLUMN_TIPO_EJERCICIO, "animales");
            values.put(DBHelper.COLUMN_ACIERTOS, aciertos);
            values.put(DBHelper.COLUMN_FALLOS, fallos);
            values.put(DBHelper.COLUMN_TIEMPO, Math.max(0, tiempoTotalPartidaSegundos - tiempoRestante));

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
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
