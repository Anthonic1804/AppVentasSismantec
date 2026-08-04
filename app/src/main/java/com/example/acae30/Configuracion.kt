package com.example.acae30

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dcastalia.localappupdate.DownloadApk
import com.example.acae30.Utilidades.AgregarHeaders
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.local.appDatabase.LimpiarBD
import com.example.acae30.data.local.entity.ServidoresEntity
import com.example.acae30.data.repository.ServidoresRepository
import com.example.acae30.controllers.ConfigController
import com.example.acae30.data.remote.dto.UpdateAppDto
import com.example.acae30.databinding.ActivityConfiguracionBinding
import com.example.acae30.modelos.Impresor.DispositivoBT
import com.example.acae30.ui.factories.ServidoresViewModelFactory
import com.example.acae30.ui.servidores.MenuServidores
import com.example.acae30.ui.servidores.ServidoresViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection


class Configuracion : AppCompatActivity() {
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private var versionActual : Float = 0f
    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null
    private var alerta: AlertDialogo? = null

    private var configController = ConfigController()
    private var funciones = Funciones()
    private var servidor: String = ""
    private var nombreServidor: String = ""
    private var ipServidor: String = ""
    private var puertoServidor: String = ""
    private var puntoVenta: String = ""
    private var idServidorActivo: Int = 0
    private var sslActivo: Int = 0

    // REFACTORIZACIÓN: Lista local de servidores para facilitar búsquedas por nombre
    private var listaServidoresEntity: List<ServidoresEntity> = emptyList()
    // REFACTORIZACIÓN: Se reemplaza ConexionController por ServidoresViewModel
    private lateinit var servidoresViewModel: ServidoresViewModel
    private var limpiarBD = LimpiarBD()
    private var isProcessing = false
    private var listaNumeroCaja = mutableListOf<Int>()
    private var numeroCaja = 0
    private lateinit var binding : ActivityConfiguracionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        alerta = AlertDialogo(this, this)

        // INICIALIZACIÓN MVVM
        val dao = AppDatabase.getInstance(this).servidoresDao()
        val repository = ServidoresRepository(dao)
        val factory = ServidoresViewModelFactory(repository)
        servidoresViewModel = ViewModelProvider(this, factory)[ServidoresViewModel::class.java]

        observarViewModel()

        nombreServidor = preferencias!!.getString("nombreServidor", "").toString()
        ipServidor = preferencias!!.getString("ip", "").toString()
        puertoServidor = preferencias!!.getInt("puerto", 0).toString()
        puntoVenta = preferencias!!.getString("puntoVenta", "").toString()

        idServidorActivo = preferencias!!.getInt("idServidorActivo", 0)
        sslActivo = preferencias!!.getInt("sslActivo", 0)

        numeroCaja = preferencias!!.getInt("numeroCaja", 0)

        //FUNCIONES AGRAGADAS PARA LOS CONTROLES DE VISTA DE INVENTARIO

        binding.swSinExistencias.isEnabled = false

        cargarNumeroCaja()

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

            val impresor = binding.txtImpresor.text

            preferencias!!.edit{
                remove("impresorIntegrado")
                putString("impresorIntegrado", impresor.toString())
            }

            Toast.makeText(this@Configuracion, "IMPRESOR CONFIGURADO", Toast.LENGTH_SHORT)
                .show()
        }

        binding.btnBuscarUpdate.setOnClickListener {

            if(isProcessing) return@setOnClickListener

            deshabilitarOpcion()

            if (funciones.isInternetAvailable(this)) {

                obtenerNuevaVersionApp()

            } else {
                habilitarOpcion()
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

            if(isProcessing) return@setOnClickListener

            deshabilitarOpcion()

            alerta!!.Cargando()

            reconectarServidor(binding.txtip.text.toString(), binding.txtpuerto.text.toString(), sslActivo)

        }//guarda los datos del servidor

        binding.btnCargarConfig.setOnClickListener {

            if(isProcessing) return@setOnClickListener

            deshabilitarOpcion()

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

                        habilitarOpcion()

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
                servidor = parent?.getItemAtPosition(position).toString()
                if (servidor == "-- SELECCIONE --") {
                    binding.txtNombreServidor.setText(nombreServidor)
                    binding.txtip.setText(ipServidor)
                    binding.txtpuerto.setText(puertoServidor)
                    binding.btnActualizarServidor.isEnabled = false
                } else {
                    // REFACTORIZACIÓN: Buscar en la lista local cargada por el ViewModel
                    val servidorSeleccionado = listaServidoresEntity.find { it.nombre == servidor }
                    servidorSeleccionado?.let {
                        val ip = it.ip.trim()
                        val puerto = it.puerto.trim()
                        idServidorActivo = it.id
                        sslActivo = it.ssl

                        binding.txtNombreServidor.setText(servidor)
                        binding.txtip.setText(ip)
                        binding.txtpuerto.setText(puerto)
                        binding.btnActualizarServidor.isEnabled = true
                        binding.cbxActivarSSL.isChecked = sslActivo == 1
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spNumeroCaja.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                lifecycleScope.launch {
                    numeroCaja = parent?.getItemAtPosition(position) as Int

                    preferencias!!.edit {
                        remove("numeroCaja")
                    }

                    preferencias!!.edit{
                        putInt("numeroCaja", numeroCaja)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        binding.btnConfigServidor.setOnClickListener{
            menuServidores()
        }


    }

    private fun cargarNumeroCaja(){

        listaNumeroCaja = (0..10).toMutableList()

        val adapterCaja = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listaNumeroCaja
        )

        adapterCaja.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spNumeroCaja.adapter = adapterCaja

        // Seleccionar valor si existe
        if (numeroCaja in listaNumeroCaja) {
            val posicion = listaNumeroCaja.indexOf(numeroCaja)
            binding.spNumeroCaja.setSelection(posicion)
        }

    }

    //FUNCIONES PARA HABILITAR Y DESHABILITAR OPCION
    private fun deshabilitarOpcion(){
        isProcessing = true

        binding.apply {
            btnActualizarServidor.isEnabled = false
            btnReconectar.isEnabled = false
            btnBuscarUpdate.isEnabled = false
            btnImpresor.isEnabled = false
        }
    }

    private fun habilitarOpcion(){
        isProcessing = false

        binding.apply {
            btnActualizarServidor.isEnabled = true
            btnReconectar.isEnabled = true
            btnBuscarUpdate.isEnabled = true
            btnImpresor.isEnabled = true
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


    //-----------------------------------
    // REFACTORIZACIÓN MVVM: Observar cambios en el ViewModel
    //-----------------------------------
    private fun observarViewModel() {
        // Observar listado de servidores para el Spinner
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                servidoresViewModel.servidores.collect { lista ->
                    listaServidoresEntity = lista
                    actualizarSpinnerServidores(lista)
                }
            }
        }

        // Observar resultado de conexión
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                servidoresViewModel.resultadoConexion.collect { respuesta ->
                    if (respuesta.isNotEmpty()) {
                        manejarRespuestaReconexion(respuesta)
                    }
                }
            }
        }

        // Observar información de actualización
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                servidoresViewModel.updateInfo.collect { updateDto ->
                    if (updateDto != null) {
                        manejarInfoActualizacion(updateDto)
                    }
                }
            }
        }
    }

    private fun actualizarSpinnerServidores(lista: List<ServidoresEntity>) {
        val nombres = mutableListOf("-- SELECCIONE --")
        nombres.addAll(lista.map { it.nombre })
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spServidor.adapter = adapter
    }

    private fun manejarRespuestaReconexion(respuesta: String) {
        if (respuesta == "Conexion Exitosa") {
            habilitarOpcion()
            alerta!!.changeText("RECONEXION EXITOSA")
        } else {
            habilitarOpcion()
            alerta!!.changeText("ERROR DE CONEXION CON EL SERVIDOR")
        }

        lifecycleScope.launch {
            delay(1000)
            alerta!!.dismisss()
        }
    }

    private fun manejarInfoActualizacion(actualizacionApp: UpdateAppDto) {
        if (actualizacionApp.version!!.isEmpty() || versionActual >= actualizacionApp.version.toFloat()) {
            habilitarOpcion()
            alerta!!.dismisss()
            Toast.makeText(applicationContext, "NO ES NECESARIO ACTUALIZAR", Toast.LENGTH_SHORT).show()
        } else {
            habilitarOpcion()
            alerta!!.dismisss()
            mensajeUpdate(actualizacionApp.version, actualizacionApp.enlaceDescarga!!, actualizacionApp.eliminarBd!!)
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

    // Copiar imagen seleccionada a almacenamiento interno
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

    private fun reconectarServidor(ip: String, puerto: String, sslActivo: Int){
        val hayInternet = funciones.isInternetAvailable(this@Configuracion)
        if(hayInternet){
            // REFACTORIZACIÓN: Usar ViewModel para verificar conexión
            servidoresViewModel.verificarConexion(ip, puerto, sslActivo, this@Configuracion)
        }else{
            habilitarOpcion()
            alerta!!.changeText("ERROR NO HAY CONEXION DE INTERNET")
            lifecycleScope.launch {
                delay(1000)
                alerta!!.dismisss()
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    private fun obtenerNuevaVersionApp(){
        alerta!!.Cargando()
        
        // REFACTORIZACIÓN: Usar ViewModel para buscar actualización
        val servidorUrl = funciones.getServidor(binding.txtip.text.toString(), binding.txtpuerto.text.toString(), this@Configuracion)
        servidoresViewModel.buscarActualizacion(servidorUrl, this@Configuracion)

        // Pequeño timeout de seguridad por si el servidor no responde
        lifecycleScope.launch {
            delay(15000) // 15 segundos
            if (isProcessing && alerta!!.isShowing()) {
                habilitarOpcion()
                alerta!!.dismisss()
                Toast.makeText(this@Configuracion, "TIEMPO DE ESPERA AGOTADO", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //FUNCION PARA CREAR EL DIALOG DE ACTUALIZAR APP
    private fun mensajeUpdate(versionServer: String, urlServer: String, eliminarBDInterna: Boolean){

        //Timber.e("[CONFIGURACION] VALOR DE BORRARBD -> $eliminarBDInterna")

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_update)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)


        tvUpdate.setOnClickListener {

            updateDialog.dismiss()
            //----------------------------------
            //Condicion para reiniciar BD
            //----------------------------------
            if(eliminarBDInterna){
                lifecycleScope.launch(Dispatchers.IO) {

                    limpiarBD.limpiarBdAlActualizar(this@Configuracion)
                    withContext(Dispatchers.Main){
                        descargarVersionApp(urlServer, "UpdateApp_$versionServer")
                    }
                }
            }else{
                descargarVersionApp(urlServer, "UpdateApp_$versionServer")
            }
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    private fun ShowAlert(mensaje: String) {
        val alert: Snackbar = Snackbar.make(binding.vistaalerta, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@Configuracion, R.color.moderado))
        alert.show()
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