package com.example.acae30

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.AbonosController
import com.example.acae30.databinding.ActivityAbonosCxcBinding
import com.example.acae30.listas.AbonosAdapter
import com.example.acae30.listas.TokenAdapter
import com.example.acae30.modelos.Abono
import com.example.acae30.modelos.PrecioPersonalizado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sign

class AbonosCxc : AppCompatActivity() {

    private lateinit var binding: ActivityAbonosCxcBinding
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var abonosController = AbonosController()
    private var funciones = Funciones()

    private var alert: AlertDialogo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbonosCxcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alert = AlertDialogo(this@AbonosCxc)

        preferencias = getSharedPreferences(this.instancia, MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            regresarInicio()
        }

        binding.nuevoAbono.setOnClickListener {
            val editor = preferencias.edit()
            editor.putString("vista", "abono")
            editor.apply()

            val intento = Intent(this, Cuentas_list::class.java)
            startActivity(intento)
            finish()
        }

        CoroutineScope(Dispatchers.IO).launch {
            mostrarDatos()
        }

        binding.btnSincronizar.setOnClickListener {
            if(funciones.isInternetAvailable(this@AbonosCxc)){
                CoroutineScope(Dispatchers.IO).launch {
                    val abonosNoTransmitidos : ArrayList<Abono> = abonosController.obtenerAbonosNoEnviados(this@AbonosCxc)
                    if(abonosNoTransmitidos.size > 0){
                        runOnUiThread {
                            alert!!.Cargando()
                            for(i in 0 until abonosNoTransmitidos.size){
                                val item = abonosNoTransmitidos[i]
                              /*  val abono = Abono(
                                    item.Fecha,
                                    item.IdCliente,
                                    item.codigoCliente,
                                    item.Cliente,
                                    item.IdSucursal,
                                    item.Sucursal,
                                    item.Abono,
                                    item.Tipo_pago,
                                    item.Numero_cheque,
                                    item.Cuenta,
                                    item.Banco,
                                    item.IdVendedor,
                                    item.Vendedor,
                                    item.Fecha_hora_proceso,
                                    item.Id_app_visita,
                                    item.PedidoEnviado
                                )*/
                            }
                        }
                        //PROCESO DE AUTO ENVIADO

                    }else{
                        runOnUiThread {
                            funciones.mensaje(this@AbonosCxc, "NO SE ENCONTRARON ABONOS SIN ENVIAR")
                        }
                    }
                }
            }else{
                funciones.mensaje(this@AbonosCxc,"ENCIENDE TUS DATOS O EL WIFI")
            }
        }

    }

    @Deprecated("This method has been deprecated in favor of using the\n     " +
            " {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      " +
            "The OnBackPressedDispatcher controls how back button events are dispatched\n     " +
            " to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun regresarInicio(){
        val intent = Intent(this@AbonosCxc, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    //FUNCION MARA MOSTRAR LA LISTA DE ABONOS EN LA ACTIVIDAD
    private fun mostrarDatos(){
        this@AbonosCxc.lifecycleScope.launch {
            try{
                val fecha = funciones.obtenerFecha()
                val lista = abonosController.obtenerAbonosSQLite(this@AbonosCxc, fecha!!)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista : ArrayList<Abono>) {
        val mLayoutManager = LinearLayoutManager(
            this@AbonosCxc,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaAbonos.layoutManager = mLayoutManager
        val adapter = AbonosAdapter(lista, this@AbonosCxc)
        binding.listaAbonos.adapter = adapter

    }
}