package com.example.acae30.listas

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.InventarioTiempoRealModel

class InventarioTiempoRealAdapter(
    private val lista: List<InventarioTiempoRealModel>,
    private val context: Context,
    val itemClick : (Int) -> Unit
) : RecyclerView.Adapter<InventarioTiempoRealAdapter.MyViewHolder>() {

    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val vistaHolder = LayoutInflater.from(parent.context).inflate(R.layout.carta_inventario, parent, false)
        return MyViewHolder(vistaHolder)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(vista: MyViewHolder, i: Int) {
        preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val decPrecios = preferencias!!.getInt("decPrecios",2)

        val id = lista[i].id
        vista.titulo.text = lista[i].codigo
        vista.descripcion.text = lista[i].descripcion
        vista.precio.text = "$" + String.format("%.${decPrecios}f", lista[i].precioUiva)
        vista.existencia.text = "${String.format("%.2f", lista[i].existencia)}" + " " + if(lista[i].unidadMedida.isNullOrBlank()) "UNIDAD" else lista[i].unidadMedida
        vista.fraccion.text = "${String.format("%.2f", lista[i].exitenciaFraccion)}" + " " + lista[i].nombreFraccion
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        internal var titulo: TextView
        internal var descripcion: TextView
        internal var precio: TextView
        internal var carta: CardView
        internal var existencia : TextView
        internal var fraccion : TextView

        //AGRENADO IMAGEN A LA VISTA MINISTURA
        internal var imagen : ImageView

        init {
            //colores = context.applicationContext.resources.getStringArray(R.array.colors)
            titulo = itemView.findViewById(R.id.txttittulo)
            descripcion = itemView.findViewById(R.id.txtdescripcion)
            precio = itemView.findViewById(R.id.txtprecio)
            carta = itemView.findViewById(R.id.carta)
            existencia = itemView.findViewById(R.id.txtexiste)
            fraccion = itemView.findViewById(R.id.txtFraccion)
            imagen = itemView.findViewById(R.id.imgFoto)
            itemView.setOnClickListener({ itemClick(layoutPosition) })
        }


    }

}