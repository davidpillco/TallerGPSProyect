package com.example.tallergps3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tallergps3.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
// Importaciones gps
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import java.util.*

class MainActivity : AppCompatActivity() {
    // Variable de ubicacion
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    //Determinamos un permiso para la API
    val PERMISSION_ID = 42
    // Acceder a cualquier vista del proyecto
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtener una instancia de la actividad
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Acceder a todos los componentes de la interfaz grafica - propiedad raiz
        setContentView(binding.root)
        //Preguntar si se tiene permisos.
        if (allPermissionsGrantedGPS()){
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        } else {
            // Si no hay permisos solicitarlos al usuario.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
        }
        // Ejecución del boton
        binding.btndetectar.setOnClickListener {
            leerubicacionactual()
        }

    }
    // Solicita que la aplicacion necesita de GPS
    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun leerubicacionactual(){
        //Evaluar los permiso de la app
        if (checkPermissions()){
            // Evaluar activacion de la ubicacion
            if (isLocationEnabled()){
                // Evaluar los permisos del usuario
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this){ task ->
                        var location: Location? = task.result
                        if (location == null){
                            requestNewLocationData()
                        } else {
                            // Presentar las coordenadas
                            binding.lbllatitud.text = location.latitude.toString()
                            binding.lbllongitud.text = location.longitude.toString()
                            // Obtener ubicacion
                            var cityName = ""
                            var countryName = ""
                            var provincia = ""
                            var postal = ""
                            // Geocoder - Clase de codificacion geografica
                            var geoCoder = Geocoder(this, Locale.getDefault())
                            var Adress = geoCoder.getFromLocation(location.latitude,location.longitude,3)
                            cityName = Adress.get(0).locality //Ciudad
                            countryName = Adress.get(0).countryName // Pais
                            provincia = Adress.get(0).adminArea // Provincia  o Estado
                            postal = Adress.get(0).postalCode // Código postal
                            binding.lblUbicacion.text = countryName +", "+ provincia+ ", "+cityName+" - "+ postal

                        }
                    }

                }
            } else {
                Toast.makeText(this, "Activar ubicación", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                this.finish()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
        }
    }
    @SuppressLint("MissingPermission")
    // Obtener actualizaciones de ubicacion
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
    }
    // Constructor - Evalua cambios en la disponibilidad de los datos de ubicacion
    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation : Location = locationResult.lastLocation
        }
    }
    // Permiso si el GPS esta habilitada
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    // Verificar permisos de la aplicacion que fueron implementados
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    // Recibe los permisos
    companion object {
        private val REQUIRED_PERMISSIONS_GPS= arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    }



}

