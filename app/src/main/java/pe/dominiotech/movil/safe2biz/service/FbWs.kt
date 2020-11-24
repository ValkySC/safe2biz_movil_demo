package pe.dominiotech.movil.safe2biz.service

import io.reactivex.Observable
import pe.dominiotech.movil.safe2biz.model.Data
import retrofit2.http.POST
import retrofit2.http.Query

interface FbWs {
    @POST("ws/null/pr_movil_FB_GERENCIA")
    fun getGerencia(@Query("fb_uea_pe_id") fb_uea_pe_id: Long): Observable<Data>

    @POST("ws/null/pr_movil_FB_AREA")
    fun getArea(@Query("fb_gerencia_id") fb_gerencia_id: Long): Observable<Data>

    @POST("ws/null/pr_ws_fb_uea")
    fun getUnidades(@Query("sc_user_id") sc_user_id: Long): Observable<Data>

}