package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.content.edit
import com.example.acae30.Utilidades.ConsumirEndpoint
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.modelos.PermisosApp.PermisosApp
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class ConfigController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var utilidades = CrearSslNoSeguro()

    private val consumirEndPoint = ConsumirEndpoint()

    //FUNCION PARA OBTERNER LA INFORMACION DE LA TABLA CONFIG SQLSERVER
    /*suspend fun obtenerConfigPagareObligatorio(context:Context){

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        //ELIMINANDO CONFIGURACION
        eliminarConfiguracionApp(context)

        try {
            val direccion = url + "config"
            val url2 = URL(direccion)

            val sslContext = utilidades.crearSslInseguro()

            with(withContext(Dispatchers.IO) {
                url2.openConnection()
            } as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier{_, _ -> true}
                }

                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                            }
                            data.close()
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {

                                for (i in 0 until respuesta.length()){
                                    val dato = respuesta.getJSONObject(i)

                                    val item = PermisosApp(

                                        pagareObligarotio = dato.getBoolean("pagare_obligatorio_app"),
                                        modificarPrecio = dato.getBoolean("modificar_precio_app"),
                                        pedidoSinExistencia = dato.getString("pedidos_sin_existencia"),
                                        networkProvider = dato.getBoolean("networkProvider_app"),
                                        usarHojaCarga = dato.getBoolean("hoja_carga_inventario_app"),
                                        mostrarPrecioApp = dato.getInt("precio_mostrar_app"),
                                        mHistorio = dato.getBoolean("m_Historico"),
                                        mHojaCarga = dato.getBoolean("m_HojaCarga"),
                                        mGastos = dato.getBoolean("m_Gastos"),
                                        mReportes = dato.getBoolean("m_Reportes"),
                                        mCxC = dato.getBoolean("m_CxC"),
                                        mAbonos = dato.getBoolean("m_Abonos"),
                                        pMantto_Clientes = dato.getBoolean("p_Mantto_Clientes"),
                                        pImprimirTKVenta = dato.getBoolean("p_Imprimir_TK_Venta"),
                                        solicitudCargaSinExistencia = dato.getBoolean("solicitud_Carga_SinExistencia"),
                                        validarHojaCarga = dato.optBoolean("validacionHojaCarga", false),
                                        docFactura = dato.getBoolean("doc_Factura"),
                                        docCreFiscal = dato.getBoolean("doc_CreFiscal"),
                                        docRecibo = dato.getBoolean("doc_Recibo"),
                                        docRemision = dato.getBoolean("doc_Remision"),
                                        docFacExportacion = dato.getBoolean("doc_FacExportacion"),
                                        empresa = dato.getString("empresa"),
                                        direccion = dato.getString("direccion"),
                                        nrc = dato.getString("nrc"),
                                        nit = dato.getString("nit"),
                                        giro = dato.getString("giro"),
                                        dteUrlQRHacienda = dato.optString("dteUrlQR_Hacienda", "0"),
                                        dteUrlQRempresa = dato.optString("dteUrlQR_empresa", "0"),
                                        numItemFactura = dato.optInt("numItemFactura", 100),
                                        numItemCreFiscal = dato.optInt("numItemCreFiscal",100),
                                        numItemRecibo = dato.optInt("numItemRecibo", 100),
                                        numItemRemision = dato.optInt("numItemRemision", 100),
                                        tipoVentaLocal = dato.optBoolean("tipoVentaLocal", false),
                                        modoDesarrollo = dato.optBoolean("modoDesarrollo", false),
                                        cargaAutomaticaCatalogos = dato.optBoolean("cargaAutomaticaCatalogos", false),
                                        decPrecios = dato.optInt("decPrecios", 2),
                                        decTotales = dato.optInt("decTotales", 2),
                                        multiplesHojaDeCarga = dato.optBoolean("multiplesHojaDeCarga", false),
                                        idBodega = dato.optInt("id_bodega_inventario"),
                                        codBodega = dato.optString("cod_bodega_inventario"),
                                        bodega = dato.optString("bodega_inventario")
                                    )

                                    confirmarPagareObligatorio(item, context)
                                }
                            } else {
                                println("ERROR AL LEER EL JSON CONFIG")
                            }
                        }
                    } else {
                        println("ERROR DE COMUNICACION CON EL SERVIDOR: $responseCode")
                    }
                } catch (e: Exception) {
                    println("ERROR SEGUNDO TRY CATCH -> ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("ERROR PRIMER TRY CATCH -> ${e.message}")
        }
    }*/
    suspend fun obtenerConfigPagareObligatorio(context: Context){

        val response = consumirEndPoint.consumirEndpoint(context, "config")
        if(response != null){

            val respuesta = JSONArray(response)

            if(respuesta.length() > 0){

                //ELIMINANDO CONFIGURACION
                eliminarConfiguracionApp(context)

                for (i in 0 until respuesta.length()){
                    val dato = respuesta.getJSONObject(i)

                    val item = PermisosApp(

                        pagareObligarotio = dato.getBoolean("pagare_obligatorio_app"),
                        modificarPrecio = dato.getBoolean("modificar_precio_app"),
                        pedidoSinExistencia = dato.getString("pedidos_sin_existencia"),
                        networkProvider = dato.getBoolean("networkProvider_app"),
                        usarHojaCarga = dato.getBoolean("hoja_carga_inventario_app"),
                        mostrarPrecioApp = dato.getInt("precio_mostrar_app"),
                        mHistorio = dato.getBoolean("m_Historico"),
                        mHojaCarga = dato.getBoolean("m_HojaCarga"),
                        mGastos = dato.getBoolean("m_Gastos"),
                        mReportes = dato.getBoolean("m_Reportes"),
                        mCxC = dato.getBoolean("m_CxC"),
                        mAbonos = dato.getBoolean("m_Abonos"),
                        pMantto_Clientes = dato.getBoolean("p_Mantto_Clientes"),
                        pImprimirTKVenta = dato.getBoolean("p_Imprimir_TK_Venta"),
                        solicitudCargaSinExistencia = dato.getBoolean("solicitud_Carga_SinExistencia"),
                        validarHojaCarga = dato.optBoolean("validacionHojaCarga", false),
                        docFactura = dato.getBoolean("doc_Factura"),
                        docCreFiscal = dato.getBoolean("doc_CreFiscal"),
                        docRecibo = dato.getBoolean("doc_Recibo"),
                        docRemision = dato.getBoolean("doc_Remision"),
                        docFacExportacion = dato.getBoolean("doc_FacExportacion"),
                        empresa = dato.getString("empresa"),
                        direccion = dato.getString("direccion"),
                        nrc = dato.getString("nrc"),
                        nit = dato.getString("nit"),
                        giro = dato.getString("giro"),
                        dteUrlQRHacienda = dato.optString("dteUrlQR_Hacienda", "0"),
                        dteUrlQRempresa = dato.optString("dteUrlQR_empresa", "0"),
                        numItemFactura = dato.optInt("numItemFactura", 100),
                        numItemCreFiscal = dato.optInt("numItemCreFiscal",100),
                        numItemRecibo = dato.optInt("numItemRecibo", 100),
                        numItemRemision = dato.optInt("numItemRemision", 100),
                        tipoVentaLocal = dato.optBoolean("tipoVentaLocal", false),
                        modoDesarrollo = dato.optBoolean("modoDesarrollo", false),
                        cargaAutomaticaCatalogos = dato.optBoolean("cargaAutomaticaCatalogos", false),
                        decPrecios = dato.optInt("decPrecios", 2),
                        decTotales = dato.optInt("decTotales", 2),
                        multiplesHojaDeCarga = dato.optBoolean("multiplesHojaDeCarga", false),
                        idBodega = if(dato.isNull("id_bodega_inventario")) null else dato.optInt("id_bodega_inventario"),
                        codBodega = if(dato.isNull("cod_bodega_inventario")) null else dato.optString("cod_bodega_inventario"),
                        bodega = if(dato.isNull("bodega_inventario")) null else dato.optString("bodega_inventario"),
                        inventarioTiempoReal = dato.optBoolean("inventario_tiempo_real", false),
                        eliminarPedidosAutomaticos = dato.optBoolean("eliminar_pedidos_automatico", false),
                        tipoBonificacion = dato.optString("tipoBonificacion", "T"),
                        habilitarFTS4 =  dato.optBoolean("habilitarFTS4", false)
                    )

                    confirmarPagareObligatorio(item, context)
                }
            }

        }

    }

    //FUNCION PARA SETEAR LA FORMA DEL PAGARE EN SHAREDPREFERENCES
    private fun confirmarPagareObligatorio(obj: PermisosApp, context: Context){

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        preferences.edit {

            putBoolean("PagareObligatorio", obj.pagareObligarotio)
            putBoolean("modificar_precio_app", obj.modificarPrecio)
            putString("pedidos_sin_existencia", obj.pedidoSinExistencia)
            putBoolean("NetworkProvider_app", obj.networkProvider)
            putBoolean("Hoja_carga_inventario_app", obj.usarHojaCarga)
            putInt("vistaInventario", 2)
            putFloat("versionActualApp", 1.0f)
            putInt("precio_mostrar_app", obj.mostrarPrecioApp)

            //Permisos de los Modulos
            putBoolean("M_Historio", obj.mHistorio)
            putBoolean("M_HojaCarga", obj.mHojaCarga)
            putBoolean("M_Gastos", obj.mGastos)
            putBoolean("M_Reportes", obj.mReportes)
            putBoolean("M_CxC", obj.mCxC)
            putBoolean("M_Abonos", obj.mAbonos)
            putBoolean("P_Mantto_Clientes", obj.pMantto_Clientes)
            putBoolean("P_Imprimir_TK_Venta", obj.pImprimirTKVenta)
            putBoolean("Solicitud_Carga_SinExistencia", obj.solicitudCargaSinExistencia)
            putBoolean("validarHojaCarga", obj.validarHojaCarga)

            //Documentos de Facturacion Permitidos
            putBoolean("Doc_Factura", obj.docFactura)
            putBoolean("Doc_CreFiscal", obj.docCreFiscal)
            putBoolean("Doc_Recibo", obj.docRecibo)
            putBoolean("Doc_Remision", obj.docRemision)
            putBoolean("Doc_FacExportacion", obj.docFacExportacion)

            //Datos de la empresa para el TK
            putString("empresa", obj.empresa)
            putString("direccion", obj.direccion)
            putString("nrc", obj.nrc)
            putString("nit", obj.nit)
            putString("giro", obj.giro)

            //Datos Qr
            putString("dteUrlQRHacienda", obj.dteUrlQRHacienda)
            putString("dteUrlQRempresa", obj.dteUrlQRempresa)

            //Item por documento
            putInt("numItemFactura", obj.numItemFactura)
            putInt("numItemCreFiscal", obj.numItemCreFiscal)
            putInt("numItemRecibo", obj.numItemRecibo)
            putInt("numItemRemision", obj.numItemRemision)

            //Modo de configuraion Local o Ruta
            putBoolean("tipoVentaLocal", obj.tipoVentaLocal)

            //IMPRESORA POR DEFECTO
            putString("tipoImpresora", "BT")
            putString("impresorIntegrado", "sinNombre")

            //Modo Desarrollo
            putBoolean("modoDesarrollo", obj.modoDesarrollo)

            //Carga automatica de Catalogos Cliente
            putBoolean("cargaAutomaticaCatalogos", obj.cargaAutomaticaCatalogos)

            //Decimales para totales y precios
            putInt("decPrecios", obj.decPrecios)
            putInt("decTotales", obj.decTotales)

            //Uso de Multiples hojas de car
            putBoolean("multiplesHojaDeCarga", obj.multiplesHojaDeCarga)

            //Uso de Bodega

            obj.idBodega?.let {
                putInt("idBodega", it)
            }

            obj.codBodega?.let {
                putString("codBodega", it)
            }

            obj.bodega?.let {
                putString("bodega", it)
            }

            //putInt("idBodega", obj.idBodega!!)
            //putString("codBodega", obj.codBodega)
            //putString("bodega", obj.bodega)

            //Inventario tiempoReal
            putBoolean("inventarioTiempoReal", obj.inventarioTiempoReal)
            putBoolean("eliminarPedidosAutomaticos", obj.eliminarPedidosAutomaticos)

            //tipo de Bonificacion
            putString("tipoBonificacion", obj.tipoBonificacion)

            //Habilitar FTS4
            putBoolean("habilitarFTS4", obj.habilitarFTS4!!)

        }
    }

    private fun eliminarConfiguracionApp(context: Context){

        preferences = context.getSharedPreferences(instancia,Context.MODE_PRIVATE)
        preferences.edit{
            remove("PagareObligatorio")
            remove("modificar_precio_app")
            remove("pedidos_sin_existencia")
            remove("NetworkProvider_app")
            remove("Hoja_carga_inventario_app")
            remove("vistaInventario")
            remove("versionActualApp")
            remove("precio_mostrar_app")
            remove("M_Historio")
            remove("M_HojaCarga")
            remove("M_Gastos")
            remove("M_Reportes")
            remove("M_CxC")
            remove("M_Abonos")
            remove("P_Mantto_Clientes")
            remove("P_Imprimir_TK_Venta")
            remove("Solicitud_Carga_SinExistencia")
            remove("Doc_Factura")
            remove("Doc_CreFiscal")
            remove("Doc_Recibo")
            remove("Doc_Remision")
            remove("Doc_FacExportacion")
            remove("empresa")
            remove("direccion")
            remove("nrc")
            remove("nit")
            remove("giro")
            remove("dteUrlQR_Hacienda")
            remove("dteUrlQR_empresa")
            //Item por documento
            remove("numItemFactura")
            remove("numItemCreFiscal")
            remove("numItemRecibo")
            remove("numItemRemision")
            //Modo de configuraion Local o Ruta
            remove("tipoVentaLocal")

            //eliminado tipo de impresora
            remove("tipoImpresora")
            remove("impresorIntegrado")

            remove("validarHojaCarga")
            remove("modoDesarrollo")

            remove("cargaAutomaticaCatalogos")

            //eliminando numero de decimales en precios y totales
            remove("decTotales")
            remove("decPrecios")

            //Uso de multiples hojas de carga
            remove("multiplesHojaDeCarga")

            //Uso de bodegas
            remove("idBodega")
            remove("codBodega")
            remove("bodega")

            //inventario tiempo real
            remove("inventarioTiempoReal")
            remove("eliminarPedidosAutomaticos")

            //Tipo Bonificacion
            remove("tipoBonificacion")

            //FullText Search 4
            remove("habilitarFTS4")
        }
    }
}