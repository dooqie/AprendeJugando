package com.aprende.jugando.ui.ejercicios;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.aprende.jugando.R;
import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.ui.ejercicios.util.Dificultad;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.MusicPlayer;
import com.aprende.jugando.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Actividad del juego Memoria.
 * @author José López Mohedano
 */

public class MemoriaActivity extends AppCompatActivity {

    private TextView timerTextView, scoreTextView, nivelTextView;
    private GridLayout gridMemoria;
    private RelativeLayout juegoLayout;

    private Button[] cartasButtons;
    private List<String> cartas;
    private int totalCartas;
    private int totalParejas;

    private int aciertos = 0;
    private int fallos = 0;
    private int tiempoRestante;
    private int tiempoTotalPartidaSegundos;
    private CountDownTimer timer;

    private int primeraCarta = -1;
    private int segundaCarta = -1;
    private boolean bloqueado = false;
    private int parejasEncontradas = 0;
    private int rachaParejasSinFallo = 0;

    private int nivelJuego = 1;
    private int tier = 2;
    private Dificultad dificultad = Dificultad.MEDIO;

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private final String[] emojis = {"🍎", "🍊", "🍋", "🍇", "🍓", "🍒", "🍐", "🍉", "🍌", "🥝"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memoria);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        tier = getIntent().getIntExtra(ExerciseExtras.EXTRA_TIER, 2);
        dificultad = Dificultad.fromTier(tier);
        tiempoTotalPartidaSegundos = dificultad.getTiempoSegundos();

        nivelJuego = getIntent().getIntExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, 1);
        aciertos = getIntent().getIntExtra("aciertos", 0);
        fallos = getIntent().getIntExtra("fallos", 0);
        rachaParejasSinFallo = 0;

        totalCartas = dificultad.getTotalCartasMemoria();
        totalParejas = totalCartas / 2;

        juegoLayout = findViewById(R.id.juegoLayoutMemoria);
        juegoLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.light_pink));

        inicializarVistas();
        construirTableroYBarajar();
        iniciarTemporizador(tiempoTotalPartidaSegundos);
        ButtonAnimUtils.addBounce(this, findViewById(R.id.btnVolverMemoria));
    }

    private void inicializarVistas() {
        timerTextView = findViewById(R.id.timerTextViewMemoria);
        scoreTextView = findViewById(R.id.scoreTextViewMemoria);
        nivelTextView = findViewById(R.id.nivelTextViewMemoria);
        gridMemoria = findViewById(R.id.gridMemoria);
        nivelTextView.setText(String.format("Nivel: %d", nivelJuego));
    }

    private void construirTableroYBarajar() {
        gridMemoria.removeAllViews();

        int cols = dificultad.getColumnasMemoria();
        int rows = dificultad.getFilasMemoria();
        gridMemoria.setColumnCount(cols);
        gridMemoria.setRowCount(rows);

        cartasButtons = new Button[totalCartas];

        int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        int minHpPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                rows >= 5 ? 56f : rows >= 4 ? 64f : 72f,
                getResources().getDisplayMetrics());
        float textSp = rows >= 5 ? 24f : rows >= 4 ? 28f : 32f;

        android.graphics.Typeface faceCartas = ResourcesCompat.getFont(this, R.font.nunito_extrabold);
        for (int i = 0; i < totalCartas; i++) {
            Button btn = new Button(this);
            btn.setText("?");
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp);
            btn.setTextColor(Color.WHITE);
            btn.setAllCaps(false);
            if (faceCartas != null) {
                btn.setTypeface(faceCartas);
            }
            btn.setBackgroundResource(R.drawable.button_rounded_yellow);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(i / cols, 1f),
                    GridLayout.spec(i % cols, 1f));
            lp.width = 0;
            lp.height = minHpPx;
            lp.setMargins(marginPx, marginPx, marginPx, marginPx);
            btn.setLayoutParams(lp);

            final int idx = i;
            btn.setOnClickListener(v -> onCartaClicada(idx));

            gridMemoria.addView(btn);
            cartasButtons[idx] = btn;
        }

        prepararCartas();
        actualizarPuntuacion();
    }

    private void prepararCartas() {
        parejasEncontradas = 0;
        primeraCarta = -1;
        segundaCarta = -1;
        bloqueado = false;

        if (totalParejas > emojis.length) {
            throw new IllegalStateException("Tablero más grande que símbolos disponibles");
        }

        cartas = new ArrayList<>();
        for (int i = 0; i < totalParejas; i++) {
            String e = emojis[i];
            cartas.add(e);
            cartas.add(e);
        }
        Collections.shuffle(cartas);

        for (int i = 0; i < totalCartas; i++) {
            cartasButtons[i].setText("?");
            cartasButtons[i].setEnabled(true);
            cartasButtons[i].setAlpha(1f);
            cartasButtons[i].setBackgroundResource(R.drawable.button_rounded_yellow);
        }
    }

    private void onCartaClicada(int indice) {
        if (bloqueado || indice == primeraCarta || !cartasButtons[indice].isEnabled()) {
            return;
        }

        cartasButtons[indice].setText(cartas.get(indice));

        if (primeraCarta == -1) {
            primeraCarta = indice;
        } else {
            segundaCarta = indice;
            bloqueado = true;

            if (cartas.get(primeraCarta).equals(cartas.get(segundaCarta))) {
                cartasButtons[primeraCarta].setBackgroundResource(R.drawable.modulo_bg_fresh_green);
                cartasButtons[segundaCarta].setBackgroundResource(R.drawable.modulo_bg_fresh_green);
                cartasButtons[primeraCarta].setEnabled(false);
                cartasButtons[segundaCarta].setEnabled(false);
                aciertos++;
                parejasEncontradas++;
                rachaParejasSinFallo++;
                Toast.makeText(this, "¡Pareja!", Toast.LENGTH_SHORT).show();

                primeraCarta = -1;
                segundaCarta = -1;
                bloqueado = false;

                if (parejasEncontradas >= totalParejas) {
                    terminarJuego();
                } else if (rachaParejasSinFallo >= dificultad.getRachaParaSubirNivel()) {
                    if (timer != null) timer.cancel();
                    nivelJuego++;
                    nivelTextView.setText(String.format("Nivel: %d", nivelJuego));
                    rachaParejasSinFallo = 0;
                    irNivelSuperado();
                }
            } else {
                fallos++;
                rachaParejasSinFallo = 0;
                Toast.makeText(this, "¡No es pareja!", Toast.LENGTH_SHORT).show();

                final int p = primeraCarta;
                final int s = segundaCarta;
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    cartasButtons[p].setText("?");
                    cartasButtons[s].setText("?");
                    primeraCarta = -1;
                    segundaCarta = -1;
                    bloqueado = false;
                }, 1000);
            }

            actualizarPuntuacion();
        }
    }

    private void irNivelSuperado() {
        Intent intent = new Intent(this, LevelUpActivity.class);
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_MEMORIA);
        intent.putExtra(ExerciseExtras.EXTRA_NIVEL_JUEGO, nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        intent.putExtra("aciertos", aciertos);
        intent.putExtra("fallos", fallos);
        startActivity(intent);
        finish();
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
        scoreTextView.setText(String.format("Parejas: %d/%d", parejasEncontradas, totalParejas));
    }

    private void terminarJuego() {
        if (timer != null) timer.cancel();
        guardarResultado();

        Intent intent = new Intent(this, InformeActivity.class);
        intent.putExtra("aciertos", aciertos);
        intent.putExtra("fallos", fallos);
        intent.putExtra("nivelMaximo", nivelJuego);
        intent.putExtra(ExerciseExtras.EXTRA_EXERCISE_TYPE, ExerciseExtras.TYPE_MEMORIA);
        intent.putExtra(ExerciseExtras.EXTRA_TIER, tier);
        startActivity(intent);
        finish();
    }

    private void guardarResultado() {
        if (sessionManager.isLoggedIn()) {
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_USUARIO_ID, sessionManager.getUserId());
            values.put(DBHelper.COLUMN_TIPO_EJERCICIO, "memoria");
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
