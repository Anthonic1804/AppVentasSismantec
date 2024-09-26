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
import com.example.acae30.modelos.Abono

class AbonosAdapter (private var list: ArrayList<Abono>, private val context: Context)  :
    RecyclerView.Adapter<AbonosAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbonosAdapter.MyViewHolder {
        val vista  = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_abono,
            parent,
            false
        )

        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: AbonosAdapter.MyViewHolder, position: Int) {
        val item = list[position]
        holder.codigo.text = item.codigoCliente.toString()
        holder.cliente.text = item.Cliente.toString()

        if(item.IdSucursal!! > 0){
            holder.sucursal.text = item.Sucursal.toString()
        }else{
            holder.sucursal.text = context.getString(R.string.sin_sucursal)
        }

        //SI EL PEDIDO HA SIDO ENVIADO
        if(item.PedidoEnviado == 0){
            holder.cvColorAbono.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.moderado))
        }else{
            holder.cvColorAbono.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.cart))
        }
        
        holder.total.text = "$ " + String.format("%.2f", item.Abono) + " ${item.Tipo_pago}"
        holder.fecha.text = item.Fecha_hora_proceso
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(item: View) : RecyclerView.ViewHolder(item){
        internal var codigo : TextView
        internal var cliente : TextView
        internal var sucursal : TextView
        internal var total : TextView
        internal var fecha : TextView
        internal var cvColorAbono : CardView

        init {
            codigo = item.findViewById(R.id.tvCodigoAbono)
            cliente  = item.findViewById(R.id.tvClienteAbono)
            sucursal = item.findViewById(R.id.tvSucursalAbono)
            total = item.findViewById(R.id.tvTotalAbono)
            fecha = item.findViewById(R.id.tvFechaAbono)
            cvColorAbono = item.findViewById(R.id.cvColorAbono)
        }
    }
}