package com.example.gastapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean showingSaldo = true;
    private LinearLayout layoutSaldo;
    private LinearLayout layoutGrafico;
    private LinearLayout llRecentList;
    private PieChart pieChart;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        uid = currentUser.getUid();

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvSaldoAmount = findViewById(R.id.tvSaldoAmount);
        ImageView imgLogo = findViewById(R.id.imgLogo);
        CardView cardSaldo = findViewById(R.id.cardSaldo);
        layoutSaldo = findViewById(R.id.layoutSaldo);
        layoutGrafico = findViewById(R.id.layoutGrafico);
        pieChart = findViewById(R.id.pieChart);
        llRecentList = findViewById(R.id.llRecentList);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton navSummary = findViewById(R.id.navSummary);

        setupGreeting(tvGreeting);
        cargarSaldo(tvSaldoAmount, imgLogo);
        cardSaldo.setOnClickListener(v -> toggleCardView());
        btnLogout.setOnClickListener(v -> logout());
        navSummary.setOnClickListener(v -> startActivity(new Intent(this, MisCuentasActivity.class)));

        findViewById(R.id.btnIngresar).setOnClickListener(v ->
                startActivity(new Intent(this, AgregarMovimientoActivity.class)));

        findViewById(R.id.btnMovimientos).setOnClickListener(v ->
                startActivity(new Intent(this, MisMovimientosActivity.class)));

        findViewById(R.id.btnCategorias).setOnClickListener(v ->
                startActivity(new Intent(this, CategoriasActivity.class)));

        findViewById(R.id.btnMovFijos).setOnClickListener(v ->
                startActivity(new Intent(this, MovimientosFijosActivity.class)));

        cargarUltimosMovimientos();
        cargarEstadisticaMensual();
    }

    private void setupGreeting(TextView tvGreeting) {
        String[] greetings = getResources().getStringArray(R.array.greetings);
        int randIndex = (int) (Math.random() * greetings.length);
        String saludoRandom = greetings[randIndex];

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String nombre = doc.getString("nombre");
                    if (nombre != null && !nombre.isEmpty()) {
                        tvGreeting.setText(nombre + saludoRandom);
                    } else {
                        tvGreeting.setText("Usuario" + saludoRandom);
                    }
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void toggleCardView() {
        layoutSaldo.setVisibility(showingSaldo ? View.GONE : View.VISIBLE);
        layoutGrafico.setVisibility(showingSaldo ? View.VISIBLE : View.GONE);
        showingSaldo = !showingSaldo;
    }

    private void cargarSaldo(TextView saldoText, ImageView icon) {
        db.collection("usuarios")
                .document(uid)
                .collection("cuentas")
                .get()
                .addOnSuccessListener(qsCuentas -> {
                    double baseSaldo = 0;
                    for (DocumentSnapshot d : qsCuentas) {
                        Double valorInicial = d.getDouble("valorInicial");
                        if (valorInicial != null) {
                            baseSaldo += valorInicial;
                        }
                    }

                    double finalBaseSaldo = baseSaldo;
                    db.collection("usuarios")
                            .document(uid)
                            .collection("movimientos")
                            .get()
                            .addOnSuccessListener(qsMovs -> {
                                double totalIngresos = 0;
                                double totalEgresos = 0;

                                for (DocumentSnapshot doc : qsMovs) {
                                    boolean ingreso = Boolean.TRUE.equals(doc.getBoolean("esIngreso"));
                                    double monto = doc.getDouble("monto") != null ? doc.getDouble("monto") : 0;

                                    if (ingreso) {
                                        totalIngresos += monto;
                                    } else {
                                        totalEgresos += monto;
                                    }
                                }

                                double total = finalBaseSaldo + totalIngresos - totalEgresos;

                                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
                                saldoText.setText(nf.format(total));

                                icon.setImageResource(total >= 100000
                                        ? R.drawable.ic_wallet
                                        : R.drawable.ic_wallet_empty);
                            });
                });
    }

    private void cargarUltimosMovimientos() {
        db.collection("usuarios")
                .document(uid)
                .collection("movimientos")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(qs -> {
                    llRecentList.removeAllViews();
                    for (DocumentSnapshot d : qs) {
                        addExpenseItem(
                                d.getString("titulo"),
                                d.getDouble("monto"),
                                d.getString("categoriaId"),
                                d.getString("fecha"),
                                d.getString("medioPagoId"),
                                d.getBoolean("esIngreso")
                        );
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void addExpenseItem(String titulo, double monto, String categoriaId, String fecha,
                                String medioPagoId, Boolean esIngreso) {

        View itemView = getLayoutInflater().inflate(R.layout.item_movimiento, llRecentList, false);

        TextView tvTitulo = itemView.findViewById(R.id.tvTitulo);
        TextView tvMonto = itemView.findViewById(R.id.tvMonto);
        TextView tvCategoria = itemView.findViewById(R.id.tvCategoria);
        TextView tvFecha = itemView.findViewById(R.id.tvFecha);
        TextView tvMedioPago = itemView.findViewById(R.id.tvMedioPago);

        tvTitulo.setText(titulo);
        tvFecha.setText(fecha);

        NumberFormat f = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        tvMonto.setText(f.format(monto));
        tvMonto.setTextColor(getResources().getColor(Boolean.TRUE.equals(esIngreso)
                ? R.color.accent_green
                : R.color.danger));

        db.collection("usuarios").document(uid)
                .collection("categorias")
                .document(categoriaId)
                .get()
                .addOnSuccessListener(cat -> tvCategoria.setText(cat.getString("nombre")));

        db.collection("usuarios").document(uid)
                .collection("cuentas")
                .document(medioPagoId)
                .get()
                .addOnSuccessListener(mp -> tvMedioPago.setText(mp.getString("nombre")));

        llRecentList.addView(itemView);
    }

    private void cargarEstadisticaMensual() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Timestamp inicioMes = new Timestamp(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Timestamp finMes = new Timestamp(calendar.getTime());

        db.collection("usuarios")
                .document(uid)
                .collection("movimientos")
                .whereGreaterThanOrEqualTo("createdAt", inicioMes)
                .whereLessThanOrEqualTo("createdAt", finMes)
                .get()
                .addOnSuccessListener(qs -> {
                    double totalIngresos = 0;
                    double totalEgresos = 0;

                    for (DocumentSnapshot d : qs) {
                        boolean esIngreso = Boolean.TRUE.equals(d.getBoolean("esIngreso"));
                        double monto = d.getDouble("monto") != null ? d.getDouble("monto") : 0;

                        if (esIngreso) {
                            totalIngresos += monto;
                        } else {
                            totalEgresos += monto;
                        }
                    }

                    NumberFormat f = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

                    TextView tvMonthUp = findViewById(R.id.tvMonthUp);
                    TextView tvMonthDown = findViewById(R.id.tvMonthDown);
                    TextView tvActualMonth = findViewById(R.id.tvActualMonth);

                    tvMonthUp.setText(f.format(totalIngresos));
                    tvMonthDown.setText(f.format(totalEgresos));

                    String nombreMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "AR"))
                            .format(Calendar.getInstance().getTime());
                    tvActualMonth.setText(
                            nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1)
                    );

                    actualizarPieChart(totalIngresos, totalEgresos);
                });
    }

    private void actualizarPieChart(double ingresos, double egresos) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (ingresos > 0) {
            entries.add(new PieEntry((float) ingresos, "Ingresos"));
        }
        if (egresos > 0) {
            entries.add(new PieEntry((float) egresos, "Egresos"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                android.graphics.Color.rgb(0, 200, 100),
                android.graphics.Color.rgb(220, 50, 50)
        );
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setUsePercentValues(false);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
    }
}
