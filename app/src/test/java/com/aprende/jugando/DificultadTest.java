package com.aprende.jugando;

import com.aprende.jugando.ui.ejercicios.util.Dificultad;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Pruebas unitarias para el enum {@link Dificultad}.
 *
 * <p>Validan que la configuración de cada nivel (tiempos, rachas, rangos de operandos
 * y dimensiones del tablero de memoria) cumple los contratos esperados por las
 * actividades de ejercicio, evitando regresiones si se ajustan los parámetros.
 * @author José López Mohedano
 */
public class DificultadTest {

    // ─── fromTier ────────────────────────────────────────────────────────────

    /**
     * PU-11: Tier 1 debe devolver FACIL.
     */
    @Test
    public void fromTier_1_esFACIL() {
        assertEquals(Dificultad.FACIL, Dificultad.fromTier(1));
    }

    /**
     * PU-12: Tier 2 debe devolver MEDIO.
     */
    @Test
    public void fromTier_2_esMEDIO() {
        assertEquals(Dificultad.MEDIO, Dificultad.fromTier(2));
    }

    /**
     * PU-13: Tier 3 debe devolver DIFICIL.
     */
    @Test
    public void fromTier_3_esDIFICIL() {
        assertEquals(Dificultad.DIFICIL, Dificultad.fromTier(3));
    }

    /**
     * PU-14: Un tier desconocido (fuera de 1-3) debe devolver MEDIO como valor defensivo.
     */
    @Test
    public void fromTier_valorInvalidoDevuelveMEDIO() {
        assertEquals("Tier desconocido debe retornar MEDIO", Dificultad.MEDIO, Dificultad.fromTier(99));
        assertEquals("Tier 0 debe retornar MEDIO", Dificultad.MEDIO, Dificultad.fromTier(0));
        assertEquals("Tier negativo debe retornar MEDIO", Dificultad.MEDIO, Dificultad.fromTier(-1));
    }

    // ─── Tiempos ─────────────────────────────────────────────────────────────

    /**
     * PU-15: El tiempo disminuye progresivamente con la dificultad.
     * FACIL (75s) > MEDIO (60s) > DIFICIL (45s).
     */
    @Test
    public void tiempoSegundos_decreceConDificultad() {
        assertTrue("FACIL debe tener más tiempo que MEDIO",
                Dificultad.FACIL.getTiempoSegundos() > Dificultad.MEDIO.getTiempoSegundos());
        assertTrue("MEDIO debe tener más tiempo que DIFICIL",
                Dificultad.MEDIO.getTiempoSegundos() > Dificultad.DIFICIL.getTiempoSegundos());
        assertEquals(75, Dificultad.FACIL.getTiempoSegundos());
        assertEquals(60, Dificultad.MEDIO.getTiempoSegundos());
        assertEquals(45, Dificultad.DIFICIL.getTiempoSegundos());
    }

    // ─── Racha para subir nivel ───────────────────────────────────────────────

    /**
     * PU-16: La racha necesaria para subir de nivel aumenta con la dificultad.
     */
    @Test
    public void rachaParaSubirNivel_aumentaConDificultad() {
        assertTrue("DIFICIL exige más racha que MEDIO",
                Dificultad.DIFICIL.getRachaParaSubirNivel() > Dificultad.MEDIO.getRachaParaSubirNivel());
        assertTrue("MEDIO exige más racha que FACIL",
                Dificultad.MEDIO.getRachaParaSubirNivel() > Dificultad.FACIL.getRachaParaSubirNivel());
    }

    // ─── Rangos de operandos ──────────────────────────────────────────────────

    /**
     * PU-17: El mínimo de operando debe ser siempre 2 para todos los niveles.
     */
    @Test
    public void getMinOperando_siempreEs2() {
        for (Dificultad d : Dificultad.values()) {
            assertEquals("El mínimo operando debe ser 2 en " + d, 2, d.getMinOperando());
        }
    }

    /**
     * PU-18: El rango de multiplicar aumenta con la dificultad
     * y el máximo nunca baja del mínimo.
     */
    @Test
    public void getMaxFactorMultiplicar_mayorOIgualQueMinimo() {
        for (Dificultad d : Dificultad.values()) {
            assertTrue("maxFactorMultiplicar >= minOperando en " + d,
                    d.getMaxFactorMultiplicar() >= d.getMinOperando());
        }
    }

    // ─── Tablero de memoria ───────────────────────────────────────────────────

    /**
     * PU-19: El total de cartas de memoria debe ser par (se juega por parejas).
     */
    @Test
    public void getTotalCartasMemoria_esPar() {
        for (Dificultad d : Dificultad.values()) {
            assertEquals("El total de cartas debe ser par en " + d,
                    0, d.getTotalCartasMemoria() % 2);
        }
    }

    /**
     * PU-20: El total de cartas se calcula correctamente como filas × columnas.
     */
    @Test
    public void getTotalCartasMemoria_coincidenConFilasPorColumnas() {
        for (Dificultad d : Dificultad.values()) {
            int esperado = d.getFilasMemoria() * d.getColumnasMemoria();
            assertEquals("Total cartas en " + d, esperado, d.getTotalCartasMemoria());
        }
    }
}
