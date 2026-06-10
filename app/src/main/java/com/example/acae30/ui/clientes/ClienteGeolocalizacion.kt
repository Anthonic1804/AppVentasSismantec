package com.example.acae30.ui.clientes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.ui.clientes.ClientesDetalle
import com.example.acae30.R
import com.example.acae30.controllers.ClientesController
import com.example.acae30.databinding.ActivityClienteGeolocalizacionBinding
import com.example.acae30.modelos.Cliente
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ClienteGeolocalizacion : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding : ActivityClienteGeolocalizacionBinding
    private lateinit var map : GoogleMap

    private var idCliente = 0
    private var clientesController = ClientesController()
    private var datosCliente : Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteGeolocalizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idCliente = intent.getIntExtra("idcliente", 0)
        datosCliente = clientesController.obtenerInformacionCliente(this@ClienteGeolocalizacion, idCliente)

        createFragment()

    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            val intent = Intent(this@ClienteGeolocalizacion, ClientesDetalle::class.java)
            intent.putExtra("idcliente", idCliente)
            startActivity(intent)
            finish()
        }
    }

    private fun createFragment() {
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createMarker()
    }

    private fun createMarker() {
        val coordinate =
            LatLng(datosCliente!!.Latitud!!.toDouble(), datosCliente!!.Longitud!!.toDouble())
        val marker : MarkerOptions = MarkerOptions().position(coordinate).title("${datosCliente!!.Cliente}")
        map.addMarker(marker)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinate, 18f),
            4000,
            null
        )
    }
}