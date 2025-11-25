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

import com.example.gastapp.models.Movimiento;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MisMovimientosActivity extends AppCompatActivity {

    public static final int REQ_ADD = 1001;
    public static final int REQ_EDIT = 2001;

    private LinearLayout llMovimientosList;
    private FirebaseFirestore db;
    private CollectionReference movRef;
    private LinearLayout progressContainer;
    private TextView tvSaldoAmount;

    // cache local para nombres de categorias y cuentas (id->nombre)
    private Map<String, String> categoriasCache = new HashMap<>();
    private Map<String, String> cuentasCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_movimientos);

        Toolbar toolbar = findViewById(R.id.toolbarMovimientos);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        llMovimientosList = findViewById(R.id.llMovimientosList);

        // si querés mostrar saldo en esta pantalla -> crea un TextView con id tvSaldoAmount en el layout.
        // Si no existe, lo ignoramos.
        tvSaldoAmount = findViewById(R.id.tvSaldoAmount); // opcional, si lo agregaste al layout

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        movRef = db.collection("usuarios").document(uid).collection("movimientos");

        // precargar categorias y cuentas en cache (user-specific)
        preloadCategoriasYCuentas(uid);

        // Escuchar movimientos ordenados por createdAt DESC
        movRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(MisMovimientosActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null) return;

                    llMovimientosList.removeAllViews();
                    double total = 0.0;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Movimiento m = doc.toObject(Movimiento.class);
                        if (m == null) continue;
                        m.setId(doc.getId());
                        // mostrar item resolviendo nombre categoria y medioPago
                        addMovementView(m);
                        total += m.getMonto();
                    }

                    // actualizar saldo si existe tvSaldoAmount
                    if (tvSaldoAmount != null) {
                        tvSaldoAmount.setText(NumberFormat.getCurrencyInstance(new Locale("es","AR")).format(total));
                    }
                });

        findViewById(R.id.bottomFab).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AgregarMovimientoActivity.class), REQ_ADD);
        });
    }

    private void preloadCategoriasYCuentas(String uid) {
        // categorias
        db.collection("usuarios").document(uid).collection("categorias")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                        String id = d.getId();
                        String nombre = d.getString("nombre");
                        if (id != null && nombre != null) categoriasCache.put(id, nombre);
                    }
                });

        // cuentas/medios de pago
        db.collection("usuarios").document(uid).collection("cuentas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                        String id = d.getId();
                        String nombre = d.getString("nombre");
                        if (id != null && nombre != null) cuentasCache.put(id, nombre);
                    }
                });
    }

    private void addMovementView(Movimiento m) {
        View item = getLayoutInflater().inflate(R.layout.item_movimiento, llMovimientosList, false);

        TextView tvTitulo = item.findViewById(R.id.tvTitulo);
        TextView tvMonto = item.findViewById(R.id.tvMonto);
        TextView tvCategoria = item.findViewById(R.id.tvCategoria);
        TextView tvFecha = item.findViewById(R.id.tvFecha);
        TextView tvMedioPago = item.findViewById(R.id.tvMedioPago);

        ImageView ivDelete = item.findViewById(R.id.ivDelete);
        ImageView ivEdit = item.findViewById(R.id.ivEdit);

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        tvTitulo.setText(m.getTitulo());
        String sign = m.isEsIngreso() ? "+ " : "- ";
        tvMonto.setText(sign + nf.format(Math.abs(m.getMonto())));
        tvMonto.setTextColor(getResources().getColor(m.isEsIngreso() ? R.color.accent_green : R.color.danger));

        // resolver categoriaId -> nombre (cache o consulta on-demand)
        String catName = (m.getCategoriaId() != null) ? categoriasCache.get(m.getCategoriaId()) : null;
        if (catName != null) {
            tvCategoria.setText(catName);
        } else if (m.getCategoriaId() != null) {
            // consulta on-demand y setea texto cuando llegue
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("usuarios").document(uid).collection("categorias")
                    .document(m.getCategoriaId())
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nombre = doc.getString("nombre");
                            if (nombre != null) {
                                categoriasCache.put(doc.getId(), nombre);
                                tvCategoria.setText(nombre);
                            }
                        }
                    });
            tvCategoria.setText("Categoría");
        } else {
            tvCategoria.setText("");
        }

        // medio de pago
        String cuentaName = (m.getMedioPagoId() != null) ? cuentasCache.get(m.getMedioPagoId()) : null;
        if (cuentaName != null) {
            tvMedioPago.setText(cuentaName);
        } else if (m.getMedioPagoId() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("usuarios").document(uid).collection("cuentas")
                    .document(m.getMedioPagoId())
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nombre = doc.getString("nombre");
                            if (nombre != null) {
                                cuentasCache.put(doc.getId(), nombre);
                                tvMedioPago.setText(nombre);
                            }
                        }
                    });
            tvMedioPago.setText("");
        } else {
            tvMedioPago.setText("");
        }

        tvFecha.setText(m.getFecha() != null ? m.getFecha() : "");

        // borrar
        ivDelete.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Eliminar")
                        .setMessage("¿Eliminar movimiento \"" + m.getTitulo() + "\"?")
                        .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Eliminar", (dialog, which) ->
                                db.collection("usuarios")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("movimientos")
                                        .document(m.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(err -> Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show())
                        ).show()
        );

        // editar
        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarMovimientoActivity.class);
            intent.putExtra("docId", m.getId());
            startActivityForResult(intent, REQ_EDIT);
        });

        llMovimientosList.addView(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // usamos listener en tiempo real, no hace falta forzar refresh
    }
}
