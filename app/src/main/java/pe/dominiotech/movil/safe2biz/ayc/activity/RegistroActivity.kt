package pe.dominiotech.movil.safe2biz.ayc.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.simple_list_with_button.*
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.ayc.adapter.RegistroAdapter
import pe.dominiotech.movil.safe2biz.ayc.dao.RegistroDao
import pe.dominiotech.movil.safe2biz.ayc.model.Registro
import pe.dominiotech.movil.safe2biz.base.model.MenuPrincipalItem
import pe.dominiotech.movil.safe2biz.service.AycWs
import pe.dominiotech.movil.safe2biz.utils.AppConstants
import pe.dominiotech.movil.safe2biz.utils.Util
import pe.dominiotech.movil.safe2biz.utils.createDialog
import pe.dominiotech.movil.safe2biz.utils.mapObject
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
import java.io.IOException
import java.util.*


class RegistroActivity : AppCompatActivity() {

    internal var statusBar: FrameLayout? = null
    private var app: MainApplication? = null
    private var menuPrincipalItem: MenuPrincipalItem? = null
    private var cardViewAdapter: RegistroAdapter? = null
    internal var onStartCount = 0
    internal var registroDao = RegistroDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    @TargetApi(23)
    private fun askPermissions() {
        val permissions = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION")
        val requestCode = 200
        requestPermissions(permissions, requestCode)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        askPermissions()

        onStartCount = 1
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else {
            onStartCount = 2
        }

        setContentView(R.layout.simple_list_with_button)
        menuPrincipalItem = intent.extras!!.getSerializable("menuPrincipalItem") as MenuPrincipalItem?
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        app = application as MainApplication
        setSupportActionBar(app_bar as Toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle("Actos y Condiciones")
            actionBar.setHomeButtonEnabled(true)
        }
        rv_list.setHasFixedSize(true)
        rv_list.layoutManager = LinearLayoutManager(applicationContext)
        val menuList = ArrayList<Registro>()
        cardViewAdapter = RegistroAdapter(menuList, View.OnClickListener { view -> onItemClickMenu(view) }, applicationContext)
        rv_list.adapter = cardViewAdapter
        btnAgregar.setOnClickListener {
            val RegistroModel = Registro()
            val i = Intent(applicationContext, RegistroDetalleActivity::class.java)
            i.putExtra("Registro", RegistroModel)
            startActivity(i)
        }

        cargarListaRegistrosAyC()
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        app!!.usuarioEnSesion
        menuInflater.inflate(R.menu.menu_sac_detalle, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
            return true
        } else if (id == R.id.btnSubir){
            Log.d("Menu", "Menu")
            uploadItems()
        }
        return super.onOptionsItemSelected(item)
    }

    fun uploadItems() {
        val app = application as MainApplication
        val list = app.registroDao.getAll(Registro::class.java)
        val requests = ArrayList<Observable<JsonObject>>()
        list.forEach {
            it as Registro
            if (it.estado == "Enviar") {
                if (it.foto_evento_ruta != null) {
                    val destFile = File(it.foto_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        it.foto_evento = it.foto_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                if (it.foto_pre_evento_ruta != null) {
                    val destFile = File(it.foto_pre_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        it.foto_pre_evento = it.foto_pre_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                val aycApi = app!!.getRetrofit().create(AycWs::class.java)
                val fieldsMap = mapObject(it)

                val response = aycApi.insertaAyc(fieldsMap)
                requests.add(response)
            }
        }

        if (requests.size > 0) {
            val dialog = createDialog(this, "Subiendo registros...")
            dialog.show()
            val response = Observable
                    .zip(requests) {
                    }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        list.forEach {accion ->
                            accion as Registro
                            if (accion.estado == "Enviar") {
                                app.registroDao.deleteById(Registro::class.java, accion.ayc_registro_id)
                            }
                        }
                        cargarListaRegistrosAyC()

                        dialog.dismiss()
                        Toast.makeText(app, "Registros subidos exitosamente", Toast.LENGTH_SHORT).show()
                        Log.d("TAG2", it.toString())
                    }) {
                        dialog.dismiss()
                        Log.e("TAG", it.toString())
                        Toast.makeText(applicationContext, "No se completó la sincronización", Toast.LENGTH_SHORT).show()
                    }
        } else {
            Toast.makeText(applicationContext, "No hay registros para sincronizar", Toast.LENGTH_SHORT).show()
        }
    }
    fun onItemClickMenu(item: View) {
        val RegistroModel = (item.tag as RegistroAdapter.ViewHolder).registroModel
        when (item.id) {
            R.id.lnlyAycRegistro -> {
                val i = Intent(applicationContext, RegistroDetalleActivity::class.java)
                i.putExtra("Registro", RegistroModel)
                i.putExtra("menuPrincipalItem", menuPrincipalItem)
                startActivity(i)
            }
        }
    }

    public override fun onRestart() {
        super.onRestart()
        cargarListaRegistrosAyC()
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
    }

    fun cargarListaRegistrosAyC() {
        val usuarioBean = app!!.usuarioEnSesion

        val registroList = registroDao.registroList
        val registroArrayList = ArrayList<Registro>()

        if (registroList != null) {
            for (registro1 in registroList) {
                if (registro1.uea == usuarioBean!!.fb_uea_pe_id!!.toString()) {
                    registroArrayList.add(registro1)
                }
            }
            cardViewAdapter!!.setList(registroArrayList)
        } else {
            cardViewAdapter!!.setList(registroArrayList)
            cardViewAdapter!!.notifyDataSetChanged()
            mostrarMensaje("No existen registros para mostrar!")
        }

    }

    fun mostrarMensaje(mensaje: String) {
        val toast = Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT)
        toast.show()
    }

}
