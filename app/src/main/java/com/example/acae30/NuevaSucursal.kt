package com.example.acae30

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.acae30.controllers.CatalogosController
import com.example.acae30.controllers.SucursalesController
import com.example.acae30.databinding.ActivityNuevaSucursalBinding
import com.example.acae30.modelos.SucursalModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevaSucursal : AppCompatActivity() {

    private lateinit var binding : ActivityNuevaSucursalBinding
    private var idcliente = 0
    private var catalogoController = CatalogosController()
    private var funciones = Funciones()
    private var sucursalesController = SucursalesController()

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

    private var latitud = ""
    private var longitud = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaSucursalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idcliente = intent.getIntExtra("idcliente", 0)
        latitud = intent.getStringExtra("latitud").toString()
        longitud = intent.getStringExtra("longitud").toString()

        cargarPais()

        cargarDepartamento()

        cargarMunicipio()

        cargarDistrito()

        cargarRutas()

    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            mensajeCancelar()
        }

        binding.btnaceptar.setOnClickListener {
            if(!funciones.isInternetAvailable(this@NuevaSucursal)){
                funciones.mensaje(this@NuevaSucursal, "CONEXION DE INTERNET INESTABLE")
            }else{
                if(binding.txtCodigoSucursal.text!!.isEmpty() || binding.txtCodigoSucursal.text!!.length < 2 || binding.txtNombreSucursal.text!!.isEmpty() || binding.txtNombreSucursal.text!!.length < 5){
                    Toast.makeText(this,"VERIFIQUE EL CODIGO O EL NOMBRE DE LA SUCURSAL", Toast.LENGTH_LONG).show()
                }else{
                    CoroutineScope(Dispatchers.IO).launch {
                        registrarsucursal()
                    }
                }
            }
        }

        //SETEANDO LA LONGITUD Y LATITUD DEL CLIENTE
        binding.txtLatitud.setText(latitud)
        binding.txtLongitud.setText(longitud)

        //IMPLEMENTANDO LOGICA DEL PAIS SELECCIONADO
        binding.spPais.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                pais = parent?.getItemAtPosition(position).toString()

                this@NuevaSucursal.lifecycleScope.launch {
                    try{
                        codigoPais = catalogoController.obtenerInformacionPais(this@NuevaSucursal, pais)!!.codigo

                        cargarDepartamento()
                        cargarMunicipio()
                        cargarDistrito()
                    }catch (e: Exception){
                        throw Exception(e.message)
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

                this@NuevaSucursal.lifecycleScope.launch {
                    try{
                        codigoDepto = if(departamento == "-- SELECCIONE --"){
                            "00"
                        }else{
                            catalogoController.obtenerInformacionDepartamento(this@NuevaSucursal, departamento)!!.codigo
                        }

                        cargarMunicipio()
                        cargarDistrito()
                    }catch (e: Exception){
                        throw Exception(e.message)
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

                this@NuevaSucursal.lifecycleScope.launch {
                    try{
                        codigoMuni = if(municipio == "-- SELECCIONE --"){
                            "00"
                        }else{
                            catalogoController.obtenerInformacionMunicipio(this@NuevaSucursal, municipio)!!.codigo
                        }

                        cargarDistrito()
                    }catch (e: Exception){
                        throw Exception(e.message)
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

                this@NuevaSucursal.lifecycleScope.launch {
                    try{
                        codigoDistri = if(distrito == "-- SELECCIONE --"){
                            "00"
                        }else{
                            catalogoController.obtenerInformacionDistrito(this@NuevaSucursal, distrito)!!.codigo
                        }
                    }catch (e: Exception){
                        throw Exception(e.message)
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

                this@NuevaSucursal.lifecycleScope.launch {
                    try{
                        idRuta = if(ruta == "-- SELECCIONE --"){
                            0
                        }else{
                            catalogoController.obtenerInformacionRuta(this@NuevaSucursal, ruta)!!.id
                        }
                    }catch (e: Exception){
                        throw Exception(e.message)
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

    }

    private suspend fun registrarsucursal() {
        var registrado : Boolean = false

        val sucursal : SucursalModel = SucursalModel(
            0,
            idcliente,
            binding.txtCodigoSucursal.text.toString(),
            binding.txtNombreSucursal.text.toString(),
            binding.txtDireccion.text.toString(),
            municipio,
            departamento,
            binding.txtTelefono.text.toString(),
            binding.txtTelefono.text.toString(),
            binding.txtCorreo.text.toString(),
            binding.txtContacto.text.toString(),
            idRuta,
            ruta,
            codigoDepto,
            codigoMuni,
            codigoPais,
            binding.txtDireccion.text.toString(),
            pais,
            binding.txtTelefono.text.toString(),
            binding.txtCorreo.text.toString(),
            codigoDistri,
            distrito,
            latitud,
            longitud
        )

        registrado = sucursalesController.enviarRegistroSucursalAlServidor(this@NuevaSucursal, sucursal)

        withContext(Dispatchers.Main){
            mensajeRegistrado(registrado)
        }

    }

    private fun cargarPais(){
        this@NuevaSucursal.lifecycleScope.launch {
            try {
                val listaPais = catalogoController.obtenerListadoPaisesSQLite(this@NuevaSucursal, "","")

                val adaptadorPais = ArrayAdapter(this@NuevaSucursal, android.R.layout.simple_spinner_item, listaPais)
                adaptadorPais.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spPais.adapter = adaptadorPais

                binding.spPais.setSelection(60, true)

            }catch (e:Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarDepartamento(){
        this@NuevaSucursal.lifecycleScope.launch {
            try{
                val listaDepartamentos = catalogoController.obtenerListadoDepartamentosSQLite(this@NuevaSucursal, codigoPais,"","")

                val departamento = ArrayAdapter(this@NuevaSucursal, android.R.layout.simple_spinner_item, listaDepartamentos)
                departamento.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spDepartamento.adapter = departamento
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarMunicipio(){
        this@NuevaSucursal.lifecycleScope.launch {
            try{
                val listaMunicipos = catalogoController.obtenerListadoMunicipiosSQLite(this@NuevaSucursal, codigoPais, codigoDepto,"","")

                val adaptadorMunicipio = ArrayAdapter(this@NuevaSucursal, android.R.layout.simple_spinner_item, listaMunicipos)
                adaptadorMunicipio.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spMunicipio.adapter = adaptadorMunicipio
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarDistrito(){
        this@NuevaSucursal.lifecycleScope.launch {
            try{
                val listaDistritos = catalogoController.obtenerListadoDistritosSQLite(this@NuevaSucursal, codigoDepto, codigoMuni, codigoPais,"","")

                val distritos = ArrayAdapter(this@NuevaSucursal, android.R.layout.simple_spinner_item, listaDistritos)
                distritos.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spDistrito.adapter = distritos
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    private fun cargarRutas(){
        this@NuevaSucursal.lifecycleScope.launch {
            try{
                val listaRustas = catalogoController.obtenerListadoRutaSQLite(this@NuevaSucursal,"","", false)

                val rutas = ArrayAdapter(this@NuevaSucursal, android.R.layout.simple_spinner_item, listaRustas)
                rutas.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spRuta.adapter = rutas
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(){
        val dialog = AlertDialog.Builder(this@NuevaSucursal)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                regresarMenuSucursales()
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
            "SUCURSAL REGISTRADA CORRECTAMENTE"
        }else{
            "ERROR AL REGISTRAR LA SUCURSAL"
        }
        val dialog = AlertDialog.Builder(this@NuevaSucursal)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                if(registrado){
                    view.dismiss()
                    regresarMenuSucursales()
                }else{
                    view.dismiss()
                }
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun regresarMenuSucursales(){
        val intent = Intent(this@NuevaSucursal, Sucursales::class.java)
        intent.putExtra("idcliente", idcliente)
        startActivity(intent)
        finish()
    }
}