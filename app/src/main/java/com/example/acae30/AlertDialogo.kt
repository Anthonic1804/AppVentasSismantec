package com.example.acae30

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView

class AlertDialogo(act: Activity, context: Context) {

    var actividad: Activity
    private lateinit var dialogo: Dialog

    init {
        actividad = act
    }

    fun Cargando() {
        dismisss() // Cerrar cualquier diálogo previo para evitar fugas de memoria o bloqueos visuales
        val ale: AlertDialog.Builder = AlertDialog.Builder(actividad)
        val ly: LayoutInflater = actividad.layoutInflater
        ale.setView(ly.inflate(R.layout.alerta_carga, null))
        ale.setCancelable(false)
        dialogo = ale.create()

        dialogo.show()
    }

    fun Enviando() {
        dismisss() // Cerrar previo
        val ale: AlertDialog.Builder = AlertDialog.Builder(actividad)
        val ly: LayoutInflater = actividad.layoutInflater
        ale.setView(ly.inflate(R.layout.alerta_enviando, null))
        ale.setCancelable(false)
        dialogo = ale.create()

        dialogo.show()
    }

    fun pedidoEnviado() {
        dismisss() // Cerrar previo
        val ale: AlertDialog.Builder = AlertDialog.Builder(actividad)
        val ly: LayoutInflater = actividad.layoutInflater
        ale.setView(ly.inflate(R.layout.alerta_enviado, null))
        ale.setCancelable(false)
        dialogo = ale.create()

        dialogo.show()
    }

    fun pedidoGuardado() {
        dismisss() // Cerrar previo
        val ale: AlertDialog.Builder = AlertDialog.Builder(actividad)
        val ly: LayoutInflater = actividad.layoutInflater
        ale.setView(ly.inflate(R.layout.alerta_guardado, null))
        ale.setCancelable(false)
        dialogo = ale.create()

        dialogo.show()
    }

    fun dismisss() {
        /*
         * Antes de cerrar el diálogo, validamos dos cosas:
         * 1. Que la variable 'dialogo' ya haya sido creada (isInitialized).
         * 2. Que el diálogo esté visible en pantalla (isShowing).
         * Esto evita que la aplicación se cierre (crash) si intentamos ocultar algo que no existe.
         */
        if (::dialogo.isInitialized && dialogo.isShowing) {
            dialogo.dismiss()
        }
    }

    fun isShowing(): Boolean {
        return ::dialogo.isInitialized && dialogo.isShowing
    }

    fun changeText(mensaje: String) {
         //Verificamos que 'dialogo' esté inicializado antes de buscar el TextView.
        if (::dialogo.isInitialized) {
            val textocarga = dialogo.findViewById<TextView>(R.id.txtcargando)
            if (textocarga != null) {
                textocarga.text = mensaje
            }
        }
    }

}