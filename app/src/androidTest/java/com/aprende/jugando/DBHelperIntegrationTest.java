package com.aprende.jugando;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.database.PermisosRepository;
import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;
import com.aprende.jugando.utils.PasswordUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Prueba de integración que valida la capa de persistencia completa:
 * creación de esquema, siembra de datos, operaciones CRUD en {@code PermisosRepository}
 * y coherencia del hash almacenado con las contraseñas de los usuarios semilla.
 *
 * <p>Se ejecuta sobre una base de datos en disco real en el dispositivo de prueba,
 * garantizando que {@link DBHelper} interactúa correctamente con SQLite de Android
 * sin mocks, lo que lo convierte en una prueba de integración real.
 * @author José López Mohedano
 */
@RunWith(AndroidJUnit4.class)
public class DBHelperIntegrationTest {

    private DBHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Borrar la BD anterior para comenzar cada test desde cero
        context.deleteDatabase("com.aprende.jugando.db");
        dbHelper = new DBHelper(context);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase("com.aprende.jugando.db");
    }

    // ─── IT-01: Creación del esquema ─────────────────────────────────────────

    /**
     * IT-01: Tras {@code onCreate}, las tres tablas deben existir en la BD.
     */
    @Test
    public void onCreate_creaLasTresTablasEsperadas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        assertTrue("Tabla usuarios debe existir",  tablaExiste(db, DBHelper.TABLE_USUARIOS));
        assertTrue("Tabla resultados debe existir", tablaExiste(db, DBHelper.TABLE_RESULTADOS));
        assertTrue("Tabla permisos debe existir",   tablaExiste(db, DBHelper.TABLE_PERMISOS));

        db.close();
    }

    // ─── IT-02: Siembra de usuarios ──────────────────────────────────────────

    /**
     * IT-02: La BD debe contener exactamente 2 usuarios al crearse: 'admin' y 'user'.
     */
    @Test
    public void onCreate_siembra2UsuariosIniciales() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_USUARIOS,
                new String[]{"COUNT(*)"}, null, null, null, null, null);
        c.moveToFirst();
        int total = c.getInt(0);
        c.close();
        db.close();

        assertEquals("Deben existir exactamente 2 usuarios semilla", 2, total);
    }

    // ─── IT-03: Hash del usuario 'admin' ─────────────────────────────────────

    /**
     * IT-03: El hash almacenado para 'admin' debe corresponder a la contraseña "admin123".
     * Valida que DBHelper y PasswordUtils son coherentes entre sí.
     */
    @Test
    public void admin_hashCoincideConPasswordAdmin123() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_USUARIOS,
                new String[]{DBHelper.COLUMN_PASSWORD},
                DBHelper.COLUMN_NOMBRE + "=?",
                new String[]{"admin"},
                null, null, null);

        assertTrue("El usuario 'admin' debe existir", c.moveToFirst());
        String hashAlmacenado = c.getString(0);
        c.close();
        db.close();

        assertTrue("El hash del admin debe verificar con 'admin123'",
                PasswordUtils.verifyPassword("admin123", hashAlmacenado));
    }

    // ─── IT-04: PermisosRepository — creación y lectura ──────────────────────

    /**
     * IT-04: Al crear permisos por defecto para un nuevo usuario, deben crearse
     * tantas filas como tipos de ejercicio existen, todos activos.
     */
    @Test
    public void permisosRepository_crearPermisosDefecto_creaTodasLasFilas() {
        // Insertar un usuario de prueba para usarlo como sujeto
        int userId = insertarUsuarioPrueba("hijo_test");

        PermisosRepository repo = new PermisosRepository(context);
        repo.crearPermisosDefecto(userId);

        Set<String> activos = repo.getPermisosActivos(userId);
        int totalEjercicios = ExerciseExtras.ALL_EXERCISE_TYPES.length;

        assertEquals("Todos los ejercicios deben estar activos por defecto",
                totalEjercicios, activos.size());
    }

    // ─── IT-05: PermisosRepository — activar/desactivar ──────────────────────

    /**
     * IT-05: Tras desactivar un permiso concreto, {@code isEjercicioActivo} debe
     * devolver {@code false} para ese ejercicio y {@code true} para los demás.
     */
    @Test
    public void permisosRepository_setPermiso_desactivaEjercicioCorrecto() {
        int userId = insertarUsuarioPrueba("hijo_test2");
        PermisosRepository repo = new PermisosRepository(context);
        repo.crearPermisosDefecto(userId);

        String ejercicioDesactivado = ExerciseExtras.TYPE_MULTIPLICAR;
        repo.setPermiso(userId, ejercicioDesactivado, false);

        assertFalse("El ejercicio desactivado no debe estar activo",
                repo.isEjercicioActivo(userId, ejercicioDesactivado));

        // El resto de ejercicios sigue activo
        for (String tipo : ExerciseExtras.ALL_EXERCISE_TYPES) {
            if (!tipo.equals(ejercicioDesactivado)) {
                assertTrue("El ejercicio " + tipo + " debe seguir activo",
                        repo.isEjercicioActivo(userId, tipo));
            }
        }
    }

    // ─── Utilidades de apoyo ──────────────────────────────────────────────────

    /** Comprueba si una tabla existe en la base de datos usando sqlite_master. */
    private boolean tablaExiste(SQLiteDatabase db, String nombreTabla) {
        Cursor c = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{nombreTabla});
        boolean existe = c.moveToFirst();
        c.close();
        return existe;
    }

    /**
     * Inserta un usuario hijo mínimo para usar como sujeto en los tests de permisos.
     *
     * @param nombre nombre único del usuario de prueba.
     * @return ID del usuario recién insertado.
     */
    private int insertarUsuarioPrueba(String nombre) {
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put(DBHelper.COLUMN_NOMBRE, nombre);
        cv.put(DBHelper.COLUMN_PASSWORD, PasswordUtils.hashPassword("test1234"));
        cv.put(DBHelper.COLUMN_ROL_ID, 2);
        cv.put(DBHelper.COLUMN_AVATAR, "default");
        cv.put(DBHelper.COLUMN_FECHA_ALTA, "2026-01-01");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DBHelper.TABLE_USUARIOS, null, cv);
        db.close();
        return (int) id;
    }
}
