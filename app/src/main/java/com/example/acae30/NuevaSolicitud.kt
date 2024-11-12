package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.SolicitudRecargasController
import com.example.acae30.databinding.ActivityNuevaSolicitudBinding
import com.example.acae30.listas.PedidoDetalleAdapter
import com.example.acae30.listas.SolicitudDetalleAdapter
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.SolicitudCarga.SolicitudCarga
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NuevaSolicitud : AppCompatActivity() {
    private lateinit var binding : ActivityNuevaSolicitudBinding
    private var funciones = Funciones()
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var solicitudController = SolicitudRecargasController()

    private var vendedor = ""
    private var idVendedor = 0

    private var idSolicitud : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaSolicitudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        vendedor =  preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)

        idSolicitud = intent.getIntExtra("idSolicitud", 0)

        cargarDetalle()
    }



    override fun onStart() {
        super.onStart()

        binding.btncancelar.setOnClickListener {
            mensajeCancelar()
        }

        binding.btnenviar.setOnClickListener {
            val enviado = solicitudController.actualizarEstadoSolicitud(this, idSolicitud)
            if(enviado){
                mensajeConfirmacion()
            }
        }

        binding.txtEmpleado.setText(vendedor)

        binding.imgbtnadd.setOnClickListener {
            val intent = Intent(this, ListadoProductosSolicitud::class.java)
            intent.putExtra("idSolicitud", idSolicitud)
            intent.putExtra("proceso", "agregar")
            startActivity(intent)
            finish()
        }

        val fecha = funciones.getFechaHoraProceso()
        binding.fechaSolicitud.text = fecha.toString()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    solicitudController.eliminarSolicitud(this@NuevaSolicitud, idSolicitud)
                }
                view.dismiss()
                menuSolicitudCarga()
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
    fun mensajeConfirmacion(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("SOLICITUD ENVIADA CORRECTAMENTE")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                solicitudListado()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    fun solicitudListado(){
        val intent = Intent(this, SolicitudCargaMenu::class.java)
        startActivity(intent)
        finish()
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
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                Toast.makeText(this@NuevaSolicitud,"ERROR AL MOSTRAR EL DETALLE DE LA SOLICITUD", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun armarLista(lista: ArrayList<SolicitudCargaDetalle>) {
        //var total = 0.toFloat()
        //val pedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        val mLayoutManager = LinearLayoutManager(
            this@NuevaSolicitud,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.cvProductosSolicitados.layoutManager = mLayoutManager
        val adapter = SolicitudDetalleAdapter(lista, this@NuevaSolicitud) { i ->
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
        binding.cvProductosSolicitados.adapter = adapter

    }
}