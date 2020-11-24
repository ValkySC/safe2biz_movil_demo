package pe.dominiotech.movil.safe2biz.base.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.login_activity.*
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.base.adapter.CountrySpinnerAdapter
import pe.dominiotech.movil.safe2biz.model.Usuario
import pe.dominiotech.movil.safe2biz.service.UsuarioService
import pe.dominiotech.movil.safe2biz.utils.Util
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.text.Normalizer
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : Activity() {
    internal var ipOrDominioServidor: String = ""
    internal var empresa: String = ""
    private var idDispositivo: String? = null
    private var app: MainApplication? = null
    private var usuarioService: UsuarioService? = null
    private var usuario = Usuario()


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        //        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIG_COMPLETA_REQUEST) {
            when (resultCode) {
                RESULT_OK -> {
                    ipOrDominioServidor = data.extras!!.getString("ip_or_dominio_servidor")!!
                    empresa = data.extras!!.getString("empresa")!!
                }
                RESULT_CANCELED -> Toast.makeText(this, " No se guardó la información... ", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, " No se guardó la información... ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun validateName(): Boolean {
        if (input_name.text.toString().trim { it <= ' ' }.isEmpty()) {
            input_layout_name!!.error = "Usuario vacío"
            requestFocus(input_name!!)
            return false
        } else {
            input_layout_name!!.isErrorEnabled = false
        }

        return true
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        setTheme(R.style.MyMaterialTheme)
        inicializarComponentes()
    }


    private fun inicializarComponentes() {
        app = application as MainApplication
        usuarioService = app!!.usuarioService
        idDispositivo = Util.getImei(this@LoginActivity)
        usuario.usuario_id = 1

        if (usuarioService!!.getBean(usuario).ipOrDominioServidor.isNotEmpty()) {
            usuario = usuarioService!!.getBean(usuario)
            ipOrDominioServidor = usuario.ipOrDominioServidor
            empresa = usuario.empresa
            if (usuario.user_login.isNotEmpty() && usuario.password.isNotBlank()) {
                input_name.setText(usuario.user_login)
                input_pass.setText(usuario.password)
            }
        }
        val datos = arrayListOf("PERÚ", "MÉXICO", "ARGENTINA")
        val flags = arrayListOf(R.drawable.icon_peru_flag, R.drawable.icon_mex_flag, R.drawable.icon_arg_flag)

        spn_Pais.setAdapter(CountrySpinnerAdapter(this, R.layout.country_item ,datos, flags))
        ic_password.setOnClickListener {
            if (input_pass.transformationMethod === HideReturnsTransformationMethod.getInstance()) {
                input_pass.transformationMethod = PasswordTransformationMethod.getInstance()
                ic_password!!.setImageResource(R.drawable.ic_eye_slash)
            } else {
                input_pass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ic_password!!.setImageResource(R.drawable.ic_eye)
            }
        }
        loginButton.setOnClickListener{
            onLoginButtonClick()
        }
        imgViewConfiguracion.setOnClickListener { onConfiguracionImageViewClick() }
    }

    private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    fun CharSequence.unaccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return REGEX_UNACCENT.replace(temp, "")
    }
    private fun onLoginButtonClick() {
        validateName()

        val emailStr = input_name!!.text.toString()
        val passwordStr = input_pass!!.text.toString()
        val paisStr = spn_Pais!!.text.toString().unaccent()

        if (emailStr == "" || passwordStr == "") {
            Toast.makeText(app, "Hay campos vacíos. Por favor, llenar todos los campos.", Toast.LENGTH_SHORT).show()
        } else {
            if (ipOrDominioServidor.isNotEmpty()) {
                if (emailStr == usuario.user_login
                        && passwordStr == usuario.password
                        && empresa == usuario.empresa
                        && ipOrDominioServidor == usuario.ipOrDominioServidor
                        && paisStr == usuario.pais
                ) {
                    usuario.idDispositivo = idDispositivo
                    app!!.usuarioEnSesion = usuario
                    val i = Intent(applicationContext, ListaUnidadesActivity::class.java)
                    startActivity(i)
                } else {
                    if (isOnline) {
                        val headers = HashMap<String, String>()
                        if (paisStr.isEmpty()) {
                            headers["userLogin"] = "$emailStr@$empresa"
                        } else {
                            headers["userLogin"] = "$paisStr\\$emailStr@$empresa"
                        }

                        headers["userPassword"] = passwordStr
                        headers["systemRoot"] = "safe2biz"
                        val parameters = HashMap<String, String>()
                        if (paisStr.isEmpty()) {
                            parameters["user_login"] = emailStr
                        } else {
                            parameters["user_login"] = "$paisStr\\$emailStr"
                        }
                        val builder = AlertDialog.Builder(this)
                        builder.setCancelable(false)
                        builder.setView(R.layout.layout_loading_dialog)
                        val dialog = builder.create()
                        dialog.show()

                        val okHttpClient = OkHttpClient().newBuilder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .build()

                        AndroidNetworking.post(ipOrDominioServidor + resources.getString(R.string.SERVICIO_SC_USER))
                                .addBodyParameter(parameters)
                                .addHeaders(headers)
                                .setTag("login")
                                .setPriority(Priority.HIGH)
                                .setOkHttpClient(okHttpClient)
                                .build()
                                .getAsJSONObject(object : JSONObjectRequestListener {
                                    override fun onResponse(response: JSONObject) {
                                        try {
                                            val data = response.getJSONArray("data")
                                            for (i in 0 until data.length()) {
                                                val `object` = data.getJSONObject(i)
                                                usuario.user_login = emailStr
                                                usuario.password = passwordStr
                                                usuario.sc_user_id = `object`.getLong("SC_USER_ID")
                                                usuario.usuario = `object`.getString("USER_LOGIN")
                                                //                                                usuario.setDni(object.getString("DNI"));
                                                usuario.fb_empleado_id = `object`.getLong("fb_empleado_id")
                                                //                                                usuario.setNombre_empleado(object.getString("nombre_empleado"));
                                                usuario.url_ext = `object`.getString("URL_EXT")
                                                usuario.ipOrDominioServidor = ipOrDominioServidor
                                                usuario.empresa = empresa
                                                usuario.pais = paisStr
                                            }
                                            val isSaveUser = usuarioService!!.save(usuario)
                                            if (1 == isSaveUser) {
                                                dialog.dismiss()
                                                usuario.idDispositivo = "1"
                                                app!!.usuarioEnSesion = usuario
                                                val intent = Intent(applicationContext, ListaUnidadesActivity::class.java)
                                                startActivity(intent)
                                            } else {
                                                mostrarMensaje("Error al guardar el usuario...")
                                            }
                                        } catch (e: JSONException) {
                                            e.printStackTrace()
                                        }

                                    }

                                    override fun onError(error: ANError) {
                                        dialog.dismiss()
                                        Log.e("Error", Integer.toString(error.errorCode))
                                        if (Integer.toString(error.errorCode) == "410") {
                                            try {
                                                val jsonObject = JSONObject(error.errorBody)
                                                val mensaje = jsonObject.getJSONArray("errors").get(0).toString()
                                                Toast.makeText(app, mensaje, Toast.LENGTH_SHORT).show()
                                            } catch (e: JSONException) {
                                                e.printStackTrace()
                                            }

                                        } else {
                                            Toast.makeText(app, "Ha ocurrido un error en la conexión, revisa tu conexión a internet o la ip/dominio del servidor...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })
                    } else {
                        Toast.makeText(app, "No existe el usuario. Necesita conexión a internet...", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(app, "Presione la tuerca para configurar el servidor...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onConfiguracionImageViewClick() {
        val i = Intent(applicationContext, ConfiguracionActivity::class.java)
        startActivityForResult(i, CONFIG_COMPLETA_REQUEST)
    }

    fun mostrarMensaje(mensaje: String) {
        val toast = Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT)
        toast.show()
    }


    companion object {
        internal val CONFIG_COMPLETA_REQUEST = 1  // The request code
    }

}


