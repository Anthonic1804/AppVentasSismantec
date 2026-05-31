package com.example.acae30

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.acae30.controllers.CatalogosController
import com.example.acae30.controllers.ClientesController
import com.example.acae30.databinding.ActivityNuevoClienteBinding
import com.example.acae30.modelos.Cliente
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoCliente : AppCompatActivity() {

    private lateinit var binding : ActivityNuevoClienteBinding
    private var catalogoController = CatalogosController()
    private var clienteController = ClientesController()
    private var funciones = Funciones()

    private var codigoPais : String = "SV"
    private var pais : String = "EL SALVADOR SV"
    private var codigoDepto : String = "00"
    private var departamento : String = "-- SELECCIONE --"
    private var codigoMuni : String = "00"
    private var municipio : String = "-- SELECCIONE --"
    private var codigoDistri : String = "00"
    private var distrito : String = "-- SELECCIONE --"
    private var idRuta : Int = 0
    private var ruta : String = "-- SELECCIONE --"
    private var tipoContribuyente : String = ""
    private var terminos : String = "Contado"
    private var codigoCliente : String = ""

    //VARIABLES PARA LA CAPTURA DE LA GEOLOCALIZACION
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var latitud = ""
    private var longitud = ""

    private var latitudEditada = ""
    private var longitudEditada = ""

    private var vista = ""
    private var idcliente = 0
    private var datosClientes : Cliente? = null

    private lateinit var preferencias: SharedPreferences

    private val instancia = "CONFIG_SERVIDOR"
    private var idVendedor : Int = 0
    private var vendedor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCapturarGeo.visibility = View.GONE

        vista = intent.getStringExtra("vista").toString()

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idVendedor = preferencias.getInt("Idvendedor", 0)
        vendedor = preferencias.getString("Vendedor", "").toString()

        if(vista == "editar"){
            idcliente = intent.getIntExtra("idcliente", 0)

            datosClientes = clienteController.obtenerInformacionCliente(this, idcliente)
            latitud = datosClientes!!.Latitud!!.ifEmpty { "00" }
            longitud = datosClientes!!.Longitud!!.ifEmpty { "00" }
            codigoPais = datosClientes!!.DTECodPais!!.ifEmpty { "00" }
            pais = datosClientes!!.DTEPais!!.ifEmpty{"-- SELECCIONE --"}
            codigoDepto = datosClientes!!.DTECodDepto!!.ifEmpty { "00" }
            departamento = datosClientes!!.Departamento!!.ifEmpty{"-- SELECCIONE --"}
            codigoMuni = datosClientes!!.DTECodMunicipio!!.ifEmpty { "00" }
            municipio = datosClientes!!.Municipio!!.ifEmpty{"-- SELECCIONE --"}
            codigoDistri = datosClientes!!.DTECodDistrito!!.ifEmpty { "00" }
            distrito = datosClientes!!.DTEDistrito!!.ifEmpty{"-- SELECCIONE --"}
            idRuta = datosClientes!!.Id_ruta!!
            ruta = datosClientes!!.Ruta!!
            tipoContribuyente = datosClientes!!.Categoria_cliente!!


            //println("DATOS DEL CLIENTE -> " + datosClientes)

            with(binding){
                btnCapturarGeo.visibility = View.VISIBLE
                btnaceptar.text = "ACTUALIZAR"
                tvNuevoCliente.text = "ACTUALIZAR CLIENTE"

                //DATOS DEL FORMULARIO
                txtNombreCliente.setText(datosClientes!!.Cliente)
                txtDireccion.setText(datosClientes!!.Direccion)
                txtTelefono.setText(datosClientes!!.Telefono_1)
                txtCorreo.setText(datosClientes!!.Correo)
                txtContacto.setText(datosClientes!!.Contacto)
                txtLatitud.setText(latitud)
                txtLongitud.setText(longitud)
                txtNombreComercial.setText(datosClientes!!.NombreComercial)
                txtDui.setText(datosClientes!!.Dui)
                txtNit.setText(datosClientes!!.Nit)
                txtNrc.setText(datosClientes!!.Nrc)
                txtCodGiro.setText(datosClientes!!.DTECodGiro)
                txtGiro.setText(datosClientes!!.Giro)
            }


            // OBTERNIENDO UBICACIÓN
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            capturarLocalizacion()
        }else{
            latitud = intent.getStringExtra("latitud").toString()
            longitud = intent.getStringExtra("longitud").toString()
        }

        cargarPais()

        cargarDepartamento()

        cargarMunicipio()

        cargarDistrito()

        cargarRutas()

        cargarContribuyente()

    }

    override fun onStart() {
        super.onStart()


        binding.btnAtras.setOnClickListener {
            mensajeCancelar(vista)
        }

        binding.btnaceptar.setOnClickListener {
            if(!funciones.isInternetAvailable(this@NuevoCliente)){
                funciones.mensaje(this@NuevoCliente, "CONEXION DE INTERNET INESTABLE")
            }else{

//                codigoCliente = if(binding.txtNrc.text.isNullOrEmpty()){
//                    binding.txtDui.text.toString()
//                }else{
//                    binding.txtNrc.text.toString()
//                }

                if(binding.txtNombreCliente.text!!.isEmpty() || binding.txtNombreCliente.text!!.length < 10){
                    Toast.makeText(this,"VERIFIQUE DATOS IMPORTANTES, NOMBRE, DUI, NRC, NIT, ETC \n CORRESPONDIENTE AL TIPO DE CLIENTE", Toast.LENGTH_LONG).show()
                }else{
                    CoroutineScope(Dispatchers.IO).launch {
                        registrarCliente()
                    }
                }
            }
        }

        //SETEANDO LA LONGITUD Y LATITUD DEL CLIENTE
        binding.txtLatitud.setText(latitud)
        binding.txtLongitud.setText(longitud)

        binding.btnCapturarGeo.setOnClickListener {
            //SETEANDO LA LONGITUD Y LATITUD DEL CLIENTE
            binding.txtLatitud.setText(latitudEditada)
            binding.txtLongitud.setText(longitudEditada)

            latitud = latitudEditada
            longitud = longitudEditada
        }

        //IMPLEMENTANDO LOGICA DEL PAIS SELECCIONADO
        binding.spPais.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                pais = parent?.getItemAtPosition(position).toString()

                this@NuevoCliente.lifecycleScope.launch {
                    try{
                        codigoPais = catalogoController.obtenerInformacionPais(this@NuevoCliente, pais)!!.codigo

                        cargarDepartamento()
                        cargarMunicipio()
                        cargarDistrito()
                    }catch (e: Exception){
                        println("ERROR AL CARGAR EL PAIS" + e.message)
                    }
                }

                if(codigoPais != "SV"){
                    codigoDepto = "00"
                    departamento = "-- SELECCIONE --"
                    codigoMuni = "00"
                    municipio = "-- SELECCIONE --"
                    codigoDistri = "00"
                    distrito = "-- SELECCIONE --"

                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DEL DEPARTAMENTO SELECCIONADO
        binding.spDepartamento.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                departamento = parent?.getItemAtPosition(position).toString()

                this@NuevoCliente.lifecycleScope.launch {
                    try{
                        codigoDepto = if(departamento == "-- SELECCIONE --"){
                            "00"
                        }else{
                            catalogoController.obtenerInformacionDepartamento(this@NuevoCliente, departamento)!!.codigo
                        }

                        cargarMunicipio()
                        cargarDistrito()
                    }catch (e: Exception){
                        println("ERROR AL CARGAR EL DEPARTAMENTO" + e.message)
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DEL MUNICIPIO SELECCIONADO
        binding.spMunicipio.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                municipio = parent?.getItemAtPosition(position).toString()

                this@NuevoCliente.lifecycleScope.launch {
                    try{
                        codigoMuni = if(municipio == "-- SELECCIONE --"){
                            "00"
                        }else{
                            catalogoController.obtenerInformacionMunicipio(this@NuevoCliente, municipio)!!.codigo
                        }

                        cargarDistrito()
                    }catch (e: Exception){
                        println("ERROR AL CARGAR EL MUNICIPIO" + e.message)
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DEL MUNICIPIO SELECCIONADO
        binding.spDistrito.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                distrito = parent?.getItemAtPosition(position).toString()

                this@NuevoCliente.lifecycleScope.launch {
                    try{
                        codigoDistri = if(distrito == "-- SELECCIONE --"){
                            "00"
                        }else{
                            catalogoController.obtenerInformacionDistrito(this@NuevoCliente, distrito)!!.codigo
                        }
                    }catch (e: Exception){
                        println("ERROR AL CARGAR EL DISTRITO" + e.message)
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DEL RUTA SELECCIONADO
        binding.spRuta.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                ruta = parent?.getItemAtPosition(position).toString()

                this@NuevoCliente.lifecycleScope.launch {
                    try{
                        idRuta = if(ruta == "-- SELECCIONE --"){
                            0
                        }else{
                            catalogoController.obtenerInformacionRuta(this@NuevoCliente, ruta)!!.id
                        }
                    }catch (e: Exception){
                        println("ERROR AL CARGAR LA RUTA DEL CIENTE " + e.message)
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DEL TIPO CONTRIBUYENTE SELECCIONADO
        binding.spContribuyente.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                tipoContribuyente = parent?.getItemAtPosition(position).toString()

                this@NuevoCliente.lifecycleScope.launch {
                    try{
                        tipoContribuyente = if(tipoContribuyente == "-- SELECCIONE --"){
                            ""
                        }else {
                            parent?.getItemAtPosition(position).toString()
                        }
                    }catch (e: Exception){
                        println("ERROR AL CARGAR EL TIPO DE CONTRIBUYENTE" + e.message)
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DE BUSQUEDA DE GIRO
        binding.txtCodGiro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(texto: Editable) {
                val busqueda : String = catalogoController.obtenerInformacionGiro(this@NuevoCliente, texto.toString())
                binding.txtGiro.setText(busqueda)
            }
        })

    }

    private suspend fun registrarCliente() {
        var registrado : Boolean = false

        val documento = if(binding.txtNit.text.isNullOrEmpty()){
            binding.txtDui.text.toString()
        }else{
            binding.txtNit.text.toString()
        }

        val deptoFormateado = funciones.normalizarTexto(departamento).uppercase()
        val muniFormateado = funciones.normalizarTexto(municipio).uppercase()

        val cliente : Cliente = Cliente(
            idcliente,
            codigoCliente,
            binding.txtNombreCliente.text.toString(),
            binding.txtDui.text.toString(),
            documento,
            binding.txtNrc.text.toString(),
            "", //binding.txtGiro.text.toString()
            tipoContribuyente,
            terminos,
            0,
            0f,
            0f,
            "Activo",
            binding.txtDireccion.text.toString(),
            muniFormateado,
            deptoFormateado,
            binding.txtTelefono.text.toString(),
            binding.txtTelefono.text.toString(),
            binding.txtCorreo.text.toString(),
            binding.txtContacto.text.toString(),
            idRuta,
            idVendedor,
            vendedor,
            "ACTIVO",
            "",
            0f,
            0,
            "N",
            binding.txtGiro.text.toString(),
            ruta,
            binding.txtDireccion.text.toString(),
            codigoDepto,
            codigoMuni,
            codigoPais,
            pais,
            binding.txtCorreo.text.toString(),
            binding.txtTelefono.text.toString(),
            latitud,
            longitud,
            binding.txtNombreComercial.text.toString(),
            binding.txtCodGiro.text.toString(),
            distrito,
            codigoDistri,
            "N"
        )

        registrado = clienteController.enviarRegistroClienteAlServidor(this@NuevoCliente, cliente)

        withContext(Dispatchers.Main){
            mensajeRegistrado(registrado)
        }
    }

    private fun cargarPais(){
        this@NuevoCliente.lifecycleScope.launch {
            try {
                val listaPais = catalogoController.obtenerListadoPaisesSQLite(this@NuevoCliente, vista, pais)

                val adaptadorPais = ArrayAdapter(this@NuevoCliente, android.R.layout.simple_spinner_item, listaPais)
                adaptadorPais.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spPais.adapter = adaptadorPais

                if(vista != "editar"){
                    binding.spPais.setSelection(60, true)
                }

            }catch (e:Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarDepartamento(){
        this@NuevoCliente.lifecycleScope.launch {
            try{
                val listaDepartamentos = catalogoController.obtenerListadoDepartamentosSQLite(this@NuevoCliente, codigoPais, vista, departamento)

                val departamento = ArrayAdapter(this@NuevoCliente, android.R.layout.simple_spinner_item, listaDepartamentos)
                departamento.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spDepartamento.adapter = departamento
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarMunicipio(){
        this@NuevoCliente.lifecycleScope.launch {
            try{
                val listaMunicipos = catalogoController.obtenerListadoMunicipiosSQLite(this@NuevoCliente, codigoPais, codigoDepto, vista, municipio)

                val adaptadorMunicipio = ArrayAdapter(this@NuevoCliente, android.R.layout.simple_spinner_item, listaMunicipos)
                adaptadorMunicipio.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spMunicipio.adapter = adaptadorMunicipio
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarDistrito(){
        this@NuevoCliente.lifecycleScope.launch {
            try{
                val listaDistritos = catalogoController.obtenerListadoDistritosSQLite(this@NuevoCliente, codigoDepto, codigoMuni, codigoPais, vista, distrito)

                val distritos = ArrayAdapter(this@NuevoCliente, android.R.layout.simple_spinner_item, listaDistritos)
                distritos.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spDistrito.adapter = distritos
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarRutas(){
        this@NuevoCliente.lifecycleScope.launch {
            try{
                val listaRustas = catalogoController.obtenerListadoRutaSQLite(this@NuevoCliente, vista, ruta, false)

                val rutas = ArrayAdapter(this@NuevoCliente, android.R.layout.simple_spinner_item, listaRustas)
                rutas.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spRuta.adapter = rutas
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarContribuyente(){
        this@NuevoCliente.lifecycleScope.launch {
            try {
                val contribuyente = ArrayAdapter<String>(this@NuevoCliente, android.R.layout.simple_spinner_dropdown_item)
                if(vista == "editar"){
                    contribuyente.addAll(listOf(tipoContribuyente ,"Pequeño Contribuyente", "Mediano Contribuyente", "Gran Contribuyente")) //LIMINADO "FACTURA EXPORTACION"
                }else{
                    contribuyente.addAll(listOf("-- SELECCIONE --" ,"Pequeño Contribuyente", "Mediano Contribuyente", "Gran Contribuyente")) //LIMINADO "FACTURA EXPORTACION"
                }
                binding.spContribuyente.adapter = contribuyente
            }catch (e:Exception){
                throw Exception(e.message)
            }
        }


    }


    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(vista : String){
        val dialog = AlertDialog.Builder(this@NuevoCliente)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                if(vista == "editar"){
                    regresarClienteDetalle()
                }else{
                    regresarMenuClientes()
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

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeRegistrado(registrado : Boolean){
        val mensaje = if(registrado){
            if(vista=="editar"){
                "CLIENTE ACTUALIZADO CORRECTAMENTE"
            }else{
                "CLIENTE REGISTRADO CORRECTAMENTE"
            }
        }else{
            "ERROR AL REGISTRAR EL CLIENTE"
        }
        val dialog = AlertDialog.Builder(this@NuevoCliente)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                if(registrado){
                    view.dismiss()
                    if(vista == "editar"){
                        regresarClienteDetalle()
                    }else{
                        regresarMenuClientes()
                    }

                }else{
                    view.dismiss()
                }
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun regresarMenuClientes() {
        val intent = Intent(this@NuevoCliente, Clientes::class.java)
        startActivity(intent)
        finish()
    }

    private fun regresarClienteDetalle(){
        val intent = Intent(this, ClientesDetalle::class.java)
        intent.putExtra("idcliente", idcliente)
        startActivity(intent)
        finish()
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
                    latitudEditada = location.latitude.toString()
                    longitudEditada = location.longitude.toString()
                } ?: run {
                    latitudEditada = 0.toString()
                    longitudEditada = 0.toString()
                }
            }
            .addOnFailureListener { e ->
                // ERROR AL NO OBTENER LA UBICACION
                Toast.makeText(this, "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}