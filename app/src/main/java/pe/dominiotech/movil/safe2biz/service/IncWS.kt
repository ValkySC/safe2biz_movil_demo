package pe.dominiotech.movil.safe2biz.service

import com.google.gson.JsonObject
import io.reactivex.Observable
import org.json.JSONObject
import pe.dominiotech.movil.safe2biz.model.Data
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface IncWS {
    @POST("ws/null/pr_movil_INC_Tipo_Evento")
    fun getTipoEvento(@Query("variable") variable: Long): Observable<Data>

    @POST("ws/null/pr_movil_INC_Subtipo_Evento")
    fun getSubtipoEvento(@Query("inc_tipo_reporte") inc_tipo_reporte: Long): Observable<Data>

    @POST("ws/null/pr_movil_INC_Detalle_Perdida")
    fun getDetallePerdida(@Query("tipo_reporte") tipo_reporte: Long): Observable<Data>

    @POST("ws/null/pr_movil_INC_Potencial_Perdida")
    fun getPotencialPerdida(@Query("variable") variable: Long): Observable<Data>

    @FormUrlEncoded
    @POST("ws/null/pr_movil_INC_Inserta_Incidente")
    fun insertarIncidente(@FieldMap fields: Map<String, String>): Observable<JsonObject>
}