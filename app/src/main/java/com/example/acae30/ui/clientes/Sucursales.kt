package com.example.acae30.ui.clientes

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.ui.clientes.NuevaSucursal
import com.example.acae30.controllers.SucursalesController
import com.example.acae30.databinding.ActivitySucursalesBinding
import com.example.acae30.listas.SucursalesAdapter
import com.example.acae30.modelos.SucursalesTarjetaModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class Sucursales : AppCompatActivity() {

    private lateinit var binding : ActivitySucursalesBinding
    private var sucursalController = SucursalesController()
    private var idcliente = 0

    //VARIABLES PARA LA CAPTURA DE LA GEOLOCALIZACION
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var latitud = "0"
    private var longitud = "0"

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //Variable para controlar el Mantenimiento de Clientes
    private var P_Mantto_Clientes: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySucursalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences(instancia, MODE_PRIVATE)

        idcliente = intent.getIntExtra("idcliente", 0)

        P_Mantto_Clientes = preferences.getBoolean("P_Mantto_Clientes", false)

        if(!P_Mantto_Clientes){
            binding.btnNuevaSucursal.visibility = View.GONE
        }

        mostrarDatos()

    }

    override fun onStart() {
        super.onStart()

        binding.btnNuevaSucursal.setOnClickListener {
            val intent = Intent(this@Sucursales, NuevaSucursal::class.java)
            intent.putExtra("idcliente", idcliente)
            intent.putExtra("latitud", latitud)
            intent.putExtra("longitud", longitud)
            startActivity(intent)
            finish()
        }

        binding.imageButton.setOnClickListener {
            val intent = Intent(this@Sucursales, ClientesDetalle::class.java)
            intent.putExtra("idcliente", idcliente)
            startActivity(intent)
            finish()
        }

        // OBTERNIENDO UBICACIÓN
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        capturarLocalizacion()
    }

    // Manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener la ubicación
                updateGPS()
            } else {
                // Permiso denegado, mostrar un mensaje o realizar otra acción
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //FUNCION PARA CAPTURAR LA GEOLOCALIZACION
    private fun capturarLocalizacion() {
        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no hay permiso, solicitarlo
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Si ya hay permiso, obtener la ubicación
            updateGPS()
        }
    }

    // HACER PETICIÓN DE POSICIÓN ACTUAL DEL GPS
    @SuppressLint("MissingPermission")
    private fun updateGPS() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // OBTENIENDO LA UBICACION ACTUAL
                location?.let {
                    latitud = location.latitude.toString()
                    longitud = location.longitude.toString()
                } ?: run {
                    latitud = 0.toString()
                    longitud = 0.toString()
                }
            }
            .addOnFailureListener { e ->
                // ERROR AL NO OBTENER LA UBICACION
                Toast.makeText(this, "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //FUNCION MARA MOSTRAR LA LISTA DE ABONOS EN LA ACTIVIDAD
    private fun mostrarDatos(){
        this@Sucursales.lifecycleScope.launch {
            try{
                val lista = sucursalController.obtenerInfoSucursalesPorCliente(this@Sucursales, idcliente)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista: ArrayList<SucursalesTarjetaModel>) {
        val mLayoutManager = LinearLayoutManager(
            this@Sucursales,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaSucursales.layoutManager = mLayoutManager
        val adapter = SucursalesAdapter(lista)
        binding.listaSucursales.adapter = adapter

    }


}