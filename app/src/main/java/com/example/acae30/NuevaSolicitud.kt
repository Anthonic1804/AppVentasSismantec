package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.CatalogosController
import com.example.acae30.controllers.SolicitudRecargasController
import com.example.acae30.databinding.ActivityNuevaSolicitudBinding
import com.example.acae30.listas.SolicitudDetalleAdapter
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevaSolicitud : AppCompatActivity() {
    private lateinit var binding : ActivityNuevaSolicitudBinding
    private var funciones = Funciones()
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var solicitudController = SolicitudRecargasController()
    private var catalagosController = CatalogosController()

    private var vendedor = ""
    private var idVendedor = 0

    private var idSolicitud : Int = 0
    private var idServidorSolicitud : Int = 0
    private var proceso : String = ""
    private var estado : String = ""

    var rutaSeleccionada : String = "-- SELECCIONE --"
    var idRutaSeleccionada : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        vendedor =  preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)

        idSolicitud = intent.getIntExtra("idSolicitud", 0)
        idServidorSolicitud = intent.getIntExtra("idServidorSolicitud", 0)
        estado = intent.getStringExtra("estado").toString()
        proceso = intent.getStringExtra("proceso").toString()


        if(proceso.contains("nuevo")){
            binding.btnImprimir.visibility = View.GONE
        }

        cargarDetalle()
        cargarRutas()

        validarEstadoSolicitud()
    }

    override fun onStart() {
        super.onStart()

        binding.btncancelar.setOnClickListener {
            mensajeCancelar("CANCELAR")
        }

        binding.btnenviar.setOnClickListener {
            when(idRutaSeleccionada){
                0 -> {
                    Toast.makeText(this,"DEBE DE SELECCIONAR UNA RUTA", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    val lista = solicitudController.obtenerDetalleSolicitudNoEnviado(this@NuevaSolicitud, idSolicitud)
                    if (lista != null) {
                        if(lista.size > 0){
                            mensajeCancelar("ENVIAR")
                        }else{
                            Toast.makeText(this,"NO SE ENCONTRARON PRODUCTO NUEVOS EN EL DETALLE", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

        binding.imgbtnadd.setOnClickListener {
            val intent = Intent(this, ListadoProductosSolicitud::class.java)
            intent.putExtra("idSolicitud", idSolicitud)
            intent.putExtra("proceso", "nuevo")
            intent.putExtra("idServidorSolicitud", idServidorSolicitud)
            intent.putExtra("estado", estado)
            startActivity(intent)
            finish()
        }

        //IMPLEMENTANDO LOGICA DEL RUTA SELECCIONADO
        binding.spRuta.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                rutaSeleccionada = parent?.getItemAtPosition(position).toString()

                this@NuevaSolicitud.lifecycleScope.launch {
                    try{
                        idRutaSeleccionada = if(rutaSeleccionada == "-- SELECCIONE --"){
                            0
                        }else{
                            catalagosController.obtenerInformacionRuta(this@NuevaSolicitud, rutaSeleccionada)!!.id
                        }
                    }catch (e: Exception){
                        println("ERROR AL CARGAR LA RUTA DEL CIENTE " + e.message)
                    }
                }

                solicitudController.actualizarRuta(this@NuevaSolicitud, idRutaSeleccionada, rutaSeleccionada, idSolicitud)

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val fecha = funciones.getFechaHoraProceso()
        binding.fechaSolicitud.text = fecha.toString()

        binding.btnGuardar.setOnClickListener {
            mensajeCancelar("GUARDAR")
        }
    }

    //FUNCION PARA ENVIAR LA SOLICITUD DE CARGA AL SERVIDOR
    private fun enviarSolicitudCarga(){
        this@NuevaSolicitud.lifecycleScope.launch {

            val enviado = solicitudController.enviarSolicitudCargaAlServidor(this@NuevaSolicitud, idSolicitud)

            if(enviado){
                //ACTUALIZANDO EL DETALLA DE LA SOLICITUD A ENVIADO = 1
                solicitudController.actualizarEstadoAlDetalle(this@NuevaSolicitud, idSolicitud)

            }

            withContext(Dispatchers.Main){
                mensajeConfirmacion(enviado)
            }

        }
    }

    //Funcion para validar Estado
    private fun validarEstadoSolicitud(){


        when(estado){
            "PROCESADO" -> {
                binding.apply {
                    btnenviar.visibility = View.GONE
                    btnGuardar.visibility = View.GONE
                    btnImprimir.visibility = View.VISIBLE
                    imgbtnadd.visibility = View.GONE

                    btncancelar.text = "REGRESAR"
                    lblTituloSolicitud.text = "SOLICITUD DE CARGA"
                    spRuta.isEnabled = false
                    cvProductosSolicitados.isEnabled = false

                }
            }
            "ANULADO" -> {
                binding.apply {
                    btnenviar.visibility = View.GONE
                    btnGuardar.visibility = View.GONE
                    btnImprimir.visibility = View.GONE
                    imgbtnadd.visibility = View.GONE

                    btncancelar.text = "REGRESAR"
                    lblTituloSolicitud.text = "SOLICITUD DE CARGA"
                    spRuta.isEnabled = false
                    cvProductosSolicitados.isEnabled = false

                }
            }
            else -> {
                binding.apply {
                    btnenviar.visibility = View.VISIBLE
                    btnGuardar.visibility = View.VISIBLE
                    btnImprimir.visibility = View.GONE
                    imgbtnadd.visibility = View.VISIBLE

                }
            }
        }

    }

    //FUNCION PARA CARGAR LAS RUTAS
    private fun cargarRutas(){
        this.lifecycleScope.launch {
            try{
                val solicitud = solicitudController.obtenerSolicitudCargaPorId(this@NuevaSolicitud, idSolicitud)
                val rutaSeleccionada = solicitud!!.ruta

                val listaRustas = catalagosController.obtenerListadoRutaSQLite(this@NuevaSolicitud, "", "", true)
                val rutas = ArrayAdapter<String>(this@NuevaSolicitud, android.R.layout.simple_spinner_dropdown_item)
                rutas.add(rutaSeleccionada)
                rutas.addAll(listaRustas)
                binding.spRuta.adapter = rutas

            }catch (e: Exception){
                println("ERROR AL CARGAR LAS RUTAS A LA SOLICITUD DE CARGA -> " + e.message)
            }
        }
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(tipo: String){

        val mensaje = when(tipo){
            "CANCELAR" -> "¿DESEA CANCELAR EL PROCESO?"
            "ENVIAR" -> "¿DESEA ENVIAR LA SOLICITUD DE CARGA?"
            else -> "¿DESEA GUARDAR EL PROCESO?"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                when(tipo){
                    "CANCELAR" -> {
                        when(estado){
                            "EMITIDO" -> {
                                when(idServidorSolicitud){
                                    0 -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            solicitudController.eliminarSolicitud(this@NuevaSolicitud, idSolicitud)
                                        }
                                    }
                                    else -> {
                                        Toast.makeText(this@NuevaSolicitud, "NO PUEDE ELIMINAR LA SOLICITUD, YA HA SIDO ENVIADA AL SERVIDOR", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }

                                menuSolicitudCarga()
                            }
                            else -> {
                                menuSolicitudCarga()
                            }
                        }
                    }
                    "ENVIAR" -> {
                        enviarSolicitudCarga()
                    }
                    else -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            solicitudController.actualizarEstadoGuardado(this@NuevaSolicitud, idSolicitud)
                        }

                        Toast.makeText(this, "SOLICITUD GUARDADA CORRECTAMENTE", Toast.LENGTH_SHORT)
                            .show()

                        menuSolicitudCarga()
                    }
                }
            }
            .setNegativeButton("CANCELAR"){view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeConfirmacion(enviado : Boolean){
        val mensaje = if(enviado){
            "SOLICITUD ENVIADA CORRECTAMENTE"
        }else{
            "ERROR PROBLEMAS DE CONEXION \n SOLICITUD ALMACENADA, TRATE DE ENVIAR MAS TARDE"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()

                if(!enviado){
                    lifecycleScope.launch(Dispatchers.IO) {
                        solicitudController.actualizarEstadoGuardado(this@NuevaSolicitud, idSolicitud)
                    }
                }

                menuSolicitudCarga()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun menuSolicitudCarga() {
        val intent = Intent(this, SolicitudCargaMenu::class.java)
        startActivity(intent)
        finish()
    }

    private fun cargarDetalle(){
        this.lifecycleScope.launch {
            try {
                val lista = solicitudController.obtenerDetalleSolicitud(this@NuevaSolicitud, idSolicitud)
                if (lista != null) {
                    if(lista.size > 0){
                        armarLista(lista)
                    }
                }
            }catch (e: Exception){
                Toast.makeText(this@NuevaSolicitud,"ERROR AL MOSTRAR EL DETALLE DE LA SOLICITUD", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun armarLista(lista: ArrayList<SolicitudCargaDetalle>) {

        val mLayoutManager = LinearLayoutManager(
            this@NuevaSolicitud,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.cvProductosSolicitados.layoutManager = mLayoutManager
        val adapter =  if(estado.contains("EMITIDO")){
            SolicitudDetalleAdapter(lista, this@NuevaSolicitud) { indice ->
                val item = lista[indice]
                val intento = Intent(this@NuevaSolicitud, AgregarProductoSolicitud::class.java)
                intento.putExtra("proceso", "editar")
                intento.putExtra("idSolicitud", idSolicitud)
                intento.putExtra("idProducto", item.id)
                intento.putExtra("codigo", item.codigoProducto)
                intento.putExtra("descripcion", item.descripcion)
                intento.putExtra("cantidad", item.cantidad)
                intento.putExtra("enviado", item.enviado)
                intento.putExtra("idServidorSolicitud", idServidorSolicitud)
                intento.putExtra("estado", estado)
                startActivity(intento)
                finish()

            }
        }else{
            SolicitudDetalleAdapter(lista, this@NuevaSolicitud) { _ -> }
        }
        binding.cvProductosSolicitados.adapter = adapter

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras
}