package com.example.gastapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AgregarCategoriaActivity extends AppCompatActivity {

    private EditText etCategoria;
    private Button btnGuardar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String categoriaId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_categoria);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditarCategoria);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        etCategoria = findViewById(R.id.etCategoriaNombre);
        btnGuardar = findViewById(R.id.btnGuardarCategoriaFinal);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        categoriaId = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");

        if (name != null) {
            etCategoria.setText(name);
            toolbar.setTitle("Editar Categoría");
        } else {
            toolbar.setTitle("Agregar Categoría");
        }

        btnGuardar.setOnClickListener(v -> saveCategoria());
    }

    private void saveCategoria() {
        String nombre = etCategoria.getText().toString().trim();

        if (nombre.isEmpty()) {
            etCategoria.setError("Ingrese un nombre");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Datos a guardar
        HashMap<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);

        if (categoriaId == null) {
            map.put("fechaCreacion", com.google.firebase.Timestamp.now());

            db.collection("usuarios")
                    .document(uid)
                    .collection("categorias")
                    .add(map)
                    .addOnSuccessListener(doc -> sendBack(doc.getId(), nombre))
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
        } else {
            // editar
            db.collection("usuarios")
                    .document(uid)
                    .collection("categorias")
                    .document(categoriaId)
                    .update("nombre", nombre)
                    .addOnSuccessListener(a -> sendBack(categoriaId, nombre))
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al editar", Toast.LENGTH_SHORT).show());
        }
    }

    private void sendBack(String id, String name) {
        Intent data = new Intent();
        data.putExtra("id", id);
        data.putExtra("name", name);
        setResult(RESULT_OK, data);
        finish();
    }
}
