package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.GastoModel

class GastosAdapter(private var list: ArrayList<GastoModel>, private val context: Context) :
    RecyclerView.Adapter<GastosAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastosAdapter.MyViewHolder {
        val vista  = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_gasto,
            parent,
            false
        )

        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        holder.descripcion.text = item.concepto
        holder.empleado.text = item.persona
        holder.total.text = "$ " + String.format("%.2f", item.valor) + " ${item.forma}"
        holder.fecha.text = item.fecha
    }


    override fun getItemCount(): Int = list.size


    class MyViewHolder(item: View) : RecyclerView.ViewHolder(item){
        val descripcion : TextView = item.findViewById(R.id.tvDescripcionGasto)
        val empleado : TextView = item.findViewById(R.id.tvEmpleadoGasto)
        val total : TextView = item.findViewById(R.id.tvTotalGasto)
        val fecha : TextView = item.findViewById(R.id.tvFechaGasto)
    }


}