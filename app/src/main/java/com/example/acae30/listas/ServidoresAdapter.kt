package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.Servidores.ServidoresModel

class ServidoresAdapter(
    private var lista: List<ServidoresModel>,
    private var context: Context,
    val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<ServidoresAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServidoresAdapter.MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_servidor,
            parent,
            false
        )

        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ServidoresAdapter.MyViewHolder, position: Int) {
        val i = lista[position]

        holder.nombreServidor.text = i.nombre
        holder.ipServidor.text = i.ip
        holder.puertoServidor.text = i.puerto
    }

    override fun getItemCount(): Int = lista.size

    inner class MyViewHolder(item: View) : RecyclerView.ViewHolder(item){

        internal var nombreServidor : TextView
        internal var ipServidor : TextView
        internal var puertoServidor : TextView

        init {
            nombreServidor = item.findViewById(R.id.tvNombreServidor)
            ipServidor = item.findViewById(R.id.tvIpServidor)
            puertoServidor = item.findViewById(R.id.tvPuertoServidor)

            item.setOnClickListener({
                itemClick(layoutPosition)
            })
        }

    }

}