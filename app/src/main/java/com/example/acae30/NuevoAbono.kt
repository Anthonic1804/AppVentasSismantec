package com.example.acae30

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.acae30.controllers.AbonosController
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.SucursalesController
import com.example.acae30.controllers.VisitaController
import com.example.acae30.databinding.ActivityNuevoAbonoBinding
import com.example.acae30.modelos.Abono
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.InformacionSucursal
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoAbono : AppCompatActivity() {

    private lateinit var binding : ActivityNuevoAbonoBinding
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    private var funciones = Funciones()
    private var clienteController = ClientesController()
    private var sucursalesController = SucursalesController()
    private var abonosController = AbonosController()
    private var visitaController = VisitaController()

    private var formaPagoSeleccionada = "-- SELECCIONE --"
    private var sucursal = "-- SELECCIONES UNA SUCURSAL --"
    private var idSucursal = 0
    private var idCliente = 0
    private var datosCliente : Cliente? = null
    private var datosSucursal : InformacionSucursal? = null
    private var vendedor = ""
    private var idVendedor = 0
    private var idVisitarServer = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var latitud = "0"
    private var longitud = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoAbonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        vendedor =  preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)

        //COMPLETANDO SPINNER FORMA PAGO
        val formaPago = ArrayAdapter<String>(this@NuevoAbono, android.R.layout.simple_spinner_dropdown_item)
        formaPago.addAll(listOf("-- SELECCIONE --","EFECTIVO", "CHEQUE"))
        binding.spFormaPago.adapter = formaPago

        //COMPLETANDO SPINNER SUCURSAL
        cargarSucursales()

        //DESACTIVANDO EL BOTON DE INGRESAR
        binding.btnAceptar.isEnabled = false
        binding.btnAceptar.setBackgroundResource(R.drawable.border_btndisable)

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

    override fun onStart() {
        super.onStart()
        idCliente = intent.getIntExtra("idcliente", 0)
        datosCliente = clienteController.obtenerInformacionCliente(this@NuevoAbono, idCliente)!!

        //IMPLEMENTANDO LOGICA DE TIPO DE PAGO SELECCIONADA EN SPINNER
        binding.spFormaPago.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                formaPagoSeleccionada = parent?.getItemAtPosition(position).toString()

                CoroutineScope(Dispatchers.IO).launch {
                    when(formaPagoSeleccionada){
                        "CHEQUE" -> {
                            runOnUiThread{
                                binding.lyContenedorCheque.visibility = View.VISIBLE
                                validar()
                            }
                        }
                        "EFECTIVO" -> {
                            runOnUiThread{
                                binding.lyContenedorCheque.visibility = View.GONE
                                validar()
                            }
                        }
                        else -> {
                            runOnUiThread {
                                validar()
                            }
                        }
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DE LA SUCURSAL SELECCIONADA
        binding.spSucursal.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                sucursal = parent?.getItemAtPosition(position).toString()
                datosSucursal = sucursalesController.obtenerInformacionSucursal(this@NuevoAbono, sucursal, idCliente)

                CoroutineScope(Dispatchers.IO).launch {
                    when(sucursal){
                        "-- SELECCIONES UNA SUCURSAL --" -> {
                            runOnUiThread {
                                validar()
                            }
                        }
                        else -> {
                            runOnUiThread {
                                validar()
                            }
                        }
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.txtCliente.setText(datosCliente!!.Cliente)

        binding.btnCancelar.setOnClickListener {
            mensaje("CANCELAR", 0)
        }

        binding.btnAceptar.setOnClickListener {

            //ASIGNADO EL ID DE LA SUCURSAL
            idSucursal = if(datosSucursal == null){
                0
            }else{
                datosSucursal!!.id
            }

            idVisitarServer = visitaController.registrarVisita(
                0,
                funciones.getFechaHoraProceso()!!,
                latitud,
                longitud,
                datosCliente!!.Id!!,
                datosCliente!!.Cliente!!,
                idVendedor,
                funciones.getFechaHoraProceso()!!,
                latitud,
                longitud,
                "",
                0,
                this@NuevoAbono,
                "ABONO")

            if(idVisitarServer > 0){
                mensaje("ACEPTAR", idVisitarServer)
            }else{
                mensaje("ERROR", 0)
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

    //FUNCION PARA VALIDAR EL ENVIO
    private fun validar(){
        if(sucursal == "-- SELECCIONES UNA SUCURSAL --" || formaPagoSeleccionada == "-- SELECCIONE --"){
            binding.btnAceptar.isEnabled = false
            binding.btnAceptar.setBackgroundResource(R.drawable.border_btndisable)
        }else{
            binding.btnAceptar.isEnabled = true
            binding.btnAceptar.setBackgroundResource(R.drawable.border_btnactualizar)
        }
    }

    //FUNCION PARA CARGAR LAS SUCURSALES AL SPINNER
    private fun cargarSucursales() {
        idCliente = intent.getIntExtra("idcliente", 0)
        val listSucursal = sucursalesController.obtenerSucursalesporIdCliente(this@NuevoAbono, idCliente)

        if(listSucursal.isEmpty()){
            binding.spSucursal.visibility = View.GONE
            binding.sinSucursal.visibility = View.VISIBLE
            sucursal = "SIN SUCURSAL"
        }else{
            binding.spSucursal.visibility = View.VISIBLE
            binding.sinSucursal.visibility = View.GONE
        }

        val adaptador = ArrayAdapter(this@NuevoAbono, android.R.layout.simple_spinner_item, listSucursal)
        adaptador.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.spSucursal.adapter = adaptador
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensaje(tipo:String, idVisitaServer: Int){
        var mensaje = ""
        mensaje = when(tipo){
            "CANCELAR" -> {
                "¿DESEA CANCELAR EL PROCESO?"
            }
            "ERROR" -> {
                "ERROR AL ENVIAR EL ABONO \n ¿DESEA REGISTRARLO PARA ENVIAR MAS TARDE?"
            }
            else -> {
                "¿DESEA INGRESAR EL ABONO?"
            }
        }
        val dialog = AlertDialog.Builder(this@NuevoAbono)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->

                when(tipo){
                    "CANCELAR" -> {
                        view.dismiss()
                        cancelarAbono()
                    }
                    "ERROR" -> {
                        view.dismiss()
                        CoroutineScope(Dispatchers.IO).launch {
                            procesarAbono("GUARDAR", idVisitaServer)
                        }
                    }
                    else -> {
                        view.dismiss()
                        CoroutineScope(Dispatchers.IO).launch {
                            procesarAbono("ENVIAR", idVisitaServer)
                        }
                    }
                }
            }
            .setNegativeButton("CANCELAR"){ view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUNCION PARA REGRESAR AL MODULO DE ABONOS
    private fun cancelarAbono(){
        val intento = Intent(this@NuevoAbono, Cuentas_list::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION PARA REGRESAR AL MENU ABONOS
    private fun listadoAbono(){
        val intento = Intent(this@NuevoAbono, AbonosCxc::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION PARA PROCESAR EL ABONO
    private suspend fun procesarAbono(tipo : String, idVisitaServer: Int){
        var respuesta : Boolean = false
        val abono : Abono = Abono(
            funciones.obtenerFecha(),
            idCliente,
            datosCliente!!.Codigo,
            datosCliente!!.Cliente,
            idSucursal,
            sucursal,
            binding.txtMonto.text.toString().toFloat(),
            formaPagoSeleccionada,
            binding.tvNumCheque.text.toString(),
            binding.tvNumCuentaCheque.text.toString(),
            binding.tvBanco.text.toString(),
            idVendedor,
            vendedor,
            funciones.getFechaHoraProceso(),
            idVisitaServer,
            0,
            0
        )

        when(tipo){
            "ENVIAR" -> {
                respuesta = abonosController.enviarAbonoAlServidor(this@NuevoAbono, abono, "NUEVO")
            }
            "GUARDAR" -> {
                respuesta = abonosController.insertarAbonoCxc(this@NuevoAbono, abono, "GUARDAR", 0)
            }
        }


        if(respuesta && tipo == "ENVIAR"){
            withContext(Dispatchers.Main){
                mensajeConfirmacion("ENVIADO")
            }
        }else if(respuesta && tipo == "GUARDAR"){
            withContext(Dispatchers.Main){
                mensajeConfirmacion("GUARDADO")
            }
        }
        else{
            withContext(Dispatchers.Main) {
                mensaje("ERROR", 0)
            }
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

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeConfirmacion(tipo: String){
        val mensaje = when(tipo){
            "ENVIADO" -> {
                "ABONO REGISTRADO CORRECTAMENTE"
            }
            else -> {
                "ABONO ALMACENADO CORRECTAMENTE"
            }
        }

        val dialog = AlertDialog.Builder(this@NuevoAbono)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                when(tipo){
                    "ENVIADO" -> {
                        view.dismiss()
                        listadoAbono()
                    }
                    else -> {
                        view.dismiss()
                        listadoAbono()
                    }
                }
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }
}