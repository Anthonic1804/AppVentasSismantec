package com.example.acae30

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityMenuReportesBinding

class MenuReportes : AppCompatActivity() {

    private var funciones = Funciones()

    private lateinit var binding : ActivityMenuReportesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuReportesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarResponsiveMenu()
    }

    override fun onStart() {
        super.onStart()

        binding.cvCierreParcial.setOnClickListener {
            cierreParcial()
        }

        binding.btnAtras.setOnClickListener {
            inicio()
        }

        binding.cvReportePorProducto.setOnClickListener {
            reporteVentasProducto()
        }
    }

    private fun reporteVentasProducto(){
        val enlace = Intent(this@MenuReportes, ReportePorProducto::class.java)
        startActivity(enlace)
        finish()
    }

    private fun reporteLiquidacion(){
        val intento = Intent(this@MenuReportes, ReporteLiquidacion::class.java)
        startActivity(intento)
        finish()
    }

    private fun inicio(){
        val intento = Intent(this@MenuReportes, Inicio::class.java)
        startActivity(intento)
        finish()
    }

    private fun cierreParcial(){
        val enlace = Intent(this@MenuReportes, CierreParcial::class.java)
        startActivity(enlace)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }

    //CONFIGURACION EL RESPONSIVE DEL MENU
    private fun configurarResponsiveMenu(){
        val flow = binding.flow
        val orientation = resources.configuration.orientation

        val cantidad = when(orientation){
            Configuration.ORIENTATION_LANDSCAPE -> 4
            else -> 2
        }

        flow.setMaxElementsWrap(cantidad)
    }
}