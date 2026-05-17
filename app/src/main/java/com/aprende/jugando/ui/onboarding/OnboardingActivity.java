package com.aprende.jugando.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.aprende.jugando.R;
import com.aprende.jugando.ui.auth.LoginActivity;

/**
 * Actividad de bienvenida (onboarding) que se muestra únicamente en la primera ejecución.
 * Presenta 4 pantallas deslizables que explican las funciones principales de la aplicación.
 * Al completar o saltar el onboarding, navega a LoginActivity y guarda una marca
 * en SharedPreferences para no volver a mostrarse.
 * @author José López Mohedano
 */
public class OnboardingActivity extends AppCompatActivity {

    /** Nombre del fichero de preferencias que almacena el estado del onboarding. */
    public static final String PREFS_ONBOARDING = "OnboardingPrefs";

    /** Clave que indica si el usuario ya ha completado el onboarding. */
    public static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private ViewPager2 viewPager;
    private Button btnNext;
    private Button btnSkip;
    private LinearLayout layoutDots;
    private OnboardingPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        btnNext   = findViewById(R.id.btnOnboardingNext);
        btnSkip   = findViewById(R.id.btnOnboardingSkip);
        layoutDots = findViewById(R.id.layoutOnboardingDots);

        adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                boolean esUltima = position == adapter.getItemCount() - 1;
                btnNext.setText(esUltima
                        ? getString(R.string.onboarding_empezar)
                        : getString(R.string.onboarding_siguiente));
                btnSkip.setVisibility(esUltima ? View.INVISIBLE : View.VISIBLE);
            }
        });

        btnNext.setOnClickListener(v -> {
            int actual = viewPager.getCurrentItem();
            if (actual < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(actual + 1);
            } else {
                finalizarOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finalizarOnboarding());
    }

    /**
     * Recrea los indicadores de punto en la parte inferior.
     * El punto activo muestra un drawable diferente al de los inactivos.
     *
     * @param paginaActual índice (0-based) de la página visible.
     */
    private void setupDots(int paginaActual) {
        layoutDots.removeAllViews();
        int total = adapter.getItemCount();
        for (int i = 0; i < total; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == paginaActual ? R.drawable.dot_active : R.drawable.dot_inactive);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(14), dpToPx(14));
            params.setMargins(dpToPx(5), 0, dpToPx(5), 0);
            dot.setLayoutParams(params);
            layoutDots.addView(dot);
        }
    }

    /**
     * Marca el onboarding como completado y navega a la pantalla de login.
     * Usa FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK para limpiar el back-stack.
     */
    private void finalizarOnboarding() {
        getSharedPreferences(PREFS_ONBOARDING, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ONBOARDING_DONE, true)
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Comprueba si el usuario ya ha visto el onboarding.
     * Se usa desde LoginActivity para decidir si redirigir aquí.
     *
     * @param context contexto de la aplicación.
     * @return {@code true} si el onboarding ya fue completado o saltado.
     */
    public static boolean isOnboardingDone(Context context) {
        return context.getSharedPreferences(PREFS_ONBOARDING, MODE_PRIVATE)
                .getBoolean(KEY_ONBOARDING_DONE, false);
    }

    /** Convierte dp a píxeles usando la densidad de pantalla del dispositivo. */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
