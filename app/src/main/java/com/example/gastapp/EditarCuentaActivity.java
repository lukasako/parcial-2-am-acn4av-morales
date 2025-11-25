package com.example.gastapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditarCuentaActivity extends AppCompatActivity {

    private EditText etNombreCuentaEdit, etMontoCuentaEdit;
    private Button btnConfirmEditCuenta;
    private FirebaseFirestore db;
    private String uid;
    private String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_mis_cuentas);

        Toolbar toolbar = findViewById(R.id.toolbarEditarCuenta);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNombreCuentaEdit = findViewById(R.id.etNombreCuentaEdit);
        etMontoCuentaEdit = findViewById(R.id.etMontoCuentaEdit);
        btnConfirmEditCuenta = findViewById(R.id.btnConfirmEditCuenta);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        docId = getIntent().getStringExtra("docId");
        if (docId == null) {
            Toast.makeText(this, "ID de cuenta no provisto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar datos existentes
        db.collection("usuarios").document(uid).collection("cuentas")
                .document(docId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc.exists()) {
                        etNombreCuentaEdit.setText(doc.getString("nombre"));
                        Number montoNum = doc.getDouble("monto") != null ? doc.getDouble("monto") : doc.getLong("monto");
                        etMontoCuentaEdit.setText(montoNum != null ? String.valueOf(montoNum) : "0");
                    } else {
                        Toast.makeText(this, "Cuenta no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });

        btnConfirmEditCuenta.setOnClickListener(v -> {
            String nombre = etNombreCuentaEdit.getText().toString().trim();
            String montoS = etMontoCuentaEdit.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(montoS)) {
                Toast.makeText(this, "Completá nombre y monto", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(montoS.replace(",", ""));
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("usuarios").document(uid).collection("cuentas")
                    .document(docId)
                    .update("nombre", nombre, "monto", monto)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cuenta actualizada", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(err -> Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
