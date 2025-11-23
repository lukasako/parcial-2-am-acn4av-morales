package com.example.gastapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etMail, etPass;
    private Button btnIngresar, btnIrCrearCuenta;

    private FirebaseAuth auth;

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etMail = findViewById(R.id.etLoginMail);
        etPass = findViewById(R.id.etLoginPass);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnIrCrearCuenta = findViewById(R.id.btnIrCrearCuenta);

        auth = FirebaseAuth.getInstance();

        btnIngresar.setOnClickListener(v -> loginUser());
        btnIrCrearCuenta.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String mail = etMail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(mail)) {
            etMail.setError("Ingrese un mail válido");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            etMail.setError("Mail inválido");
            return;
        }

        if (pass.length() < 6) {
            etPass.setError("La contraseña debe tener mínimo 6 caracteres");
            return;
        }

        auth.signInWithEmailAndPassword(mail, pass)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Ingreso exitoso", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }


}
