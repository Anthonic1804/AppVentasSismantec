package com.example.acae30.ui.clientes

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.controllers.ClientesController
import com.example.acae30.databinding.ActivityFirmarPagareBinding
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class firmarPagare : AppCompatActivity() {

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvMsj : TextView
    private lateinit var tvTitulo : TextView

    private var idcliente : Int = 0
    private var nombreCliente : String = ""
    private var direccionCliente : String = ""
    private var duiCliente : String = ""
    private var nitCliente : String = ""
    private var limiteCredito : Float = 0f
    private var porcentaje : Float = 0f
    private var plazo : Long = 0
    private var personaJuridica : String = ""
    private var textoPagare : String = ""

    private lateinit var imagenBitmap: Bitmap
    private lateinit var imageFinal : ByteArray
    private var imageFirmada : Boolean = false

    private lateinit var preferencias: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var visita = false

    private var clienteController = ClientesController()
    private var funciones = Funciones()

    private lateinit var binding : ActivityFirmarPagareBinding

    //private val tituloText = "PAGARÉ SIN PROTESTO"
    private val tituloText = ""
    val fechaPagare = "La Unión, " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    var fechaDoc = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirmarPagareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        idcliente = intent.getIntExtra("idcliente", 0)

        val datosCliente = clienteController.obtenerInformacionCliente(this@firmarPagare, idcliente)
        /*
        nombreCliente = intent.getStringExtra("nombreCliente").toString()
        direccionCliente = intent.getStringExtra("direccionCliente").toString()
        duiCliente = intent.getStringExtra("duiCliente").toString()
        limiteCredito = intent.getFloatExtra("limiteCredito", 0f)
        plazo = intent.getLongExtra("plazoCredito", 0)
         */

        nombreCliente = datosCliente!!.Cliente.toString()
        direccionCliente = datosCliente.Direccion.toString()
        duiCliente = datosCliente.Dui.toString()
        nitCliente = datosCliente.Nit.toString()
        limiteCredito = (datosCliente.Limite_credito ?: 0) as Float
        plazo = datosCliente.Plazo_credito?.toLong() ?: 0
        personaJuridica = datosCliente.Persona_juridica.toString()


        //busquedaPedido = intent.getBooleanExtra("busqueda", false)
        //visita = intent.getBooleanExtra("visita", false)

        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        visita = preferencias.getBoolean("visita", false)

        binding.btnCancelarFirma.setOnClickListener {
            mensaje("Cancelar")
        }

        binding.clear.setOnClickListener {
            binding.SignatureView.clear()
        }

        //CALCULANDO LA FECHA DE VENCIMIENTO DE ACUERDO AL PLAZO DADO EN EL CREDITO.
        val fechaVencimiento = LocalDate.now().plusDays(plazo).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

        //ASIGNADO PORCENTAJES DE INTERES SEGUN TABLA PROPORCIONADA POR EL CLIENTE
        /*porcentaje = when(limiteCredito){
            in 1.00..4380.00 -> 6.9f
            in 4381.00..8760.00 -> 4.5f
            in 8761.00..14965.00 -> 3.0f
            else -> 2.1f
        }*/
        //NUEVOS PORCENJES PROPORCIONADOS POR FERRETERIA EL REY
        //MODIFICACION 06-10-2023

        porcentaje = when(limiteCredito){
            in 1.00 .. 4380.00 -> 6.8f
            else -> 2.4f
        }

        textoPagare = "Por $ ${String.format("%.2f", limiteCredito)}; PAGARÉ SIN PROTESTO  en forma incondicional a la ordel del señor: ARMANDO ANTONIO" +
                " LOPEZ VIERA: con Documento Único de Identidad número: 01664366-2, propietario de " +
                "AGROFERRETERIA EL REY Y FORJADOS E INSERTOS EL SALVADOR, en cualquiera de sus " +
                "sucursales, en la ciudad de La Unión, la cantidad de $ ${String.format("%.2f", limiteCredito)} DÓLARES DE LOS " +
                "ESTADOS UNIDOS DE AMÉRICA, más el interés convencional del $porcentaje por ciento mensual, " +
                "teniendo como fecha de vencimiento para el pago de la deuda, el día $fechaVencimiento," +
                " calculados a partir de la fecha de suscripción del presente documento y en " +
                "caso que no fueren cubiertos el capital más los interés a su vencimiento, pagaré además a partir de " +
                "esta última fecha. El tipo de interés " +
                "quedara sujeto a aumento o disminución de acuerdo a las fluctuaciones del mercado. Para los " +
                "efectos legales de esta obligación mercantil, tomamos como domicilio especial la Ciudad de La " +
                "Unión, y en caso de acción judicial renuncio al derecho de apelar del decreto de embargo, sentencia " +
                "de remate y de toda providencia apelable que se dictare en el Juicio Mercantil Ejecutivo o sus " +
                "incidentes, siendo a mi cargo cualquier gasto que hiciere el cobro de este pagaré, inclusive los " +
                "llamados personales y aun por regla general no hubiere condenación por costas procesales y " +
                "faculto a mi acreedor para que designe la persona depositaria de los bienes que se me embarguen " +
                "a quien relevo de la obligación de rendir fianza y cuenta de administración."


        binding.save.setOnClickListener {
            fechaDoc = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            imageFirmada = binding.SignatureView.isEmpty
            if(funciones.isInternetAvailable(this@firmarPagare)){
                if(imageFirmada){
                    Toast.makeText(this, "POR FAVOR INGRESE SU FIRMA", Toast.LENGTH_LONG).show()
                }else{
                    imagenBitmap = binding.SignatureView.signatureBitmap
                    imageFinal = bitmapToByteArray(imagenBitmap)
                    verificarPermisos(it)
                }
            }else{
                funciones.mostrarAlerta("ERROR: NO TIENE CONEXION A INTERNET", this@firmarPagare, binding.vista)
            }
        }

        solicitarPermisos()

    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun verificarPermisos(view: View) {
        generarPDF(nombreCliente, direccionCliente, duiCliente)
    }

    private fun solicitarPermisos() {
        // SOLICITAR
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),  /* Este codigo es para identificar tu request */
            1
        )
    }

    private fun generarPDF(nombreCliente : String, direccionCliente : String, duiCliente : String) {
        try {
            val carpeta = "/archivospdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if(!dir.exists()){
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA CON EXITO", Toast.LENGTH_LONG).show()
            }

            val archivo = File(dir, nombreCliente + "_$fechaDoc.pdf")
            val fos = FileOutputStream(archivo)

            val documento = Document()
            PdfWriter.getInstance(documento, fos)

            documento.open()
            documento.pageSize = PageSize.LETTER

            //AGREGANDO EL TITULO AL PAGARE
            val titulo = Paragraph(
                "\n\n$tituloText\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            titulo.alignment = Element.ALIGN_CENTER
            documento.add(titulo)

            //AGREGANDO LA FECHA DEL DOCUMENTO
            val fechaDocumento = Paragraph(
                "$fechaPagare\n\n",
                FontFactory.getFont("arial", 14f, Font.NORMAL, BaseColor.BLACK)
            )
            fechaDocumento.alignment = Element.ALIGN_CENTER
            documento.add(fechaDocumento)

            //AGREGANDO EL CONTENIDO AL PAGARE
            val descripcion = Paragraph(
                textoPagare,
                FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
            )
            descripcion.alignment = Element.ALIGN_JUSTIFIED
            documento.add(descripcion)

            //AGREGANDO EL PIE AL PAGARE + LA FIRMA DEL CLIENTE
            val pieDocumento = if(personaJuridica != "S"){
                Paragraph(
                    "\n\n\n" +
                            "NOMBRE: $nombreCliente\n" +
                            "D.U.I: $duiCliente\n" +
                            "DIRECCION: $direccionCliente\n\n" +
                            "FIRMA: ",
                    FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
                )
            }else{
                Paragraph(
                    "\n\n\n" +
                            "NOMBRE: $nombreCliente\n" +
                            "N.I.T: $nitCliente\n" +
                            "DIRECCION: $direccionCliente\n\n" +
                            "FIRMA: ",
                    FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
                )
            }
            documento.add(pieDocumento)

            //CARGANDO LA FIRMA REALIZADA EN EL PDF
            val firmaDocumento = Image.getInstance(imageFinal, true)
            firmaDocumento.scaleToFit(70f, 70f)
            documento.add(firmaDocumento)

            documento.close()

            mensaje("Firmado")

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: DocumentException){
            e.printStackTrace()
        }
    }

    fun atras(){
        val intent = Intent(this, ClientesDetalle::class.java)
        intent.putExtra("idcliente", idcliente)
        startActivity(intent)
        finish()
    }
    fun pagareYaFirmado(){
        if(visita){
            val intent = Intent(this, Clientes::class.java)
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(this, ClientesDetalle::class.java)
            intent.putExtra("idcliente", idcliente)
            startActivity(intent)
            finish()
        }
    }

    private fun mensaje(msj: String){

        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMsj = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        var tituloDialogo = ""
        var mensajeDialogo = ""
        var mensajeBotonAceptar = ""

        when(msj){
            "Cancelar" -> {
                tituloDialogo = "CANCELAR PROCESO"
                mensajeDialogo = "Está seguro de Cancelar el Proceso"
                mensajeBotonAceptar = "SALIR"

                tvUpdate.setOnClickListener {
                    atras()
                    updateDialog.dismiss()
                }

                tvMsj.text = mensajeDialogo
                tvTitulo.text = tituloDialogo
                tvUpdate.text = mensajeBotonAceptar

                tvCancel.setOnClickListener {
                    updateDialog.dismiss()
                }
            }
            "Firmado" -> {
                tvCancel.visibility = View.GONE

                tituloDialogo = "PROCESO COMPLETO"
                mensajeDialogo = "El Proceso fue Generado Correctamente, Regresando a Detalle del Cliente"
                mensajeBotonAceptar = "ACEPTAR"

                tvUpdate.setOnClickListener {
                    pagareYaFirmado()
                    updateDialog.dismiss()
                }

                tvMsj.text = mensajeDialogo
                tvTitulo.text = tituloDialogo
                tvUpdate.text = mensajeBotonAceptar
            }
        }

        updateDialog.show()

    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }
}