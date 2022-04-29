package pe.dominiotech.movil.safe2biz

import android.app.Application
import android.content.res.Configuration
import android.preference.PreferenceManager
import androidx.appcompat.app.ActionBar

import com.androidnetworking.AndroidNetworking
import okhttp3.OkHttpClient
import pe.dominiotech.movil.safe2biz.ayc.dao.RegistroDao

import java.util.Locale

import pe.dominiotech.movil.safe2biz.base.dao.UsuarioDao
import pe.dominiotech.movil.safe2biz.model.Usuario
import pe.dominiotech.movil.safe2biz.ops.dao.ListaVerificacionDao
import pe.dominiotech.movil.safe2biz.service.ListaVerificacionService
import pe.dominiotech.movil.safe2biz.service.UsuarioService
import pe.dominiotech.movil.safe2biz.utils.AppConstants
import pe.dominiotech.movil.safe2biz.version.VersionService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    var usuarioEnSesion: Usuario? = null
    private var codigoDeIdioma: String? = null
    private var locale: Locale? = null
    private var actionBarPrincipal: ActionBar? = null
    lateinit var registroDao: RegistroDao
    private var versionService: VersionService? = null
    var usuarioService: UsuarioService? = null
        private set
    var listaVerificacionService: ListaVerificacionService? = null
        private set

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (locale != null) {
            newConfig.locale = locale
            Locale.setDefault(locale!!)
            baseContext.resources.updateConfiguration(newConfig, baseContext.resources.displayMetrics)
        }
    }

    override fun onCreate() {
        super.onCreate()
        AndroidNetworking.initialize(applicationContext)
        codigoDeIdioma = Locale.getDefault().language
        var defaultLanguageCode: String? = codigoDeIdioma
        if ("es".equals(codigoDeIdioma!!, ignoreCase = true)) {
            codigoDeIdioma = ""
        } else {
            codigoDeIdioma = "en"
            defaultLanguageCode = "en"
        }

        val usuarioDao = UsuarioDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)
        registroDao = RegistroDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)
        val listaVerificacionDao = ListaVerificacionDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)

        usuarioDao.updateVersion()
        versionService = VersionService()
        usuarioService = UsuarioService()
        listaVerificacionService = ListaVerificacionService()

        versionService!!.setContext(this)
        usuarioService!!.setContext(this)
        listaVerificacionService!!.setContext(this)

        usuarioService!!.setUsuarioDao(usuarioDao)
        listaVerificacionService!!.setListaVerificacionDao(listaVerificacionDao)


        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val config = baseContext.resources.configuration
        val lang = settings.getString(getString(R.string.pref_locale), defaultLanguageCode)
        if ("" != lang && config.locale.language != lang) {
            locale = Locale(lang!!)
            Locale.setDefault(locale!!)
            config.locale = locale
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        }

    }


    fun setActionBarPrincipal(actionBarPrincipal: ActionBar?) {
        this.actionBarPrincipal = actionBarPrincipal
    }

    fun getRetrofit(): Retrofit {
        var userLogin = usuarioEnSesion!!.user_login + "@" + usuarioEnSesion!!.empresa
        //if (usuarioEnSesion!!.pais!!.isNotEmpty()) {
        //    userLogin = "${usuarioEnSesion!!.pais!!}\\${userLogin}"
        //}

        val httpClient = OkHttpClient.Builder()
        httpClient
                .connectTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                    .header("userLogin", userLogin)
                    .header("userPassword", usuarioEnSesion!!.password)
                    .header("systemRoot", "safe2biz")
                    .method(original.method(), original.body())
                    .build()
            chain.proceed(request)
        }
        val client = httpClient.build()
        return Retrofit.Builder()
                .baseUrl(usuarioEnSesion!!.ipOrDominioServidor+"/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
    }
}