package com.example.acae30.ui.averias

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.acae30.Inicio
import com.example.acae30.databinding.ActivityMenuAveriasBinding

class MenuAverias : AppCompatActivity() {

    private lateinit var binding: ActivityMenuAveriasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuAveriasBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            menuInicio()
        }

    }

    private fun menuInicio(){
        val intent = Intent(this@MenuAverias, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras
}