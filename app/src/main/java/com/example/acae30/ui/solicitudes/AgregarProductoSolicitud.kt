package com.example.acae30.ui.solicitudes

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acae30.ui.solicitudes.ListadoProductosSolicitud
import com.example.acae30.R
import com.example.acae30.controllers.SolicitudRecargasController
import com.example.acae30.databinding.ActivityAgregarProductoSolicitudBinding
import com.example.acae30.data.local.models.Inventario
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgregarProductoSolicitud : AppCompatActivity() {

    private lateinit var binding : ActivityAgregarProductoSolicitudBinding
    private var solicitudController = SolicitudRecargasController()
    private var idProducto : Int = 0
    private var codigo : String = ""
    private var descripcion : String = ""
    private var existencia : Float = 0f
    private var idSolicitud : Int = 0
    private var costo : Float = 0f
    private var costoIva : Float = 0f
    private var precio : Float = 0f
    private var precio_iva : Float = 0f
    private var cantidad : Float = 0f
    private var total : Float = 0f
    private var proceso : String = ""
    private var enviado : Int = 0
    private var idServidorSolicitud : Int = 0
    private var estado: String = ""
    private lateinit var preferences: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var solicitudSinExistencia: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductoSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        proceso = intent.getStringExtra("proceso").toString()
        idSolicitud = intent.getIntExtra("idSolicitud", 0)
        idServidorSolicitud = intent.getIntExtra("idServidorSolicitud", 0)
        idProducto = intent.getIntExtra("idProducto", 0)
        codigo = intent.getStringExtra("codigo").toString()
        descripcion = intent.getStringExtra("descripcion").toString()
        estado = intent.getStringExtra("estado").toString()
        enviado = intent.getIntExtra("enviado", 0)

        preferences = getSharedPreferences(instancia, MODE_PRIVATE)
        solicitudSinExistencia = preferences.getBoolean("Solicitud_Carga_SinExistencia", false)


        if(proceso == "nuevo"){
            binding.btneliminar.visibility = View.GONE

            existencia = intent.getFloatExtra("existencia", 0f)
            costo = intent.getFloatExtra("costo", 0f)
            costoIva = intent.getFloatExtra("costoIva", 0f)
            precio = intent.getFloatExtra("precio_u", 0f)
            precio_iva = intent.getFloatExtra("precio_u_iva", 0f)

        }else{
            binding.btnagregar.text = "ACTUALIZAR PRODUCTO"
            binding.btneliminar.visibility = View.VISIBLE
            binding.txttituloproducto.text = "ACTUALIZAR PRODUCTO"

            val cantidadActual = intent.getFloatExtra("cantidad", 0f).toInt().toString()
            binding.txtcantidad.setText(cantidadActual)

            //OBTENINDO EL REGISTRO DEL PRODUCTO POR ID
            lifecycleScope.launch(Dispatchers.IO) {
                val item : Inventario? = solicitudController.obtenerInformacionProductoPorCodigo(this@AgregarProductoSolicitud, codigo)
                if(item != null){
                    existencia = item.Existencia!!.toFloat()
                    costo = item.Costo!!.toFloat()
                    costoIva = item.costo_iva!!.toFloat()
                    precio = item.Precio!!.toFloat()
                    precio_iva = item.Precio_iva!!.toFloat()
                }

                withContext(Dispatchers.Main) {
                    totalizar(cantidadActual.toFloat())
                }
            }

        }

    }

    override fun onStart() {
        super.onStart()


        binding.btnAtras.setOnClickListener {
            mensajeCancelar("")
        }

        binding.txtcodigo.text = codigo
        binding.txtdescripcion.text = descripcion
        binding.tvPrecioProducto.text = precio_iva.toString()
        binding.txtexistencia.text = existencia.toString()

        binding.txtcantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(cantidad: Editable) {
                validarCantidad(cantidad.toString())
            }
        })

        binding.btnagregar.setOnClickListener {
            when(proceso){
                "nuevo" -> {agregarProducto()}
                else -> {actualizarProducto()}
            }
        }

        binding.btneliminar.setOnClickListener {
            mensajeCancelar("ELIMINAR")
        }

    }

    //FUNCION QUE GENERA EL OBJETO DETALLE
    private fun agregarProducto(){
        total = precio_iva * cantidad
        val obj : SolicitudCargaDetalle = SolicitudCargaDetalle(
            0,
            idSolicitud,
            idProducto,
            codigo,
            descripcion,
            cantidad,
            0f,
            costo,
            costoIva,
            precio,
            precio_iva,
            total,
            0
        )

        registrarDetalle(obj)
    }

    //FUNCION QUE REGISTRA EL DETALLE EN LA TBL
    private fun registrarDetalle(obj: SolicitudCargaDetalle) {
        val encontrado = solicitudController.validarProductoDetalle(this@AgregarProductoSolicitud, obj)
        var registro : Boolean = false
        registro = if(encontrado){
            //ACTUALIZAREMOS LAS EXISTENCIAS
            solicitudController.actualizarCantidadProductoDetalle(this@AgregarProductoSolicitud, obj)
        }else{
            //REGISTRAREMOS EL NUEVO PRODUCTO
            solicitudController.insertarDetalleSolicitud(this@AgregarProductoSolicitud, obj)
        }


        if(registro){

            Toast.makeText(this,"PRODUCTO AGREGADO CORRECTAMENTE", Toast.LENGTH_LONG).show()

            val intent = Intent(this, NuevaSolicitud::class.java)
            intent.putExtra("idSolicitud", idSolicitud)
            intent.putExtra("proceso", proceso)
            intent.putExtra("idServidorSolicitud", idServidorSolicitud)
            intent.putExtra("estado", estado)
            startActivity(intent)
            finish()
        }
    }

    //FUNCION PARA ACTUALIZAR EL PRODUCTO ENE L DETALLE
    private fun actualizarProducto(){
        lifecycleScope.launch(Dispatchers.IO) {
            val cantidad = binding.txtcantidad.text.trim().toString()
            when(enviado){
                1 -> {
                    val total = precio_iva * cantidad.toFloat()

                    val obj : SolicitudCargaDetalle = SolicitudCargaDetalle(
                        0,
                        idServidorSolicitud,
                        idProducto,
                        codigo,
                        descripcion,
                        cantidad.toFloat(),
                        0f,
                        costo,
                        costoIva,
                        precio,
                        precio_iva,
                        total,
                        enviado
                    )

                    //ACTUALIZAMOS EN SERVIDOR
                    val actualizado = solicitudController.actualizarProductoEnSolicitudServidor(this@AgregarProductoSolicitud, obj)

                    if(actualizado){
                        solicitudController.actualizarProductoDelDetalle(this@AgregarProductoSolicitud, idSolicitud, codigo, cantidad.toInt())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AgregarProductoSolicitud,
                                "PRODUCTO ACTUALIZADO CORRECTAMENTE",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            nuevaSolicitud()
                        }
                    }else{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AgregarProductoSolicitud,
                                "ERROR AL ACTUALIZAR EL PRODUCTO",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            nuevaSolicitud()
                        }
                    }
                }
                else -> {
                    solicitudController.actualizarProductoDelDetalle(this@AgregarProductoSolicitud, idSolicitud, codigo, cantidad.toInt())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AgregarProductoSolicitud,
                            "PRODUCTO ACTUALIZADO CORRECTAMENTE",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        nuevaSolicitud()
                    }
                }
            }
        }
    }

    //FUNCION PARA ELIMINAR EL PRODUCTO DEL DETALLE DE LA SOLICITUD
    private fun eliminarProducto(){
        lifecycleScope.launch(Dispatchers.IO) {
            when(enviado){
                1 -> {
                    val eliminado = solicitudController.eliminarProductoEnSolicitudServidor(this@AgregarProductoSolicitud, idServidorSolicitud, codigo)
                    if(eliminado){
                        solicitudController.eliminarProductoDelDetalle(this@AgregarProductoSolicitud, idSolicitud, codigo)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AgregarProductoSolicitud,
                                "PRODUCTO ELIMINADO CORRECTAMENTE",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            nuevaSolicitud()
                        }
                    }else{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AgregarProductoSolicitud,
                                "ERROR AL ELIMINAR EL PRODUCTO",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            nuevaSolicitud()
                        }
                    }
                }
                else -> {
                    solicitudController.eliminarProductoDelDetalle(this@AgregarProductoSolicitud, idSolicitud, codigo)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AgregarProductoSolicitud,
                            "PRODUCTO ELIMINADO CORRECTAMENTE",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        nuevaSolicitud()
                    }
                }
            }
        }
    }

    //FUNCION PARA VALIDAD CANTIDAD PARA ESCARRSA
    private fun validarCantidad(cantidadIngresada: String){
        if(cantidadIngresada.isNotEmpty() && isInteger(cantidadIngresada)){
            cantidad = cantidadIngresada.toFloat()

            if (!solicitudSinExistencia){
                if(cantidad > existencia || cantidad == 0f){
                    binding.txtcantidad.error = "No puede Agregar una cantidad mayor a las existencias actuales";
                    binding.btnagregar.setBackgroundResource(R.drawable.border_btndisable)
                    binding.btnagregar.isEnabled = false
                }else{
                    binding.btnagregar.isEnabled = true
                    totalizar(cantidad)
                    binding.btnagregar.setBackgroundResource(R.drawable.border_btnenviar)
                }
            }else{
                if(cantidad == 0f){
                    binding.txtcantidad.error = "Debe de Ingresar una Cantidad";
                    binding.btnagregar.setBackgroundResource(R.drawable.border_btndisable)
                    binding.btnagregar.isEnabled = false
                }else{
                    binding.btnagregar.isEnabled = true
                    totalizar(cantidad)
                    binding.btnagregar.setBackgroundResource(R.drawable.border_btnenviar)
                }
            }

        }else{
            binding.txtcantidad.error = "Campo no puede quedar vacio"
            binding.btnagregar.isEnabled = false
            binding.btnagregar.setBackgroundResource(R.drawable.border_btndisable)
            cantidad = 0.toFloat()
            totalizar(cantidad)
        }
    }

    private fun isInteger(cadena: String): Boolean{
        return try{
            cadena.toInt()
            return  true
        }catch (nfe: NumberFormatException){
            binding.txtcantidad.setText("${String.format("", cantidad)}");
            return false
        }
    }

    private fun totalizar(cantidad: Float) {
        var total : Float = 0f
        total = precio_iva * cantidad

        binding.txttotal.text = "${String.format("%.4f".format(total) )}"
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(tipo: String){

        val mensaje = when(tipo){
            "ELIMINAR" -> "¿DESEA ELIMINAR EL PRODUCTO?"
            else -> "¿DESEA CANCELAR EL PROCESO?"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()

                if(tipo.contains("ELIMINAR")){
                    eliminarProducto()
                }else{
                    when(proceso){
                        "editar" -> {nuevaSolicitud()}
                        else -> {listadoInventario()}
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

    private fun listadoInventario(){
        val intent = Intent(this, ListadoProductosSolicitud::class.java)
        intent.putExtra("proceso", proceso)
        intent.putExtra("idSolicitud", idSolicitud)
        intent.putExtra("idServidorSolicitud", idServidorSolicitud)
        intent.putExtra("estado", estado)
        startActivity(intent)
        finish()
    }

    private fun nuevaSolicitud(){
        val intento = Intent(this@AgregarProductoSolicitud, NuevaSolicitud::class.java)
        intento.putExtra("proceso", "nuevo")
        intento.putExtra("idSolicitud", idSolicitud)
        intento.putExtra("idServidorSolicitud", idServidorSolicitud)
        intento.putExtra("estado", estado)
        startActivity(intento)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras
}