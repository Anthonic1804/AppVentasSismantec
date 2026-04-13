package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.acae30.controllers.CierreParcialController
import com.example.acae30.databinding.ActivityCierreParcialBinding
import com.example.acae30.modelos.cierreParcial.CierreParcialDTO
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class CierreParcial : AppCompatActivity() {

    private lateinit var binding: ActivityCierreParcialBinding

    private lateinit var preferencias: SharedPreferences

    private val instancia = "CONFIG_SERVIDOR"
    private lateinit var fecha : LocalDate
    private var numeroCaja = 0
    private var fechaReporte : String = ""
    private val cierre = CierreParcialController()
    private val funciones = Funciones()
    private var datosCierreParcial : List<CierreParcialDTO>? = null

    private var procesando : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCierreParcialBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        numeroCaja = preferencias.getInt("numeroCaja", 0)

        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fecha = LocalDate.parse(funciones.obtenerFecha().toString(), formato)
        binding.etFechaReporte.setText(fecha.toString())

    }

    override fun onStart() {
        super.onStart()

        binding.btncancelar.setOnClickListener {
            regresarMenuReportes()
        }

        binding.etFechaReporte.setOnClickListener {

            if(procesando) return@setOnClickListener

            procesando = true

            deshabilitarControles()

            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {

                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(it)
                binding.etFechaReporte.setText(dateStr)

                habilitarControles()
            }

            picker.addOnNegativeButtonClickListener {
                habilitarControles()
            }

            picker.isCancelable = false
            picker.show(supportFragmentManager, picker.toString())
        }

        binding.etFechaReporte.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                fecha = LocalDate.parse(s.toString(), formato)
                fechaReporte = s.toString()
                obtenerDatosCierreParcial(fecha)
            }
        })

    }

    private fun obtenerDatosCierreParcial(fecha: LocalDate){
        lifecycleScope.launch {
            try {
                datosCierreParcial = cierre.obtenerDatosCierreParcial(this@CierreParcial, numeroCaja, fecha)
                println("RESPUESTA DEL SERVIDOR -> $datosCierreParcial")
            }catch (e: Exception){
                println("ERROR AL OBTENER LOS DATOS DEL CIERRE PARCIAL EN VISTA -> ${e.message}")
            }
        }
    }

    private fun deshabilitarControles(){
        binding.apply {
            etFechaReporte.isEnabled = false
            btncancelar.isEnabled = false
            btnImprimir.isEnabled = false
        }
    }

    private fun habilitarControles(){
        binding.apply {
            etFechaReporte.isEnabled = true
            btncancelar.isEnabled = true
            btnImprimir.isEnabled = true

            procesando = false
        }
    }

    private fun regresarMenuReportes(){
        val enlace = Intent(this@CierreParcial, MenuReportes::class.java)
        startActivity(enlace)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }
}