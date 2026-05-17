package com.aprende.jugando.ui.auth;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.model.Rol;
import com.aprende.jugando.ui.main.MainActivity;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.PasswordUtils;
import com.aprende.jugando.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad de registro de nuevos usuarios.
 * Permite a los niños crear una cuenta para acceder a la aplicación.
 * @author José López Mohedano
 */
public class RegistroActivity extends AppCompatActivity {
    private EditText etUsuario;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegistro;
    private TextView tvLogin;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        ButtonAnimUtils.addBounce(this, btnRegistro);
    }

    private void initViews() {
        etUsuario = findViewById(R.id.etUsuarioRegistro);
        etPassword = findViewById(R.id.etPasswordRegistro);
        etConfirmPassword = findViewById(R.id.etConfirmPasswordRegistro);
        btnRegistro = findViewById(R.id.btnRegistro);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegistro.setOnClickListener(v -> attemptRegistro());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegistro() {
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validaciones
        if (usuario.isEmpty()) {
            etUsuario.setError("Ingresa un nombre de usuario");
            return;
        }
        if (usuario.length() < 3) {
            etUsuario.setError("El nombre debe tener al menos 3 caracteres");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingresa una contraseña");
            return;
        }
        if (password.length() < 4) {
            etPassword.setError("La contraseña debe tener al menos 4 caracteres");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        // Insertar usuario en la base de datos
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NOMBRE, usuario);
        values.put(DBHelper.COLUMN_PASSWORD, PasswordUtils.hashPassword(password));
        values.put(DBHelper.COLUMN_ROL_ID, Rol.PADRE);
        values.putNull(DBHelper.COLUMN_PADRE_ID);
        values.put(DBHelper.COLUMN_AVATAR, "default");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put(DBHelper.COLUMN_FECHA_ALTA, sdf.format(new Date()));

        long result = db.insert(DBHelper.TABLE_USUARIOS, null, values);
        db.close();

        if (result != -1) {
            // Iniciar sesión automáticamente
            sessionManager.createSession((int) result, usuario, Rol.PADRE);
            Toast.makeText(this, "¡Registro exitoso! ¡Bienvenido " + usuario + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, "El nombre de usuario ya existe", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
