package com.example.acae30

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityAbonosCxcBinding

class AbonosCxc : AppCompatActivity() {

    private lateinit var binding: ActivityAbonosCxcBinding
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbonosCxcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(this.instancia, MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            regresarInicio()
        }

        binding.nuevoAbono.setOnClickListener {
            val editor = preferencias.edit()
            editor.putString("vista", "abono")
            editor.apply()

            val intento = Intent(this, Cuentas_list::class.java)
            startActivity(intento)
            finish()
        }

    }

    @Deprecated("This method has been deprecated in favor of using the\n     " +
            " {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      " +
            "The OnBackPressedDispatcher controls how back button events are dispatched\n     " +
            " to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private fun regresarInicio(){
        val intent = Intent(this@AbonosCxc, Inicio::class.java)
        startActivity(intent)
        finish()
    }
}