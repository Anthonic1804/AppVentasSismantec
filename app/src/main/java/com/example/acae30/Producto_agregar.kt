package com.example.acae30

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.databinding.ActivityProductoAgregarBinding
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.InventarioPrecios
import com.example.acae30.modelos.JSONmodels.ActualizarPrecioPersonalizadoJSON
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.DecimalFormatSymbols
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class Producto_agregar : AppCompatActivity() {
    private var idproducto: Int? = 0
    private var precio_iva: Float = 0.toFloat()
    private var precio : Float = 0f
    private var cantidad: Float = 0.toFloat()
    private var idpedido: Int = 0
    private var idcliente: Int? = 0
    private var idpedidodetalle: Int? = 0
    private var nombrecliente: String? = ""
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0
    private var listPrecios: ArrayList<InventarioPrecios>? = null
    private var datosProducto: com.example.acae30.modelos.Inventario? = null
    private var proviene: String? = ""
    private var total_param: Float? = null
    private var precioEditado: Float = 0.toFloat()
    private var existenciaProducto: Float = 0f
    private var getSucursalPosition: Int? = null
    private var codEmpleado: Int = 0
    private var url: String? = null
    private var codigoProducto: String = ""
    private var clienteMayorista = "N"


    //---------
    //VARIABLES PARA CONTROLAR LA ESCALA SELECCIONADA
    //---------
    private var cantidadEscala = 0f
    private var idEscala: Int = 0

    //---------
    //VARIABLES DE LOS CONTROLES DEL MENSAJE FLOTANTE
    //---------
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var tvMensaje : TextView

    //---------
    //VARIABLES PARA LOS CONTROLADORES Y FUNCIONES
    //---------
    private var funciones = Funciones()
    private var inventarioController = InventarioController()
    private var clientesController = ClientesController()
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    //---------
    //VARIABLES PARA EL USO DE CONTROLES
    //---------
    private lateinit var binding : ActivityProductoAgregarBinding

    //---------
    //VARIABLES DE CONTROL
    //---------
    private var sinExistencias: Int = 0
    private var precioAutorizadoUtilizado: Int = 0
    private var precioAutorizado: Float = 0f
    private var modificarPrecio : Boolean = false
    private var precioIvaPersonalizado : Float = 0f
    private var bonificacion : Float = 0f
    private var mostrarPrecioApp : Int = 0 //MOSTRARA EL PRECIO CONFIGURADO EN LA BD DEL SERVIDOR

    private var unidadActual: String = "UNI"
    private var idUnidad = 0
    private var equivaleUni: Float = 0f
    private var equivaleFra: Float = 0f
    private var uniEquivale: String? = null

    private var condicionMercado = ""

    private var decPrecios: Int = 0
    private var decTotales: Int = 0

    private val utilidades = CrearSslNoSeguro()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductoAgregarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //-----------
        //SETEANDO LAS SHARED PREFERENCES
        //-----------
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        codEmpleado = preferencias!!.getInt("Idvendedor", 0)
        sinExistencias = if(preferencias!!.getString("pedidos_sin_existencia", "") == "S") 1 else 0
        modificarPrecio = preferencias!!.getBoolean("modificar_precio_app", false)
        mostrarPrecioApp = preferencias!!.getInt("precio_mostrar_app", 0)
        decPrecios = preferencias!!.getInt("decPrecios",2)
        decTotales = preferencias!!.getInt("decTotales",2)

        //-----------
        //SETEANDO LOS INTENT QUE VIENEN DESDE EL FORMULARIO ANTERIOR
        //-----------
        idproducto = intent.getIntExtra("idproducto", 0)
        idpedido = intent.getIntExtra("idpedido", 0)
        idcliente = intent.getIntExtra("idcliente", 0)
        idpedidodetalle = intent.getIntExtra("idpedidodetalle", 0)
        nombrecliente = intent.getStringExtra("nombrecliente")
        idvisita = intent.getIntExtra("visitaid", 0)
        codigo = intent.getStringExtra("codigo").toString()
        idapi = intent.getIntExtra("idapi", 0)
        getSucursalPosition = intent.getIntExtra("sucursalPosition", 0)
        proviene = intent.getStringExtra("proviene")
        total_param = intent.getFloatExtra("total_param", 0.toFloat())

        cargarOpcionesGenerales()

    }

    override fun onStart() {
        super.onStart()

        cargarUnidadesMedida()

        cargarListadoPrecios(unidadActual)

        // Actualizar el total cuando cambie el precio
        binding.spprecio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //MUESTRA EL VALOR SELECCIONADO EN EL PRECIO
                //Toast.makeText(applicationContext, "Valor: "+parent!!.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show()

                val nuevaCadena = parent!!.getItemAtPosition(position).toString()

                val valor = nuevaCadena.substringBefore(" ").toDoubleOrNull()

                //MUESTRA EL VALOR SELECCIONADO DEL PRECIO
                //Toast.makeText(applicationContext, "Valor Seleccionado: "+ valor.toString(), Toast.LENGTH_LONG).show()

                if (nuevaCadena.last() == '*') {
                    precio_iva = precioEditado
                } else {
                    val nuevoValor = precioFromList(valor.toString())
                    precio_iva = nuevoValor
                }

                cantidadEscala = inventarioController.obtenerEscalaSeleccionada(this@Producto_agregar,
                    idproducto!!, precio_iva, unidadActual)

                Totalizar(cantidad)

            }
        }

        binding.txtcantidad.setText(String.format("%.2f".format(cantidad)))

        binding.imgbtnatras.setOnClickListener {
            if(proviene == "editar"){

                provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)

            }else{
                val intento = Intent(this@Producto_agregar, Inventario::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", nombrecliente)
                intento.putExtra("busqueda", true)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                intento.putExtra("sucursalPosition", getSucursalPosition)
                intento.putExtra("facturaExportacion", false)
                startActivity(intento)
            }

        } // boton que lleva atras en el activity

        binding.btnagregar.setOnClickListener {
            //VERIFICANDO SI MODIFICAR PRECIO ES TRUE DESDE SQLSERVER
            if(modificarPrecio){
                agregarProducto()
            }else{
                // VERIFICANDO SI MOD PRECIO ES FALSE Y LUEGO COMPROBAR QUE EL TOKEN HAYA SIDO UTILIZADO
                if(precioAutorizadoUtilizado == 1){
                    confirmarToken(codEmpleado, codigoProducto)
                }else{
                    agregarProducto()
                }
            }
        }//AGREGANDO EL PRODUCTO AL PEDIDO

        binding.btneliminar.setOnClickListener {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    deleteDetalle(idpedidodetalle!!)
                }

                provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)

            }catch (e: Exception){
                funciones.mostrarAlerta("ERROR AL ELIMINAR EL PRODUCTO", this@Producto_agregar, binding.lienzo)
            }
        }

//        binding.txtcantidad.filters = arrayOf<InputFilter>(object : InputFilter {
//            var decimalFormatSymbols: DecimalFormatSymbols = DecimalFormatSymbols()
//            override fun filter(
//                source: CharSequence,
//                start: Int,
//                end: Int,
//                dest: Spanned,
//                dstart: Int,
//                dend: Int
//            ): CharSequence {
//                val indexPoint: Int =
//                    dest.toString().indexOf(decimalFormatSymbols.decimalSeparator)
//                if (indexPoint == -1) return source
//                val decimals = dend - (indexPoint + 1)
//                return if (decimals < 4) source else ""
//            }
//        })

        binding.spunidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                when(binding.spunidad.selectedItem.toString()){
                    "UNIDAD" -> {
                        unidadActual = "UNI"
                        calcularExitenciaSegunUnidadSeleccionada(unidadActual)
                        equivaleUni = 0f
                        equivaleFra = 0f
                    }
                    "FRACCION" -> {
                        unidadActual = "FRA"
                        calcularExitenciaSegunUnidadSeleccionada(unidadActual)
                        equivaleUni = 0f
                        equivaleFra = 0f
                    }
                    else -> {
                        unidadActual = binding.spunidad.selectedItem.toString()
                    }
                }
                cargarListadoPrecios(unidadActual)

                verificarBonificados(unidadActual)

                if(unidadActual != "UNI" || unidadActual != "FRA"){
                    lifecycleScope.launch(Dispatchers.IO) {
                        val unidadMedida = inventarioController.obtenerIdUnidadMedida(this@Producto_agregar, idproducto!!, unidadActual)

                        if(unidadMedida != null){
                            idUnidad = unidadMedida.id ?: 0

                            uniEquivale = unidadMedida.unidades

                            when(uniEquivale){
                                "UNI" -> {
                                    equivaleUni = unidadMedida.equivale
                                    calcularExitenciaSegunUnidadSeleccionada(uniEquivale!!)
                                }
                                "FRA" -> {
                                    equivaleFra = unidadMedida.equivale
                                    calcularExitenciaSegunUnidadSeleccionada(uniEquivale!!)
                                }
                                else -> {
                                    equivaleUni = 0f
                                    equivaleFra = 0f
                                }
                            }

                            /*equivaleUni = when(uniEquivale){
                                "UNI" -> {unidadMedida.equivale}
                                else -> {0f}
                            }

                            equivaleFra = when(uniEquivale){
                                "FRA" -> {unidadMedida.equivale}
                                else -> {0f}
                            }*/
                        }
                    }
                }

            }
        }

        binding.btneditarprecio.setOnClickListener {
            if(modificarPrecio){
                AlertaPrecio(this@Producto_agregar)
            }else{
                verificarPrecioAutorizado(codEmpleado, codigoProducto)
            }
        }

        binding.txtcantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(cantidad: Editable) {
                validarCantidad(cantidad.toString())
            }
        })

        //SE BUSCA EL DETALLE DEL PEDIDO
        if (idproducto!! > 0) {
            val detalle = getPedidodetalle(idpedidodetalle!!)
            this@Producto_agregar.lifecycleScope.launch {
                try {
                    val datos = datosProducto

                    if (datos != null) {

                        binding.txtcodigo.text = datos.Codigo
                        binding.txtdescripcion.text = datos.descripcion
                        codigoProducto = datos.Codigo.toString()

                        precio_iva = datos.Precio_iva!!
                        precio = datos.Precio!!
                        Totalizar(cantidad)
                        binding.txtexistencia.text = "${datos.Existencia}"
                        binding.txtExistenciasFra.text = "${datos.Existencia_u}"


                        //VALIDANDO PARA VENTA DE FACCIONES
                        //existenciaProducto = datos.Existencia!!.toFloat()

                        calcularExitenciaSegunUnidadSeleccionada(unidadActual)


                        // Agregar precios a lista

                        val precioss = ArrayList<String>()

                        var seleccionado = false

                        if (proviene == "editar") {
                            binding.btnagregar.text = "ACTUALIZAR PRODUCTO";
                            binding.txttituloproducto.text = "ACTUALIZAR PRODUCTO";
                            binding.btneditarprecio.visibility = View.INVISIBLE;

                            var cantidad_provisional = detalle!!.Cantidad
//                           visor!!.text=cantidad.toString()
                            var precio_provisional = detalle!!.Precio_venta //EDITADO PARA QUE TOME EL VALOR SELECCIONADO PARA LA VENTA
                            precioEditado = precio_provisional!!

                            //var precio_provisional = total_param!! / cantidad_provisional
                            cantidad = cantidad_provisional!!
                            precio_iva = detalle.Precio_venta!! // EDITADO PARA QUE TOME EL VALOR SELECCIONADO PARA LA VENTA

                            binding.txtcantidad.setText("${String.format("%.0f".format(cantidad) )}")

                            if ("${String.format("%.2f".format(precio_provisional) )}" == "${String.format("%.2f".format(precio_iva) )}")
                            {
                                if (detalle.Precio_editado == "*") {
                                    // precio = detalle.Precio_venta!!
                                    precioss.add("${String.format("%.2f".format(precio_iva) )}" + "*")
                                } else {
                                    // precio = detalle.Precio_venta!!
                                    precioss.add("${String.format("%.2f".format(precio_iva) )}")
                                }
                                seleccionado = true
                            }

                            if (!seleccionado) {
                                if (detalle.Precio_editado == "*") {
                                    precioss.add("${String.format("%.2f".format(precio_provisional) )}" + "*")
                                } else {
                                    precioss.add("${String.format("%.2f".format(precio_provisional) )}")//EDITADO
                                }

                            }

                            precioss.add("${String.format("%.2f".format(datos.Precio_iva) )}")
                        } else {
                            // precio vi;eta debe ir
                            precioss.add("${String.format("%.2f".format(datos.Precio_iva) )}")
                        }

                        Totalizar(cantidad)

                    } else {
                        println("No se Han encontrado los datos")
                    }
                } catch (e: Exception) {
                    /*  runOnUiThread {
                          alert!!.dismisss()
                          Toast.makeText(this@Producto_agregar, e.message, Toast.LENGTH_LONG).show()
                      }*/
                }

            }

        } else {

        }


        CambioCantidad()
    }

    //FUNCION PARA VALIDAR LA CANTIDAD
    private fun calcularExitenciaSegunUnidadSeleccionada(unidadMedida: String){
        val datos = datosProducto

        val fraccion = datos!!.Fraccion
        val existencia = datos.Existencia
        val existenciaU = datos.Existencia_u
        existenciaProducto = if(unidadMedida == "FRA" && fraccion!!.toFloat() > 1f){
            (existencia!!.toFloat() * fraccion) + existenciaU.toFloat()
        }else{
            datos.Existencia!!.toFloat()
        }
    }

    private fun cargarOpcionesGenerales(){
        this@Producto_agregar.lifecycleScope.launch {
            //---------
            //LA UNIDADES SE GENERAN AUTOMATICAS SI EL PRODUCTO LAS TIENE CONFIGURADAS
            //---------
            datosProducto = inventarioController.obtenerInformacionProductoPorId(this@Producto_agregar, idproducto!!, false)

            //--------
            // ASIGNADO DATOS DEL CLIENTE
            //--------
            clienteMayorista = clientesController.obtenerInformacionCliente(this@Producto_agregar, idcliente!!)!!.Mayorista.toString().trim()

            //SELECCIONANDO CONDICION DE MERCADO DEL PRODUCTO
            condicionMercado = datosProducto!!.condicionMercado.toString()

            //DESHABILITANDO EL PRECIO PERSONALIZADO
            binding.tvPrecioPersonalizado.visibility = View.GONE

            //OBTENIENDO EL PRECIO PERSONALIZADO POR CLIENTE
            precioIvaPersonalizado = clientesController.obtenerPrecioPersoCliente(idcliente!!,
                idproducto!!, this@Producto_agregar, false)

            if(precioIvaPersonalizado > 0){
                binding.apply {
                    tvPrecioPersonalizado.visibility = View.VISIBLE
                    spprecio.visibility = View.GONE
                    btneditarprecio.visibility = View.GONE

                    tvPrecioPersonalizado.text = "${String.format("%.${decPrecios}f".format(precioIvaPersonalizado))}"
                }
            }

            verificarBonificados(unidadActual)

            //TOMANDO LA CANTIDAD DE LAS ESCALA SELECCIONADA.
            //09/01/2024
            //cantidadEscala = seleccionarCantidadenEscala(idpedido, idproducto!!)
            cantidadEscala = inventarioController.obtenerEscalaSeleccionada(this@Producto_agregar,
                idproducto!!, precio_iva, unidadActual)

            //HABILITAR BTN ELIMINAR
            if (idpedidodetalle!! > 0) {
                binding.btneliminar.visibility = View.VISIBLE
            } else {
                binding.btneliminar.visibility = View.GONE
            }
        }
    }

    //FUNCION PARA OBTENER LA BONIFICACION POR PRODUCTO O CLIENTE
    private fun verificarBonificados(unidad: String){
        //OBTENIENDO LA BONIFICACION PERSONALIZADA POR CLIENTE
        val clienteBonificado = clientesController.obtenerBonificacionCliente(idcliente!!,
            idproducto!!,this@Producto_agregar)
        bonificacion = if(unidad == "UNI"){
            if(clienteBonificado > 0){
                clienteBonificado
            }else{
                datosProducto!!.Bonificado!!.toFloat()
            }
        } else {
            0f
        }
    }

    private fun cargarUnidadesMedida(){
        this@Producto_agregar.lifecycleScope.launch {
            try {

                if(proviene == "editar"){
                    val hojaCarga = preferencias!!.getBoolean("Hoja_carga_inventario_app", false)
                    val producto = getPedidodetalle(idpedidodetalle!!)

                    val unidadSelecciona = producto!!.Unidad!!.trim().toString()
                    idUnidad = producto.Idunidad!!
                    equivaleFra = producto.EquivaleFra
                    equivaleUni = producto.EquivaleUni
                    uniEquivale = producto.UniEquivale

                    val unidades = inventarioController.listadoUnidadesMedidaProductoById(this@Producto_agregar, idproducto!!, hojaCarga)
                    val unidadesMedida = ArrayAdapter<String>(this@Producto_agregar, android.R.layout.simple_spinner_dropdown_item)

                    unidadesMedida.add(unidadSelecciona)
                    unidadActual = when(unidadSelecciona){
                        "UNIDAD" -> { "UNI" }
                        "FRACCION" -> { "FRAC" }
                        else -> { unidadSelecciona }
                    }

                    unidadesMedida.addAll(unidades)
                    binding.spunidad.adapter = unidadesMedida
                }else{
                    val hojaCarga = preferencias!!.getBoolean("Hoja_carga_inventario_app", false)
                    val unidades = inventarioController.listadoUnidadesMedidaProductoById(this@Producto_agregar, idproducto!!, hojaCarga)
                    val unidadesMedida = ArrayAdapter<String>(this@Producto_agregar, android.R.layout.simple_spinner_dropdown_item)
                    unidadesMedida.addAll(unidades)
                    binding.spunidad.adapter = unidadesMedida
                }


            }catch (e:Exception){
                println("ERROR AL CARGAR LAS UNIDADESD DE MEDIDA -> "  + e.message)
            }
        }
    }

    private fun cargarListadoPrecios(unidadMedida : String){
        this@Producto_agregar.lifecycleScope.launch {
            listPrecios = inventarioController.obtenerEscalaPrecios(this@Producto_agregar, idproducto!!, false, unidadMedida)
            val precioss = ArrayList<String>()

            if(proviene == "editar"){
                val producto = getPedidodetalle(idpedidodetalle!!)
                val precioSeleccionado = producto!!.Precio_venta
                precioss.add("${String.format("%.${decPrecios}f".format(precioSeleccionado))}")
            }

            //-----------------------
            //Agregado el precio asignado en la ficha del producto para las unidades
            //-----------------------
            if(unidadMedida == "UNI"){
                precioss.add("${String.format("%.${decPrecios}f".format(datosProducto!!.Precio_iva))}") //PRECIO AGREGADO DEL PRODUCTO DE LA TABLA INVENTARIO
            }


            //------------------------
            //Agregando el precio asignado en la ficha del producto para las fracciones.
            //------------------------
            if(unidadMedida == "FRA"){
                precioss.add("${String.format("%.${decPrecios}f".format(datosProducto!!.Precio_u_iva))}") //PRECIO AGREGADO DEL PRODUCTO DE LA TABLA INVENTARIO
            }


            listPrecios!!.forEach {
                val unidad_cantidad = " (" + "${String.format("%.0f".format(it.Cantidad))}" + " ${it.Unidad} )"
                precioss.add("${String.format("%.${decPrecios}f".format(it.Precio_iva))}" + " ${it.Nombre}" + unidad_cantidad
                )
            }

            var adapterPrecios = ArrayAdapter(this@Producto_agregar, android.R.layout.simple_spinner_item,
                precioss
            )

            adapterPrecios.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            binding.spprecio.adapter = adapterPrecios


            //FUNCION PARA MOSTRAR EL PRECIO POR DEFECTO EN EL LISTADO DEL PRODUCTO
            //SOLO CUANDO SE AGREGA EL PRODUCTO POR PRIMERA VEZ
            if(proviene != "editar"){
                val totalIndices = binding.spprecio.adapter?.count ?: 0
                if(mostrarPrecioApp in 0 until totalIndices){
                    binding.spprecio.setSelection(mostrarPrecioApp, true)
                }else{
                    binding.spprecio.setSelection(0, true)
                }
            }

        }
    }

    //FUNCION PARA VALIDAD CANTIDAD PARA ESCARRSA
    private fun validarCantidad(cantidadIngresada: String){
        if(cantidadIngresada.isNotEmpty()){
            cantidad = cantidadIngresada.toFloat()
            var cantidadVerificar = cantidad
            if(equivaleUni > 0f){
                cantidadVerificar = cantidad.toFloat() * equivaleUni
            }

            if(equivaleFra > 0f){
                cantidadVerificar = cantidad.toFloat() * equivaleFra
            }

            if((cantidadVerificar > existenciaProducto || cantidadVerificar == 0f)  && sinExistencias == 0){
                binding.txtcantidad.error = "No puede Agregar una cantidad mayor a las existencias actuales";
                binding.btnagregar.setBackgroundResource(R.drawable.border_btndisable)
                binding.btnagregar.isEnabled = false
            }else if(cantidadVerificar < cantidadEscala && clienteMayorista == "N"){ //VALIDADO EL PRECIO SELECCIONADO EN LAS ESCALAS.
                binding.txtcantidad.error = "La cantidad no es válida para el precio seleccionado"
                binding.btnagregar.setBackgroundResource(R.drawable.border_btndisable)
                binding.btnagregar.isEnabled = false
            }else{
                binding.btnagregar.isEnabled = true
                Totalizar(cantidad)
                binding.btnagregar.setBackgroundResource(R.drawable.border_btnenviar) 
            }

        }else{
            binding.txtcantidad.error = "Campo no puede quedar vacio"
            binding.btnagregar.isEnabled = false
            cantidad = 0.toFloat()
            Totalizar(cantidad)
        }
    }

    //MODIFICACION PARA LA PAPELERIA DM
    //EDITAR CANTIDAD DE PRODUCTO SIN BORRAR
    //23-08-2022
    private fun CambioCantidad() {
        binding.txtcantidad.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus){
                binding.txtcantidad.setText("${String.format("", cantidad)}");
            }
        })
    }

    //MODIFICANDO LA CANTIDAD DE DECIMALES A 4
    //PAPELERIA DM
    //23-08-2022
    private fun Totalizar(cantidad: Float) {
        var total : Float = 0f
        total = if(precioIvaPersonalizado > 0){
            precioIvaPersonalizado * cantidad
        }else{
            precio_iva * cantidad
        }

        binding.txttotal.text = "${String.format("%.4f".format(total) )}"

        if(bonificacion > 0 && bonificacion != null && unidadActual == "UNI"){
            val productosBonificados = cantidad / bonificacion
            binding.txtBonificados.text = productosBonificados.toInt().toString()
        }else{
            binding.txtBonificados.text = 0.toString()
        }

    }

    private fun AddDetallePedido(esPrecioEditado: Boolean, bonificado:Int, precioIva: Float): Int {
        val base = funciones.obtenerInstancia(this@Producto_agregar).openHelper.writableDatabase
        var vPrecio: Float = precioIva / 1.13f
        var vPrecio_iva = precioIva

        //CONFIGURA LA DESCRIPCION DEL PRODUCTO DE ACUERDO A LA UNIDAD SELECCIONADA
        val nombreProducto = binding.txtdescripcion.text.toString()
        val descripcion = when(binding.spunidad.selectedItem.toString()){
            "UNIDAD" -> nombreProducto
            "FRACCION" -> datosProducto!!.Nombre_fraccion + ' ' + nombreProducto
            else -> binding.spunidad.selectedItem.toString().trim() + ' ' + nombreProducto
        }

        //CONFIGURA EL PRECIO PERSONALIZADO DEL CLIENTE
        if(precioIvaPersonalizado > 0){
            vPrecio = (precioIvaPersonalizado / 1.13).toFloat()
            vPrecio_iva = precioIvaPersonalizado
        }

        try {
            base.beginTransaction()
            val detalle = ContentValues()
            detalle.put("Id_pedido", idpedido)
            detalle.put("Id_producto", idproducto)
            detalle.put("Cantidad", cantidad)
            detalle.put("Unidad", unidadActual)
            detalle.put("Descripcion", descripcion)
            detalle.put("Idunidad", idUnidad)
            detalle.put("precio", vPrecio)
            detalle.put("Precio_iva", vPrecio_iva)
            detalle.put("Precio_oferta", 0.toFloat())
            detalle.put("Total", (binding.txttotal.text.toString().toFloat()) / 1.13)
            detalle.put("Total_iva", binding.txttotal.text.toString().toFloat())
            detalle.put("Descuento", 0.toFloat())
            detalle.put("Bonificado", bonificado)

            if (esPrecioEditado) {
                detalle.put("Precio_editado", "*")
            } else {
                detalle.put("Precio_editado", "")
            }

            detalle.put("Id_Inventario_Precios", idEscala)
            detalle.put("Codigo_de_barra", datosProducto!!.codigo_de_barra)
            detalle.put("EquivaleUni", equivaleUni)
            detalle.put("EquivaleFra", equivaleFra)
            detalle.put("UniEquivale", uniEquivale)
            detalle.put("Comentario", 0)



            val idpedidodetalle = base.insert("detalle_pedidos", SQLiteDatabase.CONFLICT_REPLACE, detalle)

            val consulta = "SELECT SUM(Total_iva) FROM detalle_pedidos where Id_pedido=$idpedido"
            val cursor = base.query(consulta)
            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
                //if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                base.update("pedidos", SQLiteDatabase.CONFLICT_REPLACE, t, "Id=?", arrayOf(idpedido.toString()))
                //}else{
                //throw Exception("Error en el total")
                //}
            } else {
                throw Exception("No se encontro el pedido asociado")
            }
            base.setTransactionSuccessful()
            return idpedidodetalle.toInt()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            base.endTransaction()

            //---------
            // SE DEBE DE QUITAR ESTA ASIGNACION
            //---------
            preferencias!!.edit {
                putBoolean("precioConIva", true)
            }

        }
    } //agrega el producto al pedido y actualiza el total

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    private fun getPedidodetalle(id: Int): DetallePedido? {
        val base = funciones.obtenerInstancia(this@Producto_agregar).openHelper.readableDatabase
        var visita : DetallePedido? = null
        try {
            val consulta = "SELECT * FROM detalle_producto where Id=$id"
            val cursor = base.query(consulta)

            if (cursor.count > 0) {
                cursor.moveToFirst()

                visita = DetallePedido(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getFloat(5),
                    cursor.getFloat(6),
                    cursor.getFloat(7),
                    cursor.getFloat(8),
                    cursor.getFloat(9),
                    cursor.getFloat(10),
                    cursor.getFloat(11),
                    cursor.getFloat(12),
                    cursor.getFloat(13),
                    cursor.getFloat(14),
                    cursor.getFloat(15),
                    cursor.getString(16),
                    cursor.getInt(17),
                    cursor.getFloat(18),
                    cursor.getString(19),
                    cursor.getInt(20),
                    cursor.getString(21),
                    cursor.getFloat(22),
                    cursor.getFloat(23),
                    cursor.getString(24)
                )
            }
            cursor.close()
        } catch (e: Exception) {
            println("ERROR BUSCAR EL DETALLE DEL PEDIDO -> " + e.message)
        }
        return visita
    } //obtiene el detalle del pedido

    private fun updateDetalle(iddetalle: Int?, esPrecioEditado: Boolean, bonificado: Int, precio: Float) {
        val base = funciones.obtenerInstancia(this@Producto_agregar).openHelper.writableDatabase
        val precioIva = precio
        val precioU = precio / 1.13

        //CONFIGURA LA DESCRIPCION DEL PRODUCTO DE ACUERDO A LA UNIDAD SELECCIONADA
        val nombreProducto = binding.txtdescripcion.text.toString()
        val descripcion = when(binding.spunidad.selectedItem.toString()){
            "UNIDAD" -> nombreProducto
            "FRACCION" -> datosProducto!!.Nombre_fraccion + ' ' + nombreProducto
            else -> binding.spunidad.selectedItem.toString().trim() + ' ' + nombreProducto
        }

        try {
            base.beginTransaction()
            val detalle = ContentValues()
            detalle.put("Cantidad", cantidad)
            detalle.put("Bonificado", bonificado)
            detalle.put("precio", precioU)
            detalle.put("Precio_iva", precioIva)
            //detalle.put("Cantidad", binding.spunidad.selectedItem.toString())
            detalle.put("Total_iva", binding.txttotal.text.toString().toFloat())
            detalle.put("Descripcion", descripcion)
            detalle.put("Unidad", unidadActual)
            detalle.put("Idunidad", idUnidad)

            if (esPrecioEditado) {
                detalle.put("Precio_editado", "*")
            } else {
                detalle.put("Precio_editado", "")
            }

            detalle.put("EquivaleUni", equivaleUni)
            detalle.put("EquivaleFra", equivaleFra)
            detalle.put("UniEquivale", uniEquivale)


            val idpedidodetalle = base.update(
                "detalle_pedidos",
                SQLiteDatabase.CONFLICT_REPLACE,
                detalle,
                "Id=?",
                arrayOf(iddetalle.toString())
            )

            val consulta = "SELECT SUM(Total_iva)  FROM detalle_pedidos where Id_pedido=$idpedido"
            val cursor = base.query(consulta)

            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
                //if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                base.update("pedidos", SQLiteDatabase.CONFLICT_REPLACE,t, "Id=?", arrayOf(idpedido.toString()))
//                }else{
//                    throw Exception("Error en el total")
//                }
            } else {
                println("No se encontro el pedido asociado")
            }
            cursor.close() // -----> Este no lo habia cerrado 18/09/2025
            base.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR LA ACTUALIZAR EL DETALLE DEL PEDIDO -> " + e.message)
        } finally {
            base.endTransaction()
        }
    } //ACTUALIZA EL DETALLE DEL PRODUCTO

    private fun deleteDetalle(iddetalle: Int?) {
        val base = funciones.obtenerInstancia(this@Producto_agregar).openHelper.writableDatabase
        try {
            base.beginTransaction()
            base.execSQL("DELETE FROM detalle_pedidos where Id=$iddetalle") //elimina

            val consulta = "SELECT SUM(Total_iva)  FROM detalle_pedidos where Id_pedido=$idpedido"
            val cursor = base.query(consulta)
            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
//                if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                base.update("pedidos", SQLiteDatabase.CONFLICT_REPLACE, t, "Id=?", arrayOf(idpedido.toString()))
//                }else{
//                    throw Exception("Error en el total")
//                }
            } else {
                println("No se encontro el pedido asociado")
            }
            cursor.close() // -----> Este no lo habia cerrado 18/09/2025
            base.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR AL ELIMINAR DEL DETALLE DEL PEDIDO -> " + e.message)
        } finally {
            base.endTransaction()
        }
    }

    private fun validateProduct(idproducto: Int): Int {
        val base = funciones.obtenerInstancia(this@Producto_agregar).openHelper.readableDatabase
        var i = 0
        try {

            val consulta = "SELECT *  FROM detalle_pedidos where Id_pedido=$idpedido and Id_producto=$idproducto and Unidad = '$unidadActual'"
            val cursor = base.query(consulta)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                i = cursor.getInt(0)
                return i
            }
            cursor.close()
        } catch (e: Exception) {
            println("ERROR AL VALIDAR EL PRODUCTO -> " + e.message)
        }
        return 0
    }//valida si ya existe el producto en el detalle


    //FUNCION PARA VALIDAD SI EL INGRESO ES NUMERICO DECIMAL
    private fun isNumeric(cadena: String): Boolean {
        return try {
            cadena.toFloat()
            return true
        } catch (nfe: NumberFormatException) {
            return false
        }
    }

    //FUNCION PARA VALIDAR SI EL INGRESO ES NUMERO ENTERO
    //PARA PAPELERIA DM
    //24-08-2022
    private fun isInteger(cadena: String): Boolean{
        return try{
            cadena.toInt()
            return  true
        }catch (nfe: NumberFormatException){
            binding.txtcantidad.setText("${String.format("", cantidad)}");
            return false
        }
    }

    //MODIFICADA LA CANTIDAD DE DECIMALES A 4
    //MODIFICADA 08/01/2024
    private fun precioFromList(cadena: String): Float {
        var nuevoValor = 0.toFloat()

        if (isNumeric(cadena)) {
            nuevoValor = cadena.toFloat()
        } else {
            listPrecios!!.forEach {
                var valorPrecio = "${String.format("%.${decPrecios}f".format(it.Precio_iva) )}"
                var unidad_cantidad = ""
                if (it.Cantidad!! > 0.toFloat()) {
                    unidad_cantidad = " (" + "${String.format("%.2f".format(it.Cantidad) )}" + ")"

                }
                if (cadena == valorPrecio + " ${it.Nombre}" + unidad_cantidad) {
                    nuevoValor = valorPrecio.toFloat()
                }
            }
        }

        return nuevoValor
    } // Busca en la lista de precios y retorna el precio que se ha encontrado

    //MODIFICADA LA CANTIDAD DE DECIMALES A 4
    private fun AlertaPrecio(contexto: com.example.acae30.Producto_agregar) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_precio)
        var nuevoprecio = dialogo.findViewById<EditText>(R.id.nuevoprecio)
        nuevoprecio.isEnabled = true

        var cadena_precio = binding.spprecio.selectedItem.toString()

        var nuevo_precio = 0.toFloat()

        if (cadena_precio.last() == '*') {
            nuevo_precio =
                cadena_precio.replace(cadena_precio.substring(cadena_precio.length - 1), "")
                    .toFloat()
        } else {
            nuevo_precio = precioFromList(cadena_precio)
        }

        if(!modificarPrecio){
            nuevoprecio.isEnabled = false
            nuevoprecio!!.setText("${String.format("%.${decPrecios}f".format(precioAutorizado) )}")
        }

        // Actualizar el total cuando cambie la cantidad
        nuevoprecio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                val nuevaCantidad = s.toString()
                if (nuevaCantidad == "") {
                    nuevoprecio.error = "Campo no puede quedar Vacio"
                }
            }
        })

        // Validar los decimales
        nuevoprecio.filters = arrayOf<InputFilter>(object : InputFilter {
            var decimalFormatSymbols: DecimalFormatSymbols = DecimalFormatSymbols()
            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence {
                val indexPoint: Int =
                    dest.toString().indexOf(decimalFormatSymbols.decimalSeparator)
                if (indexPoint == -1) return source
                val decimals = dend - (indexPoint + 1)
                return if (decimals < 4) source else "" //MODIFICADA LA CANTIDAD DE DECIMALES
            }
        })

        // Acccion de click al agregar precio
        dialogo.findViewById<Button>(R.id.btnguardarnuevoprecio).setOnClickListener {
            //VARIABLE PARA DETERMINAR SI EL PRECIO ES MODIFICADO O NO
            precioAutorizadoUtilizado = if(!modificarPrecio) 1 else 0

            try {
                val unidad = binding.spunidad.selectedItem.toString()

                binding.spprecio.adapter = null

                // Agregar precios a lista

                val precioss = ArrayList<String>()

                // Agregamos el nuevo precio a la lista
                var valorNuevoPrecio = nuevoprecio.text.toString()

                if (valorNuevoPrecio == "" || valorNuevoPrecio == null) {
                    precioss.add("${String.format("%.${decPrecios}f".format(0.toFloat()) )}" + "*")
                    precioEditado = 0.toFloat()
                } else {
                    precioss.add("${String.format("%.${decPrecios}f".format(valorNuevoPrecio.toFloat()) )}" + "*")
                    precioEditado = valorNuevoPrecio.toFloat()
                }

                if (listPrecios!!.size > 0) {
                    if (unidad == "UNIDAD") {
                        precioss.add("${String.format("%.${decPrecios}f".format(datosProducto!!.Precio_iva) )}")
                        listPrecios!!.forEach {
                            if (it.Unidad == "UNI" || it.Unidad == "") {
                                var unidad_cantidad = ""
                                if (it.Cantidad!! > 0.toFloat()) {
                                    unidad_cantidad =
                                        " (" + "${String.format("%.2f".format(it.Cantidad) )}" + ")"
                                }
                                precioss.add(
                                    "${
                                        String.format(
                                            "%.${decPrecios}f".format(it.Precio_iva)
                                        )
                                    }" + " ${it.Nombre}" + unidad_cantidad
                                )

                            }
                        }
                    }

                    if (unidad == "FRACCIÓN") {
                        listPrecios!!.forEach {
                            if (it.Unidad == "FRA") {
                                var unidad_cantidad = ""
                                if (it.Cantidad!! > 0.toFloat()) {
                                    unidad_cantidad =
                                        " (" + "${String.format("%.2f".format(it.Cantidad) )}" + ")"
                                }
                                precioss.add(
                                    "${
                                        String.format(
                                            "%.${decPrecios}f".format(it.Precio_iva)
                                        )
                                    }" + " ${it.Nombre}" + unidad_cantidad
                                )
                            }
                        }
                    }

                }

                // Consultar inventario precios

                var adapterPrecios = ArrayAdapter(
                    contexto,
                    android.R.layout.simple_spinner_item,
                    precioss
                )
                adapterPrecios.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spprecio.adapter = adapterPrecios

                dialogo.dismiss()
            } catch (e: Exception) {
                dialogo.dismiss()
                val alert: Snackbar = Snackbar.make(
                    binding.lienzo,
                    e.message.toString(),
                    Snackbar.LENGTH_LONG
                )
                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                alert.show()
            }

        }//boton eliminar

        dialogo.show()

    } //muestra la alerta para agregar precio

    //FUNCION PARA OBTENER EL PRECIO AUTORIZADO
    private fun verificarPrecioAutorizado(id_empleado:Int, cod_producto:String){

        preferencias = this@Producto_agregar.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferencias!!.getString("ip", ""), preferencias!!.getInt("puerto", 0).toString(), this@Producto_agregar)

        try {
            val datos = ActualizarPrecioPersonalizadoJSON(
                id_empleado,
                cod_producto
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = servidor + "token/search"
            val url = URL(ruta)

            val sslContext = utilidades.crearSslInseguro()

            with(url.openConnection() as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier{_, _ -> true}
                }

                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    if (responseCode == 201) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }
                                it.close()

                                val res: JSONObject = JSONObject(respuesta.toString())
                                precioAutorizado = res.getString("precio_asig").toString().toFloat();

                                runOnUiThread {
                                    AlertaPrecio(this@Producto_agregar)
                                }
                            } catch (e: Exception) {
                                println("ERROR AL VERIFICAR EL PRECIO AUTORIZADO -> " + e.message)
                            }
                        }
                    }else {
                        runOnUiThread {
                            mensajeError()
                        }
                    }
                } catch (e: Exception) {
                    println("error: " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR AL CARGAR VENTANA DE CAMBIAR PRECIO -> " + e.message)
        }
    }

    //FUNCION PARA CONFIRMAR LA UTILIZACION DEL TOKEN
    private fun confirmarToken(id_empleado:Int, cod_producto:String){

        preferencias = this@Producto_agregar.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferencias!!.getString("ip", ""), preferencias!!.getInt("puerto", 0).toString(), this@Producto_agregar)

        try {
            val datos = ActualizarPrecioPersonalizadoJSON(
                id_empleado,
                cod_producto
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = servidor + "token/update"
            val url = URL(ruta)

            val sslContext = utilidades.crearSslInseguro()

            with(url.openConnection() as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier{_, _ -> true}
                }

                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto)
                    or.flush()
                    if (responseCode == 201) {
                        agregarProducto()
                    }else {
                        runOnUiThread {
                            mensajeErrorProcesar()
                        }
                    }
                } catch (e: Exception) {
                    println("error: " + e.message)
                }
            }
        } catch (e: Exception) {
            println("error: " + e.message)
        }
    }

    //MENSAJE DE ERROR
    private fun mensajeError(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = getString(R.string.error_titulo)
        tvMensaje.text = "NO ENCONTRÓ PRECIO AUTORIZADO"
        tvUpdate.text = getString(R.string.error_aceptar)

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
        }

        tvCancel.visibility = View.GONE

        updateDialog.show()

    }

    //MENSAJE DE ERROR
    private fun mensajeErrorProcesar(){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = getString(R.string.error_titulo)
        tvMensaje.text = "ERROR AL AGREGAR EL PRODUCTO AL PEDIDO"
        tvUpdate.text = getString(R.string.error_aceptar)

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
        }

        tvCancel.visibility = View.GONE

        updateDialog.show()

    }

    //FUNCION PARA AGREGAR EL PRODUCTO SELECCIONADO AL PEDIDO
    private fun agregarProducto(){

        val bonificacion = binding.txtBonificados.text.toString().toInt()

        //Obtenemos el valor el valor del Spinner de Escalas
        val valor = binding.spprecio.selectedItem.toString()
        var precio: Double = 0.0

        var esPrecioEditado = false
        if (valor.last() == '*') {
            esPrecioEditado = true
            precio = valor.substringBefore('*').toDouble()
        }else{
            precio = valor.substringBefore(" ").toDouble()
        }

        when(clienteMayorista){
            "S" -> {
                try {
                    if (idpedido > 0) {
                        if (idpedidodetalle!! > 0) {
                            updateDetalle(idpedidodetalle!!, esPrecioEditado, bonificacion, precio.toFloat())
                        } else {
                            val id = validateProduct(idproducto!!)
                            if (id > 0) {
                                val data = getPedidodetalle(id)
                                cantidad += data!!.Cantidad!!
                                var t =
                                    ((binding.txttotal.text.toString().toFloat()) + data.Total_iva!!)
                                binding.txttotal.text = "${String.format("%.${decTotales}f".format(t) )}"
                                updateDetalle(id, esPrecioEditado, bonificacion, precio.toFloat())
                            } else {
                                AddDetallePedido(esPrecioEditado, bonificacion, precio.toFloat())
                            }
                        }
                    }
                    runOnUiThread {
                        provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        funciones.mostrarAlerta("ERROR: ${e.message}", this@Producto_agregar, binding.lienzo)
                    }
                }
            }
            else -> {
                // Verificamos que la cantidad si corresponda a la escala seleccionada
                if (cantidad >= cantidadEscala) { //&& precio > 0

                    try {

                        if (idpedido > 0) {
                            if (idpedidodetalle!! > 0) {
                                updateDetalle(idpedidodetalle!!, esPrecioEditado, bonificacion, precio.toFloat())
                            } else {
                                val id = validateProduct(idproducto!!)
                                if (id > 0) {
                                    val data = getPedidodetalle(id)
                                    cantidad += data!!.Cantidad!!
                                    var t =
                                        ((binding.txttotal.text.toString().toFloat()) + data.Total_iva!!)
                                    binding.txttotal.text = "${String.format("%.${decTotales}f".format(t) )}"
                                    updateDetalle(id, esPrecioEditado, bonificacion, precio.toFloat())
                                } else {
                                    AddDetallePedido(esPrecioEditado, bonificacion, precio.toFloat())
                                }
                            }
                        }
                        runOnUiThread {
                            provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            funciones.mostrarAlerta("ERROR: ${e.message}", this@Producto_agregar, binding.lienzo)
                        }
                    }
                }
                else {
                    runOnUiThread {
                        funciones.mostrarAlerta("PRECIO O CANTIDAD SON VALORES INCORRECTOS", this@Producto_agregar, binding.lienzo)
                    }
                }
            }
        }

    }

    //SELECCIONANDO ESCALA PARA EDITAR PRODUCTO EN DETALL
    private fun seleccionarCantidadenEscala(idPedido: Int, idProducto: Int): Float{
        val db = funciones.obtenerInstancia(this@Producto_agregar).openHelper.readableDatabase
        var cantidadEscala = 0f
        try {
            val consulta = "SELECT IP.Cantidad FROM detalle_pedidos AS DP " +
                    "INNER JOIN inventario_precios AS IP " +
                    "ON DP.Id_Inventario_Precios = IP.Id " +
                    "WHERE DP.Id_pedido=$idPedido AND DP.Id_producto=$idProducto"

            val cursor = db.query(consulta)

            cantidadEscala = if(cursor.count > 0){
                cursor.moveToFirst()
                cursor.getFloat(0)
            }else{
                0f
            }
            cursor.close()
        }catch (e: Exception){
            println("ERROR: AL SELECCIONAR LA ESCALA -> " + e.message)
        }
        return cantidadEscala
    }

    //FUNCION PARA REGRESAR AL DETALLE DEL PEDIDO
    private fun provieneDetallePedido(idpedido: Int, idcliente: Int?, nombrecliente: String?, idvisita: Int, codigo: String, visita: String,
        idapi: Int,
        sucursalPosition: Int?
    ) {
        val intento = Intent(this@Producto_agregar, Detallepedido::class.java)
        intento.putExtra("idpedido", idpedido)
        intento.putExtra("idcliente", idcliente)
        intento.putExtra("nombrecliente", nombrecliente)
        intento.putExtra("visitaid", idvisita)
        intento.putExtra("codigo", codigo)
        intento.putExtra("from", visita)
        intento.putExtra("idapi", idapi)
        intento.putExtra("sucursalPosition", sucursalPosition)
        intento.putExtra("facturaExportacion",false)
        startActivity(intento)
        finish()
    }
}