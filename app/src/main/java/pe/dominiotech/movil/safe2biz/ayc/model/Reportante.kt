package pe.dominiotech.movil.safe2biz.ayc.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable

@DatabaseTable(tableName = "ayc_reportante")
class Reportante : Serializable {

    @DatabaseField(columnName = "fb_empleado_id", id = true)
    var fb_empleado_id: Long? = null
    @DatabaseField(columnName = "fb_uea_pe_id")
    var fb_uea_pe_id: Long? = null
    @DatabaseField(columnName = "nombreCompleto")
    lateinit var nombreCompleto: String
    @DatabaseField(columnName = "numero_documento")
    lateinit var numero_documento: String

    override fun toString(): String = this.nombreCompleto
}
