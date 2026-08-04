package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.Cuenta
import java.text.SimpleDateFormat
import java.util.Locale

class CuentaAdapter(private val context: Context) :
    ListAdapter<Cuenta, CuentaAdapter.MyViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_detalle_cuentas,
            parent,
            false
        )
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        
        var f = ""
        try {
            if (!data.Fecha.isNullOrEmpty()) {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = parser.parse(data.Fecha!!)
                if (date != null) {
                    f = formatter.format(date)
                }
            }
        } catch (e: Exception) {
            f = data.Fecha ?: ""
        }
        
        holder.fecha.text = f
        holder.documento.text = data.Documento ?: ""
        holder.total.text = "$ " + String.format(Locale.getDefault(), "%.2f", data.Saldo_actual ?: 0f)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var fecha: TextView = itemView.findViewById(R.id.txtfecha)
        internal var documento: TextView = itemView.findViewById(R.id.txtdocumento)
        internal var total: TextView = itemView.findViewById(R.id.txttotal)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Cuenta>() {
            override fun areItemsTheSame(oldItem: Cuenta, newItem: Cuenta): Boolean {
                return oldItem.Id == newItem.Id
            }

            override fun areContentsTheSame(oldItem: Cuenta, newItem: Cuenta): Boolean {
                return oldItem == newItem
            }
        }
    }
}