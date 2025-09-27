package com.example.gastapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        //cargamos el array de saludos
        String[] greetings = getResources().getStringArray(R.array.greetings);
        //elegir un saludo al azar
        int randomIndex = new Random().nextInt(greetings.length);
        tvGreeting.setText(greetings[randomIndex]);
    }
}
