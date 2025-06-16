package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
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
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout.DispatchChangeEvent
import androidx.lifecycle.lifecycleScope
import com.example.acae30.DAO.InventarioDao
import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioPreciosEntity
import com.example.acae30.Retrofit.RetrofitCliente
import com.example.acae30.controllers.CatalogosController
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.ConfigController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.controllers.PedidosController
import com.example.acae30.database.AppDatabase
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityCargaDatosBinding
import com.example.acae30.listas.InventarioRetrofit
import com.example.acae30.modelos.Inventario
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class carga_datos : AppCompatActivity() {

    private lateinit var url: String
    private var idVendedor = 0
    private var alert: AlertDialogo? = null
    private var database: Database? = null

    private var configController = ConfigController()
    private var inventarioController = InventarioController()
    private var clietnesController = ClientesController()
    private var pedidosController = PedidosController()
    private var catalagosController = CatalogosController()
    private var funciones = Funciones()

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private lateinit var binding : ActivityCargaDatosBinding
    private var rutaClientes : String = "T"


    private lateinit var inventarioDao: InventarioDao

    private lateinit var db : AppDatabase



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCargaDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this@carga_datos)

        preferences = this@carga_datos.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        alert = AlertDialogo(this@carga_datos, this)
        database = Database(this@carga_datos)

        url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        idVendedor = preferences.getInt("Idvendedor", 0)

        rutaClientes = preferences.getString("cargarClientesPorRuta", "T").toString()

    }

    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {
            val intento = Intent(this@carga_datos, Inicio::class.java)
            startActivity(intento)
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
                        alert!!.changeText("CARGANDO CONFIGURACIONES INICIALES")
                    }

                    try {
                        configController.obtenerConfigPagareObligatorio(this@carga_datos)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR LAS CONFIGURACIONES INICIALES " + e.message)
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
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    pedidosController.eliminarPedidosAntiguos(this@carga_datos)
                }
                funciones.mostrarMensaje("PEDIDOS ELIMINADOS", this@carga_datos, binding.vistaalerta)
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
                alert!!.changeText("CARGANDO INFORMACION DE LOS CLIENTES")
            }

            delay(1000)

            try {
                getClients()
            }catch (e:Exception){
                println("ERROR AL CARGAR LA INFORMACION DE LOS CLIENTES " + e.message)
            }

            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("CARGANDO PRECIOS PERSONALIZADOS")
            }

            try {
                clietnesController.obtenerPreciosPersonalizados(this@carga_datos)
            }catch (e:Exception){
                println("ERROR AL CARGAR LOS PRECIOS PERSONALIZADOS " + e.message)
            }

            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("CARGANDO CUENTAS POR COBRAR")
            }

            try {
                getCuentas()
            }catch (e:Exception){
                println("ERROR AL CARGAR LASC CUENTAS POR COBRAR DE LOS CLIENTES " + e.message)
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

            val hojaCargaActiva = preferences.getInt("hojaCarga", 0)
            val numero = hojaCargaDialog.findViewById<TextInputEditText>(R.id.tietNumeroCarga).text.toString()

            if (numero.isEmpty() || numero.toInt() == 0) {

                hojaCargaDialog.dismiss()
                funciones.mensaje(this@carga_datos, "INGRESE UN NUMERO DE HOJA DE CARGA")

            }else if(numero.toInt() == hojaCargaActiva){

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
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("CARGANDO INVENTARIO...")
            }

            try {
                //OBTENIENDO INVENTARIO DESDE HOJA DE CARGA
                inventarioController.obtenerInventarioHojaCarga(0, numero.toInt(), idVendedor, this@carga_datos)
            }catch (e:Exception){
                println("ERROR AL CARGAR LA HOJA DE INVENTARIO " + e.message)
            }

            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("OBTENIENDO ESCALAS DE PRECIOS")
            }

            try {
                //OBTENIENDO ESCALAS DE PRECIOS
                inventarioController.obtenerEscalasPrecios(this@carga_datos)
            }catch (e:Exception){
                println("ERROR AL CARGAR LAS ESCALAS DE PRECIOS " + e.message)
            }

            delay(1000)

            withContext(Dispatchers.Main){
                alert!!.changeText("INVENTARIO CARGADO EXITOSAMENTE")
            }

        }

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
                val listaRustas = catalagosController.obtenerListadoRutaSQLite(this@carga_datos, "", "")

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

                        val editor = preferences.edit()
                        editor.putInt("idRutaSeleccionada", 0)
                        editor.putString("rutaSeleccionada", "")
                        editor.putInt("idRutaSeleccionada", idRutaSeleccionada)
                        editor.putString("rutaSeleccionada", rutaSeleccionada)
                        editor.apply()

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

    //OBTENIENDO SUCURSALES DESDE WEBSERVIS
    //09-03-2023
    private suspend fun getClients() {
        try {
            //val direccion = url!! + "clientes"
            val id_vendedor = preferences.getInt("Idvendedor", 0)
            val direccion = url + "clientes/vendedor/"+id_vendedor
            val url = URL(direccion)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        messageAsync("Cargando 10%")
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                                talla++
                                if (talla <= 45) {
                                    messageAsync("Cargando $talla%")
                                }
                            }
                            messageAsync("Cargando 45%")
                            data.close()
                            messageAsync("Cargando 50%")
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                saveClienteDataBase(respuesta)
                            } else {
                                throw Exception("Servidor no Devolvio datos")
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                   /// alert!!.dismisss()
                    funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
                }
            } //ABRIMOS LA CONEXION
        } catch (e: Exception) {
            //alert!!.dismisss()
            funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
        }

        //IMPORTANDO DATOS DE TABLA SUCURSALES CLIENTE

        try {
            val direccion = url + "sucursales"
            val url = URL(direccion)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        messageAsync("Cargando 10%")
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                                talla++
                                if (talla <= 45) {
                                    messageAsync("Cargando $talla%")
                                }
                            }
                            messageAsync("Cargando 45%")
                            data.close()
                            messageAsync("Cargando 50%")
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                saveSucursalesDatabase(respuesta) //guarda los datos en la bd
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Datos del Cliente Almacenados Exitosamente")
                                delay(1500)
                            } else {
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Datos del Cliente Almacenados Exitosamente")
                                delay(1500)
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("SERVIDOR: NO SE ENCONTRARON SUCURSALES REGISTRADAS")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
           // alert!!.dismisss()
            //funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
            println("NO SE ENCONTRARON DATOS REGISTRADOS DE SUCURSALES")
        }
    } //obtiene los clientes del servidor

    //GUARDANDO SUCURSALES EN SQLITE
    //28-01-2023
    private fun saveSucursalesDatabase(json: JSONArray) {
        val bd = database!!.writableDatabase
        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        try {
            bd!!.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put("codigo_sucursal", funciones.validateJsonIsnullString(dato, "codigo_sucursal"))
                valor.put("nombre_sucursal", funciones.validateJsonIsnullString(dato, "nombre_sucursal"))
                valor.put("direccion_sucursal", funciones.validateJsonIsnullString(dato, "dteDireccion"))//DATO DTE
                valor.put("municipio_sucursal", funciones.validateJsonIsnullString(dato, "municipio"))//DATO DTE
                valor.put("depto_sucursal", funciones.validateJsonIsnullString(dato, "departamento"))//DATO DTE
                valor.put("telefono_1", funciones.validateJsonIsnullString(dato, "dteTelefono"))//DATO DTE
                valor.put("telefono_2", funciones.validateJsonIsnullString(dato, "telefono2"))
                valor.put("correo_sucursal", funciones.validateJsonIsnullString(dato, "correo"))
                valor.put("contacto_sucursal", funciones.validateJsonIsnullString(dato, "contacto"))

                //ARGEGANDO DATOS PENDIENTE Y DTE DE LA SUCURSAL
                valor.put("Id_ruta", dato.getInt("id_ruta"))
                valor.put("Ruta", funciones.validateJsonIsnullString(dato, "ruta"))
                valor.put("DTECodDepto", funciones.validateJsonIsnullString(dato, "dteCodDepto"))
                valor.put("DTECodMunicipio", funciones.validateJsonIsnullString(dato, "dteCodMunicipio"))
                valor.put("DTECodPais", funciones.validateJsonIsnullString(dato, "dteCodPais"))
                valor.put("DTEPais", funciones.validateJsonIsnullString(dato, "dtePais"))
                valor.put("Latitud_app", funciones.validateJsonIsnullString(dato, "latitud_app"))
                valor.put("Longitud_app", funciones.validateJsonIsnullString(dato, "longitud_app"))

                bd.insert("cliente_sucursal", null, valor)
                contador += talla
                val mensaje = contador + 50.toFloat()
                messageAsync("Cargando ${mensaje.toInt()}%")
            } //FINALIZANDO ITERACION FOR
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    } //INSERTANDO DATOS EN LA TABLA SUCURSALES EN SQLITE

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
                val respuesta = api. obtenerInventario(offset, limite)

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
                            fecha_inventario = LocalDate.now().toString()
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

                println(respuesta)

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


    private suspend fun getCuentas() {
        try {
            val direccion = url + "cuentas"
            val url = URL(direccion)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        messageAsync("Cargando 10%")
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                                talla++
                                if (talla <= 45) {
                                    messageAsync("Cargando $talla%")
                                }
                            }
                            messageAsync("Cargando 45%")
                            data.close()
                            messageAsync("Cargando 50%")
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                saveCuentaDatabase(respuesta) //guarda los datos en la bd
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Cuentas Almacenadas Exitosamente")
                                delay(1500)
                                //alert!!.dismisss()
                            } else {
                                messageAsync("Cargando 100%")
                                delay(1000)
                                messageAsync("Cuentas Almacenados Exitosamente")
                                delay(1500)
                                //alert!!.dismisss()
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            //alert!!.dismisss()
            funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
        }
    }

    fun messageAsync(mensaje: String) {
        if (alert != null) {
            runOnUiThread {
                alert!!.changeText(mensaje)
            }
        }
    }//muestra la carga del mensaje de forma asincrona

    private fun saveClienteDataBase(json: JSONArray) {

        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        val bd = database!!.writableDatabase
        try {
            bd!!.beginTransaction() //inicio la transaccion

            bd.execSQL("DELETE FROM clientes") //limpiamos los registros viejos par obtener los nuevos
            bd.execSQL("DELETE FROM cliente_precios")
            bd.execSQL("DELETE FROM cliente_sucursal") //LIMPIANDO TABLA SUCURSALES

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i) //obtenemos el objecto json
                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put("Codigo", funciones.validate(dato.getString("codigo")))
                data.put("Cliente", funciones.validate(dato.getString("cliente")))
                data.put("Dui", funciones.validate(dato.getString("dui")))
                data.put("Nit", funciones.validate(dato.getString("nit")))
                data.put("Nrc", funciones.validate(dato.getString("nrc")))
                data.put("Giro", funciones.validate(dato.getString("giro")))
                data.put(
                    "Categoria_cliente",
                    funciones.validate(dato.getString("categoria_cliente"))
                )
                data.put(
                    "Terminos_cliente",
                    funciones.validate(dato.getString("terminos_cliente"))
                )
                data.put("Plazo_credito", funciones.validate(dato.getInt("plazo_credito")))
                data.put("Limite_credito",
                    funciones.validate(dato.getString("limite_credito").toFloat())
                )
                data.put("Balance", funciones.validate(dato.getString("balance").toFloat()))
                data.put("Estado_credito", funciones.validate(dato.getString("estado_credito")))
                data.put("Direccion", funciones.validate(dato.getString("direccion")))
                data.put("Municipio", funciones.validate(dato.getString("municipio")))
                data.put("Departamento", funciones.validate(dato.getString("departamento")))
                data.put("Telefono_1", funciones.validate(dato.getString("telefono1")))
                data.put("Telefono_2", funciones.validate(dato.getString("telefono2")))
                data.put("Correo", funciones.validate(dato.getString("correo")))
                data.put("Contacto", funciones.validate((dato.getString("contacto"))))
                data.put("Id_ruta", funciones.validateJsonIsNullInt(dato, "id_ruta"))
                data.put("Id_vendedor", funciones.validateJsonIsNullInt(dato, "id_vendedor"))
                data.put("Vendedor", funciones.validate(dato.getString("vendedor")))
                data.put("Status", funciones.validate(dato.getString("status")))
                data.put("Ultima_venta", funciones.validate(dato.getString("fecha_ult_venta")))
                data.put(
                    "Aporte_mensual",
                    funciones.validate(dato.getString("aporte_mensual").toFloat())
                )

                //AGREGADO EL CAMPO PARA VERIFICACION DEL PAGARE
                val pagareFirmado = if(dato.getBoolean("pagare_Firmado_app")) 1 else 0
                data.put("Firmar_pagare_app", pagareFirmado)

                //AGREGANDO EL CAMPO PARA VERIFICACION DE PERSONA JURIDICA
                data.put("Persona_juridica", funciones.validate(dato.getString("persona_juridica")))

                //VALIDANDO EL DTEGIRO ALMACENADO EN EL SERVIDOR
                data.put("dteGiro", funciones.validate(dato.getString("dteGiro")))
                data.put("Ruta", funciones.validate(dato.getString("ruta")))

                data.put("Ruta", funciones.validate(dato.getString("ruta")))
                data.put("DTECodDepto", funciones.validate(dato.getString("dteCodDepto")))
                data.put("DTECodMunicipio", funciones.validate(dato.getString("dteCodMunicipio")))
                data.put("DTECodPais", funciones.validate(dato.getString("dteCodPais")))
                data.put("DTEDireccion", funciones.validate(dato.getString("dteDireccion")))
                data.put("DTEPais", funciones.validate(dato.getString("dtePais")))
                data.put("DTETelefono", funciones.validate(dato.getString("dteTelefono")))
                data.put("DTECorreo", funciones.validate(dato.getString("dteCorreo")))
                data.put("Latitud_app", funciones.validate(dato.getString("latitud_app")))
                data.put("Longitud_app", funciones.validate(dato.getString("longitud_app")))
                data.put("Nombre_comercial", funciones.validate(dato.getString("nombre_comercial")))
                //data.put("Mayorista", funciones.validate(dato.getString("mayorista")))
                data.put("DTECodGiro", funciones.validate(dato.getString("dteCodGiro")))
                data.put("DTEDistrito", funciones.validate(dato.getString("dteDistrito")))
                data.put("DTECodDistrito", funciones.validate(dato.getString("dteCodDistrito")))

                bd.insert("clientes", null, data)
                contador += talla
                val mensaje = contador + 50.toFloat()
                messageAsync("Cargando ${mensaje.toInt()}%")
            } //recorre el json array
            bd.setTransactionSuccessful()

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }//guarda los datos en la bd

    private fun saveCuentaDatabase(json: JSONArray) {
        val bd = database!!.writableDatabase
        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        try {
            bd!!.beginTransaction() //inicia la transaccion
            bd.execSQL("DELETE FROM cuentas") //eliminamos la cuentas

            val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'cuentas'"
            bd.execSQL(sql2)

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put(
                    "Codigo_cliente",
                    funciones.validateJsonIsnullString(dato, "codigo_cliente")
                )
                valor.put("Documento", funciones.validateJsonIsnullString(dato, "documento"))
                valor.put("Fecha", funciones.validateJsonIsnullString(dato, "fecha"))
                valor.put("Valor", funciones.validateJsonIsNullFloat(dato, "valor"))
                valor.put(
                    "Abono_inicial",
                    funciones.validateJsonIsNullFloat(dato, "abono_inicial")
                )
                valor.put(
                    "Saldo_inicial",
                    funciones.validateJsonIsNullFloat(dato, "saldo_inicial")
                )
                valor.put("Plazo", funciones.validateJsonIsNullFloat(dato, "plazo"))
                valor.put(
                    "Fecha_vencimiento",
                    funciones.validateJsonIsnullString(dato, "fecha_vencimiento")
                )
                valor.put("Saldo_actual", funciones.validateJsonIsNullFloat(dato, "saldo_actual"))
                valor.put(
                    "Fecha_ult_pago",
                    funciones.validateJsonIsnullString(dato, "fecha_ult_pago")
                )
                valor.put("Valor_pago", funciones.validateJsonIsNullFloat(dato, "valor_pago"))
                valor.put("Relacionado", funciones.validateJsonIsnullString(dato, "relacionado"))
                valor.put("Status", funciones.validateJsonIsnullString(dato, "status"))
                valor.put(
                    "Fecha_cancelado",
                    funciones.validateJsonIsnullString(dato, "fecha_cancelado")
                )
                valor.put("dias_tardios", funciones.validateJsonIsNullInt(dato, "dias_tardios"))

                bd.insert("cuentas", null, valor)
                contador = contador + talla
                val mensaje = contador + 50.toFloat()
                messageAsync("Cargando ${mensaje.toInt()}%")
            } //termina el for
            bd.setTransactionSuccessful() //transaccion exitosa
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    } //inserta las cxc en la tabla

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
      //  super.onBackPressed()

    //   finish()
    }//anula el boton atras

}