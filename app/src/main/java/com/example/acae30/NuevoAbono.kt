package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.controllers.AbonosController
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.SucursalesController
import com.example.acae30.databinding.ActivityNuevoAbonoBinding
import com.example.acae30.modelos.Abono
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.InformacionSucursal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoAbono : AppCompatActivity() {

    private lateinit var binding : ActivityNuevoAbonoBinding
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var funciones = Funciones()
    private var clienteController = ClientesController()
    private var sucursalesController = SucursalesController()
    private var abonosController = AbonosController()
    private var formaPagoSeleccionada = "-- SELECCIONE --"
    private var sucursal = "-- SELECCIONES UNA SUCURSAL --"
    private var idCliente = 0
    private var datosCliente : Cliente? = null
    private var datosSucursal : InformacionSucursal? = null
    private var vendedor = ""
    private var idVendedor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoAbonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        vendedor =  preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)

        //COMPLETANDO SPINNER FORMA PAGO
        val formaPago = ArrayAdapter<String>(this@NuevoAbono, android.R.layout.simple_spinner_dropdown_item)
        formaPago.addAll(listOf("-- SELECCIONE --","EFECTIVO", "CHEQUE"))
        binding.spFormaPago.adapter = formaPago

        //COMPLETANDO SPINNER SUCURSAL
        cargarSucursales()

        //DESACTIVANDO EL BOTON DE INGRESAR
        binding.btnAceptar.isEnabled = false
        binding.btnAceptar.setBackgroundResource(R.drawable.border_btndisable)

    }

    override fun onStart() {
        super.onStart()
        idCliente = intent.getIntExtra("idcliente", 0)
        datosCliente = clienteController.obtenerInformacionCliente(this@NuevoAbono, idCliente)!!

        //IMPLEMENTANDO LOGICA DE TIPO DE PAGO SELECCIONADA EN SPINNER
        binding.spFormaPago.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                formaPagoSeleccionada = parent?.getItemAtPosition(position).toString()

                CoroutineScope(Dispatchers.IO).launch {
                    when(formaPagoSeleccionada){
                        "CHEQUE" -> {
                            runOnUiThread{
                                binding.lyContenedorCheque.visibility = View.VISIBLE
                                validar()
                            }
                        }
                        "EFECTIVO" -> {
                            runOnUiThread{
                                binding.lyContenedorCheque.visibility = View.GONE
                                validar()
                            }
                        }
                        else -> {
                            runOnUiThread {
                                validar()
                            }
                        }
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DE LA SUCURSAL SELECCIONADA
        binding.spSucursal.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                sucursal = parent?.getItemAtPosition(position).toString()
                datosSucursal = sucursalesController.obtenerInformacionSucursal(this@NuevoAbono, sucursal, idCliente)

                CoroutineScope(Dispatchers.IO).launch {
                    when(sucursal){
                        "-- SELECCIONES UNA SUCURSAL --" -> {
                            runOnUiThread {
                                validar()
                            }
                        }
                        else -> {
                            runOnUiThread {
                                validar()
                            }
                        }
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.txtCliente.setText(datosCliente!!.Cliente)

        binding.btnCancelar.setOnClickListener {
            mensaje("CANCELAR")
        }

        binding.btnAceptar.setOnClickListener {
            mensaje("ACEPTAR")
        }

    }

    //FUNCION PARA VALIDAR EL ENVIO
    fun validar(){
        if(sucursal == "-- SELECCIONES UNA SUCURSAL --" || formaPagoSeleccionada == "-- SELECCIONE --"){
            binding.btnAceptar.isEnabled = false
            binding.btnAceptar.setBackgroundResource(R.drawable.border_btndisable)
        }else{
            binding.btnAceptar.isEnabled = true
            binding.btnAceptar.setBackgroundResource(R.drawable.border_btnactualizar)
        }
    }

    //FUNCION PARA CARGAR LAS SUCURSALES AL SPINNER
    private fun cargarSucursales() {
        idCliente = intent.getIntExtra("idcliente", 0)
        val listSucursal = sucursalesController.obtenerSucursalesporIdCliente(this@NuevoAbono, idCliente)

        if(listSucursal.isEmpty()){
            binding.spSucursal.visibility = View.GONE
            binding.sinSucursal.visibility = View.VISIBLE
        }else{
            binding.spSucursal.visibility = View.VISIBLE
            binding.sinSucursal.visibility = View.GONE
        }

        val adaptador = ArrayAdapter(this@NuevoAbono, android.R.layout.simple_spinner_item, listSucursal)
        adaptador.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.spSucursal.adapter = adaptador
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensaje(tipo:String){
        var mensaje = ""
        mensaje = when(tipo){
            "CANCELAR" -> {
                "¿DESEA CANCELAR EL PROCESO?"
            }
            "ENVIADO" -> {
                "ABONO REGISTRADO CORRECTAMENTE"
            }
            "ERROR" -> {
                "ERROR AL REGISTRAR EL ABONO"
            }
            else -> {
                "¿DESEA INGRESAR EL ABONO?"
            }
        }
        val dialog = AlertDialog.Builder(this@NuevoAbono)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->

                when(tipo){
                    "CANCELAR" -> {
                        view.dismiss()
                        cancelarAbono()
                    }
                    "ENVIADO" -> {
                        view.dismiss()
                        listadoAbono()
                    }
                    "ERROR" -> {
                        view.dismiss()
                    }
                    else -> {
                        view.dismiss()
                        CoroutineScope(Dispatchers.IO).launch {
                            procesarAbono()
                        }
                    }
                }
            }
            .setNegativeButton("CANCENLAR"){ view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUNCION PARA REGRESAR AL MODULO DE ABONOS
    private fun cancelarAbono(){
        val intento = Intent(this@NuevoAbono, Cuentas_list::class.java)
        startActivity(intento)
        finish()
    }

    private fun listadoAbono(){
        val intento = Intent(this@NuevoAbono, AbonosCxc::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION PARA PROCESAR EL ABONO
    private suspend fun procesarAbono(){
        val abono : Abono = Abono(
            funciones.obtenerFecha(),
            idCliente,
            datosCliente!!.Codigo,
            datosCliente!!.Cliente,
            datosSucursal!!.id,
            sucursal,
            binding.txtMonto.text.toString().toFloat(),
            formaPagoSeleccionada,
            binding.tvNumCheque.text.toString(),
            binding.tvNumCuentaCheque.text.toString(),
            binding.tvBanco.text.toString(),
            idVendedor,
            vendedor,
            funciones.getFechaHoraProceso(),
            1 //LUEGO CAMBIAR POR EL RESPONSE DEL WS
        )

        val respuesta = abonosController.enviarAbonoAlServidor(this@NuevoAbono, abono)
        if(respuesta){
            withContext(Dispatchers.Main){
                mensaje("ENVIADO")
            }
        }else{
            withContext(Dispatchers.Main) {
                mensaje("ERROR")
            }
        }
    }
}