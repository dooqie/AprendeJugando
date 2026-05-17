package com.aprende.jugando.ui.ejercicios;

import android.content.ContentValues;
import android.content.Intent;
import android.view.Gravity;
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
 * Actividad del juego Identificación de Figuras.
 * @author José López Mohedano
 */

public class FigurasActivity extends AppCompatActivity {

    private static final int[] FONDOS_EJERCICIO = {
            R.drawable.fondo_figuras,
            R.drawable.fondo_estrellas,
            R.drawable.fondo_nubes
    };

    /** Fondo de cada opción (se restaura tras acierto/fallo). */
    private static final int[] OPCION_FONDOS = {
            R.drawable.button_rounded_purple,
            R.drawable.button_rounded_pink,
            R.drawable.button_rounded_yellow,
            R.drawable.button_rounded_cyan
    };

    /** Tier mínimo de dificultad (1=Fácil) en que entra cada figura. */
    private static final int AP_DESDE_FACIL = 1;
    private static final int AP_DESDE_MEDIO = 2;
    private static final int AP_SOLO_DIFICIL = 3;

    private static final class FiguraDef {
        final int nombreResId;
        final int drawableResId;
        final int desdeTier;

        FiguraDef(int nombreResId, int drawableResId, int desdeTier) {
            this.nombreResId = nombreResId;
            this.drawableResId = drawableResId;
            this.desdeTier = desdeTier;
        }
    }

    private static final FiguraDef[] MAESTRO = new FiguraDef[]{
            new FiguraDef(R.string.figura_circulo, R.drawable.figura_circulo, AP_DESDE_FACIL),
            new FiguraDef(R.string.figura_cuadrado, R.drawable.figura_cuadrado, AP_DESDE_FACIL),
            new FiguraDef(R.string.figura_triangulo, R.drawable.figura_triangulo, AP_DESDE_FACIL),
            new FiguraDef(R.string.figura_rectangulo, R.drawable.figura_rectangulo, AP_DESDE_FACIL),
            new FiguraDef(R.string.figura_estrella, R.drawable.figura_estrella, AP_DESDE_FACIL),
            new FiguraDef(R.string.figura_corazon, R.drawable.figura_corazon, AP_DESDE_FACIL),
            new FiguraDef(R.string.figura_rombo, R.drawable.figura_rombo, AP_DESDE_MEDIO),
            new FiguraDef(R.string.figura_trapecio, R.drawable.figura_trapecio, AP_DESDE_MEDIO),
            new FiguraDef(R.string.figura_paralelogramo, R.drawable.figura_paralelogramo, AP_DESDE_MEDIO),
            new FiguraDef(R.string.figura_pentagono, R.drawable.figura_pentagono, AP_DESDE_MEDIO),
            new FiguraDef(R.string.figura_elipse, R.drawable.figura_elipse, AP_DESDE_MEDIO),
            new FiguraDef(R.string.figura_ovalo, R.drawable.figura_ovalo, AP_DESDE_MEDIO),
            new FiguraDef(R.string.figura_hexagono, R.drawable.figura_hexagono, AP_SOLO_DIFICIL),
            new FiguraDef(R.string.figura_heptagono, R.drawable.figura_heptagono, AP_SOLO_DIFICIL),
            new FiguraDef(R.string.figura_octagono, R.drawable.figura_octagono, AP_SOLO_DIFICIL),
            new FiguraDef(R.string.figura_eneagono, R.drawable.figura_eneagono, AP_SOLO_DIFICIL),
            new FiguraDef(R.string.figura_decagono, R.drawable.figura_decagono, AP_SOLO_DIFICIL),
    };

    private TextView preguntaTextView, timerTextView, scoreTextView, nivelTextView;
    private ImageView figuraImageView;
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

    private String[] figuras;
    private int[] figurasDrawables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_figuras);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        tier = getIntent().getIntExtra(ExerciseExtras.EXTRA_TIER, 2);
        dificultad = Dificultad.fromTier(tier);
        tiempoTotalPartidaSegundos = dificultad.getTiempoSegundos();

        construirListasFigurasPorTier();

        nivelJuego = getIntent().getIntExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, 1);
        aciertos = getIntent().getIntExtra("aciertos", 0);
        fallos = getIntent().getIntExtra("fallos", 0);
        respuestasCorrectasConsecutivas = 0;
        fallosConsecutivos = 0;

        juegoLayout = findViewById(R.id.juegoLayoutFiguras);
        juegoLayout.setBackgroundResource(
                FONDOS_EJERCICIO[new Random().nextInt(FONDOS_EJERCICIO.length)]);

        inicializarVistas();
        configurarPreguntas();
        iniciarTemporizador(tiempoTotalPartidaSegundos);
        ButtonAnimUtils.addBounce(this, opcionesButtons[0], opcionesButtons[1], opcionesButtons[2],
                opcionesButtons[3], findViewById(R.id.btnVolverFiguras));
    }

    private void construirListasFigurasPorTier() {
        List<FiguraDef> activas = new ArrayList<>();
        for (FiguraDef d : MAESTRO) {
            if (tier >= d.desdeTier) {
                activas.add(d);
            }
        }
        int n = activas.size();
        figuras = new String[n];
        figurasDrawables = new int[n];
        for (int i = 0; i < n; i++) {
            FiguraDef d = activas.get(i);
            figuras[i] = getString(d.nombreResId);
            figurasDrawables[i] = d.drawableResId;
        }
    }

    private void inicializarVistas() {
        preguntaTextView = findViewById(R.id.preguntaTextViewFiguras);
        timerTextView = findViewById(R.id.timerTextViewFiguras);
        scoreTextView = findViewById(R.id.scoreTextViewFiguras);
        nivelTextView = findViewById(R.id.nivelTextViewFiguras);
        figuraImageView = findViewById(R.id.figuraImageView);

        opcionesButtons[0] = findViewById(R.id.opcion1ButtonFiguras);
        opcionesButtons[1] = findViewById(R.id.opcion2ButtonFiguras);
        opcionesButtons[2] = findViewById(R.id.opcion3ButtonFiguras);
        opcionesButtons[3] = findViewById(R.id.opcion4ButtonFiguras);

        nivelTextView.setText(String.format("Nivel: %d", nivelJuego));
    }

    private void configurarPreguntas() {
        limpiarColoresBotones();
        Random random = new Random();

        int indiceCorrecto;
        do {
            indiceCorrecto = random.nextInt(figuras.length);
        } while (indiceCorrecto == indiceAnterior && figuras.length > 1);
        indiceAnterior = indiceCorrecto;

        String figuraCorrecta = figuras[indiceCorrecto];

        figuraImageView.setImageResource(figurasDrawables[indiceCorrecto]);
        figuraImageView.clearColorFilter();
        figuraImageView.setImageTintList(null);

        float rot = (random.nextFloat() * 50f) - 25f;
        figuraImageView.setRotation(rot);
        float scale = 0.65f + random.nextFloat() * 0.55f;
        figuraImageView.setScaleX(scale);
        figuraImageView.setScaleY(scale);

        preguntaTextView.setText(R.string.pregunta_figuras);

        List<Integer> indices = new ArrayList<>();
        indices.add(indiceCorrecto);
        while (indices.size() < 4) {
            int idx = random.nextInt(figuras.length);
            if (!indices.contains(idx)) {
                indices.add(idx);
            }
        }
        Collections.shuffle(indices);

        for (int i = 0; i < 4; i++) {
            final String respuesta = figuras[indices.get(i)];
            final Button botonActual = opcionesButtons[i];
            botonActual.setText(respuesta);
            botonActual.setGravity(Gravity.CENTER);
            botonActual.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            botonActual.setCompoundDrawablePadding(0);
            final String fig = figuraCorrecta;
            botonActual.setOnClickListener(v -> {
                deshabilitarBotones();
                if (respuesta.equals(fig)) {
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

                    ExerciseAnswerFeedbackHelper.highlightCorrectText(fig, opcionesButtons);

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
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_FIGURAS);
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
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_FIGURAS);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        startActivity(intent);
        finish();
    }

    private void guardarResultado() {
        if (sessionManager.isLoggedIn()) {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_USUARIO_ID, sessionManager.getUserId());
            values.put(DBHelper.COLUMN_TIPO_EJERCICIO, "figuras");
            values.put(DBHelper.COLUMN_ACIERTOS, aciertos);
            values.put(DBHelper.COLUMN_FALLOS, fallos);
            values.put(DBHelper.COLUMN_TIEMPO, Math.max(0, tiempoTotalPartidaSegundos - tiempoRestante));

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault());
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
