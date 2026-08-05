package com.example.acae30.ui.abonos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.AlertDialogo
import com.example.acae30.ui.cuentas.Cuentas_list
import com.example.acae30.Funciones
import com.example.acae30.Inicio
import com.example.acae30.R
import com.example.acae30.controllers.AbonosController
import com.example.acae30.controllers.VisitaController
import com.example.acae30.databinding.ActivityAbonosCxcBinding
import com.example.acae30.listas.AbonosAdapter
import com.example.acae30.modelos.Abono
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbonosCxc : AppCompatActivity() {

    private lateinit var binding: ActivityAbonosCxcBinding
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var abonosController = AbonosController()
    private var visitaController = VisitaController()
    private var funciones = Funciones()

    private var alert: AlertDialogo? = null

    private var idVisitarServer = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var latitud = "0"
    private var longitud = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbonosCxcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alert = AlertDialogo(this@AbonosCxc, this@AbonosCxc)

        preferencias = getSharedPreferences(this.instancia, MODE_PRIVATE)

        // OBTERNIENDO UBICACIÓN
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        capturarLocalizacion()
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            regresarInicio()
        }

        binding.nuevoAbono.setOnClickListener {
            val editor = preferencias.edit()
            editor.putString("vista", "abono")
            editor.apply()

            val intento = Intent(this, Cuentas_list::class.java)
            startActivity(intento)
            finish()
        }

//        CoroutineScope(Dispatchers.IO).launch {
//            mostrarDatos()
//        }

        mostrarDatos()

        binding.btnSincronizar.setOnClickListener {
            if(funciones.isInternetAvailable(this@AbonosCxc)){
                CoroutineScope(Dispatchers.IO).launch {
                    transmitirAbonos()
                }
            }else{
                funciones.mensaje(this@AbonosCxc,"ENCIENDE TUS DATOS O EL WIFI")
            }
        }

    }

    @Deprecated("This method has been deprecated in favor of using the\n     " +
            " {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      " +
            "The OnBackPressedDispatcher controls how back button events are dispatched\n     " +
            " to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun regresarInicio(){
        val intent = Intent(this@AbonosCxc, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    //FUNCION MARA MOSTRAR LA LISTA DE ABONOS EN LA ACTIVIDAD
    private fun mostrarDatos(){
        this@AbonosCxc.lifecycleScope.launch {
            try{
                val fecha = funciones.obtenerFecha()
                val lista = abonosController.obtenerAbonosSQLite(this@AbonosCxc, fecha!!)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista : ArrayList<Abono>) {
        val mLayoutManager = LinearLayoutManager(
            this@AbonosCxc,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaAbonos.layoutManager = mLayoutManager
        val adapter = AbonosAdapter(lista, this@AbonosCxc)
        binding.listaAbonos.adapter = adapter

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

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    fun mensaje(mensaje: String){
        val dialog = AlertDialog.Builder(this@AbonosCxc)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUNCION PARA TRANSMITIR LOS ABONOS
    private suspend fun transmitirAbonos(){
        var respuesta : Boolean = true
        val abonosNoTransmitidos : ArrayList<Abono> = abonosController.obtenerAbonosNoEnviados(this@AbonosCxc)
        if(abonosNoTransmitidos.size > 0){
            withContext(Dispatchers.Main) {
                alert!!.Cargando()
                messageAsync("SINCRONIZANDO ABONOS")
                delay(3000)
                for (i in 0 until abonosNoTransmitidos.size) {
                    if (!respuesta) return@withContext
                    val item = abonosNoTransmitidos[i]

                    idVisitarServer = visitaController.registrarVisita(
                        0,
                        funciones.getFechaHoraProceso()!!,
                        latitud,
                        longitud,
                        item.IdCliente!!,
                        item.Cliente!!,
                        item.IdVendedor!!,
                        funciones.getFechaHoraProceso()!!,
                        latitud,
                        longitud,
                        "",
                        0,
                        this@AbonosCxc,
                        "ABONO"
                    )

                    val abono = Abono(
                        item.Fecha,
                        item.IdCliente,
                        item.codigoCliente,
                        item.Cliente,
                        item.IdSucursal,
                        item.Sucursal,
                        item.Abono,
                        item.Tipo_pago,
                        item.Numero_cheque,
                        item.Cuenta,
                        item.Banco,
                        item.IdVendedor,
                        item.Vendedor,
                        item.Fecha_hora_proceso,
                        idVisitarServer,
                        item.AbonoEnviado,
                        item.idAbonoServer
                    )
                    messageAsync("ENVIANDO ABONO \n ${item.Cliente}")
                    delay(3000)
                    if (idVisitarServer > 0) {
                        CoroutineScope(Dispatchers.IO).launch {
                            respuesta = abonosController.enviarAbonoAlServidor(
                                this@AbonosCxc,
                                abono,
                                "SINCRONIZANDO"
                            )
                            if (!respuesta) {
                                withContext(Dispatchers.Main) {
                                    alert!!.dismisss()
                                    mensaje("ERROR: NO HAY UNA CONEXION ESTABLE CON EL SERVIDOR \n INTENTAR MAS TARDE")
                                }
                                mostrarDatos()
                                respuesta = false
                            } else {
                                withContext(Dispatchers.Main) {
                                    messageAsync("ABONO ENVIADO AL SERVIDOR \n ${item.Cliente}")
                                }
                                respuesta = true
                            }
                        }
                        delay(3000)
                    } else {
                        withContext(Dispatchers.Main) {
                            alert!!.dismisss()
                            mensaje("ERROR: NO HAY UNA CONEXION ESTABLE CON EL SERVIDOR \n INTENTAR MAS TARDE")
                        }
                        mostrarDatos()
                        respuesta = false
                    }
                }
                if (respuesta) {
                    messageAsync("ABONOS SINCRONIZADOS CORRECTAMENTE")
                    delay(3000)
                    alert!!.dismisss()
                    mostrarDatos()
                }
            }
            //PROCESO DE AUTO ENVIADO

        }else{
            withContext(Dispatchers.Main) {
                funciones.mensaje(this@AbonosCxc, "NO SE ENCONTRARON ABONOS SIN ENVIAR")
            }
        }
    }

    //MENSANJE ASINCRONO
    fun messageAsync(mensaje: String) {
        if (alert != null) {
            alert!!.changeText(mensaje)
        }
    }
}