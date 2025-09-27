package com.example.gastapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //saludos dinamicos
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        //cargamos el array de saludos
        String[] greetings = getResources().getStringArray(R.array.greetings);
        //elegir un saludo al azar
        int randomIndex = new Random().nextInt(greetings.length);
        tvGreeting.setText(greetings[randomIndex]);

        //imagen billetera dinamica
        TextView tvSaldoAmount = findViewById(R.id.tvSaldoAmount);
        String saldoStr = getString(R.string.saldo_inicial);
        double saldo = Double.parseDouble(saldoStr);
        //formateo de numero a moneda
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        String saldoFormateado = currencyFormat.format(saldo);
        tvSaldoAmount.setText(saldoFormateado);
        //imagen de billetera dinamica
        ImageView imgLogo = findViewById(R.id.imgLogo);
        if (saldo >= 100000){
            imgLogo.setImageResource(R.drawable.ic_wallet);
        } else{
            imgLogo.setImageResource(R.drawable.ic_wallet_empty);
        }
    }
}
