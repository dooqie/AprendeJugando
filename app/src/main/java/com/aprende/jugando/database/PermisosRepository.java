package com.aprende.jugando.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aprende.jugando.ui.ejercicios.util.ExerciseExtras;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repositorio que encapsula las operaciones CRUD sobre la tabla {@code permisos}.
 *
 * <p>Cada fila de la tabla tiene la clave primaria compuesta
 * ({@code usuario_id}, {@code tipo_ejercicio}) y un flag {@code activo} (1/0).
 * Este diseño permite al padre activar o desactivar ejercicios individuales
 * por hijo sin necesidad de eliminar y volver a insertar filas (se usa
 * {@code CONFLICT_REPLACE} para actualizar en una sola operación).
 *
 * <p>Para retrocompatibilidad con instalaciones anteriores donde la tabla podría
 * estar vacía, los métodos de lectura devuelven el conjunto completo de permisos
 * cuando no se encuentran filas para un usuario dado.
 * @author José López Mohedano
 */
public class PermisosRepository {

    private final DBHelper dbHelper;

    /**
     * Construye el repositorio obteniendo un {@link DBHelper} con el contexto de la aplicación.
     *
     * @param ctx contexto Android; se usa {@code getApplicationContext()} internamente
     *            para evitar retener un contexto de Activity.
     */
    public PermisosRepository(Context ctx) {
        this.dbHelper = new DBHelper(ctx.getApplicationContext());
    }

    /**
     * Devuelve el conjunto completo de tipos de ejercicio disponibles.
     * Se utiliza como valor de retorno por defecto cuando un usuario no tiene
     * filas en la tabla {@code permisos} (retrocompatibilidad).
     *
     * @return {@link Set} con todos los identificadores de ejercicio definidos en
     *         {@link ExerciseExtras#ALL_EXERCISE_TYPES}.
     */
    public static Set<String> permisosCompletos() {
        HashSet<String> hs = new HashSet<>();
        for (String t : ExerciseExtras.ALL_EXERCISE_TYPES) {
            hs.add(t);
        }
        return hs;
    }

    /**
     * Devuelve los tipos de ejercicio que el usuario tiene actualmente activos.
     *
     * <p>Si la tabla no contiene ninguna fila para {@code usuarioId} (cuenta
     * creada con una versión anterior de la app), se devuelven todos los
     * ejercicios disponibles para no bloquear al usuario.
     *
     * @param usuarioId ID del usuario hijo en la tabla {@code usuarios}.
     * @return conjunto de cadenas con los tipos de ejercicio permitidos.
     */
    public Set<String> getPermisosActivos(int usuarioId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cExist = db.query(DBHelper.TABLE_PERMISOS,
                new String[]{DBHelper.COLUMN_PERMISO_TIPO_EJERCICIO},
                DBHelper.COLUMN_PERMISO_USUARIO_ID + "=?",
                new String[]{String.valueOf(usuarioId)},
                null, null, null);
        int n = cExist.getCount();
        cExist.close();
        if (n == 0) {
            db.close();
            return permisosCompletos();
        }

        Cursor c = db.query(DBHelper.TABLE_PERMISOS,
                new String[]{DBHelper.COLUMN_PERMISO_TIPO_EJERCICIO},
                DBHelper.COLUMN_PERMISO_USUARIO_ID + "=? AND " + DBHelper.COLUMN_PERMISO_ACTIVO + "=1",
                new String[]{String.valueOf(usuarioId)},
                null, null, null);
        HashSet<String> set = new HashSet<>();
        while (c.moveToNext()) {
            set.add(c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_PERMISO_TIPO_EJERCICIO)));
        }
        c.close();
        db.close();
        return set;
    }


    /**
     * Comprueba si un ejercicio concreto está activo para el usuario.
     *
     * <p>Si la fila no existe se devuelve {@code true} por retrocompatibilidad,
     * asumiendo que el ejercicio debe estar disponible.
     *
     * @param usuarioId     ID del usuario hijo.
     * @param tipoEjercicio identificador del ejercicio (constante de {@link ExerciseExtras}).
     * @return {@code true} si el ejercicio está permitido; {@code false} en caso contrario.
     */
    public boolean isEjercicioActivo(int usuarioId, String tipoEjercicio) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_PERMISOS,
                new String[]{DBHelper.COLUMN_PERMISO_ACTIVO},
                DBHelper.COLUMN_PERMISO_USUARIO_ID + "=? AND " + DBHelper.COLUMN_PERMISO_TIPO_EJERCICIO + "=?",
                new String[]{String.valueOf(usuarioId), tipoEjercicio},
                null, null, null);
        boolean activo = true;
        if (c.moveToFirst()) {
            activo = c.getInt(c.getColumnIndexOrThrow(DBHelper.COLUMN_PERMISO_ACTIVO)) != 0;
        }
        c.close();
        db.close();
        return activo;
    }

    /**
     * Inserta (o reemplaza) una fila {@code activo=1} por cada tipo de ejercicio
     * para un usuario recién creado. La operación se realiza dentro de una
     * transacción para garantizar atomicidad: o se insertan todos los permisos
     * o no se inserta ninguno.
     *
     * @param usuarioId ID del usuario hijo al que se le asignan los permisos por defecto.
     */
    public void crearPermisosDefecto(int usuarioId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String tipo : ExerciseExtras.ALL_EXERCISE_TYPES) {
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.COLUMN_PERMISO_USUARIO_ID, usuarioId);
                cv.put(DBHelper.COLUMN_PERMISO_TIPO_EJERCICIO, tipo);
                cv.put(DBHelper.COLUMN_PERMISO_ACTIVO, 1);
                db.insertWithOnConflict(DBHelper.TABLE_PERMISOS, null, cv,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Activa o desactiva un permiso individual para un usuario hijo.
     * Usa {@code CONFLICT_REPLACE} para simplificar la lógica: una sola llamada
     * sirve tanto para insertar un permiso nuevo como para modificar uno existente.
     *
     * @param usuarioId     ID del usuario hijo.
     * @param tipoEjercicio identificador del ejercicio a modificar.
     * @param activo        {@code true} para activar, {@code false} para desactivar.
     */
    public void setPermiso(int usuarioId, String tipoEjercicio, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLUMN_PERMISO_USUARIO_ID, usuarioId);
        cv.put(DBHelper.COLUMN_PERMISO_TIPO_EJERCICIO, tipoEjercicio);
        cv.put(DBHelper.COLUMN_PERMISO_ACTIVO, activo ? 1 : 0);
        db.insertWithOnConflict(DBHelper.TABLE_PERMISOS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * Recupera la lista de usuarios hijo vinculados a un padre dado, ordenados
     * alfabéticamente por nombre.
     *
     * @param padreId ID del usuario padre (rol 1) en la tabla {@code usuarios}.
     * @return lista de {@link UsuarioHijo} con el ID y nombre de cada hijo; vacía si no tiene.
     */
    public List<UsuarioHijo> getHijosDe(int padreId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBHelper.TABLE_USUARIOS,
                new String[]{DBHelper.COLUMN_ID, DBHelper.COLUMN_NOMBRE},
                DBHelper.COLUMN_PADRE_ID + "=? AND " + DBHelper.COLUMN_ROL_ID + "=?",
                new String[]{String.valueOf(padreId), String.valueOf(com.aprende.jugando.model.Rol.HIJO)},
                null, null, DBHelper.COLUMN_NOMBRE + " ASC");
        List<UsuarioHijo> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(new UsuarioHijo(
                    c.getInt(c.getColumnIndexOrThrow(DBHelper.COLUMN_ID)),
                    c.getString(c.getColumnIndexOrThrow(DBHelper.COLUMN_NOMBRE))));
        }
        c.close();
        db.close();
        return list;
    }

    public static final class UsuarioHijo {
        public final int id;
        public final String nombre;

        public UsuarioHijo(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }
}
