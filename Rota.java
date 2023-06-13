package com.example.appt3;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;


public class Rota {
    private LatLng origem;
    private LatLng destino;
    private static double distanciaInicial;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Rota(LatLng origem, LatLng destino){
        this.origem=origem;
        this.destino=destino;
        distanciaInicial=SphericalUtil.computeDistanceBetween(origem,destino)/1000;
    }
    public double calcularDistancia(){
        return SphericalUtil.computeDistanceBetween(origem,destino)/1000;
    }
    public double calcularTempoEstimado(double distancia,double vel){
        return distancia/(vel*3.6);
    }
    public double calcularDistanciaPercorrida(){
        double distanciaAtual =SphericalUtil.computeDistanceBetween(origem,destino)/1000;
        return distanciaInicial-distanciaAtual;
    }

}
