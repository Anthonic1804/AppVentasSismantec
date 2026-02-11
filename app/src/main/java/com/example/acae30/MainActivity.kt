package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.ConexionController
import com.google.android.material.snackbar.Snackbar
import io.kotzilla.sdk.KotzillaSDK
import io.kotzilla.sdk.analytics.koin.analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var ip: TextView? = null
    private var puerto: TextView? = null
    private var vista: View? = null
    private var alerta: AlertDialogo? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null
    private var funciones: Funciones? = null
    private var reconfig = false
    private var listaServidor : Spinner? = null
    private var btnGuardarServidor: Button? = null

    private var clientesController = ClientesController()
    private var conexionController = ConexionController()
    private var alert: AlertDialogo? = null

    private var servidor: String = ""

    private lateinit var puntoVenta : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        funciones = Funciones()
        reconfig = intent.getBooleanExtra("reconfig", false)

        alerta = AlertDialogo(this, this)
        ip = findViewById(R.id.txtip)
        puerto = findViewById(R.id.txtpuerto)
        vista = findViewById(R.id.alerta)
        puntoVenta = findViewById(R.id.tvPuntoVenta)
        listaServidor = findViewById(R.id.spServidor)
        btnGuardarServidor = findViewById(R.id.btnGuardarServidorConexion)


        //amarramos el widgets a las variables
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        btnGuardarServidor!!.setOnClickListener {
            validar()
        }

        alert = AlertDialogo(this@MainActivity, this)

        cargaInicial()
        cargarServidores()
    }

    override fun onStart() {
        super.onStart()

        listaServidor!!.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {


                lifecycleScope.launch(Dispatchers.IO) {
                    servidor = parent?.getItemAtPosition(position).toString()
                    try {
                        if(servidor == "-- SELECCIONE --"){
                            withContext(Dispatchers.Main){
                                withContext(Dispatchers.Main){
                                    ip!!.text = ""
                                    puerto!!.text = ""
                                    btnGuardarServidor!!.isEnabled = false
                                }
                            }
                        }else{
                            val servidorSeleccionado = conexionController.obtenerInformacionServidorSeleccionado(this@MainActivity, servidor)
                            val ipServidor = servidorSeleccionado!!.ip.trim()
                            val puertoServidor = servidorSeleccionado.puerto.trim()

                            withContext(Dispatchers.Main){
                                ip!!.text = ipServidor
                                puerto!!.text = puertoServidor
                                btnGuardarServidor!!.isEnabled = true
                            }

                        }
                    }catch (e:Exception){
                        println("ERROR AL TRAER LA INFORMACION DEL SERVIDOR -> " + e.message)
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

    }

    //FUNCION PARA VALIDAR EL SERVIODR Y CARGA DE DATOS AUTOMATIVOS
    private fun cargaInicial(){
        //VALIDANDO LA CARGA AUTOMATICA DE LOS CATALOGOS
        val cargaAutomaticaCatalogos = preferencias!!.getBoolean("cargaAutomaticaCatalogos", false)
        if(!cargaAutomaticaCatalogos){
            validateServer()
        }else{
            //AQUI CARGARA LOS CATALOGOS DE CLIENTES

            alert!!.Cargando()

            CoroutineScope(Dispatchers.IO).launch {

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO INFORMACION DE CLIENTES")
                }

                delay(1000)

                try {
                    clientesController.obtenerClientesServidor(this@MainActivity)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LOS CLIENES -> " + e.message)
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO INFORMACION DE SUCURSALES")
                }

                delay(1000)

                try {
                    clientesController.obtenerClienteSucursalesServidor(this@MainActivity)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LAS SUCURSALES -> " + e.message)
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO PRECIOS PERSONALIZADOS")
                }

                delay(1000)

                try {
                    clientesController.obtenerPreciosPersonalizados(this@MainActivity)
                }catch (e: Exception){
                    println("ERROR AL OBTENER LOS PRECIOS PERSONALIZADO")
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO CUENTAS POR COBRAR")
                }

                delay(1000)

                try {
                    clientesController.obtenerCxcServidor(this@MainActivity)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LAS CXC -> " + e.message)
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGA COMPLETA!!")
                    alert!!.dismisss()

                    //AL FINALIZAR VALIDARÁ EL SERVIDOR
                    validateServer()
                }

            }
        }
    }

    private fun validateServer() {
        if (preferencias!!.contains("puerto") && preferencias!!.contains("ip")) {
            if (reconfig) {
                ip!!.text = preferencias!!.getString("ip", "")
                puerto!!.text = preferencias!!.getInt("puerto", 0).toString()
            } else {
                if (preferencias!!.contains("sesion")) {
                    var sesionactiva = preferencias!!.getBoolean("sesion", false)
                    if (sesionactiva) {
                        val intento = Intent(this, Inicio::class.java)
                        startActivity(intento)
                        finish()
                    } else {
                        preferencias!!.edit().remove("Idvendedor").commit()
                        preferencias!!.edit().remove("Vendedor").commit()
                        val intento = Intent(this, Login::class.java)
                        startActivity(intento)
                        finish()
                    }
                } else {
                    preferencias!!.edit().remove("Idvendedor").commit()
                    preferencias!!.edit().remove("Vendedor").commit()
                    val intento = Intent(this, Login::class.java)
                    startActivity(intento)
                    finish()
                }
            } //valida si es una reconfiguracion

        }
    } //valida que ya se tenga la conexion al servidor guardada

    fun validar() {
        if (ip!!.text.isNotEmpty() && puerto!!.text.isNotEmpty() && puntoVenta.text.isNotEmpty()) {
            alerta!!.Cargando()
            val v = vista
            CoroutineScope(Dispatchers.IO).launch {
                val ip = ip!!.text.toString()
                val p = puerto!!.text.toString()
                val pVenta = puntoVenta.text.toString()
                if (funciones!!.isInternetAvailable(this@MainActivity)) {
                    ComproBarConexion(ip, p, pVenta)
                } else {
                    funciones!!.mostrarAlerta("ENCIENDE EL WIFI PARA CONTINUAR", this@MainActivity, vista!!)
                    alerta!!.dismisss()
                }
            }

        } else {
            val alerta: Snackbar =
                Snackbar.make(this.vista!!, "Debes llenar los campos", Snackbar.LENGTH_LONG)
            alerta.view.setBackgroundColor(resources.getColor(R.color.moderado))
            alerta.show()

        }
    } //funcion que valida que haya internet,revisa si se han llenado las cajas y llama la peticio


    fun ComproBarConexion(ip: String, puerto: String, pVenta: String) {
        try {
            val ruta: String = "http://$ip:$puerto/conexion" //ruta de la api
            val url = URL(ruta)
            val ctx = this.vista
            with(url.openConnection() as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"  // optional default is GET
                    val i: Int? = responseCode
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use {
                            val response = StringBuffer()
                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            } //obtenemo la respuesta del servidor
                            it.close()
                            val respuesta =
                                JSONArray(response.toString()) //se convierte en un json array
                            if (respuesta.length() > 0) {
                                val error = respuesta.getJSONObject(0)
                                if (error.getInt("error") == 200) {
                                    val editor = preferencias!!.edit()
                                    editor!!.putInt("puerto", puerto.toInt())
                                    editor.putString("ip", ip)
                                    editor.putString("puntoVenta", pVenta)
                                    editor.putString("nombreServidor", servidor)
                                    editor.commit()
                                    //se guarda la direccion del servidor y se envia al login
                                    alerta!!.dismisss()
                                    val intet: Intent = Intent(ctx!!.context, Login::class.java)
                                    startActivity(intet)
                                    //redirige hacia el login
                                    this@MainActivity.finish() //termina la actividad
                                } else {
                                    throw Exception(error.getString("response"))
                                }
                            } else {
                                throw  Exception("No se ha Recibido Respuesta del Servidor")
                            }
                        }
                    } else {
                        throw  Exception("No se encontro el Servidor")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }

        } catch (e: Exception) {
            alerta!!.dismisss()
            if (e.message.toString() == "Host unreachable") {
                val alert: Snackbar =
                    Snackbar.make(this.vista!!, "Servidor no Encontrado", Snackbar.LENGTH_LONG)
                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                alert.show()
            } else {
                val alert: Snackbar =
                    Snackbar.make(this.vista!!, e.message.toString(), Snackbar.LENGTH_LONG)
                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                alert.show()
            }

        }
    } //comprueba la comunicacion

    override fun onStop() {
        if (ip!!.text.length > 0) {
            preferencias!!.edit().putString("lbl1", ip!!.text.toString()).commit()
        }
        if (puerto!!.text.length > 0) {
            preferencias!!.edit().putInt("lbl2", puerto!!.text.toString().toInt()).commit()
        }

        //guardamos si se ha escrito algo el usuario en las cajas de texto.
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        var cajaip = ""
        var cajapuerto = ""
        if (preferencias!!.contains("lbl1")) {
            cajaip = preferencias!!.getString("lbl1", "").toString()
            preferencias!!.edit().remove("lbl1").commit()
        }
        if (preferencias!!.contains("lbl2")) {
            cajapuerto = preferencias!!.getInt("lbl2", 0).toString()
            preferencias!!.edit().remove("lbl2").commit()
        }
        //obtenemos los datos si hay
        ip!!.text = cajaip
        puerto!!.text = cajapuerto
        //se asignan a las cajas


        //los removemos de las preferencias
    }

    override fun onDestroy() {
        preferencias!!.edit().remove("lbl1").commit()
        preferencias!!.edit().remove("lbl2").commit()
        //los removemos de las preferencias
        super.onDestroy()
    } //se llama cuando se destruya la actividad

    private var doubleBackToExitPressedOnce = false
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Presiona Nuevamente Para Salir", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)

    }//anula el boton atras

    //--------------------------------
    //Funcion para obtener el listado de servidores
    //--------------------------------
    private fun cargarServidores(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val lista = conexionController.obtenerListadoNombreServidores(this@MainActivity)
                val servidor = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, lista)
                servidor.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                listaServidor!!.adapter = servidor
            }catch (e:Exception){
                println("ERROR AL TRAER LA LISTA DE SERVIDORES -> " + e.message)
            }
        }
    }

}