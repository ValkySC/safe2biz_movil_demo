package pe.dominiotech.movil.safe2biz.ayc.activity

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import butterknife.ButterKnife
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.ayc_registro_activity.*
import org.springframework.util.StringUtils
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.ayc.dao.RegistroDao
import pe.dominiotech.movil.safe2biz.ayc.model.Desviacion
import pe.dominiotech.movil.safe2biz.ayc.model.NivelRiesgo
import pe.dominiotech.movil.safe2biz.ayc.model.Registro
import pe.dominiotech.movil.safe2biz.ayc.model.Reportante
import pe.dominiotech.movil.safe2biz.base.adapter.AdaptadorSpinner
import pe.dominiotech.movil.safe2biz.base.model.Area
import pe.dominiotech.movil.safe2biz.base.model.EmpresaEspecializada
import pe.dominiotech.movil.safe2biz.base.model.Gerencia
import pe.dominiotech.movil.safe2biz.inc.model.TipoEvento
import pe.dominiotech.movil.safe2biz.ops.dao.ListaVerificacionDao
import pe.dominiotech.movil.safe2biz.service.AycWs
import pe.dominiotech.movil.safe2biz.utils.*
import pe.dominiotech.movil.safe2biz.utils.DatePicker
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RegistroDetalleActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    internal val rutaImagenes = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/ImagenesAyc/"
    private val SELECT_PICTURE = 300
    internal var onStartCount = 0
    internal var outputFileUri: Uri? = null
    private lateinit var mPhotoFile: File
    private var empleadoObjectList: MutableList<Any>? = null

    internal var listaEmpresaOcurrencia: List<Any> = ArrayList()
    /** Bindeo de todos los elementos del layout  */
    private var app: MainApplication? = null
    private var listaVerificacionDao: ListaVerificacionDao? = null
    private var registroDao: RegistroDao? = null
    private var map: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private lateinit var registroModel: Registro
    private val calendar: Calendar = Calendar.getInstance()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        app = application as MainApplication
        setContentView(R.layout.ayc_registro_activity)
        ButterKnife.bind(this)
        setSupportActionBar(app_bar as Toolbar)
        registroModel = intent.extras!!.getSerializable("Registro") as Registro
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            if (registroModel.origen != null) {
                actionBar.title = "Actos y Condiciones"
            } else {
                actionBar.title = "Nuevo Registro"
            }
            actionBar.setHomeButtonEnabled(true)
        }


        onStartCount = 1
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else {
            onStartCount = 2
        }

        listaVerificacionDao = ListaVerificacionDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)
        registroDao = RegistroDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION)

        inicializarComponentes(savedInstanceState)
        locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

        rGroup!!.setOnCheckedChangeListener { _, _ -> origenChange(false) }

        btnFecha.setOnClickListener { cargarDatePicker() }
        btnHora.setOnClickListener { cargarTimePicker() }

        val fechaYhora = Util.obtenerFechayHoraActual().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        edt_fecha!!.setText(fechaYhora[0])
        edt_hora!!.setText(fechaYhora[1])
        //Crea el directorio para las fotoes
        val newdir = File(rutaImagenes)
        newdir.mkdirs()
        closeFotoEvento.setOnClickListener{ eliminarFoto() }
        closeFotoPostEvento.setOnClickListener{ eliminarFotoPost() }
        btnCamara.setOnClickListener { camara(REQUEST_IMAGE_CAPTURE) }
        btnGaleria.setOnClickListener { galeria(SELECT_PICTURE) }
        btnCamaraPost.setOnClickListener { camara(REQUEST_IMAGE_CAPTURE_POST) }
        btnGaleriaPost.setOnClickListener { galeria(SELECT_PICTURE_POST) }

        btnGuardar.setOnClickListener { guardarRegistro() }
        mapType.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            } else {
                map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
        }
//        mapSatellite.setOnClickListener { map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE }
//        mapHibrido.setOnClickListener {  map!!.mapType = GoogleMap.MAP_TYPE_HYBRID }
//        mapNormal.setOnClickListener {  map!!.mapType = GoogleMap.MAP_TYPE_NORMAL }
//        mapTerreno.setOnClickListener {  map!!.mapType = GoogleMap.MAP_TYPE_TERRAIN }

        cargarImagenes()
    }

    private fun origenChange(first: Boolean) {
        if (!first) {
            registroModel.desviacion_nombre = ""
            registroModel.desviacion = null
            spn_Desviacion.setText("")
        }
        var origen = "C"
        if (rdBtnActo.isChecked) {
            origen = "A"
        }
        val list = cargarLista(Desviacion::class.java)
                .filter { (it as Desviacion).ayc == origen }
        val adaptadorDesviacion = AdaptadorSpinner(applicationContext, R.layout.spinner_item, list, "Desviacion")
        spn_Desviacion!!.setAdapter(adaptadorDesviacion)
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
        startActivityForResult(Intent.createChooser(intent, "Selecciona app de foto"), requestCode)
    }
    private fun eliminarFoto() {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("ELIMINAR IMAGEN ")
        dialog.setMessage("¿Desea ELIMINAR la foto?")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Si") { _, _ -> eliminarImagen() }
        dialog.setNegativeButton("No") { it, _ -> it.cancel() }
        dialog.show()
    }

    private fun eliminarFotoPost() {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("ELIMINAR IMAGEN ")
        dialog.setMessage("¿Desea ELIMINAR la foto?")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Si") { _, _ -> eliminarImagenPost() }
        dialog.setNegativeButton("No") { it, _ -> it.cancel() }
        dialog.show()
    }
    private fun eliminarImagen() {
        frameFotoEvento!!.visibility = View.GONE
        registroModel.foto_evento_ruta = null
        registroModel.foto_evento_nombre = null
        fotoEvento!!.setImageBitmap(null)
        val toast = Toast.makeText(app!!.applicationContext, "Imagen eliminada correctamente", Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun eliminarImagenPost() {
        frameFotoPostEvento!!.visibility = View.GONE
        registroModel.foto_pre_evento_ruta = null
        registroModel.foto_pre_evento_nombre = null
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
        registroModel.foto_evento_ruta = mPhotoFile.path
        registroModel.foto_evento_nombre = mPhotoFile.name
        try {
            fotoEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
        } catch (e: IOException) {
            Log.e("onImageTaken: Si", e.toString())
        }

    }

    private fun onImageTakenPost() {

        frameFotoPostEvento!!.visibility = View.VISIBLE
        registroModel.foto_pre_evento_ruta = mPhotoFile.path
        registroModel.foto_pre_evento_nombre = mPhotoFile.name
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
        if (StringUtils.hasText(registroModel.foto_evento_ruta)) {
            mPhotoFile = File(registroModel.foto_evento_ruta!!)
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            frameFotoEvento!!.visibility = View.VISIBLE
            try {
                fotoEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
            } catch (e: IOException) {
                Log.e("onImageTaken: Si", e.toString())
            }

        }
        if (StringUtils.hasText(registroModel.foto_pre_evento_ruta)) {
            mPhotoFile = File(registroModel.foto_pre_evento_ruta!!)
            outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", mPhotoFile)
            frameFotoPostEvento!!.visibility = View.VISIBLE
            try {
                fotoPostEvento!!.setImageBitmap(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri))
            } catch (e: IOException) {
                Log.e("onImageTaken: Si", e.toString())
            }

        }
    }
    private fun cargarLista(clazz: Class<*>): List<Any> {
        try {
            return app!!.registroDao.getAll(clazz) as List<Any>
        } catch (e: SQLException) {
            Log.e("error", e.message)
        }
        return ArrayList()
    }

    private fun cargarListaEmpleados(): List<Any> {
        try {

            return app!!.registroDao.getAll(Reportante().javaClass) as List<Any>
        } catch (e: SQLException) {
            Log.e("error", e.message)
        }

        return ArrayList()
    }

    fun cargarDatePicker() {
        val anho = obtenerAnho()
        val mes = obtenerMes()
        val dia = obtenerDia()
        val dialogFragment = DatePicker.nuevaInstancia(anho, mes, dia, edt_fecha, Calendar.getInstance().timeInMillis, null)
        dialogFragment.show(supportFragmentManager, "DatePicker")
    }

    fun cargarTimePicker() {
        val h = obtenerHora()
        val min = obtenerMinuto()
        val tpd = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    var hourText = hourOfDay.toString() + ""
                    var minText = minute.toString() + ""
                    if (hourText.length == 1) {
                        hourText = "0$hourText"
                    }
                    if (minText.length == 1) {
                        minText = "0$minText"
                    }
                    edt_hora!!.setText("$hourText:$minText")
                }, h, min, false)
        tpd.setTitle("Seleccionar Hora Inicio")
        tpd.show()
    }

    private fun inicializarComponentes(savedInstanceState: Bundle?) {
        aut_Quien!!.threshold = 100
        empleadoObjectList = ArrayList()
        empleadoObjectList!!.addAll(cargarListaEmpleados())
        if (empleadoObjectList!!.isEmpty()) {
            aut_Quien.visibility = View.GONE
            ic_search.visibility = View.GONE
        }
        val adaptadorEmpleado = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, ArrayList(), "Empleado", "")
        aut_Quien!!.setAdapter(adaptadorEmpleado)
        ic_search.setOnClickListener { search() }
        aut_Quien.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                textChange()
            }
        })

        aut_Quien!!.setText(registroModel.empleado_nombre)
        aut_empresa!!.setText(registroModel.empresa_nombre)
        edt_fecha!!.setText(registroModel.fecha)
        edt_hora!!.setText(registroModel.hora)
        edt_descripcion!!.setText(registroModel.descripcion)
        edt_lugar!!.setText(registroModel.lugar)
        spn_TipoEvento!!.setText(registroModel.tipo_evento_nombre)
        spn_NivelRiesgo!!.setText(registroModel.nivel_riesgo_nombre)
        edt_accion!!.setText(registroModel.accion_ejec)
        if (registroModel.corrigio != null) {
            if (registroModel.corrigio == "1") {
                rdBtnSi!!.isChecked = true
            } else {
                rdBtnNo!!.isChecked = true
            }
        }
        cargarCombos()
        if (registroModel.origen != null) {
            origenChange(true)
            if (registroModel.origen == "A") {
                rdBtnActo!!.isChecked = true
            } else {
                rdBtnCondicion!!.isChecked = true
            }
        }


        spn_Desviacion!!.setText(registroModel.desviacion_nombre)
        aut_Gerencia!!.setText(registroModel.gerencia_nombre)
        spn_Area!!.setText(registroModel.area_nombre)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mapDetalle1!!.onCreate(savedInstanceState)
        mapDetalle1!!.getMapAsync(this)
    }

    internal fun textChange() {
        aut_Quien!!.threshold = 100
    }

    private fun showMarkers() {
        val latitud = registroModel.latitud!!.toDouble()
        val longitud = registroModel.longitud!!.toDouble()
        map!!.uiSettings.isMyLocationButtonEnabled = false
        map!!.uiSettings.isZoomControlsEnabled = false
        map!!.uiSettings.isIndoorLevelPickerEnabled = false
        map!!.uiSettings.isMapToolbarEnabled = false


        val markeLugares = LatLng(latitud, longitud)
        val showMarker = map!!.addMarker(MarkerOptions()
                .title("Ubicación Actual")
                .position(markeLugares)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_google_marker)))
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(markeLugares, 17f))
        showMarker.showInfoWindow()
    }

    private fun activarGps() {
        if (!locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("ADVERTENCIA")
            dialog.setMessage("Ubicación (GPS) desactivada, para usar ésta aplicación necesita activar la (Ubicación de Google)... ¿Desea Activarla Ahora?")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Si") { dialog, which -> startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            dialog.setNegativeButton("No") { dialog, which -> dialog.cancel() }
            dialog.show()
        }
    }

    private fun notificar(): Map<String, Any> {
        val hasMapUbicacion = HashMap<String, Any>()
        val mGPS = GPSTracker(this)
        if (mGPS.canGetLocation()) {
            val mLat = mGPS.latitude
            val mLong = mGPS.longitude
            hasMapUbicacion["latitud"] = mLat
            hasMapUbicacion["longitud"] = mLong
        } else {
            hasMapUbicacion["error"] = 0
        }

        return hasMapUbicacion
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        app!!.usuarioEnSesion
        menuInflater.inflate(R.menu.menu_sac_detalle, menu)
        return true
    }

    public override fun onResume() {
        mapDetalle1!!.onResume()
        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapDetalle1!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapDetalle1!!.onLowMemory()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.btnSubir) {
            val URL_EXT = app!!.usuarioEnSesion!!.ipOrDominioServidor

            if (registroModel.validate()) {
                registroModel.estado = "Enviar"
                app!!.registroDao.createOrUpdateGeneric(registroModel)
                val builder = android.app.AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setView(R.layout.layout_loading_dialog)
                builder.setMessage("Subiendo Registro...")
                val dialog = builder.create()
                dialog.show()
                if (registroModel.foto_evento_ruta != null) {
                    val destFile = File(registroModel.foto_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        registroModel.foto_evento = registroModel.foto_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                if (registroModel.foto_pre_evento_ruta != null) {
                    val destFile = File(registroModel.foto_pre_evento_ruta!!)
                    val outputFileUri = FileProvider.getUriForFile(app!!.applicationContext, app!!.applicationContext.packageName + ".fileprovider", destFile)
                    try {
                        val imageStream = Util.encodeToBase64(Util.handleSamplingAndRotationBitmap(app!!.applicationContext, outputFileUri), Bitmap.CompressFormat.JPEG, 100)
                        registroModel.foto_pre_evento = registroModel.foto_pre_evento_nombre + ";" + imageStream
                    } catch (e: IOException) {
                        Log.e("onImageTaken: Si", e.toString())
                    }

                }
                val aycApi = app!!.getRetrofit().create(AycWs::class.java)
                val fieldsMap = mapObject(registroModel)

                val response = aycApi.insertaAyc(fieldsMap)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        dialog.dismiss()
                        app!!.registroDao.deleteById(Registro::class.java, registroModel.ayc_registro_id)

                        Log.d("TAG2", it.toString())
                        Toast.makeText(app, "Registro subido exitosamente", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }) {
                        dialog.dismiss()
                        Log.e("TAG", it.toString())
                        Toast.makeText(applicationContext, "No se completó la sincronización", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(app, "No se pudo sincronizar, hay campos incompletos.", Toast.LENGTH_SHORT).show()
            }


            return true
        } else if (id == android.R.id.home) {
            onBackPressed()
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun guardarRegistro() {
        if (listaEmpresaOcurrencia.isNotEmpty()) {
            if (rdBtnActo!!.isChecked || rdBtnCondicion!!.isChecked) {
                if (rdBtnActo.isChecked) {
                    registroModel.origen = "A"
                } else {
                    registroModel.origen = "C"
                }
            }
            if (rdBtnSi!!.isChecked || rdBtnNo!!.isChecked) {
                if (rdBtnSi.isChecked) {
                    registroModel.corrigio = "1"
                } else {
                    registroModel.corrigio = "0"
                }
            }
            registroModel.fecha = edt_fecha.text.toString()
            registroModel.hora = edt_hora.text.toString()
            registroModel.lugar = edt_lugar.text.toString()
            registroModel.descripcion = edt_descripcion.text.toString()
            registroModel.accion_ejec = edt_accion.text.toString()
            registroModel.uea = app!!.usuarioEnSesion!!.fb_uea_pe_id.toString()

//            registroModel.empleado = app!!.usuarioEnSesion!!.fb_empleado_id.toString()
            val desviacion = cargarLista(Desviacion::class.java).find { (it as Desviacion).descripcion == spn_Desviacion.text.toString() }
            if (desviacion != null) {
                desviacion as Desviacion
                registroModel.desviacion = desviacion.g_tipo_causa_id
                registroModel.desviacion_nombre = desviacion.descripcion
            }
//            val gerencia = cargarLista(Gerencia::class.java).find { (it as Gerencia).nombre == aut_Gerencia.text.toString() }
//            if (gerencia != null) {
//                gerencia as Gerencia
//                registroModel.gerencia = gerencia.fb_gerencia_id
//                registroModel.gerencia_nombre = gerencia.nombre
//            }
//            val area = cargarLista(Area::class.java).find { (it as Area).nombre == spn_Area.text.toString() }
//            if (area != null) {
//                area as Area
//                registroModel.area = area.fb_area_id
//                registroModel.area_nombre = area.nombre
//            }
            if (empleadoObjectList!!.isEmpty()) {
                registroModel.empleado = app!!.usuarioEnSesion!!.fb_empleado_id.toString()
                registroModel.empleado_nombre = null
            } else {
                val reportante = cargarLista(Reportante::class.java).find { (it as Reportante).nombreCompleto == aut_Quien.text.toString() }
                if (reportante != null) {
                    reportante as Reportante
                    registroModel.empleado = reportante.fb_empleado_id.toString()
                    registroModel.empleado_nombre = reportante.nombreCompleto
                }
            }

            val empresa = cargarLista(EmpresaEspecializada::class.java).find { (it as EmpresaEspecializada).razon_social == aut_empresa.text.toString() }
            if (empresa != null) {
                empresa as EmpresaEspecializada
                registroModel.empresa = empresa.fb_empresa_especializada_id
                registroModel.empresa_nombre = empresa.razon_social
            }
            val tipo_evento = cargarLista(TipoEvento::class.java).find { (it as TipoEvento).nombre == spn_TipoEvento.text.toString() }
            if (tipo_evento != null) {
                tipo_evento as TipoEvento
                registroModel.tipo_evento = tipo_evento.inc_tipo_reporte_id
                registroModel.tipo_evento_nombre = tipo_evento.nombre
            }
            val nivel_riesgo = cargarLista(NivelRiesgo::class.java).find { (it as NivelRiesgo).nombre == spn_NivelRiesgo.text.toString() }
            if (nivel_riesgo != null) {
                nivel_riesgo as NivelRiesgo
                registroModel.nivel_riesgo = nivel_riesgo.g_nivel_riesgo_id
                registroModel.nivel_riesgo_nombre = nivel_riesgo.nombre
            }
            app!!.registroDao.createOrUpdateGeneric(registroModel)

            Toast.makeText(applicationContext, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show()
            onBackPressed()
        } else {
            Toast.makeText(app!!.applicationContext, "No hay datos descargados, por favor descarga la información desde el menú", Toast.LENGTH_LONG).show()
        }
    }

    private fun cargarCombos() {

        val lista = cargarLista(Gerencia::class.java)
                .filter { (it as Gerencia).fb_uea_pe_id == app!!.usuarioEnSesion!!.fb_uea_pe_id }
        val adaptadorGerencia = AdaptadorSpinner(applicationContext, R.layout.spinner_item, lista, "Gerencia")
        aut_Gerencia!!.setAdapter(adaptadorGerencia)
        aut_Gerencia.setOnItemClickListener {adapterView, view, i, l ->
            val gerencia = (adapterView.getItemAtPosition(i) as Gerencia)
            if (gerencia.fb_gerencia_id != registroModel.gerencia) {
                registroModel.gerencia = gerencia.fb_gerencia_id
                registroModel.gerencia_nombre = gerencia.toString()
                val listaAreas = cargarLista(Area::class.java).filter { (it as Area).fb_gerencia_id == gerencia.fb_gerencia_id }
                val adaptadorArea = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, listaAreas, "Area")
                spn_Area!!.setAdapter(adaptadorArea)
                spn_Area.setText("")
                registroModel.area = null
                registroModel.area_nombre = null
            }
        }
        val adaptadorDesviacion = AdaptadorSpinner(applicationContext, R.layout.spinner_item, ArrayList(), "Desviacion")
        spn_Desviacion!!.setAdapter(adaptadorDesviacion)

        var adaptadorArea = AdaptadorSpinner(applicationContext, R.layout.spinner_item, ArrayList(), "Area")
        if (registroModel.gerencia != null) {
            val listaAreas = cargarLista(Area::class.java).filter { (it as Area).fb_gerencia_id == registroModel.gerencia }
            adaptadorArea = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, listaAreas, "Area")
        }
        spn_Area!!.setAdapter(adaptadorArea)
        spn_Area.setOnItemClickListener {adapterView, view, i, l ->
            val areaSelected = (adapterView.getItemAtPosition(i) as Area)
            registroModel.area = areaSelected.fb_area_id
            registroModel.area_nombre = areaSelected.toString()
        }
        val adaptadorTipoEvento = AdaptadorSpinner(applicationContext, R.layout.spinner_item, cargarLista(TipoEvento::class.java), "TipoEvento")
        spn_TipoEvento!!.setAdapter(adaptadorTipoEvento)

        val adaptadorNivelRiesgo = AdaptadorSpinner(applicationContext, R.layout.spinner_item, cargarLista(NivelRiesgo::class.java), "NivelRiesgo")
        spn_NivelRiesgo!!.setAdapter(adaptadorNivelRiesgo)

        if (listaVerificacionDao!!.empresaEspecializadaList != null) {
            listaEmpresaOcurrencia = ArrayList<Any>(listaVerificacionDao!!.empresaEspecializadaList!!)
        }
        val adaptadorEmpresaOcurrencia = AdaptadorSpinner(applicationContext, R.layout.spinner_item, listaEmpresaOcurrencia, "Empresa")
        aut_empresa!!.setAdapter(adaptadorEmpresaOcurrencia)

    }
    private fun search() {
        if (aut_Quien!!.text.length > 3) {
            val newList = ArrayList<Any>()
            for (customer in empleadoObjectList!!) {
                if ((customer as Reportante).nombreCompleto.toLowerCase().contains(aut_Quien!!.text.toString().toLowerCase())) {
                    newList.add(customer)
                }
            }
            newList.sortBy { (it as Reportante).nombreCompleto }

            if (newList.size > 0) {
                val adaptadorEmpleado = AdaptadorSpinner(app!!.applicationContext, R.layout.spinner_item, newList, "Empleado")
                aut_Quien!!.setAdapter(adaptadorEmpleado)
                aut_Quien!!.showDropDown()
            } else {
                mostrarMensaje("No hay coincidencias encontradas")
            }
        }
    }
    fun mostrarMensaje(mensaje: String) {
        val toast = Toast.makeText(app!!.applicationContext, mensaje, Toast.LENGTH_SHORT)
        toast.show()
    }
    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

    }

    override fun onProviderEnabled(s: String) {

    }

    override fun onProviderDisabled(s: String) {

    }

    override fun onMapLoaded() {

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isMyLocationButtonEnabled = false
        map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
//        map!!.uiSettings.setAllGesturesEnabled(false)
        activarGps()
        if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            val hasMapUbi = notificar()
            if (2 == hasMapUbi.size) {
                if(registroModel.latitud == null) {
                    registroModel.latitud = hasMapUbi["latitud"].toString()
                    registroModel.longitud = hasMapUbi["longitud"].toString()
                }
                showMarkers()
            }
        }
    }

    private fun obtenerAnho(): Int {
        val anho: Int
        val fecha = edt_fecha!!.text.toString()
        if (fecha == "") {
            anho = calendar.get(Calendar.YEAR)
        } else {
            val anhoText = fecha.substring(6, 10)
            anho = Integer.parseInt(anhoText)
        }
        return anho
    }

    private fun obtenerMes(): Int {
        val mes: Int
        val fecha = edt_fecha!!.text.toString()
        if (fecha == "") {
            mes = calendar.get(Calendar.MONTH) + 1
        } else {
            val mesText = fecha.substring(3, 5)
            mes = Integer.parseInt(mesText)
        }
        return mes
    }

    private fun obtenerDia(): Int {
        val dia: Int
        val fecha = edt_fecha!!.text.toString()
        if (fecha == "") {
            dia = calendar.get(Calendar.DAY_OF_MONTH)
        } else {
            val diaText = fecha.substring(0, 2)
            dia = Integer.parseInt(diaText)
        }
        return dia
    }


    private fun obtenerHora(): Int {
        val hor: Int
        val horaInicio = edt_hora!!.text.toString()
        if (horaInicio == "") {
            hor = calendar.get(Calendar.HOUR)
        } else {
            val horText = horaInicio.substring(0, 2)
            hor = Integer.parseInt(horText)
        }
        return hor
    }

    private fun obtenerMinuto(): Int {
        val minu: Int
        val horaInicio = edt_hora!!.text.toString()
        if (horaInicio == "") {
            minu = calendar.get(Calendar.MINUTE)
        } else {
            val minuText = horaInicio.substring(3, 5)
            minu = Integer.parseInt(minuText)
        }
        return minu
    }

    companion object {
        internal const val REQUEST_IMAGE_CAPTURE = 1
        internal const val REQUEST_IMAGE_CAPTURE_POST = 10
        private const val SELECT_PICTURE = 300
        private const val SELECT_PICTURE_POST = 400
    }
}