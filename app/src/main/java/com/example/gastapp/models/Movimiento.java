package com.example.gastapp.models;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

public class Movimiento implements Serializable {

    private String id;
    private String titulo;
    private double monto;
    private String categoriaId;
    private String medioPagoId;
    private String fecha;
    private boolean esIngreso;
    private boolean esFijo;

    @Nullable
    private Integer diaMovimientoFijo; // puede ser null

    @ServerTimestamp
    private Date createdAt;

    public Movimiento() {} // necesario Firestore

    public Movimiento(String titulo, double monto, String categoriaId, String medioPagoId,
                      String fecha, boolean esIngreso, boolean esFijo,
                      @Nullable Integer diaMovimientoFijo) {

        this.titulo = titulo;
        this.monto = monto;
        this.categoriaId = categoriaId;
        this.medioPagoId = medioPagoId;
        this.fecha = fecha;
        this.esIngreso = esIngreso;
        this.esFijo = esFijo;

        this.diaMovimientoFijo = esFijo ? diaMovimientoFijo : null;
    }

    // getters & setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }
    public String getMedioPagoId() { return medioPagoId; }
    public void setMedioPagoId(String medioPagoId) { this.medioPagoId = medioPagoId; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public boolean isEsIngreso() { return esIngreso; }
    public void setEsIngreso(boolean esIngreso) { this.esIngreso = esIngreso; }
    public boolean isEsFijo() { return esFijo; }
    public void setEsFijo(boolean esFijo) { this.esFijo = esFijo; }
    public Integer getDiaMovimientoFijo() { return diaMovimientoFijo; }
    public void setDiaMovimientoFijo(Integer i) { this.diaMovimientoFijo = i; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
