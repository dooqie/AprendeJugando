package com.aprende.jugando.model;

/**
 * Modelo de rol para el sistema de usuarios.
 * @author José López Mohedano
 */
public class Rol {
    public static final int ADMIN = 1;
    public static final int NINO = 2;
    /** Cuenta padre (gestiona hijos y permisos); mismo id que ADMIN en BD. */
    public static final int PADRE = ADMIN;
    /** Cuenta hijo (solo ejercicios permitidos). */
    public static final int HIJO = NINO;

    private int id;
    private String nombre;

    public Rol() {}

    public Rol(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
