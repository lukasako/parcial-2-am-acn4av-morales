package com.example.gastapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.Random;
import java.text.NumberFormat;
import java.util.Locale;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

public class MainActivity extends AppCompatActivity {
    private boolean showingSaldo = true;
    private LinearLayout layoutSaldo, layoutGrafico, llRecentList;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //saludos dinamicos
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        String[] greetings = getResources().getStringArray(R.array.greetings);
        int randomIndex = new Random().nextInt(greetings.length);
        tvGreeting.setText(greetings[randomIndex]);

        //imagen billetera dinamica
        TextView tvSaldoAmount = findViewById(R.id.tvSaldoAmount);
        String saldoStr = getString(R.string.saldo_inicial);
        double saldo = Double.parseDouble(saldoStr);
        //formateo de numero a moneda
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        tvSaldoAmount.setText(currencyFormat.format(saldo));
        ImageView imgLogo = findViewById(R.id.imgLogo);
        imgLogo.setImageResource(saldo >= 100000 ? R.drawable.ic_wallet : R.drawable.ic_wallet_empty);

        //funcion de saldo y grafico
        CardView cardSaldo = findViewById(R.id.cardSaldo);
        layoutSaldo = findViewById(R.id.layoutSaldo);
        layoutGrafico = findViewById(R.id.layoutGrafico);
        pieChart = findViewById(R.id.pieChart);
        setupPieChart();

        cardSaldo.setOnClickListener(v -> toggleCardView());

        //lista dinamica
        llRecentList = findViewById(R.id.llRecentList);
        Button btnIngresar = findViewById(R.id.btnIngresar);
        btnIngresar.setOnClickListener(v -> {
            //ejemplo, gasto en cafe y sueldo. En la app real se agrega desde pantalla ingresar movimiento
            addExpenseItem("Sueldo", 300000.0, "Ingreso mensual", "05/09/25", "Efectivo", true);
            addExpenseItem("Café", 250.0, "Gasto diario", "29/09/25", "Mercado Pago", false);
        });
    }

    private void toggleCardView() {
        if (showingSaldo) {
            layoutSaldo.setVisibility(View.GONE);
            layoutGrafico.setVisibility(View.VISIBLE);
        } else {
            layoutSaldo.setVisibility(View.VISIBLE);
            layoutGrafico.setVisibility(View.GONE);
        }
        showingSaldo = !showingSaldo;
    }

    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(70f, "Ingresos"));
        entries.add(new PieEntry(30f, "Egresos"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(android.graphics.Color.GREEN, android.graphics.Color.RED);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
    }

    @SuppressLint("SetTextI18n")
    private void addExpenseItem(String titulo, double monto, String categoria, String fecha, String medioPago, boolean esIngreso) {
        View itemView = getLayoutInflater().inflate(R.layout.item_movimiento, llRecentList, false);

        TextView tvTitulo = itemView.findViewById(R.id.tvTitulo);
        TextView tvMonto = itemView.findViewById(R.id.tvMonto);
        TextView tvCategoria = itemView.findViewById(R.id.tvCategoria);
        TextView tvFecha = itemView.findViewById(R.id.tvFecha);
        TextView tvMedioPago = itemView.findViewById(R.id.tvMedioPago);

        tvTitulo.setText(titulo);
        tvMonto.setText(NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(monto));
        tvMonto.setTextColor(getResources().getColor(esIngreso ? R.color.accent_green : R.color.danger));
        tvCategoria.setText(categoria);
        tvFecha.setText(fecha);
        tvMedioPago.setText(medioPago);

        llRecentList.addView(itemView, 0);

        if (llRecentList.getChildCount() > 3) {
            llRecentList.removeViewAt(llRecentList.getChildCount() - 1);
            findViewById(R.id.btnVerTodos).setVisibility(View.VISIBLE);
        }
    }
}
