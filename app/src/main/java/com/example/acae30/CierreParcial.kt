package com.example.acae30

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityCierreParcialBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CierreParcial : AppCompatActivity() {

    private lateinit var binding: ActivityCierreParcialBinding

    private var procesando : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCierreParcialBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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