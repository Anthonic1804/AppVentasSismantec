package com.example.acae30

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityReporteLiquidacionBinding

class ReporteLiquidacion : AppCompatActivity() {

    private lateinit var binding : ActivityReporteLiquidacionBinding
    private var funciones = Funciones()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReporteLiquidacionBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
}