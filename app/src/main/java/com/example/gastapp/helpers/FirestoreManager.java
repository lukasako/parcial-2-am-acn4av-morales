package com.example.gastapp.helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreManager {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static DocumentReference getUserDocument() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return db.collection("usuarios").document(uid);
    }

    public static CollectionReference getMovimientos() {
        return getUserDocument().collection("movimientos");
    }

    public static CollectionReference getCategorias() {
        return getUserDocument().collection("categorias");
    }

    public static DocumentReference getResumen() {
        return getUserDocument().collection("resumen").document("data");
    }
}
