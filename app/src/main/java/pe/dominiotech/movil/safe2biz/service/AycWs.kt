package pe.dominiotech.movil.safe2biz.service

import com.google.gson.JsonObject
import io.reactivex.Observable
import pe.dominiotech.movil.safe2biz.model.Data
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface AycWs {
    @POST("ws/null/pr_movil_AYC_Desviacion")
    fun getDesviacion(@Query("ayc") ayc: Long): Observable<Data>

    @POST("ws/null/pr_movil_AYC_Nivel_Riesgo")
    fun getNivelRiesgo(@Query("variable") variable: Long): Observable<Data>

    @POST("ws/null/pr_ws_fb_empleados_ee_uea")
    fun getReportantes(@Query("sc_user_id") sc_user_id: Long): Observable<Data>

    @FormUrlEncoded
    @POST("ws/null/pr_movil_AYC_Inserta_AyC")
    fun insertaAyc(@FieldMap fields: Map<String, String>): Observable<JsonObject>
}

