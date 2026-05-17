package com.aprende.jugando.ui.ejercicios.util;

/**
 * @author José López Mohedano
 */

public final class ExerciseExtras {
    public static final String EXTRA_TIER = "tierNivel";
    /** 1-based in-game level reached during session */
    public static final String EXTRA_NIVEL_JUEGO = "nivelJuego";
    public static final String EXTRA_EXERCISE_TYPE = "tipoEjercicio";
    public static final String EXTRA_TABLAS = "tablasSeleccionadas";

    public static final String TYPE_MULTIPLICAR = "multiplicar";
    public static final String TYPE_SUMAR = "sumar";
    public static final String TYPE_RESTAR = "restar";
    public static final String TYPE_DIVIDIR = "dividir";
    public static final String TYPE_MIXTO = "mixto";
    public static final String TYPE_FIGURAS = "figuras";
    public static final String TYPE_LOGICA = "logica";
    public static final String TYPE_MEMORIA = "memoria";
    /** Identificación simple de animal (dibujo / icono → nombre). */
    public static final String TYPE_ANIMALES = "animales";

    /** Tipos ejercicios sujetos a permisos (orden estable para pantallas padre/hijo). */
    public static final String[] ALL_EXERCISE_TYPES = {
            TYPE_MULTIPLICAR,
            TYPE_SUMAR,
            TYPE_RESTAR,
            TYPE_DIVIDIR,
            TYPE_MIXTO,
            TYPE_FIGURAS,
            TYPE_LOGICA,
            TYPE_MEMORIA,
            TYPE_ANIMALES,
    };

    private ExerciseExtras() {}
}
