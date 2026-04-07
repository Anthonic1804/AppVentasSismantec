package com.example.acae30

import android.content.Intent
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
    }

    override fun onStart() {
        super.onStart()

        binding.cvReporteLiquidacion.setOnClickListener {
            reporteLiquidacion()
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }
}