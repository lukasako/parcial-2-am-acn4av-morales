package com.example.gastapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CategoriasActivity extends AppCompatActivity {
    private LinearLayout llCategoriasList;
    private static final int REQ_ADD_CAT = 2001;
    private static final int REQ_EDIT_CAT = 2002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        llCategoriasList = findViewById(R.id.llCategoriasList);
        Button btnAgregar = findViewById(R.id.btnAgregarCategoria);
        //btnAgregar.setOnClickListener(v -> startActivityForResult(new Intent(this, AgregarCategoriaActivity.class), REQ_ADD_CAT));

        // ejemplos
        addCategoriaItem("Super");
        addCategoriaItem("Suscripciones");
    }

    private void addCategoriaItem(String nombre) {
        View item = getLayoutInflater().inflate(R.layout.item_categoria, llCategoriasList, false);
        TextView tvName = item.findViewById(R.id.tvCatName);
        tvName.setText(nombre);

        ImageView ivDel = item.findViewById(R.id.ivDeleteCat);
        ImageView ivEdit = item.findViewById(R.id.ivEditCat);

        ivDel.setOnClickListener(v -> llCategoriasList.removeView(item));
        /*/ ivEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AgregarCategoriaActivity.class);
            i.putExtra("name", nombre);
            startActivityForResult(i, REQ_EDIT_CAT);
            item.setTag("editing");
        });/*/

        llCategoriasList.addView(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
}

