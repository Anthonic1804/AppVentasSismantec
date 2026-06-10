package com.example.acae30.ui.devoluciones

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.Inicio
import com.example.acae30.ui.devoluciones.NuevaDevolucion
import com.example.acae30.R
import com.example.acae30.controllers.SolicitudDevolucionesController
import com.example.acae30.databinding.ActivityMenuDevolucionesBinding
import com.example.acae30.listas.DevolucionAdapter
import com.example.acae30.modelos.SolcitudDevolucion.SolicitudDevolucion
import kotlinx.coroutines.launch

class MenuDevoluciones : AppCompatActivity() {

    private lateinit var binding : ActivityMenuDevolucionesBinding
    private var solicitudDevoluciones = SolicitudDevolucionesController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuDevolucionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            inicio()
        }

        binding.nuevaDevolucion.setOnClickListener {
            this@MenuDevoluciones.lifecycleScope.launch {
                val idDevolucion = solicitudDevoluciones.crearDevolucion(this@MenuDevoluciones)

                if(idDevolucion > 0){
                    runOnUiThread {
                        nuevaDevolucion(idDevolucion)
                    }
                }else{
                    Toast.makeText(this@MenuDevoluciones, "Error al crear la devolucion", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        mostrarDatos()
    }

    private fun nuevaDevolucion(idDevolucion : Int){
        val intento = Intent(this, NuevaDevolucion::class.java)
        intento.putExtra("idDevolucion", idDevolucion)
        startActivity(intento)
        finish()
    }

    private fun inicio(){
        val intento = Intent(this, Inicio::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION MARA MOSTRAR LA LISTA DE ABONOS EN LA ACTIVIDAD
    private fun mostrarDatos(){
        this@MenuDevoluciones.lifecycleScope.launch {
            try{
                val lista = solicitudDevoluciones.obtenerListadoDevoluciones(this@MenuDevoluciones)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista : ArrayList<SolicitudDevolucion>) {
        val mLayoutManager = LinearLayoutManager(
            this@MenuDevoluciones,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaDevoluciones.layoutManager = mLayoutManager
        val adapter = DevolucionAdapter(lista, this@MenuDevoluciones) { i ->
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
            if (data.Numero == 0) {
                mensajeEnvio(data.Id)
            }
        }
        binding.listaDevoluciones.adapter = adapter

    }

    //FUNCION DE MENSAJES DE ENVIO
    fun mensajeEnvio(idDevolucion : Int){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA ENVIAR LA DEVOLUCION AL SERVIDOR?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                enviarSolicitudCargaServidor(idDevolucion)
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
            "DEVOLUCION ENVIADA CORRECTAMENTE"
        }else{
            "ERROR PROBLEMAS DE CONEXION \n" +
                    " DEVOLUCION ALMACENADA, TRATE DE ENVIAR MAS TARDE"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                val intent = Intent(this, MenuDevoluciones::class.java)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //Funcion para envio de solicitud
    private fun enviarSolicitudCargaServidor(idDevolucion: Int){
        this@MenuDevoluciones.lifecycleScope.launch {
            var enviado = false

            enviado = solicitudDevoluciones.enviarDevolucionAlServidor(this@MenuDevoluciones, idDevolucion)

            runOnUiThread {
                mensajeConfirmacion(enviado)
            }
        }
    }



}