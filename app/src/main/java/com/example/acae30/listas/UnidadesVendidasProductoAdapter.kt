package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.reporteUnidadesVendidas.UnidadesVendidasPorProducto

class UnidadesVendidasProductoAdapter(
    private val lista: List<UnidadesVendidasPorProducto>,
    context: Context
) : RecyclerView.Adapter<UnidadesVendidasProductoAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UnidadesVendidasProductoAdapter.MyViewHolder {
        val vistaHolder = LayoutInflater.from(parent.context).inflate(R.layout.carta_producto_ventasreporte, parent, false)
        return MyViewHolder(vistaHolder)
    }

    override fun onBindViewHolder(
        vista: UnidadesVendidasProductoAdapter.MyViewHolder,
        i: Int
    ) {
        val item = lista[i]

        vista.descripcion.text = item.descripcion
        vista.cantidad.text = item.cantidad.toString() + " UNI."
    }

    override fun getItemCount(): Int = lista.size

    inner class MyViewHolder(item : View) : RecyclerView.ViewHolder(item){
        internal val descripcion : TextView
        internal val cantidad : TextView

        init{
            descripcion = item.findViewById<TextView>(R.id.txtNombreProducto)
            cantidad = item.findViewById<TextView>(R.id.txtCantidadVendida)
        }
    }
}