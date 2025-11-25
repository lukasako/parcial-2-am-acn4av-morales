package com.example.gastapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gastapp.models.Movimiento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EditarMovimientoActivity extends AppCompatActivity {

    private EditText etCategoria, etNombre, etMedioPago, etMonto, etFecha;
    private Button btnConfirm;
    private FirebaseFirestore db;
    private String docId;
    private String uid;

    private AutoCompleteTextView actvCategoriaEdit, actvMedioPagoEdit;
    private List<String> categoriasIds = new ArrayList<>();
    private List<String> categoriasNombres = new ArrayList<>();
    private List<String> cuentasIds = new ArrayList<>();
    private List<String> cuentasNombres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_movimiento);

        Toolbar toolbar = findViewById(R.id.toolbarEditarMov);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        etCategoria = findViewById(R.id.etCategoria);
        etNombre = findViewById(R.id.etNombre);
        etMedioPago = findViewById(R.id.etMedioPago);
        etMonto = findViewById(R.id.etMonto);
        etFecha = findViewById(R.id.etFecha);
        btnConfirm = findViewById(R.id.btnConfirm);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        docId = getIntent().getStringExtra("docId");

        if (docId != null) {
            db.collection("usuarios").document(uid).collection("movimientos")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Movimiento m = doc.toObject(Movimiento.class);
                            if (m != null) {
                                etNombre.setText(m.getTitulo());
                                etMonto.setText(String.valueOf(m.getMonto()));
                                etFecha.setText(m.getFecha());
                                // si tenés categoriaId y medioPagoId, tratamos de obtener sus nombres
                                if (m.getCategoriaId() != null) {
                                    db.collection("usuarios").document(uid).collection("categorias")
                                            .document(m.getCategoriaId())
                                            .get().addOnSuccessListener(cDoc -> {
                                                if (cDoc.exists()) etCategoria.setText(cDoc.getString("nombre"));
                                            });
                                }
                                if (m.getMedioPagoId() != null) {
                                    db.collection("usuarios").document(uid).collection("cuentas")
                                            .document(m.getMedioPagoId())
                                            .get().addOnSuccessListener(cDoc -> {
                                                if (cDoc.exists()) etMedioPago.setText(cDoc.getString("nombre"));
                                            });
                                }
                            }
                        }
                    });
        }

        btnConfirm.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String montoS = etMonto.getText().toString().trim();
            String fecha = etFecha.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(montoS)) {
                Toast.makeText(this, "Completá nombre y monto", Toast.LENGTH_SHORT).show();
                return;
            }
            double monto;
            try {
                monto = Double.parseDouble(montoS);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            // actualizar documento (solo campos editables)
            db.collection("usuarios").document(uid).collection("movimientos")
                    .document(docId)
                    .update(
                            "titulo", nombre,
                            "monto", monto,
                            "fecha", fecha
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(err -> Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
