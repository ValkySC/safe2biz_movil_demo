package pe.dominiotech.movil.safe2biz.sac.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import kotlinx.android.synthetic.main.sac_activity.*
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.sac.adapter.AccionCorrectivaAdapter
import pe.dominiotech.movil.safe2biz.sac.dao.AccionCorrectivaDao
import pe.dominiotech.movil.safe2biz.sac.model.AccionCorrectiva
import pe.dominiotech.movil.safe2biz.service.AccWs
import pe.dominiotech.movil.safe2biz.utils.AppConstants
import pe.dominiotech.movil.safe2biz.utils.Util
import pe.dominiotech.movil.safe2biz.utils.createDialog
import pe.dominiotech.movil.safe2biz.utils.mapObject
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
import java.io.IOException
import java.util.*


class AccionCorrectivaActivity : AppCompatActivity() {

    private var app: MainApplication? = null
    private var cardViewAdapter: AccionCorrectivaAdapter? = null
    internal var onStartCount = 0
    private var accionCorrectivaList: List<AccionCorrectiva>? = ArrayList()
    private var accionCorrectivaDao = AccionCorrectivaDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)

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
        app = application as MainApplication
        onStartCount = 1
        if (savedInstanceState == null)
        // 1st time
        {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else
        // already created so reverse animation
        {
            onStartCount = 2
        }

        setContentView(R.layout.sac_activity)
        inicializarComponentes()
    }

    private fun inicializarComponentes() {

        (app_bar as Toolbar).setTitleTextColor(Color.WHITE)
        setSupportActionBar(app_bar as Toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Acciones Correctivas"
            actionBar.setHomeButtonEnabled(true)
        }
        sacRecyclerView.setHasFixedSize(true)
        sacRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        cardViewAdapter = AccionCorrectivaAdapter(accionCorrectivaList, View.OnClickListener { view -> onItemClickMenu(view) }, applicationContext)
        sacRecyclerView.adapter = cardViewAdapter

        cargarListaSac()
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        app!!.usuarioEnSesion
        menuInflater.inflate(R.menu.menu_sac_detalle, menu)

        return true
    }

    public override fun onRestart() {
        super.onRestart()
        cargarListaSac()
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
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
        val list = app.registroDao.getAll(AccionCorrectiva::class.java)
        val requests = ArrayList<Observable<JsonObject>>()
        list.forEach {
            it as AccionCorrectiva
            if (it.estado == "Enviar") {
                it.user_id = app.usuarioEnSesion!!.sc_user_id

                val destFile = File(it.evidencia_ruta!!)
                val outputFileUri = FileProvider.getUriForFile(app.applicationContext, app.applicationContext.packageName + ".fileprovider", destFile)
                try {
                    val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                    it.evidencia = it.evidencia_nombre + ";" + imageStream
                } catch (e: IOException) {
                    Log.e("onImageTaken: No", e.toString())
                }

                val fieldsMap = mapObject(it)
                it.estado = "Enviar"
                app.registroDao.createOrUpdateGeneric(it)
                val accApi = app.getRetrofit().create(AccWs::class.java)
                val response = accApi.actualizarAcc(fieldsMap)
                requests.add(response)
            }
        }

        if (requests.size > 0) {
            val dialog = createDialog(this, "Subiendo acciones...")
            dialog.show()
            val response = Observable
                    .zip(requests) {
                    }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        list.forEach {accion ->
                            accion as AccionCorrectiva
                            if (accion.estado == "Enviar") {
                                app.registroDao.deleteById(AccionCorrectiva::class.java, accion.sac_accion_correctiva_id)
                            }
                        }
                        cargarListaSac()

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
        val accionCorrectiva = (item.tag as AccionCorrectivaAdapter.ViewHolder).cardViewModelSac

        when (item.id) {
            R.id.card_view -> {
                val i = Intent(applicationContext, AccionCorrectivaDetalleActivity::class.java)
                i.putExtra("accionCorrectiva", accionCorrectiva)
                startActivity(i)
            }
        }
    }

    fun cargarListaSac() {
        val accionCorrectivaList = accionCorrectivaDao.sacBeanList
                .filter { it.uea_id == app!!.usuarioEnSesion!!.fb_uea_pe_id }
        cardViewAdapter!!.setList(accionCorrectivaList)

        if (accionCorrectivaList.isEmpty()) {
            Toast.makeText(app, "No hay registro para mostrar", Toast.LENGTH_SHORT).show()
        }
    }

}
