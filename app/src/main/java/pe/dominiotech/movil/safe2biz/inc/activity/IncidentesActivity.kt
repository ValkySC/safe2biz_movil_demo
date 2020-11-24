package pe.dominiotech.movil.safe2biz.inc.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import pe.dominiotech.movil.safe2biz.base.model.MenuPrincipalItem
import pe.dominiotech.movil.safe2biz.inc.adapter.IncidenteAdapter
import pe.dominiotech.movil.safe2biz.inc.model.Incidente
import pe.dominiotech.movil.safe2biz.service.IncWS
import pe.dominiotech.movil.safe2biz.utils.Util
import pe.dominiotech.movil.safe2biz.utils.createDialog
import pe.dominiotech.movil.safe2biz.utils.mapObject
import java.io.File
import java.io.IOException
import java.util.*


class IncidentesActivity : AppCompatActivity() {

    private var menuPrincipalItem: MenuPrincipalItem? = null
    private var adapter: IncidenteAdapter? = null
    internal var onStartCount = 0
    private var list: List<Incidente> = ArrayList()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    private fun shouldAskPermissions(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
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
        if (shouldAskPermissions()) {
            askPermissions()
        }
        /*        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {             @Override             public void uncaughtException(Thread paramThread, Throwable paramThrowable) {                 app.setUsuarioEnSesion(null);                 Intent intent = new Intent(getApplicationContext(), LoginActivity.class);                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                 startActivity(intent);                 System.exit(2);             }         });*/

        onStartCount = 1
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else {
            onStartCount = 2
        }
        menuPrincipalItem = intent.extras!!.getSerializable("menuPrincipalItem") as MenuPrincipalItem?

        setContentView(R.layout.simple_list_with_button)
        inicializarComponentes()
    }

    private fun inicializarComponentes() {


        setSupportActionBar(app_bar as Toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = menuPrincipalItem!!.titulo
            actionBar.setHomeButtonEnabled(true)
        }
        rv_list.setHasFixedSize(true)
        rv_list.layoutManager = LinearLayoutManager(applicationContext)

        adapter = IncidenteAdapter(list, View.OnClickListener { view -> onItemClickMenu(view) }, applicationContext)
        rv_list.adapter = adapter
        cargarIncidentes()
        btnAgregar.setOnClickListener { agregar() }
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }

    private fun agregar() {
        val incidente = Incidente()
        val i = Intent(applicationContext, IncidenteFormActivity::class.java)
        i.putExtra("incidente", incidente)
        startActivity(i)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
            return true
        } else if (id == R.id.btnSubir){
            Log.d("Menu", "Menu")
            uploadIncidentes()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onItemClickMenu(item: View) {
        val incidenteSeleccionado = (item.tag as IncidenteAdapter.ViewHolder).incidente
        when (item.id) {
            R.id.lnlInc -> {
                val i = Intent(applicationContext, IncidenteFormActivity::class.java)
                i.putExtra("incidente", incidenteSeleccionado)
                startActivity(i)
            }
        }
    }

    private fun uploadIncidentes () {
        val app = application as MainApplication
        val list = app.registroDao.getAll(Incidente::class.java)
        val requests = ArrayList<Observable<JsonObject>>()
        list.forEach {
            it as Incidente
            if (it.estado == "Enviar") {
                app.registroDao.createOrUpdateGeneric(it)
                val incApi = app.getRetrofit().create(IncWS::class.java)
                it.fb_empleado_id = app.usuarioEnSesion!!.fb_empleado_id
                it.uea_id = app.usuarioEnSesion!!.fb_uea_pe_id
                if (it.imagen_evento_ruta != null) {
                    val destFile = File(it.imagen_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app.applicationContext, app.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        it.imagen_evento = it.imagen_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                if (it.imagen_pre_evento_ruta != null) {
                    val destFile = File(it.imagen_pre_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app.applicationContext, app.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        it.imagen_pre_evento = it.imagen_pre_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                val fieldsMap = mapObject(it)

                val response = incApi.insertarIncidente(fieldsMap)
                requests.add(response)
            }
        }
        if (requests.size > 0) {
            val dialog = createDialog(this, "Subiendo incidentes...")
            dialog.show()
            val response = Observable
                .zip(requests) {
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    list.forEach {incidente ->
                        incidente as Incidente
                        if (incidente.estado == "Enviar") {
                            app.registroDao.deleteById(Incidente::class.java, incidente.inc_incidente_id)
                        }
                    }
                    cargarIncidentes()

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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val app = application as MainApplication
        app.usuarioEnSesion
        menuInflater.inflate(R.menu.menu_sac_detalle, menu)
        return true
    }
    private fun cargarIncidentes() {
        val app = application as MainApplication

        val usuarioBean = app.usuarioEnSesion
        var incidentes = app.registroDao.getAll(Incidente().javaClass) as ArrayList<Incidente>

        if (incidentes.isNotEmpty()) {
//            incidentes = incidentes.filter { it.fb_uea_pe_id == usuarioBean!!.fb_uea_pe_id }
        } else {
            mostrarMensaje("No existen registros para mostrar!")
        }
        adapter!!.setList(incidentes)
        adapter!!.notifyDataSetChanged()
    }

    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    public override fun onRestart() {
        super.onRestart()
        cargarIncidentes()
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
    }
}
