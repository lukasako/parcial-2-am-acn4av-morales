package com.example.gastapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gastapp.models.Movimiento;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MovimientosFijosActivity extends AppCompatActivity {

    private LinearLayout llFijosList;
    private FirebaseFirestore db;
    private Map<String, String> categoriasCache = new HashMap<>();
    private Map<String, String> cuentasCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos_fijos);

        Toolbar toolbar = findViewById(R.id.toolbarFijos);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        llFijosList = findViewById(R.id.llFijosList);
        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        preloadData(uid);

        db.collection("usuarios")
                .document(uid)
                .collection("movimientos")
                .whereEqualTo("esFijo", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    llFijosList.removeAllViews();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Movimiento m = d.toObject(Movimiento.class);
                        if (m == null) continue;
                        m.setId(d.getId());
                        addFijoItem(m, uid);
                    }
                });

        findViewById(R.id.btnAddFijo).setOnClickListener(v ->
                startActivity(new Intent(this, AgregarMovimientoActivity.class))
        );
    }

    private void preloadData(String uid) {
        db.collection("usuarios").document(uid).collection("categorias")
                .get().addOnSuccessListener(q -> {
                    for (DocumentSnapshot d : q) {
                        categoriasCache.put(d.getId(), d.getString("nombre"));
                    }
                });

        db.collection("usuarios").document(uid).collection("cuentas")
                .get().addOnSuccessListener(q -> {
                    for (DocumentSnapshot d : q) {
                        cuentasCache.put(d.getId(), d.getString("nombre"));
                    }
                });
    }

    private void addFijoItem(Movimiento m, String uid) {
        View item = getLayoutInflater().inflate(R.layout.item_movimiento, llFijosList, false);

        TextView tvTitulo = item.findViewById(R.id.tvTitulo);
        TextView tvMonto = item.findViewById(R.id.tvMonto);
        TextView tvCategoria = item.findViewById(R.id.tvCategoria);
        TextView tvFecha = item.findViewById(R.id.tvFecha);
        TextView tvMedioPago = item.findViewById(R.id.tvMedioPago);
        ImageView ivDelete = item.findViewById(R.id.ivDelete);
        ImageView ivEdit = item.findViewById(R.id.ivEdit);

        tvTitulo.setText(m.getTitulo());
        tvFecha.setText("Día " + m.getDiaMovimientoFijo() + " - Mensual");

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es","AR"));
        String sign = m.isEsIngreso() ? "+ " : "- ";
        tvMonto.setText(sign + nf.format(Math.abs(m.getMonto())));
        tvMonto.setTextColor(getResources().getColor(m.isEsIngreso() ? R.color.accent_green : R.color.danger));

        tvCategoria.setText(categoriasCache.getOrDefault(m.getCategoriaId(), ""));
        tvMedioPago.setText(cuentasCache.getOrDefault(m.getMedioPagoId(), ""));

        ivDelete.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Eliminar")
                        .setMessage("¿Eliminar movimiento \"" + m.getTitulo() + "\"?")
                        .setPositiveButton("Eliminar", (dialog, w) -> {
                            db.collection("usuarios").document(uid)
                                    .collection("movimientos")
                                    .document(m.getId())
                                    .delete();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show()
        );

        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarMovimientoActivity.class);
            intent.putExtra("docId", m.getId());
            startActivity(intent);
        });

        llFijosList.addView(item);
    }
}
