package com.example.gastapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
    private AutoCompleteTextView categoriaPicker, medioPagoPicker, tipoPicker, esFijoPicker;
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
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        categoriaPicker = findViewById(R.id.categoriaPicker);
        medioPagoPicker = findViewById(R.id.medioPagoPicker);
        tipoPicker = findViewById(R.id.tipoPicker);
        esFijoPicker = findViewById(R.id.esFijoPicker);
        etNombreAdd = findViewById(R.id.etNombre);
        etMontoAdd = findViewById(R.id.etMonto);
        etFechaAdd = findViewById(R.id.etFechaAdd);
        btnAddConfirm = findViewById(R.id.btnAgregar);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Calendar c = Calendar.getInstance();
        String fechaHoy = String.format(Locale.getDefault(), "%02d/%02d/%02d",
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR) % 100);
        etFechaAdd.setText(fechaHoy);

        etFechaAdd.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String f = String.format(Locale.getDefault(), "%02d/%02d/%02d",
                        dayOfMonth, month + 1, year % 100);
                etFechaAdd.setText(f);
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        setupStaticPickers();
        loadCategorias();
        loadCuentas();
        setupClickDropdowns();

        btnAddConfirm.setOnClickListener(v -> guardarMovimiento());
    }

    private void setupStaticPickers() {
        tipoPicker.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Ingreso", "Egreso"}));

        esFijoPicker.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Sí", "No"}));
    }

    private void setupClickDropdowns() {
        categoriaPicker.setOnClickListener(v -> categoriaPicker.showDropDown());
        medioPagoPicker.setOnClickListener(v -> medioPagoPicker.showDropDown());
        tipoPicker.setOnClickListener(v -> tipoPicker.showDropDown());
        esFijoPicker.setOnClickListener(v -> esFijoPicker.showDropDown());
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
                    categoriaPicker.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, categoriasNombres));
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
                    medioPagoPicker.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, cuentasNombres));
                });
    }

    private void guardarMovimiento() {
        String nombre = etNombreAdd.getText().toString().trim();
        String montoStr = etMontoAdd.getText().toString().trim();
        String fecha = etFechaAdd.getText().toString().trim();

        if (nombre.isEmpty() || montoStr.isEmpty()) {
            Toast.makeText(this, "Completá nombre y monto", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try { monto = Double.parseDouble(montoStr); }
        catch (Exception e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean esIngreso = tipoPicker.getText().toString().equals("Ingreso");
        boolean esFijo = esFijoPicker.getText().toString().equals("Sí");

        String catName = categoriaPicker.getText().toString().trim();
        String categoriaId = categoriasNombres.contains(catName)
                ? categoriasIds.get(categoriasNombres.indexOf(catName)) : null;

        String cuentaName = medioPagoPicker.getText().toString().trim();
        String cuentaId = cuentasNombres.contains(cuentaName)
                ? cuentasIds.get(cuentasNombres.indexOf(cuentaName)) : null;

        Movimiento m = new Movimiento(
                nombre, monto, categoriaId, cuentaId, fecha,
                esIngreso, esFijo,
                esFijo ? 1 : null
        );
        m.setCreatedAt(new Date());

        db.collection("usuarios").document(uid).collection("movimientos")
                .add(m)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Movimiento agregado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
