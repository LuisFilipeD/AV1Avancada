package com.example.appt3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Rota rota;
    private Veiculo veiculo;
    private TextView txtEndereco;
    private TextView txtDistancia;
    private Address endereco;
    private TextView txtVelocidade;
    private TextView txtTempoEstimado;
    private TextView txtDistPercorrida;

    private TextView txtConsumoT;
    private double tempoEstimado;

    FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = LocationServices.getFusedLocationProviderClient(this);
    }
    @Override
    protected void onResume(){
        super.onResume();

        txtEndereco = (TextView) findViewById(R.id.txtEndereco);
        txtDistancia = (TextView) findViewById(R.id.txtDistancia);
        txtVelocidade = (TextView) findViewById(R.id.txtVelocidade);
        txtTempoEstimado= (TextView) findViewById(R.id.txtTempoEstimado);
        txtDistPercorrida = (TextView) findViewById(R.id.txtDistPercorrida);
        txtConsumoT = (TextView) findViewById(R.id.txtConsumoT);

        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        //Garantir que o usuário esteja com o Google Play Service Atualizado
        switch (errorCode) {
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.SERVICE_DISABLED:
                Log.d("Teste", "show dialog");
                GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, 0, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();
                break;
            case ConnectionResult.SUCCESS:
                Log.d("Teste", "Google Play Service up-to-date");
                break;
        }
        // Verificar Permissoes
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Teste3", "Erro de conexao");
            return;
        }
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    //Localização atual (Atualizada)
                    LatLng origem = new LatLng(location.getLatitude(),location.getLongitude());
                    //Destino pre-definido
                    LatLng destino = new LatLng(-21.24798,-44.98994);

                    rota=new Rota(origem,destino); // Criando a rota

                    txtDistancia.setText("Distancia ate o destino: "+rota.calcularDistancia()+" Km");
                    try {
                        endereco = buscarEndereco(location.getLatitude(),location.getLongitude());
                        txtEndereco.setText("Endereco atual : " + endereco.getAddressLine(0));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    txtVelocidade.setText("Velocidade atual:"+location.getSpeed()+"Km/h");

                    tempoEstimado = rota.calcularTempoEstimado(rota.calcularDistancia(),location.getSpeed());

                    txtTempoEstimado.setText("Tempo Estimado:" + tempoEstimado + "H");

                    txtDistPercorrida.setText("Distancia Percorrida: "+rota.calcularDistanciaPercorrida()+"Km");
                    veiculo=new Veiculo(15.6);

                    txtConsumoT.setText("Consumo Total: "+veiculo.calcularGasolina(rota.calcularDistanciaPercorrida()));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

        //Configuracao de atualizacao dos valores de localizacao
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1*1000);
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("Teste", locationSettingsResponse.getLocationSettingsStates()
                                .isNetworkLocationPresent()+"");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException){
                            try{
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(MainActivity.this,10);
                            }catch (IntentSender.SendIntentException e1){
                            }
                        }
                    }
                });

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if(locationResult == null){
                    Log.i("Teste","Local is null");
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    if(!Geocoder.isPresent()){
                        return;
                    }
                }
            }
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                Log.i("Teste",locationAvailability.isLocationAvailable()+"");
            }
        };
        client.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }
    public Address buscarEndereco(double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(getApplicationContext());
        Address address=null;
        List<Address> addresses;

        addresses = geocoder.getFromLocation(latitude,longitude,1);
        if(addresses.size()>0){
            address=addresses.get(0);
        }
        return address;
    }
}