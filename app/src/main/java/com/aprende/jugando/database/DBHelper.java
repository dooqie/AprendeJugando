package com.aprende.jugando.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper de base de datos SQLite para la aplicación Aprende Jugando.
 *
 * <p>Gestiona tres tablas principales:
 * <ul>
 *   <li><b>usuarios</b>: almacena nombre, hash de contraseña, rol (1=admin, 2=niño)
 *       y referencia al padre para cuentas infantiles.</li>
 *   <li><b>resultados</b>: registra cada partida jugada (tipo de ejercicio,
 *       aciertos, fallos, tiempo y fecha).</li>
 *   <li><b>permisos</b>: mapa ejercicio↔usuario que controla qué ejercicios
 *       puede ver cada cuenta de niño.</li>
 * </ul>
 *
 * <p>Se siembran dos usuarios de prueba en {@link #onCreate}:
 * {@code admin} (rol 1, contraseña "admin123") y
 * {@code user} (rol 2, contraseña "user123").
 * @author José López Mohedano
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "com.aprende.jugando.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_PASSWORD = "password_hash";
    public static final String COLUMN_ROL_ID = "rol_id";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_FECHA_ALTA = "fecha_alta";
    /** Referencia al usuario padre (NULL para padres / admin). */
    public static final String COLUMN_PADRE_ID = "padre_id";

    public static final String TABLE_RESULTADOS = "resultados";
    public static final String COLUMN_USUARIO_ID = "usuario_id";
    public static final String COLUMN_TIPO_EJERCICIO = "tipo_ejercicio";
    public static final String COLUMN_ACIERTOS = "aciertos";
    public static final String COLUMN_FALLOS = "fallos";
    public static final String COLUMN_TIEMPO = "tiempo";
    public static final String COLUMN_FECHA = "fecha";

    public static final String TABLE_PERMISOS = "permisos";
    public static final String COLUMN_PERMISO_USUARIO_ID = "usuario_id";
    public static final String COLUMN_PERMISO_TIPO_EJERCICIO = "tipo_ejercicio";
    public static final String COLUMN_PERMISO_ACTIVO = "activo";

    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_ROL_ID + " INTEGER DEFAULT 2, " +
                    COLUMN_AVATAR + " TEXT, " +
                    COLUMN_FECHA_ALTA + " TEXT, " +
                    COLUMN_PADRE_ID + " INTEGER);";

    private static final String CREATE_TABLE_RESULTADOS =
            "CREATE TABLE " + TABLE_RESULTADOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USUARIO_ID + " INTEGER NOT NULL, " +
                    COLUMN_TIPO_EJERCICIO + " TEXT NOT NULL, " +
                    COLUMN_ACIERTOS + " INTEGER, " +
                    COLUMN_FALLOS + " INTEGER, " +
                    COLUMN_TIEMPO + " INTEGER, " +
                    COLUMN_FECHA + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_USUARIO_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_ID + "));";

    private static final String CREATE_TABLE_PERMISOS =
            "CREATE TABLE " + TABLE_PERMISOS + " (" +
                    COLUMN_PERMISO_USUARIO_ID + " INTEGER NOT NULL, " +
                    COLUMN_PERMISO_TIPO_EJERCICIO + " TEXT NOT NULL, " +
                    COLUMN_PERMISO_ACTIVO + " INTEGER NOT NULL DEFAULT 1, " +
                    "PRIMARY KEY (" + COLUMN_PERMISO_USUARIO_ID + ", " + COLUMN_PERMISO_TIPO_EJERCICIO + "), " +
                    "FOREIGN KEY(" + COLUMN_PERMISO_USUARIO_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_ID + "));";

    /**
     * Construye el helper usando el contexto de la aplicación.
     *
     * @param context contexto Android; se recomienda usar {@code getApplicationContext()}
     *                para evitar fugas de memoria.
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Crea el esquema completo de la base de datos y siembra los usuarios iniciales.
     * Se ejecuta solo la primera vez que la BD no existe en el dispositivo.
     *
     * @param db instancia de la base de datos sobre la que ejecutar las sentencias DDL.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIOS);
        db.execSQL(CREATE_TABLE_RESULTADOS);
        db.execSQL(CREATE_TABLE_PERMISOS);

        String insertAdmin = "INSERT INTO " + TABLE_USUARIOS +
                " (" + COLUMN_NOMBRE + ", " + COLUMN_PASSWORD + ", " + COLUMN_ROL_ID + ", " + COLUMN_AVATAR + ", " + COLUMN_FECHA_ALTA + ", " + COLUMN_PADRE_ID + ") " +
                "VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1, 'default', datetime('now'), NULL);";
        db.execSQL(insertAdmin);

        String insertUser = "INSERT INTO " + TABLE_USUARIOS +
                " (" + COLUMN_NOMBRE + ", " + COLUMN_PASSWORD + ", " + COLUMN_ROL_ID + ", " + COLUMN_AVATAR + ", " + COLUMN_FECHA_ALTA + ", " + COLUMN_PADRE_ID + ") " +
                "VALUES ('user', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 2, 'default', datetime('now'), " +
                "(SELECT " + COLUMN_ID + " FROM " + TABLE_USUARIOS + " WHERE " + COLUMN_NOMBRE + "='admin' LIMIT 1));";
        db.execSQL(insertUser);
    }

    /**
     * Durante el desarrollo pre-lanzamiento se recrea el esquema desde cero.
     * Cuando la app tenga usuarios reales habrá que sustituir esto por
     * migraciones ALTER TABLE que conserven los datos en caso de cambios.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERMISOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTADOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }
}
