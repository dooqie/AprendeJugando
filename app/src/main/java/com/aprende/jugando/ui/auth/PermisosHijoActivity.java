package com.aprende.jugando.ui.auth;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.database.PermisosRepository;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.ui.ejercicios.util.ExerciseLabels;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.SessionManager;

/**
 * Activa o desactiva ejercicios del menú para un hijo.
 * @author José López Mohedano
 */
public class PermisosHijoActivity extends AppCompatActivity {

    public static final String EXTRA_HIJO_ID = "hijoId";
    public static final String EXTRA_HIJO_NOMBRE = "hijoNombre";

    private int hijoId;
    private String hijoNombre;
    private PermisosRepository permisosRepository;
    private LinearLayout contenedor;
    private boolean bloqueoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permisos_hijo);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            finish();
            return;
        }

        hijoId = getIntent().getIntExtra(EXTRA_HIJO_ID, -1);
        hijoNombre = getIntent().getStringExtra(EXTRA_HIJO_NOMBRE);
        if (hijoId < 0 || hijoNombre == null) {
            finish();
            return;
        }

        permisosRepository = new PermisosRepository(this);
        contenedor = findViewById(R.id.contenedorSwitchesPermisos);

        TextView titulo = findViewById(R.id.tvPermisosTitulo);
        titulo.setText(getString(R.string.permisos_titulo, hijoNombre));

        findViewById(R.id.btnVolverPermisos).setOnClickListener(v -> finish());

        findViewById(R.id.btnActivarTodos).setOnClickListener(v -> setTodos(true));
        findViewById(R.id.btnDesactivarTodos).setOnClickListener(v -> setTodos(false));

        construirSwitches();

        ButtonAnimUtils.addBounce(this,
                findViewById(R.id.btnVolverPermisos),
                findViewById(R.id.btnActivarTodos),
                findViewById(R.id.btnDesactivarTodos));
    }

    private void construirSwitches() {
        contenedor.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int pad = (int) (12 * density);

        for (String tipo : ExerciseExtras.ALL_EXERCISE_TYPES) {
            Switch sw = new Switch(this);
            sw.setText(ExerciseLabels.labelForExercise(this, tipo));
            sw.setTextSize(16f);
            sw.setPadding(pad, pad, pad, pad);
            sw.setTag(tipo);
            boolean on = permisosRepository.isEjercicioActivo(hijoId, tipo);
            bloqueoListener = true;
            sw.setChecked(on);
            bloqueoListener = false;
            sw.setOnCheckedChangeListener(this::onPermisoCambiado);
            contenedor.addView(sw);
        }
    }

    private void onPermisoCambiado(CompoundButton buttonView, boolean isChecked) {
        if (bloqueoListener) {
            return;
        }
        String tipo = (String) buttonView.getTag();
        permisosRepository.setPermiso(hijoId, tipo, isChecked);
    }

    private void setTodos(boolean activo) {
        bloqueoListener = true;
        for (int i = 0; i < contenedor.getChildCount(); i++) {
            if (contenedor.getChildAt(i) instanceof Switch) {
                Switch sw = (Switch) contenedor.getChildAt(i);
                sw.setChecked(activo);
                String tipo = (String) sw.getTag();
                permisosRepository.setPermiso(hijoId, tipo, activo);
            }
        }
        bloqueoListener = false;
    }
}
