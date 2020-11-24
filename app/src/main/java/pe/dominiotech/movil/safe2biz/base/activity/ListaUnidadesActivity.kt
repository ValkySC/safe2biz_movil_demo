package pe.dominiotech.movil.safe2biz.base.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.simple_list.*
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.base.adapter.ListaUnidadesAdapter
import pe.dominiotech.movil.safe2biz.model.UnidadBean
import pe.dominiotech.movil.safe2biz.service.FbWs
import pe.dominiotech.movil.safe2biz.service.ListaVerificacionService
import pe.dominiotech.movil.safe2biz.service.UsuarioService
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.*


class ListaUnidadesActivity : AppCompatActivity() {

    private var app: MainApplication? = null
    private var cardViewAdapter: ListaUnidadesAdapter? = null
    internal var onStartCount = 0
    private var listaVerificacionService: ListaVerificacionService? = null
    private var usuarioService: UsuarioService? = null

    private val isOnline: Boolean
        get() {
            val cm = getSystemService(
                    Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var result = false
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    result = true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    result = true
                }
            }
            return result
        }


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)


        onStartCount = 1
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else {
            onStartCount = 2
        }

        setContentView(R.layout.simple_list)
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        app = application as MainApplication
        listaVerificacionService = app!!.listaVerificacionService
        usuarioService = app!!.usuarioService
        setSupportActionBar(app_bar as Toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Sedes"
            actionBar.setHomeButtonEnabled(true)
        }
        rv_list.setHasFixedSize(true)
        rv_list.layoutManager = LinearLayoutManager(this)
        val menuList = ArrayList<UnidadBean>()
        cardViewAdapter = ListaUnidadesAdapter(menuList, View.OnClickListener { view -> onItemClickMenu(view) }, applicationContext)
        rv_list.adapter = cardViewAdapter

        if (isOnline) {
            descargarUnidades()
            //            descargarListaUnidad(app.getUsuarioEnSesion().getUser_login(), app.getUsuarioEnSesion().getPassword(), app.getUsuarioEnSesion().getUsuario(), app.getUsuarioEnSesion().getSc_user_id()+"", app.getUsuarioEnSesion().getIpOrDominioServidor());
        } else {
            cargarListaUnidad()
        }
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        app!!.usuarioEnSesion
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_search) {

        } else if (id == android.R.id.home) {
            onBackPressed()
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun onItemClickMenu(item: View) {
        val unidadBean = (item.tag as ListaUnidadesAdapter.ViewHolder).unidadBean
        when (item.id) {
            R.id.lnlyCarViewUnidad -> {
                val usuarioBean = app!!.usuarioEnSesion
                usuarioBean!!.fb_uea_pe_id = unidadBean.fb_uea_pe_id
                usuarioBean.fb_uea_pe_abr = unidadBean.codigo
                val isUpdate = usuarioService!!.update(usuarioBean)
                if (isUpdate == 1) {
                    app!!.usuarioEnSesion = usuarioBean
                    val i = Intent(this@ListaUnidadesActivity, MenuActivity::class.java)
                    startActivity(i)
                } else {
                    app!!.usuarioEnSesion = null
                    val i = Intent(this@ListaUnidadesActivity, MenuActivity::class.java)
                    startActivity(i)
                }
            }
        }
    }

    fun cargarListaUnidad() {
        val unidades = listaVerificacionService!!.unidadBeanList

        if (null != unidades) {
            cardViewAdapter!!.setList(unidades)
            cardViewAdapter!!.notifyDataSetChanged()
        } else {
            mostrarMensaje("No existen unidades para mostrar...!")
        }
    }

    fun mostrarMensaje(mensaje: String) {
        val toast = Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun descargarUnidades() {
        val dialog = ProgressDialog(this@ListaUnidadesActivity)
        dialog.setMessage("Cargando unidades...")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val usuario = app!!.usuarioEnSesion
        val fbApi = app!!.getRetrofit().create(FbWs::class.java)
        val response = fbApi.getUnidades(usuario!!.sc_user_id)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                listaVerificacionService!!.borrarUnidades()
                it.data.forEach { item ->
                    val gson = Gson()
                    val unidad = gson.fromJson(item, UnidadBean::class.java)
                    app!!.registroDao.createOrUpdateGeneric(unidad)
                }
                cargarListaUnidad()
                dialog.dismiss()
            }) {
                Toast.makeText(applicationContext, "No se completó la sincronización", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }
}
