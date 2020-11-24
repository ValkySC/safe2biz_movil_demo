package pe.dominiotech.movil.safe2biz.ayc.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

import pe.dominiotech.movil.safe2biz.base.model.Area
import pe.dominiotech.movil.safe2biz.base.model.EmpresaEspecializada

@DatabaseTable(tableName = "ayc_registro")
class Registro : Serializable {

    @DatabaseField(columnName = "ayc_registro_id", generatedId = true)
    var ayc_registro_id: Int = 0

    @DatabaseField(columnName = "fb_uea_pe_id")
    var uea: String? = null

    @DatabaseField(columnName = "fb_empleado_id")
    var empleado: String? = null
    @DatabaseField(columnName = "fb_empleado_nombre")
    var empleado_nombre: String? = null

    @DatabaseField(columnName = "origen")
    var origen: String? = null

    @DatabaseField(columnName = "g_tipo_causa_id")
    var desviacion: Long? = null
    @DatabaseField(columnName = "g_tipo_causa_nombre")
    var desviacion_nombre: String? = null

    @DatabaseField(columnName = "fb_empresa_especializada_id")
    var empresa: Long? = null
    @DatabaseField(columnName = "fb_empresa_especializada_nombre")
    var empresa_nombre: String? = null
    @DatabaseField(columnName = "fb_gerencia")
    var gerencia: Long? = null
    @DatabaseField(columnName = "fb_gerencia_nombre")
    var gerencia_nombre: String? = null
    @DatabaseField(columnName = "fb_area_id")
    var area: Long? = null
    @DatabaseField(columnName = "fb_area_nombre")
    var area_nombre: String? = null


    @DatabaseField(columnName = "lugar")
    var lugar: String = ""

    @DatabaseField(columnName = "fecha")
    var fecha: String = ""
    @DatabaseField(columnName = "hora")
    var hora: String = ""


    @DatabaseField(columnName = "tipo_evento_id")
    var tipo_evento: Long? = null
    @DatabaseField(columnName = "tipo_evento_nombre")
    var tipo_evento_nombre: String? = null
    @DatabaseField(columnName = "nivel_riesgo_id")
    var nivel_riesgo: Long? = null
    @DatabaseField(columnName = "nivel_riesgo_nombre")
    var nivel_riesgo_nombre: String? = null

    @DatabaseField(columnName = "descripcion")
    var descripcion: String = ""


    @DatabaseField(columnName = "accion_ejec")
    var accion_ejec: String = ""

    @DatabaseField(columnName = "corrigio")
    var corrigio: String? = null

    @DatabaseField(columnName = "latitud")
    var latitud: String? = null

    @DatabaseField(columnName = "longitud")
    var longitud: String? = null

    @DatabaseField(columnName = "foto_pre_evento_nombre")
    var foto_pre_evento_nombre: String? = null
    @DatabaseField(columnName = "foto_pre_evento_ruta")
    var foto_pre_evento_ruta: String? = null
    @DatabaseField(columnName = "foto_evento_nombre")
    var foto_evento_nombre: String? = null
    @DatabaseField(columnName = "foto_evento_ruta")
    var foto_evento_ruta: String? = null
    var foto_evento: String = ""
    var foto_pre_evento: String = ""

    @DatabaseField(columnName = "estado")
    var estado: String = ""
    fun validate() : Boolean {
        return fecha != "" &&
            hora != "" &&
            desviacion != null &&
            empleado != null &&
            uea != null &&
            origen != null &&
            empresa != null &&
            gerencia != null &&
            area != null &&
            tipo_evento != null &&
            nivel_riesgo != null &&
            corrigio != null &&
            accion_ejec != "" &&
            lugar != "" &&
            descripcion != ""
    }
}
