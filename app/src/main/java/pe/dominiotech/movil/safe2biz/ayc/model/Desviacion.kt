package pe.dominiotech.movil.safe2biz.ayc.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable

@DatabaseTable(tableName = "g_tipo_causa")
class Desviacion : Serializable {

    @DatabaseField(columnName = "g_tipo_causa_id", id = true)
    var g_tipo_causa_id: Long? = null                   // Id Area
    @DatabaseField(columnName = "ayc")
    lateinit var ayc: String                      // Nombre Area
    @DatabaseField(columnName = "descripcion")
    lateinit var descripcion: String                      // Nombre Area

    override fun toString(): String = this.descripcion
}
