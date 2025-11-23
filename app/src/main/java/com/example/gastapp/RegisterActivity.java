package com.example.gastapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etMail, etPassword;
    private Button btnCrear;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        btnCrear = findViewById(R.id.btnCrearCuenta);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnCrear.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String mail = etMail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingrese un nombre");
            return;
        }

        if (apellido.isEmpty()) {
            etApellido.setError("Ingrese un apellido");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            etMail.setError("Mail inválido");
            return;
        }

        if (pass.length() < 6) {
            etPassword.setError("6 caracteres mínimo");
            return;
        }

        auth.createUserWithEmailAndPassword(mail, pass)
                .addOnSuccessListener(a -> {
                    String uid = auth.getCurrentUser().getUid();

                    HashMap<String, Object> user = new HashMap<>();
                    user.put("nombre", nombre);
                    user.put("apellido", apellido);
                    user.put("email", mail);

                    db.collection("usuarios")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                finish();
                            });

                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
