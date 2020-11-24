package pe.dominiotech.movil.safe2biz.inc.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable

@DatabaseTable(tableName = "inc_potencial_perdida")
class PotencialPerdida : Serializable {

    @DatabaseField(columnName = "inc_potencial_perdida_id", id = true)
    var inc_potencial_perdida_id: Long? = null                   // Id Area
    @DatabaseField(columnName = "codigo")
    lateinit var codigo: String                      // Nombre Area
    @DatabaseField(columnName = "nombre")
    lateinit var nombre: String                      // Nombre Area

    override fun toString(): String = this.nombre
}
