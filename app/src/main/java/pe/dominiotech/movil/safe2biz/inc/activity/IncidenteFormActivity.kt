package pe.dominiotech.movil.safe2biz.inc.activity


import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.androidnetworking.AndroidNetworking
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.inc_incidente_detalle.*
import org.springframework.util.StringUtils
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.ayc.dao.RegistroDao
import pe.dominiotech.movil.safe2biz.base.adapter.AdaptadorSpinner
import pe.dominiotech.movil.safe2biz.base.model.Area
import pe.dominiotech.movil.safe2biz.base.model.Gerencia
import pe.dominiotech.movil.safe2biz.inc.model.*
import pe.dominiotech.movil.safe2biz.service.IncWS
import pe.dominiotech.movil.safe2biz.utils.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.javaClass

class IncidenteFormActivity : AppCompatActivity(), OnClickListener, OnItemSelectedListener, OnCheckedChangeListener {

    private var app: MainApplication? = null
    internal var onStartCount = 0
    private var tipoEventoObjectList: MutableList<Any> = ArrayList()

    private lateinit var incidente: Incidente
    private var calendar: Calendar = Calendar.getInstance()
    private val rutaImagenes = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/ImagenesInc/"
    private lateinit var mPhotoFile: File
    private var outputFileUri: Uri? = null

    override fun attachBaseContext(newBase: Context) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
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
        setContentView(R.layout.inc_incidente_detalle)

        app = application as MainApplication
        incidente = intent.extras!!.getSerializable("incidente") as Incidente
        inicializarComponentes()
    }


    private fun inicializarComponentes() {
        setSupportActionBar(app_bar as Toolbar)
        AndroidNetworking.initialize(app!!)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Incidente"
            actionBar.setHomeButtonEnabled(true)
        }

        val mdformat = SimpleDateFormat("dd/MM/yyyy-HH:mm", Locale.US)
        val strDate = mdformat.format(calendar.time)
        if (incidente.fecha_evento.isEmpty()) {
            edt_fecha!!.setText(strDate.split("-")[0])
        } else {
            edt_fecha!!.setText(incidente.fecha_evento)
        }
        if (incidente.hora.isEmpty()) {
            edt_hora!!.setText(strDate.split("-")[1])
        } else {
            edt_hora!!.setText(incidente.hora)
        }
        btnGuardar!!.transformationMethod = null
        btnGuardar!!.setOnClickListener { guardarIncidente() }
        if(cargarLista(SubTipoEvento::class.java).isEmpty()) {
            Toast.makeText(app!!.applicationContext, "No hay datos descargados, por favor descarga la información desde el menú", Toast.LENGTH_LONG).show()
        }

        spn_TipoEvento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                tipoChange()
            }
        })

        tipoEventoObjectList.addAll(cargarListaTipoEvento())
        val adaptadorTipoEvento = AdaptadorSpinner(applicationContext, R.layout.spinner_item, tipoEventoObjectList, "TipoEvento")
        spn_TipoEvento!!.setAdapter(adaptadorTipoEvento)

        val adaptadorSubTipoEvento = AdaptadorSpinner(applicationContext, R.layout.spinner_item, ArrayList(), "SubTipoEvento")
        spn_SubTipoEvento!!.setAdapter(adaptadorSubTipoEvento)

        val adaptadorDetallePerdida = AdaptadorSpinner(applicationContext, R.layout.spinner_item, ArrayList(), "DetallePerdida")
        spn_DetallePerdida!!.setAdapter(adaptadorDetallePerdida)

        val adaptadorPotencialPerdida = AdaptadorSpinner(applicationContext, R.layout.spinner_item, cargarLista(PotencialPerdida::class.java), "PotencialPerdida")
        spn_PotencialPerdida!!.setAdapter(adaptadorPotencialPerdida)

        val lista = cargarLista(Gerencia::class.java)
                .filter { (it as Gerencia).fb_uea_pe_id == app!!.usuarioEnSesion!!.fb_uea_pe_id }
        val adaptadorGerencia = AdaptadorSpinner(applicationContext, R.layout.spinner_item, lista, "Gerencia")
        aut_Gerencia!!.setAdapter(adaptadorGerencia)
        aut_Gerencia.setOnItemClickListener {adapterView, view, i, l ->
            val gerencia = (adapterView.getItemAtPosition(i) as Gerencia)
            if (gerencia.fb_gerencia_id != incidente.fb_gerencia) {
                incidente.fb_gerencia = gerencia.fb_gerencia_id
                incidente.fb_gerencia_nombre = gerencia.toString()
                val listaAreas = cargarLista(Area::class.java).filter { (it as Area).fb_gerencia_id == gerencia.fb_gerencia_id }
                val adaptadorArea = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, listaAreas, "Area")
                spn_Area!!.setAdapter(adaptadorArea)
                spn_Area.setText("")
                incidente.fb_area = null
                incidente.fb_area_nombre = null
            }
        }
        var adaptadorArea = AdaptadorSpinner(applicationContext, R.layout.spinner_item, ArrayList(), "Area")
        if (incidente.fb_gerencia != null) {
            val listaAreas = cargarLista(Area::class.java).filter { (it as Area).fb_gerencia_id == incidente.fb_gerencia }
            adaptadorArea = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, listaAreas, "Area")
        }
        spn_Area!!.setAdapter(adaptadorArea)
        spn_Area.setOnItemClickListener {adapterView, view, i, l ->
            val areaSelected = (adapterView.getItemAtPosition(i) as Area)
            incidente.fb_area = areaSelected.fb_area_id
            incidente.fb_area_nombre = areaSelected.toString()
        }
        closeFotoEvento.setOnClickListener{ eliminarFoto() }
        closeFotoPostEvento.setOnClickListener{ eliminarFotoPost() }
        btnCamara.setOnClickListener { camara(REQUEST_IMAGE_CAPTURE) }
        btnGaleria.setOnClickListener { galeria(SELECT_PICTURE) }
        btnCamaraPost.setOnClickListener { camara(REQUEST_IMAGE_CAPTURE_POST) }
        btnGaleriaPost.setOnClickListener { galeria(SELECT_PICTURE_POST) }
        cargarImagenes()

        btnFecha.setOnClickListener { cargarDatePicker() }
        btnHora.setOnClickListener{ cargarTimePicker() }


        cargarDatos()
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
    private fun eliminarFoto() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("ELIMINAR IMAGEN ")
        dialog.setMessage("¿Desea ELIMINAR la imagen?")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Si") { _, _ -> eliminarImagen() }
        dialog.setNegativeButton("No") { it, _ -> it.cancel() }
        dialog.show()
    }

    private fun eliminarFotoPost() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("ELIMINAR IMAGEN ")
        dialog.setMessage("¿Desea ELIMINAR la imagen?")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Si") { _, _ -> eliminarImagenPost() }
        dialog.setNegativeButton("No") { it, _ -> it.cancel() }
        dialog.show()
    }
    private fun eliminarImagen() {
        frameFotoEvento!!.visibility = View.GONE
        incidente.imagen_evento_ruta = null
        incidente.imagen_evento_nombre = null
        fotoEvento!!.setImageBitmap(null)
        val toast = Toast.makeText(app!!.applicationContext, "Imagen eliminada correctamente", Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun eliminarImagenPost() {
        frameFotoPostEvento!!.visibility = View.GONE
        incidente.imagen_pre_evento_ruta = null
        incidente.imagen_pre_evento_nombre = null
        fotoPostEvento!!.setImageBitmap(null)
        val toast = Toast.makeText(app!!.applicationContext, "Imagen eliminada correctamente", Toast.LENGTH_SHORT)
        toast.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == -1) {
            onImageTaken()
        } else if (requestCode == SELECT_PICTURE && resultCode == -1) {
            mPhotoFile = File(getImagenPath(data!!.data))
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            outputFileUri = data.data
            onImageTaken()

        } else if (requestCode == REQUEST_IMAGE_CAPTURE_POST && resultCode == -1) {
            onImageTakenPost()
        } else if (requestCode == SELECT_PICTURE_POST && resultCode == -1) {
            mPhotoFile = File(getImagenPath(data!!.data))
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            outputFileUri = data.data
            onImageTakenPost()
        } else if (resultCode == 0) {
            Toast.makeText(this.app!!.applicationContext, " Imagen no fue tomada... ", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this.app!!.applicationContext, " Imagen no fue tomada... ", Toast.LENGTH_SHORT).show()
        }

    }

    private fun onImageTaken() {
        frameFotoEvento!!.visibility = View.VISIBLE
        incidente.imagen_evento_ruta = mPhotoFile.path
        incidente.imagen_evento_nombre = mPhotoFile.name
        try {
            fotoEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
        } catch (e: IOException) {
            Log.e("onImageTaken: Si", e.toString())
        }

    }

    private fun onImageTakenPost() {

        frameFotoPostEvento!!.visibility = View.VISIBLE
        incidente.imagen_pre_evento_ruta = mPhotoFile.path
        incidente.imagen_pre_evento_nombre = mPhotoFile.name
        try {
            fotoPostEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
        } catch (e: IOException) {
            Log.e("onImageTaken: Si", e.toString())
        }

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
    private fun cargarImagenes() {
        if (StringUtils.hasText(incidente.imagen_evento_ruta)) {
            mPhotoFile = File(incidente.imagen_evento_ruta!!)
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            frameFotoEvento!!.visibility = View.VISIBLE
            try {
                fotoEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
            } catch (e: IOException) {
                Log.e("onImageTaken: Si", e.toString())
            }

        }
        if (StringUtils.hasText(incidente.imagen_pre_evento_ruta)) {
            mPhotoFile = File(incidente.imagen_pre_evento_ruta!!)
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            frameFotoPostEvento!!.visibility = View.VISIBLE
            try {
                fotoPostEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
            } catch (e: IOException) {
                Log.e("onImageTaken: Si", e.toString())
            }

        }
    }
    private fun guardarIncidente() {
        if(tipoEventoObjectList.isNotEmpty()){
            incidente.fecha_evento = edt_fecha.text.toString()
            incidente.hora = edt_hora.text.toString()
            incidente.lugar_evento = edt_lugar.text.toString()
            incidente.descripcion_evento = edt_descripcion.text.toString()
            val tipoEvento = cargarLista(TipoEvento::class.java).find { (it as TipoEvento).nombre == spn_TipoEvento.text.toString() }
            if (tipoEvento != null) {
                tipoEvento as TipoEvento
                incidente.inc_tipo_evento = tipoEvento.inc_tipo_reporte_id
                incidente.inc_tipo_evento_nombre = tipoEvento.nombre
            }
            val subtipoEvento = cargarLista(SubTipoEvento::class.java).find { (it as SubTipoEvento).nombre == spn_SubTipoEvento.text.toString() }
            if (subtipoEvento != null) {
                subtipoEvento as SubTipoEvento
                incidente.inc_sub_tipo_evento = subtipoEvento.inc_sub_tipo_reporte_id
                incidente.inc_sub_tipo_evento_nombre = subtipoEvento.nombre
            }
            val detallePerdida = cargarLista(DetallePerdida::class.java).find { (it as DetallePerdida).nombre == spn_DetallePerdida.text.toString() }
            if (detallePerdida != null) {
                detallePerdida as DetallePerdida
                incidente.inc_segun_tipo = detallePerdida.inc_segun_tipo_id
                incidente.inc_segun_tipo_nombre = detallePerdida.nombre
            }
            val potencialPerdida = cargarLista(PotencialPerdida::class.java).find { (it as PotencialPerdida).nombre == spn_PotencialPerdida.text.toString() }
            if (potencialPerdida != null) {
                potencialPerdida as PotencialPerdida
                incidente.inc_potencial_perdida = potencialPerdida.inc_potencial_perdida_id
                incidente.inc_potencial_perdida_nombre = potencialPerdida.nombre
            }
            val area = cargarLista(Area::class.java).find { (it as Area).nombre == spn_Area.text.toString() }
            if (area != null) {
                area as Area
                incidente.fb_area = area.fb_area_id
                incidente.fb_area_nombre = area.nombre
            }
            val gerencia = cargarLista(Gerencia::class.java).find { (it as Gerencia).nombre == aut_Gerencia.text.toString() }
            if (gerencia != null) {
                gerencia as Gerencia
                incidente.fb_gerencia = gerencia.fb_gerencia_id
                incidente.fb_gerencia_nombre = gerencia.nombre
            }
            app!!.registroDao.createOrUpdateGeneric(incidente)
            mostrarMensaje("Datos guardados correctamente")
            onBackPressed()
        } else {
            Toast.makeText(app!!.applicationContext, "No hay datos descargados, por favor descarga la información desde el menú", Toast.LENGTH_LONG).show()
        }
    }
    internal fun textChange() {
        var gerencia = ""
        val lista = cargarLista(Gerencia::class.java)
                .filter { (it as Gerencia).fb_uea_pe_id == app!!.usuarioEnSesion!!.fb_uea_pe_id }
        for (gerenciaIt in lista) {
            if ((gerenciaIt as Gerencia).nombre == aut_Gerencia!!.text.toString()) {
                gerencia = gerenciaIt.fb_gerencia_id.toString()
            }
        }
        if (gerencia != "") {
            val lista = cargarLista(Area::class.java).filter { (it as Area).fb_gerencia_id.toString() == gerencia }
            val adaptadorArea = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, lista, "Area")
            spn_Area!!.setAdapter(adaptadorArea)

        } else {
            val adaptadorArea = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, ArrayList(), "Area")
            spn_Area!!.setAdapter(adaptadorArea)
            spn_Area!!.setText("")
            incidente.fb_area_nombre = null
            incidente.fb_area = null
        }

        btnGuardar!!.isEnabled = edt_fecha!!.text.toString() != "" &&
                edt_hora!!.text.toString() != ""
    }

    private fun tipoChange () {
        var tipoEvento = ""
        for (tipoEventoIt in cargarLista(TipoEvento::class.java)) {
            if ((tipoEventoIt as TipoEvento).nombre == spn_TipoEvento!!.text.toString()) {
                tipoEvento = tipoEventoIt.inc_tipo_reporte_id.toString()
            }
        }
        if (tipoEvento != "") {
            if (spn_TipoEvento.text.toString() == "Salud" || spn_TipoEvento.text.toString() == "Seguridad") {
                spn_SubTipoEvento.visibility = VISIBLE
            } else {
                spn_SubTipoEvento.visibility = GONE
            }
            if (spn_TipoEvento.text.toString() != incidente.inc_tipo_evento_nombre) {
                spn_DetallePerdida!!.setText("")
                spn_SubTipoEvento!!.setText("")
                incidente.inc_sub_tipo_evento_nombre = null
                incidente.inc_sub_tipo_evento = 0
                incidente.inc_segun_tipo_nombre = null
                incidente.inc_segun_tipo = null
            }
            val lista = cargarLista(SubTipoEvento::class.java).filter { (it as SubTipoEvento).inc_tipo_reporte_id.toString() == tipoEvento }
            val adaptadorSubtipo = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, lista, "SubTipoEvento")
            spn_SubTipoEvento!!.setAdapter(adaptadorSubtipo)

            val lista2 = cargarLista(DetallePerdida::class.java).filter { (it as DetallePerdida).inc_tipo_reporte_id.toString() == tipoEvento }
            val adaptadorDetallePerdida = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, lista2, "DetallePerdida")
            spn_DetallePerdida!!.setAdapter(adaptadorDetallePerdida)
        }
    }

    private fun cargarListaTipoEvento(): List<Any> {
        try {
            return app!!.registroDao.getAll(TipoEvento().javaClass) as List<Any>
        } catch (e: SQLException) {
            Log.e("error", e.message)
        }
        return ArrayList()
    }
    private fun cargarLista(clazz: Class<*>): List<Any> {
        try {
            return app!!.registroDao.getAll(clazz) as List<Any>
        } catch (e: SQLException) {
            Log.e("error", e.message)
        }
        return ArrayList()
    }

    private fun cargarDatePicker() {
        val dia = edt_fecha.text.split("/")[0].toInt()
        val mes = edt_fecha.text.split("/")[1].toInt()
        val anho = edt_fecha.text.split("/")[2].toInt()
        val dialogFragment = DatePicker.nuevaInstancia(anho, mes, dia, edt_fecha, Calendar.getInstance().timeInMillis, null)
        dialogFragment.show(supportFragmentManager, "DatePicker")
    }

    private fun cargarTimePicker() {
        val h = edt_hora.text.split(":")[0].toInt()
        val min = edt_hora.text.split(":")[1].toInt()
        val tpd = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    var hourText = hourOfDay.toString() + ""
                    var minText = minute.toString() + ""
                    if (hourText.length == 1) {
                        hourText = "0$hourText"
                    }
                    if (minText.length == 1) {
                        minText = "0$minText"
                    }
                    edt_hora.setText("$hourText:$minText")
                }, h, min, false)
        tpd.setTitle("Seleccionar Hora Inicio")
        tpd.show()
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }



    fun mostrarMensaje(mensaje: String) {
        val toast = Toast.makeText(app!!.applicationContext, mensaje, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun cargarDatos() {
        if (incidente.inc_incidente_id > 0) {
            aut_Gerencia.setText(incidente.fb_gerencia_nombre)
            spn_TipoEvento.setText(incidente.inc_tipo_evento_nombre)
            spn_SubTipoEvento.setText(incidente.inc_sub_tipo_evento_nombre)
            spn_DetallePerdida.setText(incidente.inc_segun_tipo_nombre)
            spn_PotencialPerdida.setText(incidente.inc_potencial_perdida_nombre)
            spn_Area.setText(incidente.fb_area_nombre)
            edt_lugar.setText(incidente.lugar_evento)
            edt_descripcion.setText(incidente.descripcion_evento)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        app!!.usuarioEnSesion
        menuInflater.inflate(R.menu.menu_sac_detalle, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.btnSubir) {
            if (incidente.validate()) {
                incidente.estado = "Enviar"
                val dialog = createDialog(this, "Subiendo incidente...")
                dialog.show()
                app!!.registroDao.createOrUpdateGeneric(incidente)
                val incApi = app!!.getRetrofit().create(IncWS::class.java)
                incidente.fb_empleado_id = app!!.usuarioEnSesion!!.fb_empleado_id
                incidente.uea_id = app!!.usuarioEnSesion!!.fb_uea_pe_id
                if (incidente.imagen_evento_ruta != null) {
                    val destFile = File(incidente.imagen_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        incidente.imagen_evento = incidente.imagen_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                if (incidente.imagen_pre_evento_ruta != null) {
                    val destFile = File(incidente.imagen_pre_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        incidente.imagen_pre_evento = incidente.imagen_pre_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                val fieldsMap = mapObject(incidente)

                val response = incApi
                    .insertarIncidente(fieldsMap)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    // Will be triggered if all requests will end successfully (4xx and 5xx also are successful requests too)
                    .subscribe({
                        dialog.dismiss()
                        app!!.registroDao.deleteById(Incidente::class.java, incidente.inc_incidente_id)
                        Log.d("TAG2", it.toString())
                        Toast.makeText(app, "Registro subido exitosamente", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                        //Do something on successful completion of all requests
                    }) {
                        Log.e("TAG", it.toString())
                        dialog.dismiss()
                        Toast.makeText(applicationContext, "No se completó la sincronización", Toast.LENGTH_SHORT).show()

                        //Do something on error completion of requests
                    }
            } else {
                Toast.makeText(app, "Hay campos sin completar", Toast.LENGTH_SHORT).show()
            }
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
        internal const val REQUEST_IMAGE_CAPTURE = 1
        internal const val REQUEST_IMAGE_CAPTURE_POST = 10
        private const val SELECT_PICTURE = 300
        private const val SELECT_PICTURE_POST = 400
    }
}
