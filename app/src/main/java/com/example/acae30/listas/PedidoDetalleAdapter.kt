package com.example.acae30.listas

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.DetallePedido

class PedidoDetalleAdapter(
    private var context: Context,
    val itemClick: (Int) -> Unit
) : ListAdapter<DetallePedido, PedidoDetalleAdapter.MyViewHolder>(DiffCallback()) {

    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PedidoDetalleAdapter.MyViewHolder {
        val vista =
            LayoutInflater.from(parent.context).inflate(R.layout.detalle_pedido, parent, false)
        return MyViewHolder(vista)
    }


    //MODIFICACION PARA AUMENTAR EL NUMERO DE DECIMALES A 4
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    override fun onBindViewHolder(vista: PedidoDetalleAdapter.MyViewHolder, position: Int) {
        preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val decTotales = preferencias!!.getInt("decTotales", 2)

        val data = getItem(position)
        
        // Formatear cantidad de forma segura
        val cant = data.Cantidad ?: 0f
        val bonif = (data.Bonificado ?: 0).toFloat()
        val totalCant = cant + bonif
        
        vista.cantidad.text = String.format("%.2f", totalCant)
        
        // Formatear descripción de forma segura
        val desc = data.Descripcion ?: "Sin descripción"
        val loteInfo = if (data.Lote != null) " | LOTE: ${data.Lote} | F. VENCIMIENTO: ${data.FechaVencimiento ?: "N/A"}" else ""
        vista.descripcion.text = "$desc$loteInfo"
        
        // Formatear total de forma segura
        val totalIva = data.Total_iva ?: 0f
        val asterisco = if (data.Precio_editado == "*") "*" else ""
        vista.total.text = "$${String.format("%.${decTotales}f", totalIva)}$asterisco"
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    class DiffCallback : DiffUtil.ItemCallback<DetallePedido>() {
        override fun areItemsTheSame(oldItem: DetallePedido, newItem: DetallePedido): Boolean {
            return oldItem.Id == newItem.Id
        }

        override fun areContentsTheSame(oldItem: DetallePedido, newItem: DetallePedido): Boolean {
            return oldItem == newItem
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        internal var cantidad: TextView
        internal var descripcion: TextView
        internal var total: TextView

        init {
            cantidad = itemView.findViewById(R.id.txtcantidad)
            descripcion = itemView.findViewById(R.id.txtdescripcion)
            total = itemView.findViewById(R.id.txtprecio)
            itemView.setOnClickListener({ itemClick(layoutPosition) })

        }

    }

}
