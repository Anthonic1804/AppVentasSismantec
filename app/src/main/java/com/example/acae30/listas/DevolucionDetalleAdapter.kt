package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.SolcitudDevolucion.SolicitudDevolucionDetalle

class DevolucionDetalleAdapter(
    private var lista: ArrayList<SolicitudDevolucionDetalle>, private var context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<DevolucionDetalleAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DevolucionDetalleAdapter.MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.detalle_devolucion,parent, false)
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: DevolucionDetalleAdapter.MyViewHolder, position: Int) {
        val data = lista[position]
        holder.descripcion.text = data.Producto
        holder.cantidad.text = data.Cantidad.toString()
        holder.bueno.text = data.Bueno.toString()
        holder.averia.text = data.Averia.toString()
    }

    override fun getItemCount(): Int = lista.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        internal var descripcion: TextView
        internal var cantidad: TextView
        internal var bueno: TextView
        internal var averia: TextView

        init {
            descripcion = itemView.findViewById(R.id.txtdescripcion)
            cantidad = itemView.findViewById(R.id.txtcantidad)
            bueno = itemView.findViewById(R.id.txtBueno)
            averia = itemView.findViewById(R.id.txtAveria)

            itemView.setOnClickListener {
                itemClick(layoutPosition)
            }
        }

    }
}