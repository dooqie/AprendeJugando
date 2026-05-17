package com.aprende.jugando.ui.main;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aprende.jugando.R;
import com.aprende.jugando.database.PermisosRepository;
import com.aprende.jugando.ui.auth.LoginActivity;
import com.aprende.jugando.ui.ejercicios.SeleccionNivelActivity;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.ui.admin.AdminActivity;
import com.aprende.jugando.ui.ejercicios.HistorialActivity;
import com.aprende.jugando.ui.ejercicios.ProgresoActivity;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.MusicPlayer;
import com.aprende.jugando.utils.SessionManager;

import java.util.Set;

/**
 * Actividad principal (menú de la aplicación).
 *  * Muestra los módulos disponibles según el rol del usuario.
 * @author José López Mohedano
 */
public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private GridLayout gridModulos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Verificar sesión
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupModulos();
        setupUserInfo();
        ButtonAnimUtils.addBounce(this, findViewById(R.id.btnCerrarSesion));
    }

    private void initViews() {
        gridModulos = findViewById(R.id.gridModulos);
    }

    private void setupUserInfo() {
        TextView tvBienvenida = findViewById(R.id.tvBienvenida);
        String userName = sessionManager.getUserName();
        tvBienvenida.setText("¡Hola, " + userName + "!");

        // Música removida - agregará música relajante más adelante
    }

    private void setupModulos() {
        gridModulos.removeAllViews();

        PermisosRepository permRepo = new PermisosRepository(this);
        Set<String> permisosEjercicios = sessionManager.isAdmin()
                ? PermisosRepository.permisosCompletos()
                : permRepo.getPermisosActivos(sessionManager.getUserId());

        if (sessionManager.isAdmin()) {
            agregarModulo(getString(R.string.admin_titulo),
                    () -> abrirActividad(AdminActivity.class),
                    ContextCompat.getDrawable(this, R.drawable.btn_menu_admin), 2,
                    R.drawable.modulo_bg_amber);
        }

        for (String tipo : ExerciseExtras.ALL_EXERCISE_TYPES) {
            if (!permisosEjercicios.contains(tipo)) {
                continue;
            }
            switch (tipo) {
                case ExerciseExtras.TYPE_MULTIPLICAR:
                    agregarModulo(R.drawable.btn_menu_multiplicar, getString(R.string.exercise_multiplicar),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_teal);
                    break;
                case ExerciseExtras.TYPE_SUMAR:
                    agregarModulo(R.drawable.btn_menu_sumar, getString(R.string.exercise_sumar),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_sky_teal);
                    break;
                case ExerciseExtras.TYPE_RESTAR:
                    agregarModulo(R.drawable.btn_menu_restar, getString(R.string.exercise_restar),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_coral);
                    break;
                case ExerciseExtras.TYPE_DIVIDIR:
                    agregarModulo(R.drawable.btn_menu_dividir, getString(R.string.exercise_dividir),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_lime);
                    break;
                case ExerciseExtras.TYPE_MIXTO:
                    agregarModulo(R.drawable.btn_menu_mixto, getString(R.string.exercise_mixto),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_warm_orange);
                    break;
                case ExerciseExtras.TYPE_FIGURAS:
                    agregarModulo(R.drawable.btn_menu_figuras, getString(R.string.exercise_figuras),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_rose);
                    break;
                case ExerciseExtras.TYPE_LOGICA:
                    agregarModulo(R.drawable.btn_menu_logica, getString(R.string.exercise_logica),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_purple);
                    break;
                case ExerciseExtras.TYPE_MEMORIA:
                    agregarModulo(R.drawable.btn_menu_memoria, getString(R.string.exercise_memoria),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_candy_pink);
                    break;
                case ExerciseExtras.TYPE_ANIMALES:
                    agregarModulo(R.drawable.btn_menu_animales, getString(R.string.exercise_animales),
                            () -> abrirSeleccion(tipo), R.drawable.modulo_bg_peach);
                    break;
                default:
                    break;
            }
        }

        agregarModulo("Historial", () -> abrirActividad(HistorialActivity.class),
                ContextCompat.getDrawable(this, R.drawable.btn_menu_historial), 1,
                R.drawable.modulo_bg_blue_teal);
        agregarModulo("Progreso", () -> abrirActividad(ProgresoActivity.class),
                ContextCompat.getDrawable(this, R.drawable.btn_menu_progreso), 1,
                R.drawable.modulo_bg_fresh_green);
    }

    private void agregarModulo(int iconRes, String titulo, Runnable onClick, @DrawableRes int bgRes) {
        agregarModulo(titulo, onClick, ContextCompat.getDrawable(this, iconRes), 1, bgRes);
    }

    private void agregarModulo(String titulo, Runnable onClick, Drawable icono, int span,
            @DrawableRes int bgRes) {
        View moduloView = getLayoutInflater().inflate(R.layout.item_modulo, gridModulos, false);

        ImageView ivIcono = moduloView.findViewById(R.id.ivIconoModulo);
        TextView tvTitulo = moduloView.findViewById(R.id.tvTituloModulo);

        moduloView.setBackgroundResource(bgRes);

        if (icono != null) {
            ivIcono.setImageDrawable(icono);
        }
        tvTitulo.setText(titulo);

        moduloView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce_scale));
            v.postDelayed(onClick::run, 300);
        });

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, span, (float) span);
        params.setMargins(16, 16, 16, 16);
        moduloView.setLayoutParams(params);

        gridModulos.addView(moduloView);
    }

    private void abrirActividad(Class<?> activityClass) {
        MusicPlayer.stopMusic();
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out);
    }

    private void abrirSeleccion(String exerciseType) {
        MusicPlayer.stopMusic();
        Intent intent = new Intent(MainActivity.this, SeleccionNivelActivity.class);
        intent.putExtra(SeleccionNivelActivity.EXTRA_EXERCISE, exerciseType);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out);
    }

    public void onCerrarSesionClicked(View view) {
        new AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí", (dialog, which) -> {
                sessionManager.logout();
                MusicPlayer.stopMusic();
                navigateToLogin();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
