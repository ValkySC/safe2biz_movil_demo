package pe.dominiotech.movil.safe2biz.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

import pe.dominiotech.movil.safe2biz.utils.AppConstants

@DatabaseTable(tableName = AppConstants.TABLA_UNIDAD)
class UnidadBean : Serializable {

    @DatabaseField(columnName = "fb_uea_pe_id", id = true)
    var fb_uea_pe_id: Long = 0                   // Id unidad
    @DatabaseField(columnName = "codigo")
    var codigo: String? = null                      // codigo (Nombre en Abreviatura)
    @DatabaseField(columnName = "nombre")
    var nombre: String? = null                      // Nombre Unidad
    @DatabaseField(columnName = "sc_user_id")
    private val sc_user_id: Long? = null                      // Id Usuario
}
