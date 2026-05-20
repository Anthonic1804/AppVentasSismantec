package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.acae30.DAO.InventarioDao
import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioPreciosEntity
import com.example.acae30.Retrofit.RetrofitCliente
import com.example.acae30.controllers.CatalogosController
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.HojaCargaController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.controllers.PedidosController
import com.example.acae30.database.AppDatabase
import com.example.acae30.databinding.ActivityCargaDatosBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate

class carga_datos : AppCompatActivity() {

    private lateinit var url: String
    private var idVendedor = 0
    private var alert: AlertDialogo? = null
    private var inventarioController = InventarioController()
    private var clientesController = ClientesController()
    private var pedidosController = PedidosController()
    private var catalagosController = CatalogosController()
    private var hojaController = HojaCargaController()
    private var funciones = Funciones()

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private lateinit var binding : ActivityCargaDatosBinding
    private var rutaClientes : String = "T"
    private var validarHoja: Boolean = false

    private var M_CxC: Boolean = false
    private var inventarioTiempoReal : Boolean = false


    //private lateinit var inventarioDao: InventarioDao

    //----------------------------------
    //Instancia de la bd para Room
    //----------------------------------
    private lateinit var db : AppDatabase

    private var isProcessing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCargaDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarResponsiveMenu()

        //--------------------------------
        //Inicializando la instancia de la bd
        //--------------------------------
        db = AppDatabase.getInstance(this@carga_datos)

        preferences = this@carga_datos.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        alert = AlertDialogo(this@carga_datos, this)

        url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), this@carga_datos)

        idVendedor = preferences.getInt("Idvendedor", 0)

        rutaClientes = preferences.getString("cargarClientesPorRuta", "T").toString()
        validarHoja = preferences.getBoolean("validarHojaCarga", false)
        M_CxC = preferences.getBoolean("M_CxC", false)
        inventarioTiempoReal = preferences.getBoolean("inventarioTiempoReal", false)


        var multiplesHojaDeCarga = preferences.getBoolean("multiplesHojaDeCarga", false)
        println("MULTIPLES HOJAS ACTIVAS -> " + multiplesHojaDeCarga)

    }

    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {
            val intento = Intent(this@carga_datos, Inicio::class.java)
            startActivity(intento)
            finish()
        }

        binding.cvClientes.setOnClickListener {

            if(isProcessing) return@setOnClickListener

            deshabilitarOpciones()

            if (funciones.isInternetAvailable(this@carga_datos)) {
                when(rutaClientes){
                    "R" -> {
                        cargarClientesRuta()
                    }else -> {
                        cargaClientes()
                    }
                }
            } else {
                habilitarOpciones()
                funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
            }
        }

        binding.cvInventario.setOnClickListener {

            if(isProcessing) return@setOnClickListener

            deshabilitarOpciones()

            if(inventarioTiempoReal){
                funciones.mensaje(this@carga_datos, "ESTÁ TRABAJANDO CON INVENTARIO EN TIEMPO REAL")
            }else{
                val hojaCarga = preferences.getBoolean("Hoja_carga_inventario_app", false)
                if (funciones.isInternetAvailable(this@carga_datos)) {
                    alert!!.Cargando()

                    if(!hojaCarga){
                        /*
                        * Si la Hoja de Carga está desactivada en SQL Server
                        * Carga el inventario Completo
                        * */
                        CoroutineScope(Dispatchers.IO).launch {

                            delay(1000)

                            withContext(Dispatchers.Main){
                                alert!!.changeText("CARGANDO INFORMACION DE INVENTARIO")
                            }

                            delay(1000)

                            try {
                                /*val bd = funciones.obtenerInstancia(this@carga_datos).openHelper.writableDatabase
                                bd.execSQL("DELETE FROM Inventario")
                                bd.execSQL("DELETE FROM Inventario_precios")
                                bd.execSQL("DELETE FROM Inventario_unidades")
                                bd.execSQL("DELETE FROM inventario_lotes")
                                bd.execSQL("DELETE FROM lineas")*/

                                inventarioController.limpiandoTablasInventario(this@carga_datos)

                                delay(2000)

                                inventarioController.obtenerInventarioGeneral(this@carga_datos, alert!!)

                            }catch (e:Exception){
                                println("ERROR AL CARGAR LA INFORMACION DE INVENTARIO " + e.message)
                            }

                            delay(1000)

                            withContext(Dispatchers.Main){
                                alert!!.changeText("CARGANDO ESCALAS DE PRECIOS")
                            }

                            delay(1000)

                            try {
                                //inventarioController.obtenerEscalasPrecios(this@carga_datos)
                                inventarioController.obtenerInventarioPrecios(this@carga_datos, alert!!)
                            }catch (e:Exception){
                                println("ERROR AL CARGAR LAS ESCALAS DE INVENTARIO " + e.message)
                            }

                            delay(1000)

                            withContext(Dispatchers.Main){
                                alert!!.changeText("CARGANDO UNIDADES DE MEDIDA")
                            }

                            delay(1000)

                            try{
                                //inventarioController.obtenerUnidadesMedidaServidor(this@carga_datos)
                                inventarioController.obtenerInventarioUnidades(this@carga_datos, alert!!)
                            }catch (e:Exception){
                                println("ERROR AL OBTENER LAS UNIDADES DE MEDIDA -> " + e.message)
                            }

                            delay(1000)

                            withContext(Dispatchers.Main){
                                alert!!.changeText("CARGANDO INVENTARIO LOTES")
                            }

                            delay(1000)

                            try {
                                inventarioController.obtenerInventarioLotes(this@carga_datos, alert!!)
                            }catch (e:Exception){
                                println("ERROR AL OBTENER LOS LOTES DEL INVENTARIO -> ${e.message}")
                            }

                            delay(1000)

                            withContext(Dispatchers.Main){
                                alert!!.changeText("CARGANDO LINEAS")
                            }

                            delay(1000)

                            try {
                                inventarioController.obtenerListadoLineasServidor(this@carga_datos)
                            }catch (e: Exception){
                                Timber.e(e, "[CARGAR_DATOS] ERROR AL CARGAR LAS LINEAS DESDE EL SERVIDOR")
                            }

                            delay(1000)

                            withContext(Dispatchers.Main){
                                alert!!.changeText("INVENTARIO CARGADO CORRECTAMENTE")
                            }

                            delay(1000)

                            try {
                                inventarioController.obtenerFechaInventario(this@carga_datos)
                            }catch (e:Exception){
                                println("ERROR AL CARGAR LA FECHA DE INVENTARIO " + e.message)
                            }

                            //FIN DA LA CARGA DE DATOS
                            delay(1500)

                            withContext(Dispatchers.Main){

                                habilitarOpciones()

                                alert!!.dismisss()
                            }

                        }
                    }else{
                        /*
                        * Si está Activa solamente cargar el inventario de dicha hoja
                        * */
                        ingresarHojaCarga()

                        alert!!.dismisss()
                    }

                } else {
                    habilitarOpciones()
                    funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
                }
            }

        }

        binding.cvCatalogos.setOnClickListener {

            if(isProcessing) return@setOnClickListener

            deshabilitarOpciones()

            lifecycleScope.launch {
                if (funciones.isInternetAvailable(this@carga_datos)) {

                    runOnUiThread {
                        alert!!.Cargando()
                    }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CARGANDO CATALOGO PRINCIPALES") }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CARGANDO CATALOGO PAISES") }

                    try {
                        catalagosController.obtenerCatalogoPais(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE PAISES " + e.message)
                    }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CARGANDO CATALOGO DEPARTAMENTOS") }

                    try {
                        catalagosController.obtenerCatalogoDepartamento(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE DEPARTAMENTOS " + e.message)
                    }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CARGANDO CATALOGO MUNICIPIOS") }

                    try {
                        catalagosController.obtenerCatalogoMunicipio(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE MUNICIPIOS " + e.message)
                    }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CARGANDO CATALOGO DISTRITOS") }

                    try {
                        catalagosController.obtenerCatalogoDistrito(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE DISTRITOS " + e.message)
                    }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CARGANDO CATALOGO ACTIVIDADES ECONOMICAS") }

                    try {
                        catalagosController.obtenerCatalogoGiro(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE ACTIVIDADES ECONOMICAS " + e.message)
                    }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CARGANDO CATALOGO RUTAS") }

                    try {
                        catalagosController.obtenerCatalogoRuta(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE RUTAS " + e.message)
                    }

                    delay(1000)

                    runOnUiThread { alert!!.changeText("CATALOGOS CARGADOS EXITOSAMENTE") }

                    //FIN DA LA CARGA DE DATOS
                    delay(1500)

                    runOnUiThread {
                        habilitarOpciones()

                        alert!!.dismisss()
                    }
                } else {
                    habilitarOpciones()
                    runOnUiThread { funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta) }
                }
            }
        }

        binding.cvEliminarPedidos.setOnClickListener {

            if(isProcessing) return@setOnClickListener

            deshabilitarOpciones()

            /*Toast.makeText(this@carga_datos, "FUNCION EN VERIFICACION", Toast.LENGTH_SHORT)
                .show()*/

            try {
                this@carga_datos.lifecycleScope.launch {
                    val eliminados = pedidosController.eliminarPedidosAntiguos(this@carga_datos, false)
                    if (eliminados){
                        withContext(Dispatchers.Main){
                            habilitarOpciones()
                            funciones.mostrarMensaje("PEDIDOS ELIMINADOS", this@carga_datos, binding.vistaalerta)
                        }
                    }else{
                        withContext(Dispatchers.Main){
                            habilitarOpciones()
                            funciones.mostrarAlerta("NO SE ENCONTRARON PEDIDOS", this@carga_datos, binding.vistaalerta)
                        }
                    }
                }

            } catch (e: Exception) {
                habilitarOpciones()
                funciones.mostrarAlerta("ERROR AL ELIMINAR LOS PEDIDOS -> ${e.message}", this@carga_datos, binding.vistaalerta)
            }

        }
    }

    //FUNCIONES PARA HABILITAR Y DESHABILITAR LAS OPCIONES
    private fun deshabilitarOpciones(){
        isProcessing = true

        binding.apply {
            cvClientes.isEnabled = false
            cvCatalogos.isEnabled = false
            cvInventario.isEnabled = false
            cvEliminarPedidos.isEnabled = false
        }
    }

    private fun habilitarOpciones(){

        isProcessing = false

        binding.apply {
            cvClientes.isEnabled = true
            cvCatalogos.isEnabled = true
            cvInventario.isEnabled = true
            cvEliminarPedidos.isEnabled = true
        }

    }

    //FUNCIONES PARA CARGA DE CLIENTES
    private fun cargaClientes(){
        alert!!.Cargando()

        CoroutineScope(Dispatchers.IO).launch {

            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("CARGANDO INFORMACION DE CLIENTES")
            }

            delay(1000)

            try {
                clientesController.obtenerClientesServidor(this@carga_datos)
            }catch (e:Exception){
                println("ERROR AL OBTENER LOS CLIENES -> " + e.message)
            }

            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("CARGANDO INFORMACION DE SUCURSALES")
            }

            delay(1000)

            try {
                clientesController.obtenerClienteSucursalesServidor(this@carga_datos)
            }catch (e:Exception){
                println("ERROR AL OBTENER LAS SUCURSALES -> " + e.message)
            }

            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("CARGANDO PRECIOS PERSONALIZADOS")
            }

            delay(1000)

            try {
                //clientesController.obtenerPreciosPersonalizados(this@carga_datos)
                clientesController.obtenerClientesPrecios(this@carga_datos, alert!!)
            }catch (e: Exception){
                println("ERROR AL OBTENER LOS PRECIOS PERSONALIZADO")
            }

            delay(1000)

            if(M_CxC){
                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO CUENTAS POR COBRAR")
                }

                delay(1000)

                try {
                    clientesController.obtenerCxcServidor(this@carga_datos)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LAS CXC -> " + e.message)
                }

                delay(1000)
            }

            withContext(Dispatchers.Main){
                alert!!.changeText("INFORMACION DE CLIENTES CARGADA CORRECTAMENTE")
            }

            //FIN DA LA CARGA DE DATOS
            delay(1500)

            withContext(Dispatchers.Main){

                habilitarOpciones()

                alert!!.dismisss()
            }

        }
    }

    //FUNCION PARA MOSTRAR EL DIALOG DE INGRESO DE HOJA DE CARGA
    private fun ingresarHojaCarga() {

        var procesando = false

        val hojaCargaDialog = Dialog(this, R.style.Theme_Dialog)
        hojaCargaDialog.setCancelable(false)

        hojaCargaDialog.setContentView(R.layout.hoja_carga)

        val btnAceptar = hojaCargaDialog.findViewById<TextView>(R.id.tvCargar)
        val btnCancelar = hojaCargaDialog.findViewById<TextView>(R.id.tvCancelar)

        btnAceptar.setOnClickListener {

            if(procesando) return@setOnClickListener

            procesando = true

            btnAceptar.isEnabled = false
            btnCancelar.isEnabled = false

            //val hojaCargaActiva = preferences.getInt("hojaCarga", 0)
            val numero = hojaCargaDialog.findViewById<TextInputEditText>(R.id.tietNumeroCarga).text.toString()
            val numeroHoja = numero.ifEmpty {
                0
            }

            val hojaYaRegistrada = hojaController.verificarHojaCargaIngresada(this@carga_datos, numeroHoja.toString().toInt())

            if (numeroHoja.toString().toInt() == 0) {

                habilitarOpciones()

                hojaCargaDialog.dismiss()
                funciones.mensaje(this@carga_datos, "INGRESE UN NUMERO DE HOJA DE CARGA")

                procesando = false
                btnAceptar.isEnabled = true
                btnCancelar.isEnabled = true

            }else if(hojaYaRegistrada){

                habilitarOpciones()

                hojaCargaDialog.dismiss()
                funciones.mensaje(this@carga_datos, "LA HOJA DE CARGA YA SE ENCUENTRA CARGADA")

                procesando = false
                btnAceptar.isEnabled = true
                btnCancelar.isEnabled = true

            }else {

                cargarInventarioDesdeHoja(numero)

                habilitarOpciones()

                hojaCargaDialog.dismiss()
            }
        }

        btnCancelar.setOnClickListener {

            habilitarOpciones()

            hojaCargaDialog.dismiss()
        }

        hojaCargaDialog.show()

    }

    private fun cargarInventarioDesdeHoja(numero: String) {

        lifecycleScope.launch {
            var hojaRegistrada: Int = 0

            withContext(Dispatchers.IO){

                withContext(Dispatchers.Main){
                    alert!!.Cargando()
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO INVENTARIO...")
                }

                try {
                    //OBTENIENDO INVENTARIO DESDE HOJA DE CARGA
                    hojaRegistrada = inventarioController.obtenerInventarioHojaCarga(false, numero.toInt(), idVendedor, this@carga_datos)
                }catch (e:Exception){
                    println("ERROR AL CARGAR LA HOJA DE INVENTARIO " + e.message)
                }

                if(hojaRegistrada == 1){
                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("OBTENIENDO ESCALAS DE PRECIOS")
                    }

                    delay(1000)

                    try {
                        //OBTENIENDO ESCALAS DE PRECIOS
                        inventarioController.obtenerInventarioPrecios(this@carga_datos, alert!!)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR LAS ESCALAS DE PRECIOS " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO UNIDADES DE MEDIDA")
                    }

                    delay(1000)

                    try{
                        inventarioController.obtenerInventarioUnidades(this@carga_datos, alert!!)
                    }catch (e:Exception){
                        println("ERROR AL OBTENER LAS UNIDADES DE MEDIDA -> " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO INVENTARIO LOTES")
                    }

                    delay(1000)

                    try{
                        inventarioController.obtenerInventarioLotes(this@carga_datos, alert!!)
                    }catch (e:Exception){
                        println("ERROR AL OBTENER INVENTARIO LOTES -> " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO LINEAS")
                    }

                    delay(1000)

                    try {
                        inventarioController.obtenerListadoLineasServidor(this@carga_datos)
                    }catch (e: Exception){
                        Timber.e(e, "[CARGAR_DATOS] ERROR AL CARGAR LAS LINEAS DESDE EL SERVIDOR")
                    }

                    delay(1000)

                    if(hojaRegistrada == 1){
                        withContext(Dispatchers.Main){
                            alert!!.dismisss()
                            mensajeInventarioHoja("INVENTARIO REGISTRADO CORRECTAMENTE", hojaRegistrada)
                        }
                    }

                }else{
                    withContext(Dispatchers.Main){
                        alert!!.dismisss()
                        mensajeInventarioHoja("NO SE ENCONTRO LA HOJA DE CARGA", hojaRegistrada)
                    }
                }

            }

        }

    }

    //FUNCION PARA VALIDAR HOJA
    private fun validarHojaCarga(){
        val intent = Intent(this@carga_datos, ValidarHojaCarga::class.java)
        startActivity(intent)
        finish()
    }

    //FUNCION PARA MOSTRAR EL DIALOG DE CARGA DE CLIENTES POR RUTA
    private fun cargarClientesRuta() {

        var procesando = false

        val clientesRuta = Dialog(this, R.style.Theme_Dialog)
        clientesRuta.setCancelable(false)

        clientesRuta.setContentView(R.layout.cargar_cliente_ruta)

        val btnAceptar = clientesRuta.findViewById<TextView>(R.id.btnCargar)
        val btnCancelar = clientesRuta.findViewById<TextView>(R.id.btnCancelar)
        val spRuta = clientesRuta.findViewById<Spinner>(R.id.spRuta)

        var rutaSeleccionada : String = "-- SELECCIONE --"
        var idRutaSeleccionada : Int = 0


        this.lifecycleScope.launch {
            try{
                val listaRustas = catalagosController.obtenerListadoRutaSQLite(this@carga_datos, "", "", false)

                val rutas = ArrayAdapter(this@carga_datos, android.R.layout.simple_spinner_item, listaRustas)
                rutas.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                spRuta.adapter = rutas
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }

        //IMPLEMENTANDO LOGICA DEL RUTA SELECCIONADO
        spRuta.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                rutaSeleccionada = parent?.getItemAtPosition(position).toString()

                this@carga_datos.lifecycleScope.launch {
                    try{
                        idRutaSeleccionada = if(rutaSeleccionada == "-- SELECCIONE --"){
                            0
                        }else{
                            catalagosController.obtenerInformacionRuta(this@carga_datos, rutaSeleccionada)!!.id
                        }

                        preferences.edit {
                            putInt("idRutaSeleccionada", 0)
                            putString("rutaSeleccionada", "")
                            putInt("idRutaSeleccionada", idRutaSeleccionada)
                            putString("rutaSeleccionada", rutaSeleccionada)
                        }

                    }catch (e: Exception){
                        println("ERROR AL CARGAR LA RUTA DEL CIENTE " + e.message)
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnAceptar.setOnClickListener {

            if(procesando) return@setOnClickListener

            procesando = true

            btnAceptar.isEnabled = false
            btnCancelar.isEnabled = false

            if(rutaSeleccionada == "-- SELECCIONE --") {
                Toast.makeText(this, "DEBE DE SELECCIONAR UNA RUTA", Toast.LENGTH_SHORT)
                    .show()

                procesando = false

                btnAceptar.isEnabled = true
                btnCancelar.isEnabled = true
            }else{

                habilitarOpciones()

                clientesRuta.dismiss()
                cargaClientes()
            }
        }

        btnCancelar.setOnClickListener {

            habilitarOpciones()

            clientesRuta.dismiss()
        }

        clientesRuta.show()

    }



    private fun messageAsync(mensaje: String) {
        if (alert != null) {
            runOnUiThread {
                alert!!.changeText(mensaje)
            }
        }
    }//muestra la carga del mensaje de forma asincrona

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
      //  super.onBackPressed()

    //   finish()
    }//anula el boton atras

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeInventarioHoja(mensaje: String, hojaRegistrada: Int){
        val dialog = AlertDialog.Builder(this@carga_datos)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()

                if(validarHoja && hojaRegistrada == 1){
                    validarHojaCarga()
                }

            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //CONFIGURACION EL RESPONSIVE DEL MENU
    private fun configurarResponsiveMenu(){
        val flow = binding.flow
        val orientation = resources.configuration.orientation

        val cantidad = when(orientation){
            Configuration.ORIENTATION_LANDSCAPE -> 4
            else -> 2
        }

        flow.setMaxElementsWrap(cantidad)
    }

}