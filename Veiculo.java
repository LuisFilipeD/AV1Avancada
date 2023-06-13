package com.example.appt3;

public class Veiculo {

    private double consumoTotal;
    private double consumoMedio;
    public Veiculo(double consumoMedio){
        this.consumoMedio= consumoMedio;
    }
    public double calcularGasolina(double distancia){
        return distancia*consumoMedio;
    }
}
