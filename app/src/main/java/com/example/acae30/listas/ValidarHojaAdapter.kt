package com.example.acae30.listas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.InventarioHojaValidar

class ValidarHojaAdapter(
    private val lista: List<InventarioHojaValidar>,
    private val onCheckChanged: (InventarioHojaValidar, Boolean) -> Unit
) : RecyclerView.Adapter<ValidarHojaAdapter.MyViewHolder>() {

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v){
        val codigo: TextView = v.findViewById(R.id.lblCodigoProducto)
        val descripcion: TextView = v.findViewById(R.id.lblDescripcionProducto)
        val existencia: TextView = v.findViewById(R.id.lblExistenciaProducto)
        val aceptado: CheckBox = v.findViewById(R.id.cbxAceptado)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ValidarHojaAdapter.MyViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.carta_inventario_validar,
            parent,
            false
        )
        return MyViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ValidarHojaAdapter.MyViewHolder, position: Int) {
        val item = lista[position]

        holder.codigo.text = item.Codigo
        holder.descripcion.text = item.descripcion
        holder.existencia.text = item.Existencia.toString() + " UNIDADES"

        holder.aceptado.setOnCheckedChangeListener(null)

        holder.aceptado.isChecked = item.validadoHoja == 1

        holder.aceptado.setOnCheckedChangeListener { _, isChecked ->

            item.validadoHoja = if(isChecked) 1 else 0

            onCheckChanged(item, isChecked)
        }

    }

    override fun getItemCount(): Int = lista.size


}