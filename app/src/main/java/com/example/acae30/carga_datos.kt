package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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


    private lateinit var inventarioDao: InventarioDao

    private lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCargaDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this@carga_datos)

        preferences = this@carga_datos.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        alert = AlertDialogo(this@carga_datos, this)

        url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), this@carga_datos)

        idVendedor = preferences.getInt("Idvendedor", 0)

        rutaClientes = preferences.getString("cargarClientesPorRuta", "T").toString()
        validarHoja = preferences.getBoolean("validarHojaCarga", false)

    }

    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {
            val intento = Intent(this@carga_datos, Inicio::class.java)
            startActivity(intento)
            finish()
        }

        binding.cvClientes.setOnClickListener {
            if (funciones.isInternetAvailable(this@carga_datos)) {
                when(rutaClientes){
                    "R" -> {
                        cargarClientesRuta()
                    }else -> {
                        cargaClientes()
                    }
                }
            } else {
                funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
            }
        }

        binding.cvInventario.setOnClickListener {
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
                            val bd = funciones.obtenerInstancia(this@carga_datos).openHelper.writableDatabase
                            bd.execSQL("DELETE FROM Inventario")
                            bd.execSQL("DELETE FROM Inventario_precios")
                            bd.execSQL("DELETE FROM Inventario_unidades")

                            getInventario()

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
                            obtenerEscalasPrecios()
                        }catch (e:Exception){
                            println("ERROR AL CARGAR LAS ESCALAS DE INVENTARIO " + e.message)
                        }

                        delay(1000)

                        withContext(Dispatchers.Main){
                            alert!!.changeText("CARGANDO UNIDADES DE MEDIDA")
                        }

                        delay(1000)

                        try{
                            inventarioController.obtenerUnidadesMedidaServidor(this@carga_datos)
                        }catch (e:Exception){
                            println("ERROR AL OBTENER LAS UNIDADES DE MEDIDA -> " + e.message)
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
                funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
            }
        }

        binding.cvCatalogos.setOnClickListener {
            if (funciones.isInternetAvailable(this@carga_datos)) {
                alert!!.Cargando()
                CoroutineScope(Dispatchers.IO).launch {

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO CATALOGO PRINCIPALES")
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO CATALOGO PAISES")
                    }

                    try {
                        catalagosController.obtenerCatalogoPais(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE PAISES " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO CATALOGO DEPARTAMENTOS")
                    }

                    try {
                        catalagosController.obtenerCatalogoDepartamento(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE DEPARTAMENTOS " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO CATALOGO MUNICIPIOS")
                    }

                    try {
                        catalagosController.obtenerCatalogoMunicipio(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE MUNICIPIOS " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO CATALOGO DISTRITOS")
                    }

                    try {
                        catalagosController.obtenerCatalogoDistrito(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE DISTRITOS " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO CATALOGO ACTIVIDADES ECONOMICAS")
                    }

                    try {
                        catalagosController.obtenerCatalogoGiro(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE ACTIVIDADES ECONOMICAS " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CARGANDO CATALOGO RUTAS")
                    }

                    try {
                        catalagosController.obtenerCatalogoRuta(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR EL CATALOGO DE RUTAS " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.changeText("CATALOGOS CARGADOS EXITOSAMENTE")
                    }

                    //FIN DA LA CARGA DE DATOS
                    delay(1500)

                    withContext(Dispatchers.Main){
                        alert!!.dismisss()
                    }
                }
            } else {
                funciones.mostrarAlerta("ENCIENDE TUS DATOS O EL WIFI", this@carga_datos, binding.vistaalerta)
            }
        }

        binding.cvEliminarPedidos.setOnClickListener {

            /*Toast.makeText(this@carga_datos, "FUNCION EN VERIFICACION", Toast.LENGTH_SHORT)
                .show()*/

            try {
                this@carga_datos.lifecycleScope.launch {
                    val eliminados = pedidosController.eliminarPedidosAntiguos(this@carga_datos, false)
                    if (eliminados){
                        withContext(Dispatchers.Main){
                            funciones.mostrarMensaje("PEDIDOS ELIMINADOS", this@carga_datos, binding.vistaalerta)
                        }
                    }else{
                        withContext(Dispatchers.Main){
                            funciones.mostrarAlerta("NO SE ENCONTRARON PEDIDOS", this@carga_datos, binding.vistaalerta)
                        }
                    }
                }

            } catch (e: Exception) {
                funciones.mostrarAlerta("ERROR AL ELIMINAR LOS PEDIDOS -> ${e.message}", this@carga_datos, binding.vistaalerta)
            }

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
                clientesController.obtenerPreciosPersonalizados(this@carga_datos)
            }catch (e: Exception){
                println("ERROR AL OBTENER LOS PRECIOS PERSONALIZADO")
            }

            delay(1000)

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

            withContext(Dispatchers.Main){
                alert!!.changeText("INFORMACION DE CLIENTES CARGADA CORRECTAMENTE")
            }

            //FIN DA LA CARGA DE DATOS
            delay(1500)

            withContext(Dispatchers.Main){
                alert!!.dismisss()
            }

        }
    }

    //FUNCION PARA MOSTRAR EL DIALOG DE INGRESO DE HOJA DE CARGA
    private fun ingresarHojaCarga() {
        val hojaCargaDialog = Dialog(this, R.style.Theme_Dialog)
        hojaCargaDialog.setCancelable(false)

        hojaCargaDialog.setContentView(R.layout.hoja_carga)

        val btnAceptar = hojaCargaDialog.findViewById<TextView>(R.id.tvCargar)
        val btnCancelar = hojaCargaDialog.findViewById<TextView>(R.id.tvCancelar)

        btnAceptar.setOnClickListener {

            //val hojaCargaActiva = preferences.getInt("hojaCarga", 0)
            val numero = hojaCargaDialog.findViewById<TextInputEditText>(R.id.tietNumeroCarga).text.toString()

            val hojaYaRegistrada = hojaController.verificarHojaCargaIngresada(this@carga_datos, numero.trim().toInt())

            if (numero.isEmpty() || numero.toInt() == 0) {

                hojaCargaDialog.dismiss()
                funciones.mensaje(this@carga_datos, "INGRESE UN NUMERO DE HOJA DE CARGA")

            }else if(hojaYaRegistrada){

                hojaCargaDialog.dismiss()
                funciones.mensaje(this@carga_datos, "LA HOJA DE CARGA YA SE ENCUENTRA CARGADA")

            }else {

                cargarInventarioDesdeHoja(numero)

                hojaCargaDialog.dismiss()
            }
        }

        btnCancelar.setOnClickListener {
            hojaCargaDialog.dismiss()
        }

        hojaCargaDialog.show()

    }

    private fun cargarInventarioDesdeHoja(numero: String) {

        lifecycleScope.launch {
            var hojaRegistrada: Int = 0

            withContext(Dispatchers.IO){

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

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("OBTENIENDO ESCALAS DE PRECIOS")
                }

                delay(1000)

                try {
                    //OBTENIENDO ESCALAS DE PRECIOS
                    inventarioController.obtenerEscalasPrecios(this@carga_datos)
                }catch (e:Exception){
                    println("ERROR AL CARGAR LAS ESCALAS DE PRECIOS " + e.message)
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO UNIDADES DE MEDIDA")
                }

                delay(1000)

                try{
                    inventarioController.obtenerUnidadesMedidaServidor(this@carga_datos)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LAS UNIDADES DE MEDIDA -> " + e.message)
                }


                delay(1000)
            }

            mensajeInventarioHoja("INVENTARIO REGISTRADO CORRECTAMENTE", hojaRegistrada)
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
            if(rutaSeleccionada == "-- SELECCIONE --") {
                Toast.makeText(this, "DEBE DE SELECCIONAR UNA RUTA", Toast.LENGTH_SHORT)
                    .show()
            }else{
                clientesRuta.dismiss()
                cargaClientes()
            }
        }

        btnCancelar.setOnClickListener {
            clientesRuta.dismiss()
        }

        clientesRuta.show()

    }

    //FUNCION PARA LA LECTURA DEL ENDPOINT DE INVENTARIO
   private suspend fun getInventario() = withContext(Dispatchers.IO) {
        val baseUrl = url
        inventarioDao = db.inventarioDao()
        val api = RetrofitCliente.obtenerApi(baseUrl)

        val limite = 1000
        var offset = 0
        var hayMas = true
        var totalInsertados = 0

        try {

            val totalRegistros = try {
                api.obtenerTotalRegistrosInventario()
            }catch (e: Exception){
                println("No se pudo obtener el total de inventario -> ${e.message}")
                null
            }

            while (hayMas) {
                val respuesta = api.obtenerInventario(offset, limite)

                if (respuesta.isNotEmpty()) {
                    val entidades = respuesta.map {
                        InventarioEntity(
                            id = it.id,
                            codigo = it.codigo ?: "",
                            codigo_de_barra = it.codigo_de_barra ?: " ",
                            tipo = it.tipo ?: "",
                            descripcion = it.descripcion ?: "",
                            unidad_medida = it.unidad_medida ?: " ",
                            fraccion = it.fraccion ?: 0f,
                            nombre_fraccion = it.nombre_fraccion ?: " ",
                            costo = it.costo ?: 0f,
                            costo_iva = it.costo_iva ?: 0f,
                            ult_costo = it.ult_costo ?: 0f,
                            ult_costo_iva = it.ult_costo_iva ?: 0f,
                            existencia = it.existencia ?: 0f,
                            existencia_u = it.existencia_u ?: 0f,
                            precio = it.precio ?: 0f,
                            precio_u = it.precio_u ?: 0f,
                            precio_u_iva = it.precio_u_iva ?: 0f,
                            precio_iva = it.precio_iva ?: 0f,
                            bonificado = it.bonificado ?: 0f,
                            lote = it.lote ?: " ",
                            fecha_vencimiento = it.fecha_vencimiento ?: " ",
                            precio2 = it.precio2 ?: 0f,
                            precio2_iva = it.precio2_iva ?: 0f,
                            precio_u2 = it.precio_u2 ?: 0f,
                            precio_u2_iva = it.precio_u2_iva ?: 0f,
                            precio_viñeta = it.precio_viñeta ?: 0f,
                            precio_viñeta_iva = it.precio_viñeta_iva ?: 0f,
                            fecha_inventario = LocalDate.now().toString(),
                            validadoHoja = 1,
                            condicion_mercado = it.condicion_mercado ?: "NORMAL"
                        )
                    }

                    inventarioDao.insertarTodos(entidades)
                    totalInsertados += entidades.size

                    //Calculando el porcentaje
                    if(totalRegistros != null && totalRegistros > 0){
                        val progreso = (totalInsertados * 100) / totalRegistros

                        withContext(Dispatchers.Main){
                            messageAsync("Cargando Inventario: ${progreso} %")
                        }
                    }

                    offset += limite

                } else {
                    hayMas = false
                }
            }


        } catch (e: Exception) {
            println("Error general: ${e.message}")
        }
   }

    private suspend fun obtenerEscalasPrecios() = withContext(Dispatchers.IO){
        val baseUrl = url

        inventarioDao = db.inventarioDao()

        val api = RetrofitCliente.obtenerApi(baseUrl)

        val limite = 1000
        var offset = 0
        var hayMas = true
        var totalInsertados = 0

        try {
            val totalEscalas = try {
                api.obtenerTotalRegistrosPrecios()
            }catch (e: Exception){
                println("No se pudo obtener el total de Escalas -> ${e.message}")
                null
            }

            while(hayMas){
                val respuesta = api.obtenerEscalasPrecios(offset, limite)

                //println(respuesta)

                if (respuesta.isNotEmpty()) {
                    val entidades = respuesta.map {
                        InventarioPreciosEntity(
                            id = it.id,
                            id_inventario = it.id_inventario ?: 0,
                            codigo_producto = it.codigo_producto ?: " ",
                            nombre = it.nombre ?: "",
                            terminos = it.terminos ?: "",
                            plazo = it.plazo ?: 0f,
                            unidad = it.unidad ?: " ",
                            cantidad = it.cantidad ?: 0f,
                            porcentaje = it.porcentaje ?: 0f,
                            precio = it.precio ?: 0f,
                            precio_iva = it.precio_iva ?: 0f,
                            id_inventario_unidad = it.id_inventario_unidad ?: 0
                        )
                    }

                    inventarioDao.insertarEscalas(entidades)
                    totalInsertados += entidades.size

                    //Calculando el porcentaje
                    if(totalEscalas != null && totalEscalas > 0){
                        val progreso = (totalInsertados * 100) / totalEscalas

                        withContext(Dispatchers.Main){
                            messageAsync("Cargando Escalas: ${progreso} %")
                        }
                    }

                    offset += limite

                } else {
                    hayMas = false
                }
            }
        }catch (e:Exception){
            println("Error de Escalas General: ${e.message}")
        }

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

}