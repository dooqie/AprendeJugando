package com.aprende.jugando.ui.ejercicios;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aprende.jugando.R;
import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actividad que muestra gráficos de progreso del usuario.
 * Incluye gráfico de barras (aciertos por ejercicio) y gráfico circular (distribución).
 * @author José López Mohedano
 */
public class ProgresoActivity extends AppCompatActivity {
    private BarChart barChart;
    private PieChart pieChart;
    private SessionManager sessionManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        Button btnVolver = findViewById(R.id.btnVolverProgreso);

        cargarDatosGrafico();
        cargarGraficoCircular();

        btnVolver.setOnClickListener(v -> finish());
        ButtonAnimUtils.addBounce(this, btnVolver);
    }

    private void cargarDatosGrafico() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        Map<String, int[]> datos = obtenerDatosPorEjercicio();
        
        int index = 0;
        for (Map.Entry<String, int[]> entry : datos.entrySet()) {
            labels.add(entry.getKey());
            entries.add(new BarEntry(index, entry.getValue()[0])); // Aciertos
            index++;
        }

        if (entries.isEmpty()) {
            TextView tvSinDatos = findViewById(R.id.tvSinDatos);
            tvSinDatos.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Aciertos por ejercicio");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void cargarGraficoCircular() {
        Map<String, int[]> datos = obtenerDatosPorEjercicio();
        
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        int[] colorArray = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.MAGENTA, Color.CYAN};
        int colorIndex = 0;
        
        int totalAciertos = 0;
        for (int[] valores : datos.values()) {
            totalAciertos += valores[0];
        }
        
        for (Map.Entry<String, int[]> entry : datos.entrySet()) {
            float porcentaje = totalAciertos > 0 ? (entry.getValue()[0] * 100f / totalAciertos) : 0;
            entries.add(new PieEntry(porcentaje, entry.getKey()));
            colors.add(colorArray[colorIndex % colorArray.length]);
            colorIndex++;
        }

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Distribución de ejercicios");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();
    }

    private Map<String, int[]> obtenerDatosPorEjercicio() {
        Map<String, int[]> datos = new HashMap<>();
        
        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DBHelper.TABLE_RESULTADOS,
            new String[]{DBHelper.COLUMN_TIPO_EJERCICIO, "SUM(" + DBHelper.COLUMN_ACIERTOS + ") as total_aciertos", "SUM(" + DBHelper.COLUMN_FALLOS + ") as total_fallos"},
            DBHelper.COLUMN_USUARIO_ID + " = ?",
            new String[]{String.valueOf(sessionManager.getUserId())},
            DBHelper.COLUMN_TIPO_EJERCICIO,
            null,
            null
        );

        while (cursor.moveToNext()) {
            String tipo = cursor.getString(0);
            int aciertos = cursor.getInt(1);
            datos.put(tipo, new int[]{aciertos});
        }
        
        cursor.close();
        db.close();
        
        return datos;
    }

    public void onVolverClicked(View view) {
        finish();
    }
}
