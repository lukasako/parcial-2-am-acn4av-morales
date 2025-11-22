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

        //ajuste pendiente: habilitar botón para agregar categoría
        btnAgregar.setOnClickListener(v ->
                startActivityForResult(new Intent(this, AgregarCategoriaActivity.class), REQ_ADD_CAT)
        );

        // Ejemplos iniciales
        addCategoriaItem("Super");
        addCategoriaItem("Suscripciones");
    }

    private void addCategoriaItem(String nombre) {
        View item = getLayoutInflater().inflate(R.layout.item_categoria, llCategoriasList, false);

        TextView tvName = item.findViewById(R.id.tvCatName);
        tvName.setText(nombre);

        ImageView ivDel = item.findViewById(R.id.ivDeleteCat);
        ImageView ivEdit = item.findViewById(R.id.ivEditCat);

        // Eliminar
        ivDel.setOnClickListener(v -> llCategoriasList.removeView(item));

        // Editar
        ivEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AgregarCategoriaActivity.class);
            i.putExtra("name", nombre);
            item.setTag("editing"); // marcamos este item
            startActivityForResult(i, REQ_EDIT_CAT);
        });

        llCategoriasList.addView(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        String newName = data.getStringExtra("name");

        if (requestCode == REQ_ADD_CAT) {
            //agregar categoría nueva
            if (newName != null && !newName.isEmpty()) {
                addCategoriaItem(newName);
            }
        }

        if (requestCode == REQ_EDIT_CAT) {
            //Editar categoría existente
            for (int i = 0; i < llCategoriasList.getChildCount(); i++) {
                View item = llCategoriasList.getChildAt(i);

                if ("editing".equals(item.getTag())) {
                    TextView tv = item.findViewById(R.id.tvCatName);
                    tv.setText(newName);
                    item.setTag(null);
                    break;
                }
            }
        }
    }
}
