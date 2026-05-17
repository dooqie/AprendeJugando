package com.aprende.jugando.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utilidad para gestionar la sesión del usuario.
 * Maneja el inicio y cierre de sesión usando SharedPreferences.
 * @author José López Mohedano
 */
public class SessionManager {
    private static final String PREF_NAME = "SessionPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROL = "user_rol";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Inicia sesión con los datos del usuario.
     */
    public void createSession(int userId, String userName, int rolId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putInt(KEY_USER_ROL, rolId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Cierra la sesión actual.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Verifica si hay una sesión activa.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Obtiene el ID del usuario actual.
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * Obtiene el nombre del usuario actual.
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    /**
     * Obtiene el rol del usuario actual.
     */
    public int getUserRol() {
        return prefs.getInt(KEY_USER_ROL, 2);
    }

    /**
     * Verifica si el usuario actual es administrador.
     */
    public boolean isAdmin() {
        return getUserRol() == 1;
    }
}
