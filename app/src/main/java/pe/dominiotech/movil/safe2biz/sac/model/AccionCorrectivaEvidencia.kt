package pe.dominiotech.movil.safe2biz.sac.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

@DatabaseTable(tableName = "SAC_ACCION_CORRECTIVA_EVIDENCIA")
class AccionCorrectivaEvidencia : Serializable {

    @DatabaseField(columnName = "nombre", id = true)
    var nombre: String? = null
    @DatabaseField(columnName = "ruta")
    var ruta: String = ""
    @DatabaseField(columnName = "sac_accion_correctiva_id")
    var sac_accion_correctiva_id: Int = 0

    override fun toString(): String = this.ruta
}
