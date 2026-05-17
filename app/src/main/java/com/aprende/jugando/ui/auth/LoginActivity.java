package com.aprende.jugando.ui.auth;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.ui.main.MainActivity;
import com.aprende.jugando.ui.onboarding.OnboardingActivity;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.PasswordUtils;
import com.aprende.jugando.utils.SessionManager;

/**
 * Actividad de inicio de sesión.
 * Permite a los usuarios autenticarse para acceder a la aplicación.
 * @author José López Mohedano
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etUsuario;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegistro;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Mostrar onboarding solo en la primera ejecución de la app
        if (!OnboardingActivity.isOnboardingDone(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        // Verificar si ya hay sesión activa
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
        ButtonAnimUtils.addBounce(this, btnLogin);
    }

    private void initViews() {
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegistro = findViewById(R.id.tvRegistro);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (usuario.isEmpty()) {
            etUsuario.setError("Ingresa tu nombre de usuario");
            etUsuario.requestFocus();
            return;
        }
        if (usuario.length() < 3) {
            etUsuario.setError("El nombre de usuario debe tener al menos 3 caracteres");
            etUsuario.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingresa tu contraseña");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 4) {
            etPassword.setError("La contraseña debe tener al menos 4 caracteres");
            etPassword.requestFocus();
            return;
        }

        // Verificar credenciales
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        Cursor cursor = db.query(
            DBHelper.TABLE_USUARIOS,
            null,
            DBHelper.COLUMN_NOMBRE + " = ? AND " + DBHelper.COLUMN_PASSWORD + " = ?",
            new String[]{usuario, hashedPassword},
            null, null, null
        );

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID));
            String userName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOMBRE));
            int rolId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ROL_ID));

            sessionManager.createSession(userId, userName, rolId);
            cursor.close();
            db.close();
            
            Toast.makeText(this, "¡Bienvenido " + userName + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            cursor.close();
            db.close();
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
