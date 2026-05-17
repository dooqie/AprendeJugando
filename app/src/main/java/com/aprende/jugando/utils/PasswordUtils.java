package com.aprende.jugando.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidades estáticas para el manejo seguro de contraseñas.
 *
 * <p>Las contraseñas nunca se almacenan en texto plano: se guarda únicamente
 * el hash SHA-256 en hexadecimal (64 caracteres). SHA-256 se eligió por su
 * disponibilidad en todas las versiones de Android (sin dependencias extra)
 * y por ser suficiente para este contexto educativo de baja criticidad.
 *
 * <p>En un sistema de producción con datos sensibles sería preferible usar
 * bcrypt o Argon2 con sal, pero SHA-256 es adecuado para el alcance de esta app.
 * @author José López Mohedano
 */
public class PasswordUtils {

    /**
     * Genera el hash SHA-256 de la contraseña en representación hexadecimal.
     *
     * @param password contraseña en texto plano; no debe ser {@code null}.
     * @return cadena hexadecimal de 64 caracteres con el hash de la contraseña.
     * @throws RuntimeException si el algoritmo SHA-256 no está disponible en la JVM
     *         (situación que no debería ocurrir en Android).
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash de contraseña", e);
        }
    }

    /**
     * Verifica si una contraseña en texto plano corresponde a un hash almacenado.
     *
     * @param password contraseña candidata en texto plano.
     * @param hash     hash SHA-256 en hexadecimal contra el que comparar.
     * @return {@code true} si {@code hashPassword(password).equals(hash)}.
     */
    public static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}
