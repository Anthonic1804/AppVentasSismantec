package com.example.acae30.listas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.SucursalesTarjetaModel

class SucursalesAdapter (private var list: ArrayList<SucursalesTarjetaModel>)  :
    RecyclerView.Adapter<SucursalesAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val vista  = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_sucursal,
            parent,
            false
        )

        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        holder.codigo.text = item.codigoSucursal
        holder.sucursal.text = item.nombreSucursal
        holder.direccion.text = item.direccionSucursal
        holder.telefono.text = item.telefono
        holder.ruta.text = item.ruta

    }

    override fun getItemCount(): Int = list.size


    class MyViewHolder(item: View) : RecyclerView.ViewHolder(item){
        val codigo : TextView = item.findViewById(R.id.tvCodigoSucursal)
        val sucursal : TextView = item.findViewById(R.id.tvNombreSucursal)
        val direccion : TextView = item.findViewById(R.id.tvDireccionSucursal)
        val telefono : TextView = item.findViewById(R.id.tvTelefonoSucursal)
        val ruta : TextView = item.findViewById(R.id.tvRutaSucursal)
    }
}