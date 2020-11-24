package pe.dominiotech.movil.safe2biz.service

import com.google.gson.JsonObject
import io.reactivex.Observable
import pe.dominiotech.movil.safe2biz.model.Data
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface AccWs {
    @POST("ws/null/pr_movil_ACC_Consulta_Pendientes")
    fun getPendientes(
        @Query("fb_uea_id") fb_uea_id: Long,
        @Query("fb_empleado") fb_empleado: Long
    ): Observable<Data>

    @FormUrlEncoded
    @POST("ws/null/pr_movil_ACC_Actualiza")
    fun actualizarAcc(@FieldMap fields: Map<String, String>): Observable<JsonObject>
}

