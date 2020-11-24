package pe.dominiotech.movil.safe2biz.inc.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable

@DatabaseTable(tableName = "inc_detalle_perdida")
class DetallePerdida : Serializable {

    @DatabaseField(columnName = "inc_segun_tipo_id", id = true)
    var inc_segun_tipo_id: Long? = null                   // Id Area
    @DatabaseField(columnName = "inc_tipo_reporte_id")
    var inc_tipo_reporte_id: Long? = null                   // Id Area
    @DatabaseField(columnName = "codigo")
    lateinit var codigo: String                      // Nombre Area
    @DatabaseField(columnName = "nombre")
    lateinit var nombre: String                      // Nombre Area

    override fun toString(): String = this.nombre
}
