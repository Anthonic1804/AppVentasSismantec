package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.dcastalia.localappupdate.DownloadApk
import com.example.acae30.Utilidades.AgregarHeaders
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.database.LimpiarBD
import com.example.acae30.controllers.ConexionController
import com.example.acae30.controllers.ConfigController
import com.example.acae30.databinding.ActivityConfiguracionBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection


class Configuracion : AppCompatActivity() {

    private var versionAppServer : String? = null
    private var urlAppServer : String? = null
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private var versionActual : Float = 0f

    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null
    private var alerta: AlertDialogo? = null

    private var configController = ConfigController()
    private var funciones = Funciones()
    private var conexionController = ConexionController()
    private val agregarHeaders = AgregarHeaders()

    private var servidor: String = ""
    private var nombreServidor: String = ""
    private var ipServidor: String = ""
    private var puertoServidor: String = ""
    private var puntoVenta: String = ""

    private var utilidades = CrearSslNoSeguro()

    private var idServidorActivo: Int = 0
    private var sslActivo: Int = 0

    //private var limpiarBD = LimpiarBD()


    private lateinit var binding : ActivityConfiguracionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        alerta = AlertDialogo(this, this)

        nombreServidor = preferencias!!.getString("nombreServidor", "").toString()
        ipServidor = preferencias!!.getString("ip", "").toString()
        puertoServidor = preferencias!!.getInt("puerto", 0).toString()
        puntoVenta = preferencias!!.getString("puntoVenta", "").toString()

        idServidorActivo = preferencias!!.getInt("idServidorActivo", 0)
        sslActivo = preferencias!!.getInt("sslActivo", 0)

        //FUNCIONES AGRAGADAS PARA LOS CONTROLES DE VISTA DE INVENTARIO

        binding.swSinExistencias.isEnabled = false

        //CARGANDO SERVIDORES AL SPINNER
        cargarServidores()

        //ACTUALIZAR CONFIG PARA PEDIDOS SIN EXISTENCIAS
        binding.swSinExistencias.isChecked = preferencias!!.getString("pedidos_sin_existencia", "") == "S"


        // 2 -> LISTADO
        // 1 -> VISTA MINIATURA
        binding.swlista.isChecked = preferencias!!.getInt("vistaInventario", 0) == 2
        binding.swminiatura.isChecked = preferencias!!.getInt("vistaInventario", 0) == 1

        //ACTIVANDO SWITCH DE IMRPESORES
        binding.swBluetooth.isChecked = preferencias!!.getString("tipoImpresora", "") == "BT"
        binding.swIntegrada.isChecked = preferencias!!.getString("tipoImpresora", "") == "INT"

        if(preferencias!!.getString("tipoImpresora", "") == "BT"){
            binding.lyImpresor.visibility = View.GONE
        }

        binding.txtImpresor.setText(preferencias!!.getString("impresorIntegrado", ""))

        versionActualApp()
        binding.tvVersionActualApp.text = "ACAE APP Ver. $versionActual"

        binding.swlista.setOnCheckedChangeListener { _, isChecked ->
            preferencias!!.edit {
                remove("vistaInventario")
                if (isChecked) {
                    binding.swminiatura.isChecked = false
                    putInt("vistaInventario", 2)
                } else {
                    binding.swminiatura.isChecked = true
                    putInt("vistaInventario", 1)
                }
            }
        }

        binding.swminiatura.setOnCheckedChangeListener { _, isChecked ->
            preferencias!!.edit {
                remove("vistaInventario")
                if (isChecked) {
                    binding.swlista.isChecked = false
                    putInt("vistaInventario", 1)
                } else {
                    binding.swlista.isChecked = true
                    putInt("vistaInventario", 2)
                }
            }
        }

        //ACTIVANDO LOGICA DE SWITCH DE IMPRESORES
        binding.swBluetooth.setOnCheckedChangeListener { _, isChecked ->
            preferencias!!.edit {
                remove("tipoImpresora")
                if (isChecked) {
                    binding.swIntegrada.isChecked = false
                    putString("tipoImpresora", "BT")
                    binding.lyImpresor.visibility = View.GONE
                    remove("impresorIntegrado")
                } else {
                    binding.swIntegrada.isChecked = true
                    putString("tipoImpresora", "INT")
                    binding.lyImpresor.visibility = View.VISIBLE
                }
            }

        }

        binding.swIntegrada.setOnCheckedChangeListener { _, isChecked ->
            preferencias!!.edit {
                remove("tipoImpresora")
                if (isChecked) {
                    binding.swBluetooth.isChecked = false
                    putString("tipoImpresora", "INT")
                    binding.lyImpresor.visibility = View.VISIBLE
                } else {
                    binding.swBluetooth.isChecked = true
                    putString("tipoImpresora", "BT")
                    binding.lyImpresor.visibility = View.GONE
                    remove("impresorIntegrado")
                }
            }

        }

        binding.btnImpresor.setOnClickListener {

            var impresor = binding.txtImpresor.text

            preferencias!!.edit{
                remove("impresorIntegrado")
                putString("impresorIntegrado", impresor.toString())
            }

            Toast.makeText(this@Configuracion, "IMPRESOR CONFIGURADO", Toast.LENGTH_SHORT)
                .show()
        }

        binding.btnBuscarUpdate.setOnClickListener {
            if (funciones.isInternetAvailable(this)) {

                CoroutineScope(Dispatchers.IO).launch {
                    obtenerNuevaVersionApp()
                }//COURUTINA CARGAR DATOS DE ACTUALIZACION

            } else {
                ShowAlert("ERROR: NO TIENES CONEXION A INTERNET")
            }
        }

        // Recuperar la imagen guardada al iniciar
        val prefs = getSharedPreferences("MisImagenes", MODE_PRIVATE)
        val filePath = prefs.getString("imagenFile", null)

        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                binding.imgLogoEmpresa.setImageURI(Uri.fromFile(file))
            }
        }else{
            val nombreImagen = "sinlogo"
            val resId = resources.getIdentifier(nombreImagen, "drawable", packageName)

            val drawable = ContextCompat.getDrawable(this, resId)
            binding.imgLogoEmpresa.setImageDrawable(drawable)
        }

        verificarModoDesarrollo()


    } //funcion que inicializa las variables

    override fun onStart() {
        super.onStart()

        obtenerInformacionServidor()

        binding.imgbtnatras.setOnClickListener {
            regresarMenuPrincipal()
        }//boton atras

        binding.btnReconectar.setOnClickListener {
            val contexto = this
            alerta!!.Cargando()

            CoroutineScope(Dispatchers.IO).launch {
                reconectarServidor(binding.txtip.text.toString(), binding.txtpuerto.text.toString(), contexto)
            }

        }//guarda los datos del servidor

        binding.btnCargarConfig.setOnClickListener {
            if (funciones.isInternetAvailable(this@Configuracion)) {
                alerta!!.Cargando()
                CoroutineScope(Dispatchers.IO).launch {

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alerta!!.changeText("CARGANDO CONFIGURACIONES INICIALES")
                    }

                    try {
                        configController.obtenerConfigPagareObligatorio(this@Configuracion)
                    }catch (e:Exception){
                        println("ERROR AL CARGAR LAS CONFIGURACIONES INICIALES " + e.message)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alerta!!.changeText("CONFIGURACIONES INICIALES CARGADAS CORRECTAMENTE")
                    }

                    //FIN DA LA CARGA DE DATOS
                    delay(1500)

                    withContext(Dispatchers.Main){
                        alerta!!.dismisss()

                        //AGREGAR CONDICION PARA CERRAR LA APP SI MODO DESARROLLO ESTÁ ACTIVO
                        //PARA PODER ACTIVAR KOTZILLA
                        val modoDesarrollo = preferencias!!.getBoolean("modoDesarrollo", false)
                        if(modoDesarrollo){
                            mensajeConfirmacion()
                        }else{
                            regresarMenuPrincipal()
                        }
                    }
                }
            } else {
                ShowAlert("ENCIENDE TUS DATOS O EL WIFI")
            }
        }

        binding.imgLogoEmpresa.setOnClickListener {
            seleccionarImagen()
        }

        binding.btnActualizarServidor.setOnClickListener {
            val nombreServidor : String = binding.txtNombreServidor.text!!.trim().toString()
            val ip : String = binding.txtip.text!!.trim().toString()
            val puerto : Int = binding.txtpuerto.text!!.trim().toString().toInt()
            val puntoVenta : String = binding.tvPuntoVenta.text!!.trim().toString()

            if(ip.isNotEmpty() && puntoVenta.isNotEmpty()){
                actualizarConexionServidor(ip, puerto, puntoVenta, nombreServidor)
                Toast.makeText(this@Configuracion, "SERVIDOR ACTUALIZADO", Toast.LENGTH_SHORT)
                    .show()

                regresarMenuPrincipal()

            }else{
                Toast.makeText(this@Configuracion, "FALTAN DATOS IMPORTANTES", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.spServidor.onItemSelectedListener = object : OnItemSelectedListener {
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
                                    binding.txtNombreServidor.setText(nombreServidor)
                                    binding.txtip.setText(ipServidor)
                                    binding.txtpuerto.setText(puertoServidor)
                                    binding.btnActualizarServidor.isEnabled = false
                                }
                            }
                        }else{
                            val servidorSeleccionado = conexionController.obtenerInformacionServidorSeleccionado(this@Configuracion, servidor)
                            val ipServidor = servidorSeleccionado!!.ip.trim()
                            val puertoServidor = servidorSeleccionado.puerto.trim()
                            idServidorActivo = servidorSeleccionado.id
                            sslActivo = servidorSeleccionado.ssl

                            withContext(Dispatchers.Main){
                                binding.txtNombreServidor.setText(servidor)
                                binding.txtip.setText(ipServidor)
                                binding.txtpuerto.setText(puertoServidor)
                                binding.btnActualizarServidor.isEnabled = true

                                binding.cbxActivarSSL.isChecked = sslActivo == 1
                            }

                        }
                    }catch (e:Exception){
                        println("ERROR AL TRAER LA INFORMACION DEL SERVIDOR -> " + e.message)
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        binding.btnConfigServidor.setOnClickListener{
            menuServidores()
        }


    }

    private fun regresarMenuPrincipal() {
        val intento = Intent(this, Inicio::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION PARA AVERIFICAR SI ESTA EN MODO DESARROLLO
    private fun verificarModoDesarrollo(){
        val modoDesarrollo = preferencias!!.getBoolean("modoDesarrollo", false)
        if(modoDesarrollo){
            binding.apply {
                txtip.isEnabled = true
                txtpuerto.isEnabled = true
                tvPuntoVenta.isEnabled = true
                //btnActualizarServidor.visibility = View.VISIBLE
            }
        }else{
            binding.apply {
                txtip.isEnabled = false
                txtpuerto.isEnabled = false
                tvPuntoVenta.isEnabled = false
                //btnActualizarServidor.visibility = View.GONE
            }
        }
    }

    //CAMBIAR DATOS DEL SERVIDOR
    private fun actualizarConexionServidor(ip: String, puerto: Int, puntoVenta: String, nombreServidor: String){

        preferencias!!.edit {
            remove("puerto")
            remove("ip")
            remove("puntoVenta")
            remove("nombreServidor")
            remove("idServidorActivo")
            remove("sslActivo")
        }

        preferencias!!.edit {
            putString("ip", ip)
            putInt("puerto", puerto)
            putString("puntoVenta", puntoVenta)
            putString("nombreServidor", nombreServidor)
            putInt("idServidorActivo", idServidorActivo)
            putInt("sslActivo", sslActivo)
        }
    }


    private fun seleccionarImagen() {
        seleccionarImagenLauncher.launch("image/*") // solo permite imágenes
    }

    // Launcher para seleccionar imagen

    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val fileName = "logoEmpresaSeleccionado.jpg" // puedes hacerlo dinámico
                val savedFile = guardarImagenEnInterno(uri, fileName)

                if (savedFile != null) {
                    binding.imgLogoEmpresa.setImageURI(Uri.fromFile(savedFile))

                    // Guardar en SharedPreferences
                    val prefs = getSharedPreferences("MisImagenes", MODE_PRIVATE)
                    prefs.edit { putString("imagenFile", savedFile.absolutePath) }
                }
            }
        }

    // 🔹 Copiar imagen seleccionada a almacenamiento interno
    private fun guardarImagenEnInterno(uri: Uri, fileName: String): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, fileName) // guardado en /data/data/tu.app/files/
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //FUNCION PARA OBTENER LA IP DEL SERVIDOR Y EL PUERTO DE CONEXION
    private fun obtenerInformacionServidor() {
        binding.apply {
            txtNombreServidor.setText(nombreServidor)
            txtip.setText(ipServidor)
            txtpuerto.setText(puertoServidor)
            tvPuntoVenta.setText(puntoVenta)
        }
    } //obtiene la ip y el puerto del servidor

    private fun reconectarServidor(ip: String, puerto: String, context: Context) {
        if (funciones.isInternetAvailable(this)) {
            try {
                val servidor = funciones.getServidor(ip, puerto, this@Configuracion)
                val ruta: String = servidor + "conexion"
                val url = URL(ruta)

                val sslContext = utilidades.crearSslInseguro()

                with(url.openConnection() as HttpURLConnection) {


                    if(this is HttpsURLConnection){
                        sslSocketFactory = sslContext.socketFactory
                        hostnameVerifier = HostnameVerifier{_, _ -> true}
                    }

                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use {
                            val response = StringBuffer()
                            var inputline = it.readLine()
                            while (inputline != null) {
                                response.append(inputline)
                                inputline = it.readLine()
                            }
                            it.close() //cerramos el buffer
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                val res = respuesta.getJSONObject(0) //obtenemos los datos


                                if (res.getInt("error") > 0) {
                                    preferencias!!.edit(commit = true) {
                                        this!!.putInt("puerto", puerto.toInt())
                                        putString("ip", ip)
                                    }

                                    alerta!!.dismisss()
                                    val alert: Snackbar = Snackbar.make(binding.vistaalerta, res.getString("response"), Snackbar.LENGTH_LONG)
                                    alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.btnVerde))
                                    alert.show()

                                } else {
                                    throw  Exception(res.getString("response"))
                                } //valida que la respuesta sea  la correcta
                            } else {
                                throw  Exception("Se Conecto con el Servidor, No hubo Respuesta")
                            }//valida que se haya obtenido datos del JSON
                        } //obtenmos los datos que nos envia el servidor
                    } else {
                        throw  Exception("Error de Comunicacion, Codigo:$responseCode")
                    } //valida que el codigo de respuesta del servidor sea ok 200
                }
            } catch (e: Exception) {

                println("ERROR 1 -> " + e.message)

                alerta!!.dismisss()
                val alert: Snackbar =
                    Snackbar.make(binding.vistaalerta, e.message.toString(), Snackbar.LENGTH_LONG)
                alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.moderado))
                alert.show()
            } //valida se si presenta algun error de conexion u otro
        } else {
            alerta!!.dismisss()
            val alert: Snackbar = Snackbar.make(
                binding.vistaalerta,
                "Enciende los Datos o el Wifi",
                Snackbar.LENGTH_LONG
            )
            alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.moderado))
            alert.show()
        } //valida que este encendido los datos o el wifi
    }//valida que haya comunicacion con el servidor


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    //FUNCION PARA VERIFICAR LA VERSION DE LA APP INSTALADA
    private suspend fun obtenerNuevaVersionApp() {
        try {
            val servidor = funciones.getServidor(binding.txtip.text.toString(), binding.txtpuerto.text.toString(), this@Configuracion)
            val direccion = servidor + "updateapp"
            val url = URL(direccion)

            val sslContext = utilidades.crearSslInseguro()

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {

                val token = preferencias!!.getString("token", "")
                agregarHeaders.agregarHeaders(this, token, sslContext)

                try {
                    runOnUiThread {
                        alerta!!.Cargando()
                    }
                    delay(5000)
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                                talla++
                            }
                            data.close()
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                for (i in 0 until respuesta.length()) {
                                    val dato = respuesta.getJSONObject(i)

                                    versionAppServer = funciones.validateJsonIsnullString(dato, "version")
                                    urlAppServer = funciones.validateJsonIsnullString(dato, "url")

                                    runOnUiThread {
                                        if(versionActual >= versionAppServer!!.toFloat()){
                                            alerta!!.dismisss()
                                            Toast.makeText(applicationContext, "NO ES NECESARIO ACTUALIZAR", Toast.LENGTH_SHORT).show()
                                        }else{
                                            alerta!!.dismisss()
                                            mensajeUpdate(versionAppServer.toString(), urlAppServer.toString())
                                        }
                                    }

                                } //termina el for
                            } else {
                                alerta!!.dismisss()
                                ShowAlert("NO SE ENCONTRARON DATOS DE ACTUALIZACIOIN")
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        alerta!!.dismisss()
                        throw Exception("SERVIDOR: NO SE ENCONTRARON DATOS DE ACTUALIZACION")
                    }
                } catch (e: Exception) {
                    alerta!!.dismisss()
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            alerta!!.dismisss()
            ShowAlert("ERROR AL CONECTARSE CON EL SERVIDOR")
        }
    }

    private fun ShowAlert(mensaje: String) {
        val alert: Snackbar = Snackbar.make(binding.vistaalerta, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@Configuracion, R.color.moderado))
        alert.show()
    }


    //FUNCION PARA CREAR EL DIALOG DE ACTUALIZAR APP
    private fun mensajeUpdate(versionServer: String, urlServer: String){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_update)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {
            //updateDialog.dismiss()
            //Toast.makeText(applicationContext, "FUNCION EN DESARROLLO", Toast.LENGTH_SHORT).show()
            updateDialog.dismiss()
            descargarVersionApp(urlServer, "UpdateApp_$versionServer")

            //----------------------------------
            //Condicion para reiniciar BD
            //----------------------------------
            /*if(BuildConfig.VERSION_CODE < versionAppServer!!.toInt()){

                lifecycleScope.launch(Dispatchers.IO) {

                    limpiarBD.limpiarBdAlActualizar(this@Configuracion)
                    withContext(Dispatchers.Main){
                        descargarVersionApp(urlServer, "UpdateApp_$versionServer")
                    }
                }
            }*/
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    //FUNCION PARA ACTUALIZAR LA VERSION ACTUAL DE LA APP
    private fun versionActualApp(){
        val versionName = try{
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName
        }catch (e: Exception){
            1f
        }

        versionActual = versionName.toString().toFloat()

    }

    //FUNCION PARA DESCARGAR Y EJECUTAR LA INSTALACION DE LA ACTUALIZACION
    private fun descargarVersionApp(url: String, filename: String){
        val downloadApk = DownloadApk(this@Configuracion)
        downloadApk.startDownloadingApk(url, filename);
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeConfirmacion(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("LA APLICACIÓN SE REINICIARÁ PARA COMPLETAR EL PROCESO")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                restartApp(this@Configuracion)
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUNCION REINICIAR APP
    private fun restartApp(context: Context) {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)

        // Cierra el proceso actual
        Runtime.getRuntime().exit(0)
    }

    //--------------------------------
    //Funcion para obtener el listado de servidores
    //--------------------------------
    private fun cargarServidores(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val lista = conexionController.obtenerListadoNombreServidores(this@Configuracion)
                val servidor = ArrayAdapter(this@Configuracion, android.R.layout.simple_spinner_item, lista)
                servidor.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.spServidor.adapter = servidor
            }catch (e:Exception){
                println("ERROR AL TRAER LA LISTA DE SERVIDORES -> " + e.message)
            }
        }
    }

    //-------------------------------------
    //Funcion para redireccionar al menu Servidores
    //-------------------------------------
    private fun menuServidores(){
        val enlace = Intent(this@Configuracion, MenuServidores::class.java)
        enlace.putExtra("Menu", "CONFIG")
        startActivity(enlace)
        finish()
    }

}