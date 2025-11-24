package com.example.gastapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.gastapp.models.Movimiento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgregarMovimientoActivity extends AppCompatActivity {

    private EditText etNombreAdd, etMontoAdd, etFechaAdd;
    private AutoCompleteTextView actvCategoria, actvMedioPago;
    private CheckBox cbEsIngresoAdd, cbEsFijoAdd;
    private Button btnAddConfirm;
    private FirebaseFirestore db;
    private String uid;

    private List<String> categoriasIds = new ArrayList<>();
    private List<String> categoriasNombres = new ArrayList<>();
    private List<String> cuentasIds = new ArrayList<>();
    private List<String> cuentasNombres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_movimiento);

        Toolbar toolbar = findViewById(R.id.toolbarAgregarMov);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNombreAdd = findViewById(R.id.etNombreAdd);
        etMontoAdd = findViewById(R.id.etMontoAdd);
        etFechaAdd = findViewById(R.id.etFechaAdd);
        actvCategoria = findViewById(R.id.actvCategoria);
        actvMedioPago = findViewById(R.id.actvMedioPago);
        cbEsIngresoAdd = findViewById(R.id.cbEsIngresoAdd);
        cbEsFijoAdd = findViewById(R.id.cbEsFijoAdd);
        btnAddConfirm = findViewById(R.id.btnAddConfirm);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Calendar c = Calendar.getInstance();
        String fechaHoy = String.format(Locale.getDefault(), "%02d/%02d/%02d",
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR) % 100);
        etFechaAdd.setText(fechaHoy);

        etFechaAdd.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String f = String.format(Locale.getDefault(), "%02d/%02d/%02d",
                                dayOfMonth, month + 1, year % 100);
                        etFechaAdd.setText(f);
                    },
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        loadCategorias();
        loadCuentas();

        btnAddConfirm.setOnClickListener(v -> {
            String nombre = etNombreAdd.getText().toString().trim();
            String montoStr = etMontoAdd.getText().toString().trim();
            String fecha = etFechaAdd.getText().toString().trim();
            boolean esIngreso = cbEsIngresoAdd.isChecked();
            boolean esFijo = cbEsFijoAdd.isChecked();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(montoStr)) {
                Toast.makeText(this, "Completá nombre y monto", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(montoStr);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedCatName = actvCategoria.getText().toString().trim();
            String selectedCatId = null;
            if (!selectedCatName.isEmpty()) {
                int idx = categoriasNombres.indexOf(selectedCatName);
                if (idx >= 0) selectedCatId = categoriasIds.get(idx);
            }

            String selectedCuentaName = actvMedioPago.getText().toString().trim();
            String selectedCuentaId = null;
            if (!selectedCuentaName.isEmpty()) {
                int idx2 = cuentasNombres.indexOf(selectedCuentaName);
                if (idx2 >= 0) selectedCuentaId = cuentasIds.get(idx2);
            }

            Integer dia = esFijo ? 1 : null;
            Movimiento m = new Movimiento(
                    nombre,
                    monto,
                    selectedCatId,
                    selectedCuentaId,
                    fecha,
                    esIngreso,
                    esFijo,
                    dia
            );
            m.setCreatedAt(new Date());

            db.collection("usuarios").document(uid).collection("movimientos")
                    .add(m)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Movimiento agregado", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(err -> Toast.makeText(this,
                            "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loadCategorias() {
        db.collection("usuarios").document(uid).collection("categorias")
                .get()
                .addOnSuccessListener(qs -> {
                    categoriasIds.clear();
                    categoriasNombres.clear();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        categoriasIds.add(d.getId());
                        categoriasNombres.add(d.getString("nombre"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, categoriasNombres);
                    actvCategoria.setAdapter(adapter);
                });
    }

    private void loadCuentas() {
        db.collection("usuarios").document(uid).collection("cuentas")
                .get()
                .addOnSuccessListener(qs -> {
                    cuentasIds.clear();
                    cuentasNombres.clear();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        cuentasIds.add(d.getId());
                        cuentasNombres.add(d.getString("nombre"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, cuentasNombres);
                    actvMedioPago.setAdapter(adapter);
                });
    }
}
