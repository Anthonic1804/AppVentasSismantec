package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.SolicitudCarga.SolicitudCarga
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle

class SolicitudAdapter(
    private var list: ArrayList<SolicitudCarga>, private var context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<SolicitudAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolicitudAdapter.MyViewHolder {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.tarjeta_solicitud, parent, false)
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(vista: SolicitudAdapter.MyViewHolder, position: Int) {
        val data = list[position]
        vista.empleado.text = data.empleado
        vista.fecha.text = data.fecha
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var empleado: TextView
        internal var fecha: TextView

        init {
            empleado = itemView.findViewById(R.id.tvEmpleadoSolicitud)
            fecha = itemView.findViewById(R.id.tvFechaSolicitud)
            itemView.setOnClickListener({ itemClick(layoutPosition) })

        }

    }

}