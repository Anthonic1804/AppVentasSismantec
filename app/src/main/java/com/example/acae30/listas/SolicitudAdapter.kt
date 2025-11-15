package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.SolicitudCarga.SolicitudCarga

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
        vista.ruta.text = data.ruta
        vista.fecha.text = data.fecha
        vista.estado.text = data.estado
        vista.hoja.text = data.numHoja.toInt().toString()


        //SI LA SOLICITUD HA SIDO ENVIADA CORRECTAMENTE
        if(data.enviado == 0){
            vista.card.setCardBackgroundColor(
                ContextCompat.getColor(vista.itemView.context, R.color.moderado)
            )
        }else{
            vista.card.setCardBackgroundColor(
                ContextCompat.getColor(vista.itemView.context, R.color.cart)
            )
        }
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var ruta: TextView
        internal var fecha: TextView
        internal var card : CardView
        internal var estado : TextView
        internal var hoja : TextView

        init {
            ruta = itemView.findViewById(R.id.tvRutaSolicitud)
            fecha = itemView.findViewById(R.id.tvFechaSolicitud)
            card = itemView.findViewById(R.id.colorCard)
            estado = itemView.findViewById(R.id.txtEstadoSolicitud)
            hoja = itemView.findViewById(R.id.txtNumHoja)
            itemView.setOnClickListener({ itemClick(layoutPosition) })

        }

    }

}