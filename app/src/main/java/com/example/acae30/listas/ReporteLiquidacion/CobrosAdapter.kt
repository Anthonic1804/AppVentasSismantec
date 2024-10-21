package com.example.acae30.listas.ReporteLiquidacion

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.ReporteLiquidacion.VentaContado

class CobrosAdapter(private var lista : ArrayList<VentaContado>, private val context: Context) :
    RecyclerView.Adapter<CobrosAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.forma_pago,
            parent,
            false
        )
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = lista[position]

        holder.formaPago.text = item.tipoPago.uppercase()
        holder.totalCard.text = "$ " + "${String.format("%.4f".format(item.total))}"
    }

    override fun getItemCount(): Int = lista.size

    class MyViewHolder(item : View) : RecyclerView.ViewHolder(item){
        val formaPago : TextView = item.findViewById(R.id.tvFormaPagoCard)
        val totalCard : TextView = item.findViewById(R.id.tvTotalCard)
    }

}