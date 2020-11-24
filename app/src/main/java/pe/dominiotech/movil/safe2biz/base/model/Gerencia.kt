package pe.dominiotech.movil.safe2biz.base.model

import com.google.gson.annotations.SerializedName
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.Serializable

import pe.dominiotech.movil.safe2biz.utils.AppConstants

@DatabaseTable(tableName = "FB_GERENCIA")
class Gerencia : Serializable {

    @DatabaseField(columnName = "fb_gerencia_id", id = true)
    var fb_gerencia_id: Long = 0                   // Id Area
    @DatabaseField(columnName = "fb_uea_pe_id")
    var fb_uea_pe_id: Long = 0
    @DatabaseField(columnName = "codigo")
    lateinit var codigo: String                      // codigo
    @DatabaseField(columnName = "nombre")
    lateinit var nombre: String

    override fun toString(): String = this.nombre
}
