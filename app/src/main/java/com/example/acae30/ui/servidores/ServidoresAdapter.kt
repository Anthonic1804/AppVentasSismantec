package com.example.acae30.ui.servidores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.data.local.entity.ServidoresEntity

class ServidoresAdapter(
    private val itemClick: (ServidoresEntity) -> Unit
) : ListAdapter<ServidoresEntity, ServidoresAdapter.MyViewHolder>(
    DiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_servidor, parent, false)

        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)

        holder.nombreServidor.text = item.nombre
        holder.ipServidor.text = item.ip
        holder.puertoServidor.text = item.puerto

        holder.itemView.setOnClickListener {
            itemClick(item)
        }
    }

    //override fun getItemCount(): Int = lista.size

    class MyViewHolder(item: View) : RecyclerView.ViewHolder(item){

        val nombreServidor: TextView =
            item.findViewById(R.id.tvNombreServidor)

        val ipServidor: TextView =
            item.findViewById(R.id.tvIpServidor)

        val puertoServidor: TextView =
            item.findViewById(R.id.tvPuertoServidor)

    }

    companion object {

        private val DiffCallback =
            object : DiffUtil.ItemCallback<ServidoresEntity>() {

                override fun areItemsTheSame(
                    oldItem: ServidoresEntity,
                    newItem: ServidoresEntity
                ): Boolean {

                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: ServidoresEntity,
                    newItem: ServidoresEntity
                ): Boolean {

                    return oldItem == newItem
                }
            }
    }

}