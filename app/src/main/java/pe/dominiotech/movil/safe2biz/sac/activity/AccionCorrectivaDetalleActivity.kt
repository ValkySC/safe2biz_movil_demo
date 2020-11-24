package pe.dominiotech.movil.safe2biz.sac.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast
import androidx.core.content.FileProvider

import com.androidnetworking.AndroidNetworking
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.sac_detalle_activity.*

import org.springframework.util.StringUtils

import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.sac.dao.AccionCorrectivaDao
import pe.dominiotech.movil.safe2biz.sac.model.AccionCorrectiva
import pe.dominiotech.movil.safe2biz.service.AccWs
import pe.dominiotech.movil.safe2biz.utils.AppConstants
import pe.dominiotech.movil.safe2biz.utils.Util
import pe.dominiotech.movil.safe2biz.utils.mapObject

class AccionCorrectivaDetalleActivity : AppCompatActivity(), OnClickListener, OnItemSelectedListener, OnCheckedChangeListener {

    private var app: MainApplication? = null
    internal var onStartCount = 0
    private val SELECT_PICTURE = 300
    internal val rutaImagenes = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/ImagenesSac/"
    private lateinit var mPhotoFile: File

    internal var outputFileUri: Uri? = null
    private lateinit var accionCorrectiva: AccionCorrectiva
    private var calendar: Calendar? = null
    private var usuario: String? = null
    private var password: String? = null
    override fun attachBaseContext(newBase: Context) {

        super.attachBaseContext(newBase)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

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

        setContentView(R.layout.sac_detalle_activity)

        //Crea el directorio para las imagenes
        val newdir = File(rutaImagenes)
        newdir.mkdirs()
        app = application as MainApplication
        usuario = app!!.usuarioEnSesion!!.user_login
        password = app!!.usuarioEnSesion!!.password
        accionCorrectiva = intent.extras!!.getSerializable("accionCorrectiva") as AccionCorrectiva
        inicializarComponentes()
    }


    private fun inicializarComponentes() {
        (app_bar as Toolbar).setTitleTextColor(Color.WHITE)
        setSupportActionBar(app_bar as Toolbar)
        AndroidNetworking.initialize(app!!)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            if (null != accionCorrectiva.codigo_accion_correctiva) {
                actionBar.title = "Acción Correctiva"
            } else {
                actionBar.title = "Codigo"
            }
            actionBar.setHomeButtonEnabled(true)
        }

        tvSubTituloSac.setTypeface(null, Typeface.BOLD)
        tvCodigoSac.setTypeface(null, Typeface.BOLD)
        tvDescripcionSac.setTypeface(null, Typeface.BOLD)
        tvOrigenSac.setTypeface(null, Typeface.BOLD)
        tvFechaSac.setTypeface(null, Typeface.BOLD)
        tvResponsableSac.setTypeface(null, Typeface.BOLD)
        tvCodigoCarViewSac.text = accionCorrectiva.codigo_accion_correctiva
        tvDescripcionCarViewSac.text = accionCorrectiva.accion_correctiva_detalle
        tvFechaCarViewSac.text = accionCorrectiva.fecha_acordada_ejecucion
        tvOrigenCarViewSac.text = accionCorrectiva.origen
        tvResponsableCarViewSac.text = accionCorrectiva.nombre_responsable_correccion
        if (null == accionCorrectiva.fecha_eje) {
            edFecha!!.setText(Util.obtenerFechayHoraActual().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        } else {
            edFecha!!.setText(accionCorrectiva.fecha_eje)
        }
        edt_obs.setText(accionCorrectiva.obs_resp_corr)
        closeFotoEvento.setOnClickListener { eliminarFoto() }
        btnGuardar!!.transformationMethod = null
        if (accionCorrectiva.evidencia_ruta != null) {
            btnGuardar.isEnabled = true
        }
        btnGuardar!!.setOnClickListener {
            val accionCorrectivaDao = AccionCorrectivaDao(applicationContext, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)
            accionCorrectiva.fecha_eje = edFecha!!.text.toString()
            accionCorrectiva.obs_resp_corr = edt_obs!!.text.toString()

            try {
                accionCorrectivaDao.createOrUpdate(accionCorrectiva)
                Toast.makeText(applicationContext, "Se guardó correctamente", Toast.LENGTH_SHORT).show()
                onBackPressed()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        btnCamara.setOnClickListener { camara(REQUEST_IMAGE_CAPTURE) }
        btnGaleria.setOnClickListener { galeria(SELECT_PICTURE) }
        cargarImagenes()
    }
    private fun cargarImagenes() {
        if (StringUtils.hasText(accionCorrectiva.evidencia_ruta)) {
            mPhotoFile = File(accionCorrectiva.evidencia_ruta!!)
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            frameFotoEvento!!.visibility = View.VISIBLE
            try {
                fotoEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
            } catch (e: IOException) {
                Log.e("onImageTaken: Si", e.toString())
            }

        }
    }
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir = this.getExternalFilesDir(rutaImagenes)
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }
    private fun camara(requestCode: Int) {
        try {
            mPhotoFile = createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
            // Error occurred while creating the File
        }

        outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        startActivityForResult(takePictureIntent, requestCode)
    }

    private fun galeria(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(Intent.createChooser(intent, "Selecciona app de imagen"), requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == -1) {
            onImageTaken()
        } else if (requestCode == SELECT_PICTURE && resultCode == -1) {
            mPhotoFile = File(getImagenPath(data!!.data))
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            outputFileUri = data.data
            onImageTaken()

        } else if (resultCode == 0) {
            Toast.makeText(this.app!!.applicationContext, " Imagen no fue tomada... ", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this.app!!.applicationContext, " Imagen no fue tomada... ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarFoto() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("ELIMINAR IMAGEN ")
        dialog.setMessage("¿Desea ELIMINAR la imagen?")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Si") { _, _ -> eliminarImagen() }
        dialog.setNegativeButton("No") { it, _ -> it.cancel() }
        dialog.show()
    }

    private fun eliminarImagen() {
        btnGuardar.isEnabled = false
        frameFotoEvento!!.visibility = View.GONE
        accionCorrectiva.evidencia_ruta = null
        accionCorrectiva.evidencia_nombre = null
        fotoEvento!!.setImageBitmap(null)
        val toast = Toast.makeText(app!!.applicationContext, "Imagen eliminada correctamente", Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun getImagenPath(contentUri: Uri?): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = this.contentResolver.query(contentUri!!, proj, null, null, null)
            assert(cursor != null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }
    private fun onImageTaken() {
        btnGuardar.isEnabled = true
        frameFotoEvento!!.visibility = View.VISIBLE
        accionCorrectiva.evidencia_ruta = mPhotoFile.path
        accionCorrectiva.evidencia_nombre = mPhotoFile.name
        try {
            fotoEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
        } catch (e: IOException) {
            Log.e("onImageTaken: Si", e.toString())
        }

    }
    fun showDatePickerDialog(v: View) {
        val anho = obtenerAnho()
        val mes = obtenerMes()
        val dia = obtenerDia()
        val dialogFragment = pe.dominiotech.movil.safe2biz.utils.DatePicker.nuevaInstancia(anho, mes, dia, edFecha, Calendar.getInstance().timeInMillis, null)
        dialogFragment.show(supportFragmentManager, "DatePicker")
    }


    private fun obtenerAnho(): Int {
        val anho: Int
        val fecha = edFecha!!.text.toString()
        if (fecha == "") {
            calendar = Calendar.getInstance()
            anho = calendar!!.get(Calendar.YEAR)
        } else {
            val anhoText = fecha.substring(6, 10)
            anho = Integer.parseInt(anhoText)
        }
        return anho
    }

    private fun obtenerMes(): Int {
        val mes: Int
        val fecha = edFecha!!.text.toString()
        if (fecha == "") {
            calendar = Calendar.getInstance()
            mes = calendar!!.get(Calendar.MONTH) + 1
        } else {
            val mesText = fecha.substring(3, 5)
            mes = Integer.parseInt(mesText)
        }
        return mes
    }

    private fun obtenerDia(): Int {
        val dia: Int
        val fecha = edFecha!!.text.toString()
        if (fecha == "") {
            calendar = Calendar.getInstance()
            dia = calendar!!.get(Calendar.DAY_OF_MONTH)
        } else {
            val diaText = fecha.substring(0, 2)
            dia = Integer.parseInt(diaText)
        }
        return dia
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
        if (id == R.id.btnSubir) {
            if (accionCorrectiva.evidencia_ruta != null) {

                val dialog = ProgressDialog(this@AccionCorrectivaDetalleActivity)
                dialog.setMessage("Enviando datos al servidor...")
                dialog.show()
                accionCorrectiva.user_id = app!!.usuarioEnSesion!!.sc_user_id!!


                val destFile = File(accionCorrectiva.evidencia_ruta!!)
                val outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", destFile)
                try {
                    val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                    accionCorrectiva.evidencia = accionCorrectiva.evidencia_nombre + ";" + imageStream
                } catch (e: IOException) {
                    Log.e("onImageTaken: No", e.toString())
                }

                val fieldsMap = mapObject(accionCorrectiva)
                accionCorrectiva.estado = "Enviar"
                app!!.registroDao.createOrUpdateGeneric(accionCorrectiva)
                val accApi = app!!.getRetrofit().create(AccWs::class.java)
                val response = accApi.actualizarAcc(fieldsMap)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        app!!.registroDao.deleteById(AccionCorrectiva::class.java, accionCorrectiva.sac_accion_correctiva_id)

                        Log.d("TAG2", it.toString())
                        dialog.dismiss()
                        Toast.makeText(app, "Registro subido exitosamente", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }) {
                        Log.e("TAG", it.toString())
                        dialog.dismiss()
                        Toast.makeText(applicationContext, "No se completó la sincronización", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(app, "No se pudo sincronizar, no hay imágenes guardadas para esta acción correctiva. ", Toast.LENGTH_SHORT).show()
            }

            return true
        } else if (id == android.R.id.home) {
            onBackPressed()
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {

    }

    companion object {
        internal val REQUEST_IMAGE_CAPTURE = 1
    }


}
