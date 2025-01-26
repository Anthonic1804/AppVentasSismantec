package com.example.acae30

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityMenuDevolucionesBinding
import org.jetbrains.annotations.Async.Execute

class MenuDevoluciones : AppCompatActivity() {

    private lateinit var binding : ActivityMenuDevolucionesBinding
    private var funciones = Funciones()

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuDevolucionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            inicio()
        }

        binding.nuevaDevolucion.setOnClickListener {
            nuevaDevolucion()
        }
    }

    //FUNCION PARA CREAR UNA NUEVA DEVOLUCION
    private fun crearDevolucion(context: Context) : Int{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val idVendedor = preferences.getInt("Idvendedor", 0)
        val nombreVendedor =  preferences.getString("Vendedor", "")
        val hojaCargaActiva = preferences.getInt("hojaCarga", 0)
        val idHojaCarga = preferences.getInt("idHojaCarga", 0)



        val base = funciones.getDataBase(context).writableDatabase
        val fecha = funciones.obtenerFecha()
        var idDevolucion : Int = 0

        try{
            base.beginTransaction()
            val contenido = ContentValues()
            contenido.put("Numero" , 0)
            contenido.put("Fecha", fecha)
            contenido.put("Id_hoja_de_carga", 0)
            contenido.put("Hoja_de_carga", 0)
            contenido.put("Id_ruta", 0)
            contenido.put("Ruta", "RutaNombre")
            contenido.put("Id_vendedor", 0)
            contenido.put("Vendedor", "VendedorNombre")
            contenido.put("Estado", "PROCESADO")
            val id = base.insert("devolucion", null, contenido)
            idDevolucion = id.toInt()

            base.setTransactionSuccessful()

        }catch (e: Exception){
            idDevolucion = 0
            println("ERROR AL CREAR LA NUEVA DEVOLUCION " + e.message)
        }finally {
            base.endTransaction()
            base.close()
        }

        return  idDevolucion
    }

    private fun nuevaDevolucion(){
        val intento = Intent(this, NuevaDevolucion::class.java)
        startActivity(intento)
        finish()
    }

    private fun inicio(){
        val intento = Intent(this, Inicio::class.java)
        startActivity(intento)
        finish()
    }



}