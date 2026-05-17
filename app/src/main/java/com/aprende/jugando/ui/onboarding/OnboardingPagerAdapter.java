package com.aprende.jugando.ui.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aprende.jugando.R;

/**
 * Adaptador para las páginas del onboarding.
 * Cada página muestra un emoji, un título y una descripción sobre las funciones de la app.
 * @author José López Mohedano
 */
public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final Context context;

    /** Recursos de string para los títulos de cada página. */
    private final int[] titles = {
            R.string.onboarding_titulo_1,
            R.string.onboarding_titulo_2,
            R.string.onboarding_titulo_3,
            R.string.onboarding_titulo_4
    };

    /** Recursos de string para las descripciones de cada página. */
    private final int[] descriptions = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3,
            R.string.onboarding_desc_4
    };

    /** Emojis ilustrativos para cada pantalla. */
    private final String[] emojis = {"🎓", "🧮", "⭐", "👨‍👧"};

    /** Colores de fondo para cada página (resaltan visualmente cada sección). */
    private final int[] backgroundColors = {
            R.color.light_blue,
            R.color.light_green,
            R.color.light_yellow,
            R.color.light_pink
    };

    public OnboardingPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    /**
     * Enlaza los datos de la página (emoji, título, descripción y color de fondo)
     * con la vista correspondiente.
     */
    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.tvEmoji.setText(emojis[position]);
        holder.tvTitle.setText(context.getString(titles[position]));
        holder.tvDescription.setText(context.getString(descriptions[position]));
        holder.itemView.setBackgroundColor(
                context.getResources().getColor(backgroundColors[position], context.getTheme())
        );
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    /** ViewHolder que referencia las vistas de cada página del onboarding. */
    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        final TextView tvEmoji;
        final TextView tvTitle;
        final TextView tvDescription;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvOnboardingEmoji);
            tvTitle = itemView.findViewById(R.id.tvOnboardingTitle);
            tvDescription = itemView.findViewById(R.id.tvOnboardingDesc);
        }
    }
}
