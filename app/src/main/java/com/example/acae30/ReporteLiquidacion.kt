package com.example.acae30

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.ReporteLiquidacionController
import com.example.acae30.databinding.ActivityReporteLiquidacionBinding
import com.example.acae30.listas.AbonosAdapter
import com.example.acae30.listas.ReporteLiquidacion.CobrosAdapter
import com.example.acae30.modelos.Abono
import com.example.acae30.modelos.ReporteLiquidacion.VentaContado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReporteLiquidacion : AppCompatActivity() {

    private lateinit var binding : ActivityReporteLiquidacionBinding
    private var funciones = Funciones()
    private var reporte = ReporteLiquidacionController()
    private var totalContado = 0f
    private var totalCredito = 0f
    private var totalCobros = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReporteLiquidacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            val listadoCobros = reporte.obtenerCobros(this@ReporteLiquidacion)
            withContext(Dispatchers.Main){
                armarListaCobros(listadoCobros)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val listadoVentaContado = reporte.obtenerVentaContado(this@ReporteLiquidacion)
            withContext(Dispatchers.Main){
                armarListaContado(listadoVentaContado)
            }
        }

        totalContado = reporte.obtenerTotalVentaContado(this@ReporteLiquidacion)
        binding.tvTotalContado2.text = "$ " + "${String.format("%.4f".format(totalContado))}"

        totalCredito = reporte.obtenerTotalVentaCredito(this@ReporteLiquidacion)
        binding.tvTotalCredito2.text = "$ " + "${String.format("%.4f".format(totalCredito))}"

        totalCobros = reporte.obtenerTotalVentaCobros(this@ReporteLiquidacion)
        binding.tvTotalCobros2.text = "$ " + "${String.format("%.4f".format(totalCobros))}"

        val totalDiario = totalContado + totalCredito + totalCobros
        binding.tvTotalDiario2.text = "$ " + "${String.format("%.2f".format(totalDiario))}"

    }



    override fun onStart() {
        super.onStart()

        binding.btncancelar.setOnClickListener {
            mensaje("DESEA CANCELAR EL PROCESO?", "CANCELAR")
        }

        binding.btnaceptar.setOnClickListener {
            mensaje("DESEA IMPRIMIR EL REPORTE DE LIQUIDACION?", "IMPRIMIR")
        }
    }

    private fun menuReportes(){
        val intento = Intent(this@ReporteLiquidacion, MenuReportes::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW COBROS
    private fun armarListaCobros(lista : ArrayList<VentaContado>) {
        val mLayoutManager = LinearLayoutManager(
            this@ReporteLiquidacion,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvCobros.layoutManager = mLayoutManager
        val adapter = CobrosAdapter(lista, this@ReporteLiquidacion)
        binding.rvCobros.adapter = adapter

    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW VENTA AL CONTADO
    private fun armarListaContado(lista : ArrayList<VentaContado>) {
        val mLayoutManager = LinearLayoutManager(
            this@ReporteLiquidacion,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvVentaContado.layoutManager = mLayoutManager
        val adapter = CobrosAdapter(lista, this@ReporteLiquidacion)
        binding.rvVentaContado.adapter = adapter

    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    fun mensaje(mensaje: String,  tipo: String){
        val dialog = AlertDialog.Builder(this@ReporteLiquidacion)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                when(tipo){
                    "CANCELAR" -> {
                        view.dismiss()
                        menuReportes()
                    }
                    else -> {
                        funciones.mensaje(this@ReporteLiquidacion, "OPCION EN DESARROLLO")
                    }
                }
            }
            .setNegativeButton("CANCELAR"){ view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      " +
            "{@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      " +
            "The OnBackPressedDispatcher controls how back button events are dispatched\n      " +
            "to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    /*/FUNCION PARA IMPRIMIR EL REPORTE DE LIQUIDACION
    fun imprimirReporteLiquidacion(){
        val printManager = ContextCompat.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = getString(R.string.app_name) + " ReporteLiquidacion"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            
        }, null)
    }*/

}