package com.aprende.jugando.ui.ejercicios.util;

/**
 * Enum que centraliza la configuración de los tres niveles de dificultad.
 *
 * <p>Cada constante define:
 * <ul>
 *   <li><b>tier</b>: número de banda (1=Fácil, 2=Medio, 3=Difícil), usado como
 *       identificador al pasar datos entre actividades via Intent extras.</li>
 *   <li><b>tiempoSegundos</b>: tiempo de cuenta atrás del ejercicio. A mayor
 *       dificultad, menos tiempo disponible.</li>
 *   <li><b>rachaParaSubirNivel</b>: aciertos consecutivos necesarios para
 *       disparar {@code LevelUpActivity} y acumular un nivel.</li>
 * </ul>
 *
 * <p>Los rangos de operandos se calculan de forma lazy mediante métodos {@code get*}
 * en lugar de campos para mantener el enum compacto y evitar combinatoria en el constructor.
 * El mínimo es siempre 2 para evitar trivialismos (×1, +0, etc.).
 * @author José López Mohedano
 */
public enum Dificultad {
    /** Nivel fácil: 75 segundos, 3 aciertos consecutivos para subir. */
    FACIL(1, 75, 3),
    /** Nivel medio: 60 segundos, 4 aciertos consecutivos para subir. */
    MEDIO(2, 60, 4),
    /** Nivel difícil: 45 segundos, 5 aciertos consecutivos para subir. */
    DIFICIL(3, 45, 5);

    private final int tier;
    private final int tiempoSegundos;
    private final int rachaParaSubirNivel;

    Dificultad(int tier, int tiempoSegundos, int rachaParaSubirNivel) {
        this.tier = tier;
        this.tiempoSegundos = tiempoSegundos;
        this.rachaParaSubirNivel = rachaParaSubirNivel;
    }

    public int getTier() {
        return tier;
    }

    public int getTiempoSegundos() {
        return tiempoSegundos;
    }

    public int getRachaParaSubirNivel() {
        return rachaParaSubirNivel;
    }

    /** Mínimo común para operandos (tablas, factores, sumandos, divisor/cociente ≥ 2). */
    public int getMinOperando() {
        return 2;
    }

    /** Rango máximo para operando en sumas/restas inclusivo sobre el máximo. */
    public int getMaxOperandoSuma() {
        switch (this) {
            case FACIL:
                return 10;
            case MEDIO:
                return 15;
            default:
                return 20;
        }
    }

    /** Multiplicación y división exacta: valores en [getMinOperando(), getMaxFactorMultiplicar()]. */
    public int getMaxFactorMultiplicar() {
        switch (this) {
            case FACIL:
                return 10;
            case MEDIO:
                return 12;
            default:
                return 12;
        }
    }

    /** Lógica: inicio de secuencia (1-based upper bound aleatorio sobre [1, máx]). */
    public int getMaxInicioSecuencia() {
        switch (this) {
            case FACIL:
                return 6;
            case MEDIO:
                return 8;
            default:
                return 10;
        }
    }

    /** Lógica: paso máximo de la progresión. */
    public int getMaxPasoSecuencia() {
        switch (this) {
            case FACIL:
                return 3;
            case MEDIO:
                return 4;
            default:
                return 5;
        }
    }

    /** Lógica: paso mínimo para no usar +1 cuando no se desea desde Fácil. */
    public int getMinPasoSecuencia() {
        switch (this) {
            case FACIL:
                return 2;
            case MEDIO:
                return 2;
            default:
                return 3;
        }
    }

    /** Memoria: filas del tablero (4 columnas fijas → 12/16/20 cartas). */
    public int getFilasMemoria() {
        switch (this) {
            case FACIL:
                return 3;
            case MEDIO:
                return 4;
            default:
                return 5;
        }
    }

    /** Memoria: columnas fijas del tablero. */
    public int getColumnasMemoria() {
        return 4;
    }

    public int getTotalCartasMemoria() {
        return getFilasMemoria() * getColumnasMemoria();
    }

    /**
     * Obtiene la constante de dificultad correspondiente a un tier numérico.
     * Se usa al leer el extra {@code EXTRA_TIER} de un Intent para recuperar
     * la configuración completa de dificultad de forma uniforme.
     *
     * @param tier número de banda (1, 2 o 3).
     * @return la constante {@link Dificultad} asociada; {@link #MEDIO} si el valor
     *         es desconocido (comportamiento defensivo).
     */
    public static Dificultad fromTier(int tier) {
        switch (tier) {
            case 1:
                return FACIL;
            case 2:
                return MEDIO;
            case 3:
                return DIFICIL;
            default:
                return MEDIO;
        }
    }
}
