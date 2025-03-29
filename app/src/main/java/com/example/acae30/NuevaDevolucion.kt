package com.example.acae30

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.SolicitudDevolucionesController
import com.example.acae30.databinding.ActivityNuevaDevolucionBinding
import com.example.acae30.listas.DevolucionDetalleAdapter
import com.example.acae30.modelos.SolcitudDevolucion.SolicitudDevolucionDetalle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NuevaDevolucion : AppCompatActivity() {

    private var preferencias : SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var solicitudDevoluciones = SolicitudDevolucionesController()
    private lateinit var binding : ActivityNuevaDevolucionBinding

    private var vendedor = ""
    private var hojaCarga = 0

    private var idDevolucion = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaDevolucionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        idDevolucion = intent.getIntExtra("idDevolucion", 0)
        vendedor = preferencias!!.getString("Vendedor", "").toString()
        hojaCarga = preferencias!!.getInt("hojaCarga", 0)

        cargarDetalle()
    }

    override fun onStart() {
        super.onStart()

        binding.tvEmpleadoDevolucion.text = vendedor
        binding.tvHojaCarga.text = hojaCarga.toString()

        binding.btnAtras.setOnClickListener {
            mensajeCancelar()
        }

        binding.btnAgregarProducto.setOnClickListener {
            agregarProducto()
        }

        binding.btnaceptar.setOnClickListener {
            this@NuevaDevolucion.lifecycleScope.launch {
                val detalle = solicitudDevoluciones.obtenerDetalleDevolucion(this@NuevaDevolucion, idDevolucion)
                if (detalle.size > 0){
                    var enviado = false

                    enviado = solicitudDevoluciones.enviarDevolucionAlServidor(this@NuevaDevolucion, idDevolucion)

                    runOnUiThread {
                        mensajeConfirmacion(enviado)
                    }
                }else{
                    runOnUiThread {
                        Toast.makeText(this@NuevaDevolucion,"EL DETALLE NO SE PUEDE ENVIAR SIN PRODUCTOS", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    fun mensajeConfirmacion(enviado : Boolean){
        val mensaje = if(enviado){
            "DEVOLUCION ENVIADA CORRECTAMENTE"
        }else{
            "ERROR PROBLEMAS DE CONEXION \n DEVOLUCION ALMACENADA, TRATE DE ENVIAR MAS TARDE"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    solicitudDevoluciones.descargarProductosInventario(idDevolucion, this@NuevaDevolucion)
                }
                view.dismiss()
                menuDevoluciones()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun menuDevoluciones(){
        val intento = Intent(this, MenuDevoluciones::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    solicitudDevoluciones.eliminarDevolucion(this@NuevaDevolucion, idDevolucion)
                }
                view.dismiss()
                menuDevoluciones()
            }
            .setNegativeButton("CANCELAR"){view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun agregarProducto(){
        val intento = Intent(this, ListadoProductosSolicitud::class.java)
        intento.putExtra("vista", "devolucion")
        intento.putExtra("idDevolucion", idDevolucion)
        startActivity(intento)
        finish()
    }

    private fun cargarDetalle(){
        this.lifecycleScope.launch {
            try {
                val lista = solicitudDevoluciones.obtenerDetalleDevolucion(this@NuevaDevolucion, idDevolucion)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                Toast.makeText(this@NuevaDevolucion,"ERROR AL MOSTRAR EL DETALLE DE LA DEVOLUCION", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun armarLista(lista: ArrayList<SolicitudDevolucionDetalle>) {

        val mLayoutManager = LinearLayoutManager(
            this@NuevaDevolucion,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvDetalleDevolucion.layoutManager = mLayoutManager
        val adapter = DevolucionDetalleAdapter(lista, this@NuevaDevolucion) { i ->
            /* if(pedido!!.Enviado != 1 && from == "visita"){
                 val data = lista[i]
                 val intento = Intent(this@Detallepedido, Producto_agregar::class.java)
                 intento.putExtra("idpedidodetalle", data.Id)
                 intento.putExtra("idpedido", data.Id_pedido)
                 intento.putExtra("idcliente", idcliente)
                 intento.putExtra("nombrecliente", nombre)
                 intento.putExtra("idproducto", data.Id_producto)
                 intento.putExtra("proviene", "editar")
                 intento.putExtra("total_param", data.Total_iva)
                 intento.putExtra("sucursalPosition", getSucursalPosition)
                 intento.putExtra("facturaExportacion", FacturaExportacion)
                 startActivity(intento)
                 finish()
             }*/
        }
        //binding.txttotal.text = "$" + "${String.format("%.4f", total)}"
        binding.rvDetalleDevolucion.adapter = adapter

    }

}