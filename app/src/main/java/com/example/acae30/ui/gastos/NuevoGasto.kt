package com.example.acae30.ui.gastos

import android.R
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.Funciones
import com.example.acae30.controllers.GastosController
import com.example.acae30.databinding.ActivityNuevoGastoBinding
import com.example.acae30.modelos.GastoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoGasto : AppCompatActivity() {
    private lateinit var binding : ActivityNuevoGastoBinding
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var funciones = Funciones()
    private var gastoController = GastosController()

    private var vendedor = ""
    private var idVendedor = 0
    private var formaPagoSeleccionada = "-- SELECCIONE --"
    private var TIPO_MOVIMIENTO : String = "Egreso Vario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoGastoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        vendedor =  preferencias!!.getString("Vendedor", "").toString()
        idVendedor = preferencias!!.getInt("Idvendedor", 0)

        //COMPLETANDO SPINNER FORMA PAGO
        val formaPago = ArrayAdapter<String>(this@NuevoGasto, R.layout.simple_spinner_dropdown_item)
        formaPago.addAll(listOf("-- SELECCIONE --","Efectivo", "Cheque"))
        binding.spFormaPago.adapter = formaPago

        //DESACTIVANDO EL BOTON DE INGRESAR
        binding.btnAceptar.isEnabled = false
        binding.btnAceptar.setBackgroundResource(com.example.acae30.R.drawable.border_btndisable)
    }

    override fun onStart() {
        super.onStart()

        binding.btnCancelar.setOnClickListener {
            mensaje("CANCELAR")
        }

        binding.btnAceptar.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                procesarGasto()
            }
        }

        //IMPLEMENTANDO LOGICA DE TIPO DE PAGO SELECCIONADA EN SPINNER
        binding.spFormaPago.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    }

    //FUNCION PARA PROCESAR EL ENVIO DEL GASTO
    private suspend fun procesarGasto() {
        if(funciones.isInternetAvailable(this@NuevoGasto)){
            if(binding.txtMonto.text!!.isEmpty() || binding.txtMonto.text.toString().toFloat() <= 0){
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@NuevoGasto,
                        "Debe de ingresar un monto correcto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{

                val gasto : GastoModel = GastoModel(
                    funciones.obtenerFecha().toString(),
                    TIPO_MOVIMIENTO,
                    binding.txtDescripcion.text.toString(),
                    binding.tvNumCuentaCheque.text.toString(),
                    binding.tvBanco.text.toString(),
                    binding.tvNumCheque.text.toString(),
                    "",
                    binding.txtMonto.text.toString().toFloat(),
                    formaPagoSeleccionada,
                    vendedor,
                    1
                )

                val respuesta = gastoController.enviarGastoAlServidor(this@NuevoGasto, gasto)
                if(respuesta){
                    withContext(Dispatchers.Main) {
                        mensajeConfirmacion()
                    }
                }
            }
        }else{
            funciones.mensaje(this@NuevoGasto,"ENCIENDE TUS DATOS O EL WIFI")
        }
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensaje(tipo:String){
        var mensaje = ""
        mensaje = when(tipo){
            "CANCELAR" -> {
                "¿DESEA CANCELAR EL PROCESO?"
            }
            else -> {
                "¿DESEA INGRESAR EL ABONO?"
            }
        }
        val dialog = AlertDialog.Builder(this@NuevoGasto)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->

                when(tipo){
                    "CANCELAR" -> {
                        view.dismiss()
                        regresar()
                    }
                    else -> {
                        view.dismiss()
                        CoroutineScope(Dispatchers.IO).launch {
                            procesarGasto()
                        }
                    }
                }
            }
            .setNegativeButton("CANCELAR"){ view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(com.example.acae30.R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeConfirmacion(){
        val dialog = AlertDialog.Builder(this@NuevoGasto)
            .setTitle("INFORMACION")
            .setMessage("GASTO REGISTRADO CORRECTAMENTE")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                regresar()
            }
            .setCancelable(false)
            .setIcon(com.example.acae30.R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun regresar(){
        val intent = Intent(this@NuevoGasto, MenuGasto::class.java)
        startActivity(intent)
        finish()
    }

    //FUNCION PARA VALIDAR EL ENVIO
    private fun validar(){
        if(formaPagoSeleccionada == "-- SELECCIONE --"){
            binding.btnAceptar.isEnabled = false
            binding.btnAceptar.setBackgroundResource(com.example.acae30.R.drawable.border_btndisable)
        }else{
            binding.btnAceptar.isEnabled = true
            binding.btnAceptar.setBackgroundResource(com.example.acae30.R.drawable.border_btnactualizar)
        }
    }
}