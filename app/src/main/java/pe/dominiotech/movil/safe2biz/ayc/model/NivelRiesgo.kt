package pe.dominiotech.movil.safe2biz.ayc.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable

@DatabaseTable(tableName = "g_nivel_riesgo")
class NivelRiesgo : Serializable {

    @DatabaseField(columnName = "g_nivel_riesgo_id", id = true)
    var g_nivel_riesgo_id: Long? = null
    @DatabaseField(columnName = "nombre")
    lateinit var nombre: String

    override fun toString(): String = this.nombre
}
