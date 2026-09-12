package com.example.acae30.listas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.data.local.entity.PedidosEntity
import java.util.Locale

class PedidosBorradoresAdapter(
    private val idPedidoActivo: Int,
    private val onPedidoSelected: (PedidosEntity) -> Unit
) : RecyclerView.Adapter<PedidosBorradoresAdapter.ViewHolder>() {

    private var items: List<PedidosEntity> = emptyList()

    fun submitList(newList: List<PedidosEntity>) {
        items = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvIdPedido)
        val tvInfo: TextView = view.findViewById(R.id.tvFechaMonto)
        val tvBadge: TextView = view.findViewById(R.id.tvBadgeActivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_abierto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvId.text = item.nombreCliente
        holder.tvInfo.text = "Pedido #${item.id} | Creado: ${item.fechaCreado} | Total: $${String.format(Locale.getDefault(), "%.2f", item.total)}"
        
        if (item.id == idPedidoActivo) {
            holder.tvBadge.visibility = View.VISIBLE
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onPedidoSelected(item) }
    }

    override fun getItemCount(): Int = items.size
}
