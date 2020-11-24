package pe.dominiotech.movil.safe2biz.sac.model

import com.google.gson.annotations.SerializedName
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

import pe.dominiotech.movil.safe2biz.utils.AppConstants

@DatabaseTable(tableName = AppConstants.TABLA_SAC)
class AccionCorrectiva : Serializable {
    @DatabaseField(columnName = "sac_accion_correctiva_id", id = true)
    @SerializedName("sac_accion_correctiva_id")
    var sac_accion_correctiva_id: Int = 0                   // Id
    @DatabaseField(columnName = "uea_id")
    var uea_id: Long? = null
    @DatabaseField(columnName = "codigo_accion_correctiva")
    var codigo_accion_correctiva: String? = null
    @DatabaseField(columnName = "accion_correctiva_detalle")
    var accion_correctiva_detalle: String? = null
    @DatabaseField(columnName = "fecha_acordada_ejecucion")
    var fecha_acordada_ejecucion: String? = null
    @DatabaseField(columnName = "nombre_responsable_correccion")
    var nombre_responsable_correccion: String? = null
    @DatabaseField(columnName = "origen")
    var origen: String? = null
    @DatabaseField(columnName = "fecha_ejecucion")
    var fecha_eje: String? = null
    @DatabaseField(columnName = "evidencia_nombre")
    var evidencia_nombre: String? = null
    @DatabaseField(columnName = "evidencia_ruta")
    var evidencia_ruta: String? = null
    @DatabaseField(columnName = "obs_resp_corr")
    var obs_resp_corr: String? = null
    var user_id: Long = 0
    var evidencia: String = ""
    @DatabaseField(columnName = "estado")
    var estado: String = ""
}
