package com.aprende.jugando.utils;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Utilidad para gestionar la reproducción de música en la aplicación.
 * Permite iniciar, pausar, reanudar y detener la música de fondo o efectos de sonido.
 * @author José López Mohedano
 */
public class MusicPlayer {
    private static MediaPlayer mediaPlayer;

    public static void startMusic(Context context, int resId) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(context, resId);

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> stopMusic());
            mediaPlayer.start();
        }
    }

    public static void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
