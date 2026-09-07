package com.example.acae30.listas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.data.local.entity.ClienteSucursalEntity
import java.util.Locale

class SucursalBusquedaAdapter(
    private var sucursalesOriginal: List<ClienteSucursalEntity>,
    private val onSucursalSelected: (ClienteSucursalEntity) -> Unit
) : RecyclerView.Adapter<SucursalBusquedaAdapter.ViewHolder>(), Filterable {

    private var sucursalesFiltradas: List<ClienteSucursalEntity> = sucursalesOriginal

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreSucursal)
        val tvDireccion: TextView = view.findViewById(R.id.tvDireccionSucursal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sucursal_busqueda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sucursal = sucursalesFiltradas[position]
        holder.tvNombre.text = sucursal.nombreSucursal
        holder.tvDireccion.text = sucursal.direccionSucursal ?: "Sin dirección"
        holder.itemView.setOnClickListener { onSucursalSelected(sucursal) }
    }

    override fun getItemCount(): Int = sucursalesFiltradas.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                sucursalesFiltradas = if (charSearch.isEmpty()) {
                    sucursalesOriginal
                } else {
                    val resultList = mutableListOf<ClienteSucursalEntity>()
                    for (row in sucursalesOriginal) {
                        if (row.nombreSucursal?.lowercase(Locale.ROOT)?.contains(charSearch.lowercase(Locale.ROOT)) == true) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = sucursalesFiltradas
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                sucursalesFiltradas = results?.values as List<ClienteSucursalEntity>
                notifyDataSetChanged()
            }
        }
    }
}
