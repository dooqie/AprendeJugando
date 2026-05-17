package com.aprende.jugando.model;

/**
 * Modelo de resultado de ejercicio.
 * Almacena los resultados de cada sesión de juego por usuario.
 * @author José López Mohedano
 */
public class Resultado {
    private int id;
    private int usuarioId;
    private String tipoEjercicio;
    private int aciertos;
    private int fallos;
    private int tiempo;
    private String fecha;

    public Resultado() {}

    public Resultado(int id, int usuarioId, String tipoEjercicio, int aciertos, int fallos, int tiempo, String fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tipoEjercicio = tipoEjercicio;
        this.aciertos = aciertos;
        this.fallos = fallos;
        this.tiempo = tiempo;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getTipoEjercicio() { return tipoEjercicio; }
    public void setTipoEjercicio(String tipoEjercicio) { this.tipoEjercicio = tipoEjercicio; }

    public int getAciertos() { return aciertos; }
    public void setAciertos(int aciertos) { this.aciertos = aciertos; }

    public int getFallos() { return fallos; }
    public void setFallos(int fallos) { this.fallos = fallos; }

    public int getTiempo() { return tiempo; }
    public void setTiempo(int tiempo) { this.tiempo = tiempo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getDiferencia() { return aciertos - fallos; }
}
