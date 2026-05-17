package com.aprende.jugando.model;

/**
 * Modelo de usuario para el sistema de autenticación.
 * Representa un usuario de la aplicación (niño o administrador).
 * @author José López Mohedano
 */
public class Usuario {
    private int id;
    private String nombre;
    private String passwordHash;
    private int rolId;
    private String avatar;
    private String fechaAlta;

    public Usuario() {}

    public Usuario(int id, String nombre, String passwordHash, int rolId, String avatar, String fechaAlta) {
        this.id = id;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.rolId = rolId;
        this.avatar = avatar;
        this.fechaAlta = fechaAlta;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(String fechaAlta) { this.fechaAlta = fechaAlta; }

    public boolean isAdmin() { return rolId == 1; }
}
