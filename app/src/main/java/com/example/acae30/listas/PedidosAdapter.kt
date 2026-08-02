package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.modelos.Pedidos

/**
 * REFACTORIZACIÓN MVVM: PedidosAdapter ahora hereda de ListAdapter.
 * Esto permite el uso de DiffUtil para actualizaciones eficientes de la lista.
 */
class PedidosAdapter(
    private val context: Context,
    private val itemClick: (Int) -> Unit
) : ListAdapter<Pedidos, PedidosAdapter.MyViewHolder>(DiffCallback) {

    private var ani: Funciones? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.carta_pedidos, parent, false)
        return MyViewHolder(vista)
    }

    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        ani!!.AnimacionCircularReavel(holder.itemView)
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // En ListAdapter se usa getItem(position) en lugar de lista[position]
        val data = getItem(position)
        
        holder.txtPedido.text = "PED${data.Id}"
        holder.txtCliente.text = data.Nombre_cliente
        holder.txtTipoDoc.text = data.Tipo_documento

        var total = data.Total ?: 0f
        if((data.Iva_Percibido ?: 0f) > 0f){
            total = (data.Total ?: 0f) - (data.Iva_Percibido ?: 0f)
        }

        holder.txtTotal.text = "$" + "${String.format("%.2f".format(total))}"

        var estado = "ENVIADO"
        if (data.Enviado == 0) {
            estado = "NO ENVIADO"
            holder.txtEstado.setBackgroundResource(R.drawable.border_status_red)
        } else {
            // Restaurar fondo verde si ya fue enviado (importante en ListAdapter al reciclar vistas)
            holder.txtEstado.setBackgroundResource(R.drawable.border_status_green)
        }
        holder.txtEstado.text = estado
        holder.txtFecha.text = data.Fecha_creado

        var transmitido = "TRANSMITIDO"
        holder.txtTransmitido.setBackgroundResource(R.drawable.border_status_green)
        holder.txtEstado.visibility = View.VISIBLE

        if(data.pedido_dte == 0){
            transmitido = "NO TRANSMITIDO"
            holder.txtTransmitido.setBackgroundResource(R.drawable.border_status_red)
        }

        if(data.pedido_dte_error == 1 && data.pedido_dte == 0){
            transmitido = "ERROR DE TRANSMISION"
            holder.txtTransmitido.setBackgroundResource(R.drawable.border_status_red)
        }

        if(data.pedido_dte_error == 2){
            transmitido = "INVALIDADO"
            holder.txtTransmitido.setBackgroundResource(R.drawable.border_status_red)
            holder.txtEstado.visibility = View.GONE
        }

        holder.txtTransmitido.text = transmitido
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var txtPedido: TextView = itemView.findViewById(R.id.txtPedido)
        internal var txtCliente: TextView = itemView.findViewById(R.id.txtCliente)
        internal var txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        internal var txtEstado: TextView = itemView.findViewById(R.id.txtEnviado)
        internal var txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        internal var txtTransmitido: TextView = itemView.findViewById(R.id.tvTransmitido)
        internal var txtTipoDoc: TextView = itemView.findViewById(R.id.txtTipoDoc)

        init {
            ani = Funciones()
            itemView.setOnClickListener { itemClick(adapterPosition) }
        }
    }

    /**
     * Objeto estático para comparar elementos de la lista de forma eficiente.
     */
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Pedidos>() {
            override fun areItemsTheSame(oldItem: Pedidos, newItem: Pedidos): Boolean {
                // Compara si es el mismo objeto por ID
                return oldItem.Id == newItem.Id
            }

            override fun areContentsTheSame(oldItem: Pedidos, newItem: Pedidos): Boolean {
                // Compara si el contenido (estado, total, etc) ha cambiado
                return oldItem == newItem
            }
        }
    }
}
