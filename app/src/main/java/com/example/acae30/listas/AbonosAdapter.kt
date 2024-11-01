package com.example.acae30.listas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.controllers.AbonosController
import com.example.acae30.modelos.Abono

class AbonosAdapter (private var list: ArrayList<Abono>, private val context: Context)  :
    RecyclerView.Adapter<AbonosAdapter.MyViewHolder>(){

        private var funciones = Funciones()
        private var abonoController = AbonosController()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val vista  = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_abono,
            parent,
            false
        )

        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        holder.codigo.text = item.codigoCliente.toString()
        holder.cliente.text = item.Cliente.toString()

        if(item.IdSucursal!! > 0){
            holder.sucursal.text = item.Sucursal.toString()
        }else{
            holder.sucursal.text = context.getString(R.string.sin_sucursal)
        }

        //SI EL PEDIDO HA SIDO ENVIADO
        if(item.AbonoEnviado == 0){
            holder.cvColorAbono.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.moderado))
        }else{
            holder.cvColorAbono.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.cart))
        }
        
        holder.total.text = "$ " + String.format("%.2f", item.Abono) + " ${item.Tipo_pago}"
        holder.fecha.text = item.Fecha_hora_proceso


        holder.btnEliminarAbono.setOnClickListener {
            if(item.AbonoEnviado == 0){
                funciones.mensaje(context, "DEBE ENVIAR EL ABONO PARA LUEGO ANULARLO")
            }else{
                abonoController.mensajeAnulacion(context, item.Cliente.toString(), item.idAbonoServer)
            }
        }
    }

    override fun getItemCount(): Int = list.size


    class MyViewHolder(item: View) : RecyclerView.ViewHolder(item){
        val codigo : TextView = item.findViewById(R.id.tvCodigoAbono)
        val cliente : TextView = item.findViewById(R.id.tvClienteAbono)
        val sucursal : TextView = item.findViewById(R.id.tvSucursalAbono)
        val total : TextView = item.findViewById(R.id.tvTotalAbono)
        val fecha : TextView = item.findViewById(R.id.tvFechaAbono)
        val cvColorAbono : CardView = item.findViewById(R.id.cvColorAbono)
        val btnEliminarAbono : ImageView = item.findViewById(R.id.btnEliminarAbono)
    }
}