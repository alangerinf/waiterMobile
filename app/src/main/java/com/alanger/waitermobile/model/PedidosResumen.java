package com.alanger.waitermobile.model;

import java.io.Serializable;

public class PedidosResumen implements Serializable {


    private int    unidades;
    private String nombre;
    private float  importeTotal;

    public PedidosResumen(){
        this.unidades=0;
        this.nombre="";
        this.importeTotal=0;
    }

    public PedidosResumen(int unidades,String nombre,float importeTotal){
        this.unidades=unidades;
        this.nombre=nombre;
        this.importeTotal=importeTotal;
    }


    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(float importeTotal) {
        this.importeTotal = importeTotal;
    }
}
