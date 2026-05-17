package com.aprende.jugando.ui.ejercicios.util;

import android.widget.Button;

import com.aprende.jugando.R;

/**
 * Resalta la opción correcta (verde) cuando el jugador falla, comparando el texto mostrado en cada botón.
 * @author José López Mohedano
 */
public final class ExerciseAnswerFeedbackHelper {

    private ExerciseAnswerFeedbackHelper() {
    }

    /**
     * Busca el botón cuyo texto coincide con {@code correctAnswer} y aplica el drawable de acierto.
     */
    public static void highlightCorrectNumeric(int correctAnswer, Button b1, Button b2, Button b3, Button b4) {
        highlightCorrectNumeric(correctAnswer, new Button[]{b1, b2, b3, b4});
    }

    public static void highlightCorrectNumeric(int correctAnswer, Button[] optionButtons) {
        applyHighlight(String.valueOf(correctAnswer), optionButtons);
    }

    /**
     * Igual que {@link #highlightCorrectNumeric(int, Button[])} pero para etiquetas de texto (figuras, animales).
     */
    public static void highlightCorrectText(String correctAnswer, Button[] optionButtons) {
        if (correctAnswer == null) {
            return;
        }
        applyHighlight(correctAnswer, optionButtons);
    }

    private static void applyHighlight(String expectedLabel, Button[] optionButtons) {
        if (expectedLabel == null || optionButtons == null) {
            return;
        }
        for (Button b : optionButtons) {
            if (b != null && expectedLabel.contentEquals(b.getText())) {
                b.setBackgroundResource(R.drawable.button_feedback_correct);
                b.setAlpha(1.0f);
                return;
            }
        }
    }
}
