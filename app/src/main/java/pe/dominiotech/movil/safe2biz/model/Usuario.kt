package pe.dominiotech.movil.safe2biz.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

import pe.dominiotech.movil.safe2biz.utils.AppConstants

@DatabaseTable(tableName = AppConstants.TABLA_USUARIO)
class Usuario : Serializable {

    @DatabaseField(columnName = "usuario_id", generatedId = true)
    var usuario_id: Int = 0                 // Id local
    @DatabaseField(columnName = "sc_user_id")
    var sc_user_id: Long = 0
    @DatabaseField(columnName = "empresa")
    var empresa: String = ""
    @DatabaseField(columnName = "pais")
    var pais: String? = null
    @DatabaseField(columnName = "user_login")
    var user_login: String = ""                 // Usuario login
    @DatabaseField(columnName = "password")
    var password: String = ""               // Contrasenia
    @DatabaseField(columnName = "usuario")
    var usuario: String? = null
    @DatabaseField(columnName = "dni")
    var dni: String? = null                        // Dni
    @DatabaseField(columnName = "fb_empleado_id")
    var fb_empleado_id: Long = 0               // Codigo empleado
    @DatabaseField(columnName = "nombre_empleado")
    var nombre_empleado: String? = null            // Nombre completo empleado
    @DatabaseField(columnName = "fb_uea_pe_id")
    var fb_uea_pe_id: Long = 0                // Codigo unidad
    @DatabaseField(columnName = "fb_uea_pe_abr")
    var fb_uea_pe_abr: String? = null              // Abreviatura nombre unidad
    @DatabaseField(columnName = "ip_o_dominio_servidor")
    var ipOrDominioServidor: String = ""        // Ip o dominio del servidor
    @DatabaseField(columnName = "user_login_servidor")
    var user_login_servidor: String? = null                 // Usuario login servidor
    @DatabaseField(columnName = "password_servidor")
    var password_servidor: String? = null                   // Contrasenia servidor
    @DatabaseField(columnName = "url_ext")
    var url_ext: String? = null                   // URL_Ext
    @DatabaseField(columnName = "url_app")
    var url_app: String? = null                   // URL_Ext
    @DatabaseField(columnName = "arroba")
    var arroba: String? = null                   // URL_Ext
    @DatabaseField(columnName = "enterprise")
    var enterprise: String? = null                   // Enterprise

    var idDispositivo: String? = null
    var urlProfilePicture: String? = null
}
