package pe.dominiotech.movil.safe2biz.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import pe.dominiotech.movil.safe2biz.R
import kotlinx.android.synthetic.main.country_item.view.*

class CountrySpinnerAdapter(context: Context, idCelda: Int, val datos: ArrayList<String>, val flags: ArrayList<Int>) : ArrayAdapter<Any>(context, idCelda) {
    override fun getItem(p0: Int): Any? {
        return datos[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return datos.size
    }

    override fun getView(i: Int, v: View?, parent: ViewGroup): View {
        var rowView = v
        if (v == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.country_item, parent, false)
        }
        val flag = rowView!!.country_flag
        val name = rowView.country_name
        flag.setImageResource(flags[i])
        name.text = datos[i]
        return rowView
    }

    override fun getDropDownView(i: Int, convertView: View?, parent: ViewGroup): View {

        var rowView = convertView
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.country_item, parent, false)
        }
        val flag = rowView!!.country_flag
        val name = rowView.country_name
        flag.setImageResource(flags[i])
        name.text = datos[i]
        return rowView
    }
}
