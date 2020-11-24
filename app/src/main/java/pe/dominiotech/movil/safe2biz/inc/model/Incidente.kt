package pe.dominiotech.movil.safe2biz.inc.model

import com.google.gson.annotations.SerializedName
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

import pe.dominiotech.movil.safe2biz.utils.AppConstants

@DatabaseTable(tableName = "inc_incidente")
class Incidente : Serializable {

    @DatabaseField(columnName = "inc_incidente_id", generatedId = true)
    var inc_incidente_id: Int = 0
    @DatabaseField(columnName = "inc_tipo_evento")
    var inc_tipo_evento: Long? = null
    @DatabaseField(columnName = "inc_tipo_evento_nombre")
    var inc_tipo_evento_nombre: String? = null
    @DatabaseField(columnName = "inc_sub_tipo_evento")
    var inc_sub_tipo_evento: Long? = null
    @DatabaseField(columnName = "inc_sub_tipo_evento_nombre")
    var inc_sub_tipo_evento_nombre: String? = null
    @DatabaseField(columnName = "inc_segun_tipo")
    var inc_segun_tipo: Long? = null
    @DatabaseField(columnName = "inc_segun_tipo_nombre")
    var inc_segun_tipo_nombre: String? = null
    @DatabaseField(columnName = "inc_potencial_perdida")
    var inc_potencial_perdida: Long? = null
    @DatabaseField(columnName = "inc_potencial_perdida_nombre")
    var inc_potencial_perdida_nombre: String? = null
    @DatabaseField(columnName = "fb_gerencia")
    var fb_gerencia: Long? = null
    @DatabaseField(columnName = "fb_gerencia_nombre")
    var fb_gerencia_nombre: String? = null
    @DatabaseField(columnName = "fb_area")
    var fb_area: Long? = null
    @DatabaseField(columnName = "fb_area_nombre")
    var fb_area_nombre: String? = null
    @DatabaseField(columnName = "fecha_evento")
    var fecha_evento: String = ""
    @DatabaseField(columnName = "hora")
    var hora: String = ""
    @DatabaseField(columnName = "lugar_evento")
    var lugar_evento: String = ""
    @DatabaseField(columnName = "descripcion_evento")
    var descripcion_evento: String = ""
    @DatabaseField(columnName = "imagen_pre_evento_nombre")
    var imagen_pre_evento_nombre: String? = null
    @DatabaseField(columnName = "imagen_pre_evento_ruta")
    var imagen_pre_evento_ruta: String? = null
    @DatabaseField(columnName = "imagen_evento_nombre")
    var imagen_evento_nombre: String? = null
    @DatabaseField(columnName = "imagen_evento_ruta")
    var imagen_evento_ruta: String? = null
    var fb_empleado_id: Long = 0
    var uea_id: Long = 0
    var imagen_evento: String = ""
    var imagen_pre_evento: String = ""
    @DatabaseField(columnName = "estado")
    var estado: String = ""

    fun validate() : Boolean {
        return fecha_evento != "" &&
            hora != "" &&
            descripcion_evento != "" &&
            inc_tipo_evento != null &&
//            inc_sub_tipo_evento != null &&
            inc_segun_tipo != null &&
            inc_potencial_perdida != null &&
            fb_area != null &&
            fb_gerencia != null &&
            lugar_evento != ""
    }
}
