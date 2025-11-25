package com.example.gastapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.util.Locale;

public class MisCuentasActivity extends AppCompatActivity {

    public static final int REQ_ADD = 3001;
    public static final int REQ_EDIT = 3002;

    private LinearLayout llListaCuentas;
    private FirebaseFirestore db;
    private CollectionReference cuentasRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_cuentas);

        Toolbar toolbar = findViewById(R.id.toolbarMisCuentas);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        llListaCuentas = findViewById(R.id.llListaCuentas);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // No user: no cargamos
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cuentasRef = db.collection("usuarios").document(uid).collection("cuentas");

        // Listener en tiempo real - ordenado por createdAt descendente
        cuentasRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable com.google.firebase.firestore.QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(MisCuentasActivity.this, "Error al cargar cuentas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (snapshots == null) return;

                        llListaCuentas.removeAllViews();
                        double total = 0.0;

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            if (!doc.exists()) continue;
                            String id = doc.getId();
                            String nombre = doc.getString("nombre");
                            Number montoNum = doc.getDouble("monto") != null ? doc.getDouble("monto") : doc.getLong("monto");
                            double monto = montoNum != null ? montoNum.doubleValue() : 0.0;

                            total += monto;
                            addCuentaItem(id, nombre, monto);
                        }

                        // opcional: mostrar total (si agregaste tvSaldoAmount en layout)
                        TextView tvSaldoAmount = findViewById(R.id.tvSaldoAmount);
                        if (tvSaldoAmount != null) {
                            tvSaldoAmount.setText(NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(total));
                        }
                    }
                });

        findViewById(R.id.btnAgregarCuenta).setOnClickListener(v -> {
            startActivityForResult(new Intent(MisCuentasActivity.this, AgregarCuentaActivity.class), REQ_ADD);
        });
    }

    private void addCuentaItem(String docId, String nombre, double monto) {
        View item = getLayoutInflater().inflate(R.layout.item_mis_cuentas, llListaCuentas, false);

        TextView tvNombre = item.findViewById(R.id.tvNombreCuenta);
        TextView tvMonto = item.findViewById(R.id.tvMontoCuenta);
        ImageView ivDelete = item.findViewById(R.id.ivDeleteCuenta);
        ImageView ivEdit = item.findViewById(R.id.ivEditCuenta);

        tvNombre.setText(nombre != null ? nombre : "Sin nombre");
        tvMonto.setText(NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(monto));

        // borrar con confirmación
        ivDelete.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Eliminar cuenta")
                        .setMessage("¿Eliminar la cuenta '" + (nombre != null ? nombre : "") + "'?")
                        .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Eliminar", (dialog, which) ->
                                cuentasRef.document(docId).delete()
                                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(err -> Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show())
                        ).show()
        );

        // editar -> abrir activity de edición pasando docId
        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarCuentaActivity.class);
            intent.putExtra("docId", docId);
            startActivityForResult(intent, REQ_EDIT);
        });

        llListaCuentas.addView(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // No necesitamos forzar refresh — snapshot listener actualiza la lista automáticamente
    }
}
