package pe.dominiotech.movil.safe2biz.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.ayc.model.Desviacion
import pe.dominiotech.movil.safe2biz.ayc.model.NivelRiesgo
import pe.dominiotech.movil.safe2biz.ayc.model.Reportante
import pe.dominiotech.movil.safe2biz.ayc.model.TipoRiesgo
import pe.dominiotech.movil.safe2biz.base.model.Area
import pe.dominiotech.movil.safe2biz.base.model.EmpresaEspecializada
import pe.dominiotech.movil.safe2biz.base.model.Gerencia
import pe.dominiotech.movil.safe2biz.base.model.Turno
import pe.dominiotech.movil.safe2biz.inc.model.*
import java.util.*

//class AdaptadorSpinner(context: Context, private var idCelda: Int, datos: List<Any>, clase: String) : ArrayAdapter<Any>(context, idCelda, datos) {
class AdaptadorSpinner : ArrayAdapter<Any> {

    private var mContext: Context? = context
    private var idCelda: Int = 0
    private var datos: List<Any>? = null
    private var claseName: String? = null
    private var search: String = ""

    private var itemsAllEmpleado: ArrayList<Reportante>? = null
    private var suggestionsEmpleado: ArrayList<Reportante>? = null

    private var itemsAll: ArrayList<EmpresaEspecializada>? = null
    private var suggestions: ArrayList<EmpresaEspecializada>? = null

    private var itemsAllGerencia: ArrayList<Gerencia>? = null
    private var suggestionsGerencia: ArrayList<Gerencia>? = null


    private val nameFilter = object : Filter() {
        override fun convertResultToString(resultValue: Any): String {
            return (resultValue as EmpresaEspecializada).razon_social
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint != null) {
                suggestions!!.clear()
                for (customer in itemsAll!!) {
                    if (customer.razon_social.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions!!.add(customer)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions!!.size
                return filterResults
            } else {
                return FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            val filteredList = results!!.values!! as? ArrayList<EmpresaEspecializada>
            if (results!!.count > 0) {
                clear()
                for (c in filteredList!!) {
                    add(c)
                }
                notifyDataSetChanged()
            }
        }
    }

    private val gerenciaFilter = object : Filter() {
        override fun convertResultToString(resultValue: Any): String {
            return (resultValue as Gerencia).nombre
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint != null) {
                suggestionsGerencia!!.clear()
                for (customer in itemsAllGerencia!!) {
                    if (customer.nombre.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestionsGerencia!!.add(customer)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestionsGerencia
                filterResults.count = suggestionsGerencia!!.size
                return filterResults
            } else {
                return FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            val filteredList = results.values as? ArrayList<Gerencia>
            if (results!!.count > 0) {
                clear()
                for (c in filteredList!!) {
                    add(c)
                }
                notifyDataSetChanged()
            }
        }
    }

    constructor(context: Context, idCelda: Int, datos: List<Any>, clase: String) : super(context, idCelda, datos) {
        this.mContext = context
        this.idCelda = idCelda
        this.datos = datos
        this.claseName = clase
        if (clase == "Empresa") {
            this.itemsAll = ArrayList()
            for (i in datos.indices) {
                println(datos[i])
                itemsAll!!.add(datos[i] as EmpresaEspecializada)
            }
            this.suggestions = ArrayList()
        }
        if (clase == "Gerencia") {
            this.itemsAllGerencia = ArrayList()
            for (i in datos.indices) {
                println(datos[i])
                itemsAllGerencia!!.add(datos[i] as Gerencia)
            }
            this.suggestionsGerencia = ArrayList()
        }
    }
    constructor(context: Context, idCelda: Int, datos: List<Any>, clase: String, search: String) : super(context, idCelda, datos) {
        this.mContext = context
        this.idCelda = idCelda
        this.datos = datos
        this.claseName = clase
        this.search = search
        if (clase == "Empleado") {
            this.itemsAllEmpleado = ArrayList()
            for (i in datos.indices) {
                println(datos[i])
                itemsAllEmpleado!!.add(datos[i] as Reportante)
            }
            this.suggestionsEmpleado = ArrayList()
        }
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var v = convertView
        try {
            val texto: TextView
            if (v == null) {
                val vi = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(idCelda, parent, false)
                texto = v!!.findViewById(R.id.tvTexto)
                v.tag = texto
            } else {
                texto = v.tag as TextView
            }
            setText(position, texto)
            texto.setTextColor(mContext!!.resources.getColor(R.color.app_letra_tab))
        } catch (ignored: Exception) {
        }

        return v!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var v = convertView
        try {
            val texto: TextView
            if (v == null) {
                val vi = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(idCelda, parent, false)
                texto = v!!.findViewById(R.id.tvTexto)
                v.tag = texto
            } else {
                texto = v.tag as TextView
            }

            setText(position, texto)

            texto.setTextColor(mContext!!.resources.getColor(R.color.app_letra_tab))

        } catch (e: Exception) {

        }

        return v
    }

    fun setText(position: Int, texto: TextView) {
        val item1 = datos!![position]
        when (claseName) {
            "IncidenteFormActivity" -> texto.text = item1 as String
            "TurnoClass" -> texto.text = (item1 as Turno).name
            "Area" -> texto.text = (item1 as Area).nombre
            "Empresa" -> texto.text = (item1 as EmpresaEspecializada).razon_social
            "TipoRiesgo" -> texto.text = (item1 as TipoRiesgo).name
            "Gerencia" -> texto.text = (item1 as Gerencia).nombre
            "TipoEvento" -> texto.text = (item1 as TipoEvento).nombre
            "SubTipoEvento" -> texto.text = (item1 as SubTipoEvento).nombre
            "DetallePerdida" -> texto.text = (item1 as DetallePerdida).nombre
            "PotencialPerdida" -> texto.text = (item1 as PotencialPerdida).nombre
            "Desviacion" -> texto.text = (item1 as Desviacion).descripcion
            "NivelRiesgo" -> texto.text = (item1 as NivelRiesgo).nombre
            "Empleado" -> texto.text = (item1 as Reportante).nombreCompleto
        }
    }

    override fun getFilter(): Filter {

        if (claseName == "Empresa") {

            return nameFilter
        }
        if (claseName == "Gerencia") {
            return gerenciaFilter
        }
        return super.getFilter()
    }
}
