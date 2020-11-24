package pe.dominiotech.movil.safe2biz.inc.model

import com.google.gson.annotations.SerializedName
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

import pe.dominiotech.movil.safe2biz.utils.AppConstants

@DatabaseTable(tableName = "inc_sub_tipo_reporte")
class SubTipoEvento : Serializable {

    @DatabaseField(columnName = "inc_sub_tipo_reporte_id", id = true)
    var inc_sub_tipo_reporte_id: Long? = null                   // Id Area
    @DatabaseField(columnName = "inc_tipo_reporte_id")
    var inc_tipo_reporte_id: Long? = null                   // Id Area
    @DatabaseField(columnName = "nombre")
    lateinit var nombre: String                      // Nombre Area

    override fun toString(): String = this.nombre
}
