package com.aprende.jugando.ui.ejercicios;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aprende.jugando.R;
import com.aprende.jugando.database.DBHelper;
import com.aprende.jugando.utils.ButtonAnimUtils;
import com.aprende.jugando.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Actividad que muestra el historial de resultados del usuario.
 * @author José López Mohedano
 */
public class HistorialActivity extends AppCompatActivity {
    private ListView listView;
    private SessionManager sessionManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        listView = findViewById(R.id.historialListView);
        Button btnAtras = findViewById(R.id.btnAtrasHistorial);

        cargarHistorial();

        btnAtras.setOnClickListener(v -> finish());
        ButtonAnimUtils.addBounce(this, btnAtras);
    }

    private void cargarHistorial() {
        List<HashMap<String, String>> data = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = sessionManager.isAdmin() ? null :
                DBHelper.COLUMN_USUARIO_ID + " = ?";
        String[] selectionArgs = sessionManager.isAdmin() ? null :
                new String[]{String.valueOf(sessionManager.getUserId())};

        Cursor cursor = db.query(
                DBHelper.TABLE_RESULTADOS,
                null,
                selection,
                selectionArgs,
                null, null,
                DBHelper.COLUMN_ID + " DESC"
        );

        while (cursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            int ac = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ACIERTOS));
            int fa = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FALLOS));
            int dif = ac - fa;
            map.put("tipo", cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIPO_EJERCICIO)));
            map.put("aciertos", String.valueOf(ac));
            map.put("fallos", String.valueOf(fa));
            map.put("diferencia", String.valueOf(dif));
            map.put("fecha", cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FECHA)));
            data.add(map);
        }
        cursor.close();
        db.close();

        String[] from = {"tipo", "aciertos", "fallos", "diferencia", "fecha"};
        int[] to = {R.id.colTipo, R.id.colAciertos, R.id.colFallos, R.id.colDiferencia, R.id.colFecha};

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.item_historial, from, to);
        adapter.setViewBinder((view, data12, textRepresentation) -> {
            if (view.getId() == R.id.colDiferencia) {
                android.widget.TextView tv = (android.widget.TextView) view;
                tv.setText(textRepresentation);
                try {
                    int d = Integer.parseInt(textRepresentation);
                    int green = ContextCompat.getColor(HistorialActivity.this, R.color.accent_success);
                    int red = ContextCompat.getColor(HistorialActivity.this, R.color.accent_red);
                    tv.setTextColor(d >= 0 ? green : red);
                } catch (NumberFormatException e) {
                    tv.setTextColor(Color.BLACK);
                }
                return true;
            }
            return false;
        });
        listView.setAdapter(adapter);
    }

    public void onActualizarClicked(android.view.View view) {
        cargarHistorial();
    }
}
