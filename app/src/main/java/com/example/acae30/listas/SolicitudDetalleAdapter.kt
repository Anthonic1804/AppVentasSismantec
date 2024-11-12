package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle

class SolicitudDetalleAdapter(
    private var list: ArrayList<SolicitudCargaDetalle>, private var context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<SolicitudDetalleAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolicitudDetalleAdapter.MyViewHolder {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.detalle_solicitud, parent, false)
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(vista: SolicitudDetalleAdapter.MyViewHolder, position: Int) {
        val data = list[position]
        vista.cantidad.text = "${String.format("%.0f", data.cantidad)}"
        vista.descripcion.text = data.descripcion
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var cantidad: TextView
        internal var descripcion: TextView

        init {
            cantidad = itemView.findViewById(R.id.txtcantidad)
            descripcion = itemView.findViewById(R.id.txtdescripcion)
            itemView.setOnClickListener({ itemClick(layoutPosition) })

        }

    }

}