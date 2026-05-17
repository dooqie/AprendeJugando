package com.aprende.jugando.ui.admin;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.database.PermisosRepository;
import com.aprende.jugando.model.Rol;
import com.aprende.jugando.model.Usuario;
import com.aprende.jugando.ui.auth.PermisosHijoActivity;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.PasswordUtils;
import com.aprende.jugando.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Panel de administración para gestionar usuarios y ver estadísticas.
 * @author José López Mohedano
 */
public class AdminActivity extends AppCompatActivity {
    private ListView lvUsuarios;
    private Spinner spinnerEstadisticas;
    private EditText etNuevoUsuario;
    private EditText etNuevaContrasena;
    private Spinner spinnerNuevoRol;
    private Button btnCrearUsuario;
    private Button btnEliminarUsuario;
    private Button btnVerEstadisticas;
    private Button btnEditarPermisos;
    private SessionManager sessionManager;
    private DBHelper dbHelper;
    private PermisosRepository permisosRepository;
    private List<Usuario> listaUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        permisosRepository = new PermisosRepository(this);

        // Verificar que es admin
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        cargarUsuarios();
        ButtonAnimUtils.addBounce(this, btnCrearUsuario, btnEliminarUsuario, btnVerEstadisticas,
                btnEditarPermisos, findViewById(R.id.btnVolverAdmin));
    }

    private void initViews() {
        lvUsuarios = findViewById(R.id.lvUsuarios);
        spinnerEstadisticas = findViewById(R.id.spinnerEstadisticas);
        etNuevoUsuario = findViewById(R.id.etNuevoUsuario);
        etNuevaContrasena = findViewById(R.id.etNuevaContrasena);
        spinnerNuevoRol = findViewById(R.id.spinnerNuevoRol);
        btnCrearUsuario = findViewById(R.id.btnCrearUsuario);
        btnEliminarUsuario = findViewById(R.id.btnEliminarUsuario);
        btnVerEstadisticas = findViewById(R.id.btnVerEstadisticas);
        btnEditarPermisos = findViewById(R.id.btnEditarPermisos);

        lvUsuarios.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Configurar spinner de creación de usuarios
        String[] tipos = {"Niño", "Admin"};
        ArrayAdapter<String> rolAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNuevoRol.setAdapter(rolAdapter);

        // Configurar spinner de estadísticas
        String[] estadisticasTipos = {"Todos", "Multiplicar", "Sumar", "Figuras", "Lógica", "Memoria"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estadisticasTipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadisticas.setAdapter(adapter);

        btnCrearUsuario.setOnClickListener(v -> crearUsuario());
        btnEliminarUsuario.setOnClickListener(v -> eliminarUsuarioSeleccionado());
        btnVerEstadisticas.setOnClickListener(v -> verEstadisticas());
        btnEditarPermisos.setOnClickListener(v -> editarPermisosSeleccionado());
    }

    private void editarPermisosSeleccionado() {
        int posicion = lvUsuarios.getCheckedItemPosition();
        if (posicion == ListView.INVALID_POSITION) {
            Toast.makeText(this, "Selecciona un usuario", Toast.LENGTH_SHORT).show();
            return;
        }
        Usuario usuario = listaUsuarios.get(posicion);
        if (usuario.getRolId() == Rol.ADMIN) {
            Toast.makeText(this, "Los administradores tienen acceso a todos los ejercicios", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, PermisosHijoActivity.class);
        i.putExtra(PermisosHijoActivity.EXTRA_HIJO_ID, usuario.getId());
        i.putExtra(PermisosHijoActivity.EXTRA_HIJO_NOMBRE, usuario.getNombre());
        startActivity(i);
    }

    private void cargarUsuarios() {
        listaUsuarios = new ArrayList<>();
        List<String> nombresUsuarios = new ArrayList<>();

        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USUARIOS, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Usuario usuario = new Usuario();
            usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID)));
            usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOMBRE)));
            usuario.setRolId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ROL_ID)));
            listaUsuarios.add(usuario);
            
            String rol = usuario.getRolId() == 1 ? "[Admin]" : "[Niño]";
            nombresUsuarios.add(usuario.getNombre() + " " + rol);
        }
        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, nombresUsuarios);
        lvUsuarios.setAdapter(adapter);
    }

    private void eliminarUsuarioSeleccionado() {
        int posicion = lvUsuarios.getCheckedItemPosition();
        if (posicion == ListView.INVALID_POSITION) {
            Toast.makeText(this, "Selecciona un usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario usuario = listaUsuarios.get(posicion);
        
        // No permitir eliminar al admin
        if (usuario.getNombre().equals("admin")) {
            Toast.makeText(this, "No se puede eliminar el usuario admin", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("¿Estás seguro de eliminar a " + usuario.getNombre() + "?")
            .setPositiveButton("Sí", (dialog, which) -> {
                android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
                
                // Eliminar resultados del usuario
                db.delete(DBHelper.TABLE_RESULTADOS, DBHelper.COLUMN_USUARIO_ID + " = ?", 
                    new String[]{String.valueOf(usuario.getId())});

                // Eliminar permisos
                db.delete(DBHelper.TABLE_PERMISOS, DBHelper.COLUMN_PERMISO_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(usuario.getId())});

                // Eliminar usuario
                db.delete(DBHelper.TABLE_USUARIOS, DBHelper.COLUMN_ID + " = ?", 
                    new String[]{String.valueOf(usuario.getId())});
                db.close();
                
                Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                cargarUsuarios();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void crearUsuario() {
        String nombre = etNuevoUsuario.getText().toString().trim();
        String contrasena = etNuevaContrasena.getText().toString().trim();
        String rolSeleccionado = spinnerNuevoRol.getSelectedItem().toString();
        int rolId = rolSeleccionado.equals("Admin") ? 1 : 2;

        // Validación campo a campo con setError para feedback visual directo
        if (nombre.isEmpty()) {
            etNuevoUsuario.setError("Ingresa un nombre de usuario");
            etNuevoUsuario.requestFocus();
            return;
        }
        if (nombre.length() < 3) {
            etNuevoUsuario.setError("El nombre debe tener al menos 3 caracteres");
            etNuevoUsuario.requestFocus();
            return;
        }
        if (nombre.length() > 20) {
            etNuevoUsuario.setError("El nombre no puede superar 20 caracteres");
            etNuevoUsuario.requestFocus();
            return;
        }
        if (contrasena.isEmpty()) {
            etNuevaContrasena.setError("Ingresa una contraseña");
            etNuevaContrasena.requestFocus();
            return;
        }
        if (contrasena.length() < 4) {
            etNuevaContrasena.setError("La contraseña debe tener al menos 4 caracteres");
            etNuevaContrasena.requestFocus();
            return;
        }
        if (usuarioExiste(nombre)) {
            etNuevoUsuario.setError("El nombre de usuario ya está en uso");
            etNuevoUsuario.requestFocus();
            return;
        }

        String hash = PasswordUtils.hashPassword(contrasena);
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NOMBRE, nombre);
        values.put(DBHelper.COLUMN_PASSWORD, hash);
        values.put(DBHelper.COLUMN_ROL_ID, rolId);
        values.put(DBHelper.COLUMN_AVATAR, "default");
        values.put(DBHelper.COLUMN_FECHA_ALTA, fechaActual);
        if (rolId == Rol.HIJO) {
            values.put(DBHelper.COLUMN_PADRE_ID, sessionManager.getUserId());
        } else {
            values.putNull(DBHelper.COLUMN_PADRE_ID);
        }

        android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert(DBHelper.TABLE_USUARIOS, null, values);
        db.close();

        if (result != -1) {
            if (rolId == Rol.HIJO) {
                permisosRepository.crearPermisosDefecto((int) result);
            }
            Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show();
            etNuevoUsuario.setText("");
            etNuevaContrasena.setText("");
            spinnerNuevoRol.setSelection(0);
            cargarUsuarios();
        } else {
            Toast.makeText(this, "Error al crear usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean usuarioExiste(String nombre) {
        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USUARIOS, new String[]{DBHelper.COLUMN_ID},
            DBHelper.COLUMN_NOMBRE + " = ?", new String[]{nombre}, null, null, null);
        boolean existe = cursor.moveToFirst();
        cursor.close();
        db.close();
        return existe;
    }

    private void verEstadisticas() {
        String tipoSeleccionado = spinnerEstadisticas.getSelectedItem().toString();
        String tipo = tipoSeleccionado.equals("Todos") ? null : tipoSeleccionado.toLowerCase();

        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        StringBuilder mensaje = new StringBuilder("Estadísticas ");
        if (tipo != null) {
            mensaje.append("(").append(tipo).append("):\n\n");
        } else {
            mensaje.append("generales:\n\n");
        }

        // Contar usuarios
        Cursor cursorUsuarios = db.query(DBHelper.TABLE_USUARIOS, new String[]{"COUNT(*)"}, null, null, null, null, null);
        if (cursorUsuarios.moveToFirst()) {
            mensaje.append("Total usuarios: ").append(cursorUsuarios.getInt(0)).append("\n");
        }
        cursorUsuarios.close();

        // Contar resultados
        String selection = tipo != null ? DBHelper.COLUMN_TIPO_EJERCICIO + " = ?" : null;
        String[] selectionArgs = tipo != null ? new String[]{tipo} : null;
        
        Cursor cursorResultados = db.query(DBHelper.TABLE_RESULTADOS, 
            new String[]{"SUM(" + DBHelper.COLUMN_ACIERTOS + ")", "SUM(" + DBHelper.COLUMN_FALLOS + ")", "COUNT(*)"},
            selection, selectionArgs, null, null, null);
        
        if (cursorResultados.moveToFirst()) {
            int totalAciertos = cursorResultados.getInt(0);
            int totalFallos = cursorResultados.getInt(1);
            int totalPartidas = cursorResultados.getInt(2);
            
            mensaje.append("Total partidas: ").append(totalPartidas).append("\n");
            mensaje.append("Total aciertos: ").append(totalAciertos).append("\n");
            mensaje.append("Total fallos: ").append(totalFallos).append("\n");
            
            if (totalPartidas > 0) {
                int media = totalAciertos / totalPartidas;
                mensaje.append("Media de aciertos: ").append(media).append("\n");
            }
        }
        cursorResultados.close();
        db.close();

        new AlertDialog.Builder(this)
            .setTitle("Estadísticas")
            .setMessage(mensaje.toString())
            .setPositiveButton("Aceptar", null)
            .show();
    }

    public void onVolverClicked(View view) {
        finish();
    }
}
