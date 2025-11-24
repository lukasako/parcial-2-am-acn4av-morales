package com.example.gastapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AgregarCuentaActivity extends AppCompatActivity {

    private EditText etNombreCuentaAdd, etMontoCuentaAdd;
    private Button btnConfirmAddCuenta;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_mis_cuentas);

        Toolbar toolbar = findViewById(R.id.toolbarAgregarCuenta);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNombreCuentaAdd = findViewById(R.id.etNombreCuentaAdd);
        etMontoCuentaAdd = findViewById(R.id.etMontoCuentaAdd);
        btnConfirmAddCuenta = findViewById(R.id.btnConfirmAddCuenta);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        btnConfirmAddCuenta.setOnClickListener(v -> {
            String nombre = etNombreCuentaAdd.getText().toString().trim();
            String montoS = etMontoCuentaAdd.getText().toString().trim();

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

            Map<String, Object> data = new HashMap<>();
            data.put("nombre", nombre);
            data.put("monto", monto);
            data.put("createdAt", Timestamp.now());

            db.collection("usuarios").document(uid).collection("cuentas")
                    .add(data)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Cuenta agregada", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(err -> Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
