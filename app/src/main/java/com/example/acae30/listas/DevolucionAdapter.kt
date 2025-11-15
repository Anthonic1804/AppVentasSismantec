package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.SolcitudDevolucion.SolicitudDevolucion
import org.w3c.dom.Text

class DevolucionAdapter (
    private var list: ArrayList<SolicitudDevolucion>, private var context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<DevolucionAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DevolucionAdapter.MyViewHolder {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.tarjeta_solicitud, parent, false)
        return MyViewHolder(vista)
    }


    override fun onBindViewHolder(vista: DevolucionAdapter.MyViewHolder, position: Int) {
        val data = list[position]
        vista.ruta.text = data.Ruta
        vista.fecha.text = data.Fecha
        vista.header.text = "SOLICITUD DE DEVOLUCION"
        vista.estado.visibility = View.GONE
        vista.numHoja.text = "NUM. DEVOLUCION"
        vista.txtNumHoja.text = data.Numero.toString()


        //SI LA SOLICITUD HA SIDO ENVIADA CORRECTAMENTE
        if(data.Numero == 0){
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
        internal var header : TextView
        internal var estado : LinearLayout
        internal var numHoja : TextView
        internal  var txtNumHoja : TextView

        init {
            ruta = itemView.findViewById(R.id.tvRutaSolicitud)
            fecha = itemView.findViewById(R.id.tvFechaSolicitud)
            card = itemView.findViewById(R.id.colorCard)
            header = itemView.findViewById(R.id.txtcodigo)
            estado = itemView.findViewById(R.id.lyEstadosolicitud)
            numHoja = itemView.findViewById(R.id.tvNumHoja)
            txtNumHoja = itemView.findViewById(R.id.txtNumHoja)
            itemView.setOnClickListener({ itemClick(layoutPosition) })

        }

    }

}