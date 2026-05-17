package com.aprende.jugando;

import com.aprende.jugando.utils.PasswordUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Pruebas unitarias para {@link PasswordUtils}.
 *
 * <p>Se verifica el comportamiento del hash SHA-256 y la función de verificación,
 * garantizando que la capa de seguridad de contraseñas funciona correctamente
 * sin dependencia del entorno Android.
 * @author José López Mohedano
 */
public class PasswordUtilsTest {

    // ─── hashPassword ────────────────────────────────────────────────────────

    /**
     * PU-01: El hash de una contraseña no debe ser nulo ni vacío.
     */
    @Test
    public void hashPassword_noEsNuloNiVacio() {
        String hash = PasswordUtils.hashPassword("miClave123");
        assertNotNull("El hash no debe ser nulo", hash);
        assertFalse("El hash no debe estar vacío", hash.isEmpty());
    }

    /**
     * PU-02: SHA-256 siempre produce 64 caracteres hexadecimales (256 bits / 4 bits por char).
     */
    @Test
    public void hashPassword_tieneExactamente64Caracteres() {
        String hash = PasswordUtils.hashPassword("test");
        assertEquals("El hash SHA-256 debe tener 64 caracteres", 64, hash.length());
    }

    /**
     * PU-03: La función es determinista: la misma entrada siempre produce la misma salida.
     */
    @Test
    public void hashPassword_esDeterminista() {
        String h1 = PasswordUtils.hashPassword("contraseña");
        String h2 = PasswordUtils.hashPassword("contraseña");
        assertEquals("El hash debe ser idéntico para la misma entrada", h1, h2);
    }

    /**
     * PU-04: Contraseñas distintas deben producir hashes distintos (resistencia a colisiones).
     */
    @Test
    public void hashPassword_diferentesEntradasProducenHashesDistintos() {
        String h1 = PasswordUtils.hashPassword("abc");
        String h2 = PasswordUtils.hashPassword("ABC");
        assertNotEquals("Contraseñas distintas deben tener hashes distintos", h1, h2);
    }

    /**
     * PU-05: El hash solo contiene caracteres hexadecimales válidos (0-9, a-f).
     */
    @Test
    public void hashPassword_soloContieneCaracteresHex() {
        String hash = PasswordUtils.hashPassword("cualquier_clave_42");
        assertTrue("El hash debe ser hexadecimal válido", hash.matches("[0-9a-f]{64}"));
    }

    /**
     * PU-06: La contraseña vacía también produce un hash válido (SHA-256 de cadena vacía).
     * Valor esperado precalculado con SHA-256("").
     */
    @Test
    public void hashPassword_cadenVaciaProduceHashValido() {
        String hashVacio = PasswordUtils.hashPassword("");
        assertEquals("El hash de cadena vacía debe ser el SHA-256 estándar",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                hashVacio);
    }

    // ─── verifyPassword ───────────────────────────────────────────────────────

    /**
     * PU-07: Verificar con la contraseña correcta debe devolver {@code true}.
     */
    @Test
    public void verifyPassword_contraseniaCorrectaDevuelveTrue() {
        String clave = "admin123";
        String hash  = PasswordUtils.hashPassword(clave);
        assertTrue("La verificación con clave correcta debe ser verdadera",
                PasswordUtils.verifyPassword(clave, hash));
    }

    /**
     * PU-08: Verificar con una contraseña incorrecta debe devolver {@code false}.
     */
    @Test
    public void verifyPassword_contraseniaIncorrectaDevuelveFalse() {
        String hash = PasswordUtils.hashPassword("claveReal");
        assertFalse("La verificación con clave incorrecta debe ser falsa",
                PasswordUtils.verifyPassword("claveErronea", hash));
    }

    /**
     * PU-09: La comparación es sensible a mayúsculas/minúsculas.
     */
    @Test
    public void verifyPassword_sensibleAMayusculas() {
        String hash = PasswordUtils.hashPassword("Clave");
        assertFalse("'clave' no debe verificar contra el hash de 'Clave'",
                PasswordUtils.verifyPassword("clave", hash));
    }

    /**
     * PU-10: El hash SHA-256 de la cadena "test" debe coincidir con el valor
     * estándar precalculado externamente (RFC/NIST). Verifica que el algoritmo
     * utilizado es realmente SHA-256 y no otra variante.
     */
    @Test
    public void hashPassword_valorSHA256EstandarConocido() {
        String hashEsperado =
                "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
        assertEquals("El hash de 'test' debe coincidir con el SHA-256 estándar",
                hashEsperado, PasswordUtils.hashPassword("test"));
    }
}
