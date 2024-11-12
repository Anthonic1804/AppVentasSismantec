package com.example.acae30

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.controllers.SolicitudRecargasController
import com.example.acae30.databinding.ActivityAgregarProductoSolicitudBinding
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductoSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        proceso = intent.getStringExtra("proceso").toString()
        idSolicitud = intent.getIntExtra("idSolicitud", 0)
        idProducto = intent.getIntExtra("idProducto", 0)
        codigo = intent.getStringExtra("codigo").toString()
        descripcion = intent.getStringExtra("descripcion").toString()
        existencia = intent.getFloatExtra("existencia", 0f)
        costo = intent.getFloatExtra("costo", 0f)
        costoIva = intent.getFloatExtra("costoIva", 0f)
        precio = intent.getFloatExtra("precio_u", 0f)
        precio_iva = intent.getFloatExtra("precio_u_iva", 0f)

        if(proceso == "agregar"){
            binding.btneliminar.visibility = View.GONE
        }else{
            binding.btnagregar.text = "ACTUALIZAR PRODUCTO"
            binding.btneliminar.visibility = View.VISIBLE
        }

    }

    override fun onStart() {
        super.onStart()


        binding.btnAtras.setOnClickListener {
            mensajeCancelar()
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
            total = precio_iva * cantidad
            val obj : SolicitudCargaDetalle = SolicitudCargaDetalle(
                0,
                idSolicitud,
                idProducto,
                codigo,
                descripcion,
                cantidad,
                costo,
                costoIva,
                precio,
                precio_iva,
                total
            )

            registrarDetalle(obj)
        }

    }

    private fun registrarDetalle(obj: SolicitudCargaDetalle) {
        val registro = solicitudController.insertarDetalleSolicitud(this, obj)
        if(registro){

            Toast.makeText(this,"PRODUCTO AGREGADO CORRECTAMENTE", Toast.LENGTH_LONG).show()

            val intent = Intent(this, NuevaSolicitud::class.java)
            intent.putExtra("idSolicitud", idSolicitud)
            startActivity(intent)
            finish()
        }
    }

    //FUNCION PARA VALIDAD CANTIDAD PARA ESCARRSA
    private fun validarCantidad(cantidadIngresada: String){
        if(cantidadIngresada.isNotEmpty() && isInteger(cantidadIngresada)){
            cantidad = cantidadIngresada.toFloat()

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
    private fun mensajeCancelar(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                listadoInventario()
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
        startActivity(intent)
        finish()
    }
}