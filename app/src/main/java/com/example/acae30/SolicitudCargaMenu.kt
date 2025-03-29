package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.SolicitudRecargasController
import com.example.acae30.databinding.ActivitySolicitudCargaMenuBinding
import com.example.acae30.listas.AbonosAdapter
import com.example.acae30.listas.SolicitudAdapter
import com.example.acae30.modelos.Abono
import com.example.acae30.modelos.SolicitudCarga.SolicitudCarga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolicitudCargaMenu : AppCompatActivity() {
    private lateinit var binding : ActivitySolicitudCargaMenuBinding
    private val funciones = Funciones()
    private val solicitudController = SolicitudRecargasController()
    private var alert: AlertDialogo? = null

    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    private var vendedor = ""
    private var idVendedor = 0

    private var idSolicitud : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudCargaMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alert = AlertDialogo(this@SolicitudCargaMenu, this)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        vendedor =  preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)

    }

    override fun onStart() {
        super.onStart()

        mostrarDatos()

        binding.btnAtras.setOnClickListener {
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
            finish()
        }

        binding.actualizarInventario.setOnClickListener {
            mensaje()
        }

        binding.nuevaSolicitud.setOnClickListener {
            insertarNuevaSoliciud()
        }
    }

    //INSERTANDO NUEVA SOLICITUD
    private fun insertarNuevaSoliciud() {
        val fecha = funciones.getFechaHoraProceso()

        val obj : SolicitudCarga = SolicitudCarga(
            0,
            idVendedor,
            vendedor,
            fecha!!,
            0,
            0,
            0,
            "N",
            "",
            0f
        )

        CoroutineScope(Dispatchers.IO).launch {
            idSolicitud = solicitudController.guardarNuevaSolicitud(this@SolicitudCargaMenu, obj)

            withContext(Dispatchers.Main){
                nuevaSolicitud(idSolicitud)
            }
        }
    }

    private fun nuevaSolicitud(solicitud : Int){
        val intent = Intent(this, NuevaSolicitud::class.java)
        intent.putExtra("idSolicitud", solicitud)
        startActivity(intent)
        finish()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    fun mensaje(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿ACTUALIZAR INFORMACION Y EXISTENCIAS DE INVENTARIO \n" +
                    "PARA REALIZAR LA SOLICITUD DE CARGA DE PRODUCTO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                alert!!.Cargando()
                CoroutineScope(Dispatchers.IO).launch {
                    solicitudController.obtenerInventarioServidor(this@SolicitudCargaMenu, alert!!)
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

    //FUNCION MARA MOSTRAR LA LISTA DE ABONOS EN LA ACTIVIDAD
    private fun mostrarDatos(){
        this@SolicitudCargaMenu.lifecycleScope.launch {
            try{
                val lista = solicitudController.obtenerListadosolicitudes(this@SolicitudCargaMenu)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista : ArrayList<SolicitudCarga>) {
        val mLayoutManager = LinearLayoutManager(
            this@SolicitudCargaMenu,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaSolicitud.layoutManager = mLayoutManager
        val adapter = SolicitudAdapter(lista, this@SolicitudCargaMenu){ i ->
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
            val data = lista[i]
            if(data.enviado == 0){
                mensajeEnvio(data.id)
            }
        }
        binding.listaSolicitud.adapter = adapter

    }

    //Funcion para envio de solicitud
    private fun enviarSolicitudCargaServidor(idSolicitudCarga: Int){
        this@SolicitudCargaMenu.lifecycleScope.launch {
            var enviado = false

            enviado = solicitudController.enviarSolicitudCargaAlServidor(this@SolicitudCargaMenu, idSolicitudCarga)

            runOnUiThread {
                mensajeConfirmacion(enviado)
            }
        }
    }

    //FUNCION DE MENSAJES DE ENVIO
    fun mensajeEnvio(idSolicitudCarga : Int){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA ENVIAR LA SOLICITUD DE CARGA AL SERVIDOR?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                enviarSolicitudCargaServidor(idSolicitudCarga)
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
    fun mensajeConfirmacion(enviado : Boolean){
        val mensaje = if(enviado){
            "SOLICITUD ENVIADA CORRECTAMENTE"
        }else{
            "ERROR PROBLEMAS DE CONEXION \n" +
                    " SOLICITUD ALMACENADA, TRATE DE ENVIAR MAS TARDE"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                val intent = Intent(this, SolicitudCargaMenu::class.java)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }
}