package com.example.acae30

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.example.acae30.controllers.ReporteVentaUnidadesController
import com.example.acae30.databinding.ActivityReportePorProductoBinding
import com.example.acae30.listas.UnidadesVendidasProductoAdapter
import com.example.acae30.modelos.reporteUnidadesVendidas.UnidadesVendidasPorProducto
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReportePorProducto : AppCompatActivity() {

    private val reporte = ReporteVentaUnidadesController()
    private lateinit var binding : ActivityReportePorProductoBinding

    lateinit var lista : List<UnidadesVendidasPorProducto>
    private lateinit var preferencias: SharedPreferences

    private var idvendedor = 0
    private val instancia = "CONFIG_SERVIDOR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportePorProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idvendedor = preferencias.getInt("Idvendedor", 0)

        permisosBluetooth()

    }

    override fun onStart() {
        super.onStart()

        binding.etFechaReporte.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(it)
                binding.etFechaReporte.setText(dateStr)
            }

            picker.show(supportFragmentManager, picker.toString())
        }

        binding.etFechaReporte.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val fecha = LocalDate.parse(s.toString(), formato)
                obtenerUnidadesVendidasPorProducto(fecha)
            }
        })

        binding.btncancelar.setOnClickListener {
            mensaje()
        }

        binding.btnImprimir.setOnClickListener { imprimirRecibo() }

    }

    private fun obtenerUnidadesVendidasPorProducto(fecha : LocalDate){
        lifecycleScope.launch {
            try {
                lista = reporte.obtenerUnidadesVendidasPorProducto(this@ReportePorProducto, idvendedor, fecha)
                mostrarLista(lista)
            }catch (e: Exception){
                println("ERROR AL OBTENER EL LISTADO DE UNIDADES VENDIDAS POR PRODUCTO -> " + e.message)
            }
        }
    }

    private fun mostrarLista(lista : List<UnidadesVendidasPorProducto>){
        val mLayoutManager = LinearLayoutManager(this@ReportePorProducto, LinearLayoutManager.VERTICAL, false)
        binding.listaProductosVendidos.layoutManager = mLayoutManager
        val adapter = UnidadesVendidasProductoAdapter(lista, this@ReportePorProducto)
        binding.listaProductosVendidos.adapter = adapter
    }

    private fun regresarMenuReportes(){
        val enlace = Intent(this@ReportePorProducto, MenuReportes::class.java)
        startActivity(enlace)
        finish()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    fun mensaje(){
        val dialog = AlertDialog.Builder(this@ReportePorProducto)
            .setTitle("INFORMACION")
            .setMessage("¿Desea Cancelar el proceso?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                regresarMenuReportes()
            }
            .setNegativeButton("CANCELAR"){ view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //Funcion para los permisos Bluetooth
    private fun permisosBluetooth() {
        val permissions = when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            }
            else -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            }
        }

        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), 1001)
        } else {
            //Toast.makeText(this, "Permisos Bluetooth concedidos ✅", Toast.LENGTH_SHORT).show()
        }
    }

    //FUNCION PARA DETERMINAR LA CONEXION DE LA IMPRESORA
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun imprimirRecibo() {
        try {
            val tipoImpresora = preferencias.getString("tipoImpresora", "")
            when(tipoImpresora){
                "BT" -> {
                    // ===============================
                    // Si no hay USB, probar Bluetooth
                    // ===============================
                    val btConnection = BluetoothPrintersConnections.selectFirstPaired()
                    if (btConnection != null) {
                        reporte.imprimirTicket(btConnection, this@ReportePorProducto, lista)
                    } else {
                        Toast.makeText(this, "No se encontró impresora USB ni Bluetooth", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // ===============================
                    // Detectar impresora Integrada
                    // ===============================
                    reporte.imprimirReciboIntegrado(this@ReportePorProducto, lista)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}