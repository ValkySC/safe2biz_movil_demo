package pe.dominiotech.movil.safe2biz.base.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.descargar_modulos_activity.*
import org.json.JSONException
import org.json.JSONObject
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.ayc.dao.RegistroDao
import pe.dominiotech.movil.safe2biz.ayc.model.*
import pe.dominiotech.movil.safe2biz.base.model.Area
import pe.dominiotech.movil.safe2biz.base.model.EmpresaEspecializada
import pe.dominiotech.movil.safe2biz.base.model.Gerencia
import pe.dominiotech.movil.safe2biz.base.model.Turno
import pe.dominiotech.movil.safe2biz.inc.model.DetallePerdida
import pe.dominiotech.movil.safe2biz.inc.model.PotencialPerdida
import pe.dominiotech.movil.safe2biz.inc.model.SubTipoEvento
import pe.dominiotech.movil.safe2biz.inc.model.TipoEvento
import pe.dominiotech.movil.safe2biz.model.Data
import pe.dominiotech.movil.safe2biz.model.ResultadoBean
import pe.dominiotech.movil.safe2biz.model.Usuario
import pe.dominiotech.movil.safe2biz.ops.model.*
import pe.dominiotech.movil.safe2biz.sac.model.AccionCorrectiva
import pe.dominiotech.movil.safe2biz.service.AccWs
import pe.dominiotech.movil.safe2biz.service.AycWs
import pe.dominiotech.movil.safe2biz.service.FbWs
import pe.dominiotech.movil.safe2biz.service.IncWS
import pe.dominiotech.movil.safe2biz.utils.AppConstants
import pe.dominiotech.movil.safe2biz.utils.LogApp
import java.sql.SQLException
import java.util.*

class DescargarActivity : AppCompatActivity(), OnClickListener {

    private var btnDescargar: Button? = null

    private var app: MainApplication? = null
    private var usuario: Usuario? = null
    lateinit var registroDao: RegistroDao
    private var totalCount = 0
    internal var count = 0
    lateinit var Dialog1: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        LogApp.log(AppConstants.ruta_log_lista_verificacion, AppConstants.ARCHIVO_LOG_LISTA_VERIFICACION, "[DescargarActivity] entro ")
        setupTheme()
        super.onCreate(savedInstanceState)
        registroDao = RegistroDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)
        Dialog1 = ProgressDialog(this)
        //        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        //            @Override
        //            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
        //                app.setUsuarioEnSesion(null);
        //                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        //                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //                startActivity(intent);
        //                System.exit(2);
        //            }
        //        });

        setContentView(R.layout.descargar_modulos_activity)
        mostrarBarraAcciones()
        inicializarComponentes()
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }

    private fun mostrarBarraAcciones() {

        app = application as MainApplication
        usuario = app!!.usuarioEnSesion
        setSupportActionBar(app_bar as Toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setTitle(R.string.lb_titulo_descarga)
        }

    }

    private fun inicializarComponentes() {

        chkOps!!.setOnClickListener(this)
        chkAyc!!.setOnClickListener(this)
        chkSac!!.setOnClickListener(this)
        chkInc!!.setOnClickListener(this)

        btnDescargar = findViewById(R.id.btnDescargar)
        btnDescargar!!.setOnClickListener(this)


        chkOps!!.isChecked = false
        chkOps!!.isEnabled = true
        chkAyc!!.isChecked = false
        chkAyc!!.isEnabled = true
        chkSac!!.isChecked = false
        chkSac!!.isEnabled = true

    }

    override fun onClick(v: View) {

        if (v === btnDescargar) {

            val URL_EXT = app!!.usuarioEnSesion!!.ipOrDominioServidor
            //            String URL_EXT = "http://192.168.1.54:7777/safe2biz";
            val headers = HashMap<String, String>()
            var userLogin = "${usuario!!.user_login}@${usuario!!.empresa}"
            //if (usuario!!.pais!!.isNotEmpty()) {
            //    userLogin = "${usuario!!.pais!!}\\${userLogin}"
            //}
            headers["userLogin"] = userLogin
            headers["userPassword"] = usuario!!.password
            headers["systemRoot"] = "safe2biz"
            val parameters = HashMap<String, String>()

            if (totalCount > 0) {
                Dialog1.setMessage("Descargando datos del servidor...")
                Dialog1.show()
            }

            if (chkAyc!!.isChecked) {
                val aycApi = app!!.getRetrofit().create(AycWs::class.java)
                val incApi = app!!.getRetrofit().create(IncWS::class.java)
                val fbApi = app!!.getRetrofit().create(FbWs::class.java)
                val reportantes = aycApi.getReportantes(usuario!!.sc_user_id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        app!!.registroDao.deleteAll(Reportante::class.java)
                        it.data.forEach { item ->
                            val gson = Gson()
                            val new = gson.fromJson(item, Reportante::class.java)
                            app!!.registroDao.createOrUpdateGeneric(new)
                        }
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
                val desviaciones = aycApi.getDesviacion(0)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.data.forEach { item ->
                            val gson = Gson()
                            val new = gson.fromJson(item, Desviacion::class.java)
                            app!!.registroDao.createOrUpdateGeneric(new)
                        }
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
                val niveles = aycApi.getNivelRiesgo(0)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.data.forEach { item ->
                            val gson = Gson()
                            val new = gson.fromJson(item, NivelRiesgo::class.java)
                            app!!.registroDao.createOrUpdateGeneric(new)
                        }
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
                val tipos = incApi.getTipoEvento(0)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.data.forEach { item ->
                            val gson = Gson()
                            val new = gson.fromJson(item, TipoEvento::class.java)
                            app!!.registroDao.createOrUpdateGeneric(new)
                        }
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
                val gerencias = fbApi.getGerencia(0)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.data.forEach { item ->
                            val gson = Gson()
                            val new = gson.fromJson(item, Gerencia::class.java)
                            app!!.registroDao.createOrUpdateGeneric(new)
                        }
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
                val areas = fbApi.getArea(0)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.data.forEach { item ->
                            val gson = Gson()
                            val new = gson.fromJson(item, Area::class.java)
                            app!!.registroDao.createOrUpdateGeneric(new)
                        }
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
                parameters["code"] = "origen"
                AndroidNetworking.post("$URL_EXT/ws/null/pr_ws_master_table")
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("master_table")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val origen = Origen()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        origen.code = `object`.getString("code")
                                        origen.name = `object`.getString("name")

                                        if (registroDao.getById(Origen::class.java, origen.code) != null) {
                                            println("Ya está")
                                        } else {
                                            registroDao.createOrUpdateGeneric(origen)
                                        }
                                    }
                                    addCount()


                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                parameters.clear()
                parameters["code"] = "tipo_riesgo_ayc"
                AndroidNetworking.post("$URL_EXT/ws/null/pr_ws_master_table")
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("master_table")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val tipoRiesgo = TipoRiesgo()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        tipoRiesgo.code = `object`.getString("code")
                                        tipoRiesgo.name = `object`.getString("name")
                                        if (registroDao.getById(TipoRiesgo::class.java, tipoRiesgo.code) != null) {
                                            println("Ya está")
                                        } else {
                                            registroDao.createOrUpdateGeneric(tipoRiesgo)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })

                parameters.clear()
                parameters["user_login"] = usuario!!.user_login
                AndroidNetworking.post("$URL_EXT/ws/null/pr_ws_fb_empresa_especializada")
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Empresa")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val empresaEspecializada = EmpresaEspecializada()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        empresaEspecializada.fb_empresa_especializada_id = `object`.getLong("fb_empresa_especializada_id")
                                        empresaEspecializada.razon_social = `object`.getString("razon_social")
                                        empresaEspecializada.ruc_empresa = `object`.getString("ruc_empresa")
                                        if (registroDao.getById(EmpresaEspecializada::class.java, empresaEspecializada.fb_empresa_especializada_id) != null) {
                                            Log.d("Empresa", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(empresaEspecializada)
                                        }
                                    }

                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
            }
            if (chkOps!!.isChecked) {
                parameters.clear()
                parameters["user_login"] = usuario!!.user_login
                AndroidNetworking.post(URL_EXT + resources.getString(R.string.SERVICIO_OPS_TIPO_RESULTADO))
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Ops")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val tipoResultadoBean = TipoResultadoBean()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        tipoResultadoBean.opsTipoResultadoId = `object`.getLong("ops_tipo_resultado_id")
                                        tipoResultadoBean.codigo = `object`.getString("codigo")
                                        tipoResultadoBean.nombre = `object`.getString("nombre")
                                        if (registroDao.getById(TipoResultadoBean::class.java, tipoResultadoBean.opsTipoResultadoId) != null) {
                                            Log.d("Ops", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(tipoResultadoBean)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                AndroidNetworking.post(URL_EXT + resources.getString(R.string.SERVICIO_OPS_LISTA_VERIFICACION))
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Ops")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val listaVerificacion = ListaVerificacion()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        listaVerificacion.ops_lista_verificacion_id = `object`.getLong("ops_lista_verificacion_id")
                                        listaVerificacion.codigo = `object`.getString("codigo")
                                        listaVerificacion.nombre = `object`.getString("nombre")
                                        listaVerificacion.opsTipoResultadoId = `object`.getLong("ops_tipo_resultado_id")
                                        Log.d("Ops", listaVerificacion.nombre)

                                        if (registroDao.getById(ListaVerificacion::class.java, listaVerificacion.ops_lista_verificacion_id) != null) {
                                            Log.d("Ops", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(listaVerificacion)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                AndroidNetworking.post(URL_EXT + resources.getString(R.string.SERVICIO_OPS_LISTA_VERIFICACION_SECCION))
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Ops")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val listaVerifSeccion = ListaVerifSeccion()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        listaVerifSeccion.ops_lista_verif_seccion_id = `object`.getLong("ops_lista_verif_seccion_id")
                                        listaVerifSeccion.ops_lista_verif_categoria_id = `object`.getLong("ops_lista_verif_categoria_id")
                                        listaVerifSeccion.ops_lista_verificacion_id = `object`.getLong("ops_lista_verificacion_id")
                                        listaVerifSeccion.nombre = `object`.getString("nombre")
                                        if (registroDao.getById(ListaVerifSeccion::class.java, listaVerifSeccion.ops_lista_verif_seccion_id) != null) {
                                            Log.d("Ops", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(listaVerifSeccion)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                AndroidNetworking.post(URL_EXT + resources.getString(R.string.SERVICIO_OPS_LISTA_VERIFICACION_CATEGORIA))
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Ops")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val listaVerifCategoria = ListaVerifCategoria()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        listaVerifCategoria.ops_lista_verif_categoria_id = `object`.getLong("ops_lista_verif_categoria_id")
                                        listaVerifCategoria.ops_lista_verificacion_id = `object`.getLong("ops_lista_verificacion_id")
                                        listaVerifCategoria.nombre = `object`.getString("nombre")
                                        if (registroDao.getById(ListaVerifCategoria::class.java, listaVerifCategoria.ops_lista_verif_categoria_id) != null) {
                                            Log.d("Ops", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(listaVerifCategoria)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                AndroidNetworking.post(URL_EXT + resources.getString(R.string.SERVICIO_OPS_LISTA_VERIFICACION_PREGUNTA))
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Ops")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val listaVerifPregunta = ListaVerifPregunta()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        listaVerifPregunta.ops_lista_verif_pregunta_id = `object`.getLong("ops_lista_verif_pregunta_id")
                                        listaVerifPregunta.ops_lista_verif_seccion_id = `object`.getLong("ops_lista_verif_seccion_id")
                                        listaVerifPregunta.ops_lista_verif_categoria_id = `object`.getLong("ops_lista_verif_categoria_id")
                                        listaVerifPregunta.ops_lista_verificacion_id = `object`.getLong("ops_lista_verificacion_id")
                                        listaVerifPregunta.nombre = `object`.getString("nombre")
                                        listaVerifPregunta.flag_pregunta = `object`.getInt("flag_pregunta")
                                        if (registroDao.getById(ListaVerifPregunta::class.java, listaVerifPregunta.ops_lista_verif_pregunta_id) != null) {
                                            Log.d("Ops", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(listaVerifPregunta)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                AndroidNetworking.post(URL_EXT + resources.getString(R.string.SERVICIO_OPS_LISTA_VERIFICACION_RESULTADO))
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Ops")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val listaVerifCategoria = ResultadoBean()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        listaVerifCategoria.ops_lista_verif_resultado_id = `object`.getLong("ops_lista_verif_resultado_id")
                                        listaVerifCategoria.ops_tipo_resultado_id = `object`.getLong("ops_tipo_resultado_id")
                                        listaVerifCategoria.nombre = `object`.getString("nombre")
                                        listaVerifCategoria.codigo = `object`.getString("codigo")
                                        if (registroDao.getById(ResultadoBean::class.java, listaVerifCategoria.ops_lista_verif_resultado_id) != null) {
                                            Log.d("Ops", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(listaVerifCategoria)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                parameters.clear()
                parameters["code"] = "turno"
                AndroidNetworking.post("$URL_EXT/ws/null/pr_ws_master_table")
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("master_table")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val turno = Turno()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        turno.code = `object`.getString("code")
                                        turno.name = `object`.getString("name")
                                        if (registroDao.getById(Turno::class.java, turno.code) != null) {
                                            println("Ya está")
                                        } else {
                                            registroDao.createOrUpdateGeneric(turno)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                parameters.clear()
                parameters["user_login"] = usuario!!.user_login
                AndroidNetworking.post("$URL_EXT/ws/null/pr_ws_fb_area")
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Area")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val area = Area()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        area.fb_area_id = `object`.getLong("fb_area_id")
                                        area.codigo = `object`.getString("codigo")
                                        area.nombre = `object`.getString("nombre")
                                        if (registroDao.getById(Area::class.java, area.fb_area_id) != null) {
                                            Log.d("Area", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(area)
                                        }
                                    }
                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
                parameters.clear()
                parameters["user_login"] = usuario!!.user_login
                AndroidNetworking.post("$URL_EXT/ws/null/pr_ws_fb_empresa_especializada")
                        .addHeaders(headers)
                        .addBodyParameter(parameters)
                        .setTag("Empresa")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                try {
                                    val empresaEspecializada = EmpresaEspecializada()
                                    val data = response.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val `object` = data.getJSONObject(i)
                                        empresaEspecializada.fb_empresa_especializada_id = `object`.getLong("fb_empresa_especializada_id")
                                        empresaEspecializada.razon_social = `object`.getString("razon_social")
                                        empresaEspecializada.ruc_empresa = `object`.getString("ruc_empresa")
                                        empresaEspecializada.g_rol_empresa_id = `object`.getLong("g_rol_empresa_id")
                                        if (registroDao.getById(EmpresaEspecializada::class.java, empresaEspecializada.fb_empresa_especializada_id) != null) {
                                            Log.d("Empresa", "Ya existe")
                                        } else {
                                            registroDao.createOrUpdateGeneric(empresaEspecializada)
                                        }
                                    }

                                    addCount()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: SQLException) {
                                    e.printStackTrace()
                                }

                            }

                            override fun onError(error: ANError) {
                                cancelRequest(error.toString())
                            }
                        })
            }

            if (chkSac!!.isChecked) {
                val accApi = app!!.getRetrofit().create(AccWs::class.java)
                val response = accApi.getPendientes(usuario!!.fb_uea_pe_id, usuario!!.fb_empleado_id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        app!!.registroDao.deleteAll(AccionCorrectiva::class.java)
                        it.data.forEach { item ->
                            val gson = Gson()
                            val new = gson.fromJson(item, AccionCorrectiva::class.java)
                            new.uea_id = app!!.usuarioEnSesion!!.fb_uea_pe_id
                            app!!.registroDao.createOrUpdateGeneric(new)
                        }
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
            }

            if (chkInc!!.isChecked) {
                val incApi = app!!.getRetrofit().create(IncWS::class.java)
                val fbApi = app!!.getRetrofit().create(FbWs::class.java)
                val requests = ArrayList<Observable<Data>>()
                requests.add(incApi.getTipoEvento(0))
                requests.add(incApi.getSubtipoEvento(0))
                requests.add(incApi.getDetallePerdida(0))
                requests.add(incApi.getPotencialPerdida(0))
                requests.add(fbApi.getArea(0))
                requests.add(fbApi.getGerencia(0))
                val result = Observable
                    .zip(requests) {
                        it.forEachIndexed { index, it2 ->
                            (it2 as Data).data.forEach { item ->
                                val gson = Gson()
                                when (index) {
                                    0 -> registroDao.createOrUpdateGeneric(gson.fromJson(item, TipoEvento::class.java))
                                    1 -> registroDao.createOrUpdateGeneric(gson.fromJson(item, SubTipoEvento::class.java))
                                    2 -> registroDao.createOrUpdateGeneric(gson.fromJson(item, DetallePerdida::class.java))
                                    3 -> registroDao.createOrUpdateGeneric(gson.fromJson(item, PotencialPerdida::class.java))
                                    4 -> registroDao.createOrUpdateGeneric(gson.fromJson(item, Area::class.java))
                                    5 -> registroDao.createOrUpdateGeneric(gson.fromJson(item, Gerencia::class.java))
                                }
                            }
                        }
                        Any()
                    }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        addCount()
                    }) {
                        cancelRequest(it.toString())
                    }
            }
        } else if (v === chkOps) {
            if (chkOps!!.isChecked) {
                totalCount -= 9
                chkOps!!.isChecked = false
            } else {
                totalCount += 9

                chkOps!!.isChecked = true
            }
        } else if (v === chkAyc) {
            if (chkAyc!!.isChecked) {
                totalCount -= 9
                chkAyc!!.isChecked = false
            } else {
                totalCount += 9
                chkAyc!!.isChecked = true
            }
        } else if (v === chkSac) {
            if (chkSac!!.isChecked) {
                totalCount -= 1
                chkSac!!.isChecked = false
            } else {
                totalCount += 1
                chkSac!!.isChecked = true
            }
        } else if (v === chkInc) {
            if (chkInc!!.isChecked) {
                totalCount -= 1
                chkInc!!.isChecked = false
            } else {
                totalCount += 1
                chkInc!!.isChecked = true
            }
        }

        btnDescargar!!.isEnabled = chkOps!!.isChecked || chkSac!!.isChecked || chkAyc!!.isChecked || chkInc!!.isChecked
    }

    private fun addCount() {
        count++
        if (count == totalCount) {
            Dialog1.dismiss()
            Dialog1.hide()
            Toast.makeText(app, "Se completó la sincronización", Toast.LENGTH_SHORT).show()
            count = 0
        }
    }

    private fun cancelRequest(error: String) {
        Log.e("Error", error)
        Dialog1.dismiss()
        Dialog1.hide()
        Toast.makeText(app, "No se completó la sincronización", Toast.LENGTH_SHORT).show()
        count = 0
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
