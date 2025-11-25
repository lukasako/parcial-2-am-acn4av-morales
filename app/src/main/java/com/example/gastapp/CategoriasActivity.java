package com.example.gastapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CategoriasActivity extends AppCompatActivity {

    private LinearLayout llCategoriasList;
    private static final int REQ_ADD_CAT = 2001;
    private static final int REQ_EDIT_CAT = 2002;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        llCategoriasList = findViewById(R.id.llCategoriasList);

        Button btnAgregar = findViewById(R.id.btnAgregarCategoria);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // carga inicial desde Firestore
        loadCategorias();

        btnAgregar.setOnClickListener(v -> {
            Intent i = new Intent(this, AgregarCategoriaActivity.class);
            startActivityForResult(i, REQ_ADD_CAT);
        });
    }

    private void loadCategorias() {
        llCategoriasList.removeAllViews();

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(uid)
                .collection("categorias")
                .orderBy("fechaCreacion")
                .get()
                .addOnSuccessListener(snaps -> {
                    for (QueryDocumentSnapshot snap : snaps) {
                        String id = snap.getId();
                        String nombre = snap.getString("nombre");
                        addCategoriaItem(id, nombre);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando categorías", Toast.LENGTH_SHORT).show()
                );
    }

    private void addCategoriaItem(String id, String nombre) {
        View item = getLayoutInflater().inflate(R.layout.item_categoria, llCategoriasList, false);

        TextView tvName = item.findViewById(R.id.tvCatName);
        tvName.setText(nombre);

        ImageView ivDel = item.findViewById(R.id.ivDeleteCat);
        ImageView ivEdit = item.findViewById(R.id.ivEditCat);

        // ELIMINAR
        ivDel.setOnClickListener(v -> deleteCategoria(id, item));

        // EDITAR
        ivEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AgregarCategoriaActivity.class);
            i.putExtra("id", id);
            i.putExtra("name", nombre);
            startActivityForResult(i, REQ_EDIT_CAT);
        });

        llCategoriasList.addView(item);
    }

    private void deleteCategoria(String id, View item) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios")
                .document(uid)
                .collection("categorias")
                .document(id)
                .delete()
                .addOnSuccessListener(a -> {
                    llCategoriasList.removeView(item);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error eliminando", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        String name = data.getStringExtra("name");
        String id = data.getStringExtra("id");

        if (requestCode == REQ_ADD_CAT) {
            loadCategorias();
        }

        if (requestCode == REQ_EDIT_CAT) {
            loadCategorias();
        }
    }
}
