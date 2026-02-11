package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acae30.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashScreen : AppCompatActivity() {

    private lateinit var binding : ActivitySplashScreenBinding
    private lateinit var preferencias : SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val ip = preferencias.getString("ip","")
        val puerto = preferencias.getInt("puerto", 0)
        val idVendedor = preferencias.getInt("Idvendedor", 0)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch(Dispatchers.IO) {
                if(ip == "" && puerto == 0) {
                    withContext(Dispatchers.Main){
                        menuServidores()
                    }
                }else if(idVendedor == 0){
                    withContext(Dispatchers.Main){
                        login()
                    }
                }else{
                    withContext(Dispatchers.Main){
                        menuInicio()
                    }
                }
            }
        }, 3000)

    }

    //-----------------------------------
    //Redireccionando al Inicio
    //-----------------------------------
    private fun menuInicio(){
        val enlace = Intent(this@SplashScreen, Inicio::class.java)
        startActivity(enlace)
        finish()
    }

    //-----------------------------------
    //Redireccionando al Login
    //-----------------------------------
    private fun login(){
        val enlace = Intent(this@SplashScreen, Login::class.java)
        startActivity(enlace)
        finish()
    }

    //-----------------------------------
    //Redireccionando al Menu de Servidores
    //-----------------------------------
    private fun menuServidores(){
        val enlace = Intent(this@SplashScreen, MenuServidores::class.java)
        startActivity(enlace)
        finish()
    }
}