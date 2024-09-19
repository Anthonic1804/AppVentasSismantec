package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        holder.total.text = "$ " + String.format("%.2f", item.Abono)
        holder.fecha.text = item.Fecha_hora_proceso
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(item: View) : RecyclerView.ViewHolder(item){
        internal var codigo : TextView
        internal var cliente : TextView
        internal var total : TextView
        internal var fecha : TextView

        init {
            codigo = item.findViewById(R.id.tvCodigoAbono)
            cliente  = item.findViewById(R.id.tvClienteAbono)
            total = item.findViewById(R.id.tvTotalAbono)
            fecha = item.findViewById(R.id.tvFechaAbono)
        }
    }
}