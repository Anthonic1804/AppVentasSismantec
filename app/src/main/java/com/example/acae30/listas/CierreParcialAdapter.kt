package com.example.acae30.listas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.R
import com.example.acae30.modelos.cierreParcial.CierreItem

class CierreParcialAdapter(
    private val lista: List<CierreItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (lista[position]) {
            is CierreItem.Header -> 0
            is CierreItem.TipoDocumento -> 1
            is CierreItem.Correlativo -> 2
            is CierreItem.FormaPago -> 3
            is CierreItem.Termino -> 4
            is CierreItem.Resumen -> 5
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            0 -> HeaderVH(inflater.inflate(R.layout.header_cierre_parcial, parent, false))
            1 -> TipoVH(inflater.inflate(R.layout.tipo_doc_cierre_parcial, parent, false))
            2 -> CorrelativoVH(inflater.inflate(R.layout.correlativo_cierre_parcial, parent, false))
            3 -> FormaPagoVH(inflater.inflate(R.layout.forma_pago_cierre_parcial, parent, false))
            4 -> TerminoVH(inflater.inflate(R.layout.forma_pago_cierre_parcial, parent, false))
            5 -> ResumenVH(inflater.inflate(R.layout.forma_pago_cierre_parcial, parent, false))
            else -> throw IllegalArgumentException("Tipo no soportado")
        }

    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = lista[position]) {

            is CierreItem.Header -> (holder as HeaderVH).bind(item)
            is CierreItem.TipoDocumento -> (holder as TipoVH).bind(item)
            is CierreItem.Correlativo -> (holder as CorrelativoVH).bind(item)
            is CierreItem.FormaPago -> (holder as FormaPagoVH).bind(item)
            is CierreItem.Termino -> (holder as TerminoVH).bind(item)
            is CierreItem.Resumen -> (holder as ResumenVH).bind(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader = view.findViewById<TextView>(R.id.tvHeaderCierreParcial)
        fun bind(item: CierreItem.Header) {
            tvHeader.text = item.titulo
        }
    }

    class TipoVH(view: View) : RecyclerView.ViewHolder(view) {

        private val tvTipoDocumento = view.findViewById<TextView>(R.id.tvTipoDocumento)
        fun bind(item: CierreItem.TipoDocumento) {
            tvTipoDocumento.text = item.nombre
        }
    }

    class CorrelativoVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: CierreItem.Correlativo) {
            itemView.findViewById<TextView>(R.id.tvInicial).text = item.inicial
            itemView.findViewById<TextView>(R.id.tvFinal).text = item.final
        }
    }

    class FormaPagoVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: CierreItem.FormaPago) {
            itemView.findViewById<TextView>(R.id.tvNombre).text = item.nombre
            itemView.findViewById<TextView>(R.id.tvMonto).text = "$ %.2f".format(item.monto)
        }
    }

    class TerminoVH(view: View) : RecyclerView.ViewHolder(view) {

        private val tvNombre = view.findViewById<TextView>(R.id.tvNombre)
        private val tvMonto = view.findViewById<TextView>(R.id.tvMonto)

        fun bind(item: CierreItem.Termino) {
            tvNombre.text = "Total"
            tvMonto.text = "$ %.2f".format(item.monto)
        }
    }

    class ResumenVH(view: View) : RecyclerView.ViewHolder(view) {

        private val tvNombre = view.findViewById<TextView>(R.id.tvNombre)
        private val tvMonto = view.findViewById<TextView>(R.id.tvMonto)

        fun bind(item: CierreItem.Resumen) {
            tvNombre.text = item.titulo
            tvMonto.text = "$ %.2f".format(item.monto)
        }
    }

}