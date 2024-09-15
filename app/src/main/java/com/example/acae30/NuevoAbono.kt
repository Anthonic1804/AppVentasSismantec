package com.example.acae30

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.SucursalesController
import com.example.acae30.databinding.ActivityNuevoAbonoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NuevoAbono : AppCompatActivity() {

    private lateinit var binding : ActivityNuevoAbonoBinding
    private var clienteController = ClientesController()
    private var sucursalesController = SucursalesController()
    private var formaPagoSeleccionada = ""
    private var idCliente = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoAbonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //COMPLETANDO SPINNER FORMA PAGO
        val formaPago = ArrayAdapter<String>(this@NuevoAbono, android.R.layout.simple_spinner_dropdown_item)
        formaPago.addAll(listOf("-- SELECCIONE --","EFECTIVO", "CHEQUE", "DEPOSITO A CUENTA"))
        binding.spFormaPago.adapter = formaPago

        //COMPLETANDO SPINNER SUCURSAL
        cargarSucursales()

    }

    override fun onStart() {
        super.onStart()
        idCliente = intent.getIntExtra("idcliente", 0)
        val datosCliente = clienteController.obtenerInformacionCliente(this@NuevoAbono, idCliente)

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
                                binding.lyContenedorDeposito.visibility = View.GONE
                            }
                        }
                        "EFECTIVO" -> {
                            runOnUiThread{
                                binding.lyContenedorCheque.visibility = View.GONE
                                binding.lyContenedorDeposito.visibility = View.GONE
                            }
                        }
                        "DEPOSITO A CUENTA" -> {
                            runOnUiThread {
                                binding.lyContenedorCheque.visibility = View.GONE
                                binding.lyContenedorDeposito.visibility = View.VISIBLE
                            }

                        }
                        else -> {

                        }
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.txtCliente.setText(datosCliente!!.Cliente)

        binding.btnCancelar.setOnClickListener {
            mensaje()
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
    private fun mensaje(){
        val dialog = AlertDialog.Builder(this@NuevoAbono)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                cancelarAbono()
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

}