package pe.dominiotech.movil.safe2biz.base.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast

import kotlinx.android.synthetic.main.configuracion_activity.*
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.model.Usuario
import pe.dominiotech.movil.safe2biz.service.UsuarioService


class ConfiguracionActivity : Activity() {
    private var app: MainApplication? = null
    private var usuarioService: UsuarioService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {             @Override             public void uncaughtException(Thread paramThread, Throwable paramThrowable) {                 app.setUsuarioEnSesion(null);                 Intent intent = new Intent(getApplicationContext(), LoginActivity.class);                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                 startActivity(intent);                 System.exit(2);             }         });*/

        setContentView(R.layout.configuracion_activity)
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        app = application as MainApplication
        usuarioService = app!!.usuarioService
        btnGuardarConfiguracion.transformationMethod = null
        val usuarioBean = obtenerDatosUsuario()
        cargarInformacion(usuarioBean)
        edt_ipSv.setTypeface(null, Typeface.BOLD)
        edt_empresa.setTypeface(null, Typeface.BOLD)
        btnGuardarConfiguracion.setOnClickListener { onSave() }
    }


    fun onSave() {
        val ipOrDominioConfigStr = edt_ipSv!!.text.toString()
        val empresa = edt_empresa!!.text.toString()
        val msj = verificarCamposObligatorios(ipOrDominioConfigStr, empresa)
        if (msj.isNotEmpty()) {
            val toast = Toast.makeText(applicationContext, msj, Toast.LENGTH_SHORT)
            toast.show()
        } else {
            val usuarioBean = obtenerDatosUsuario()
            usuarioBean!!.ipOrDominioServidor = ipOrDominioConfigStr
            usuarioBean.empresa = empresa
            usuarioService!!.save(usuarioBean)

            val data = Intent()
            data.putExtra("ip_or_dominio_servidor", ipOrDominioConfigStr)
            data.putExtra("empresa", empresa)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    fun verificarCamposObligatorios(ipOrDominioConfigStr: String, empresa: String): String {
        var mensaje = ""
        if (ipOrDominioConfigStr.isEmpty()) {
            mensaje = "Ingresar IP o dominio del servidor"
        } else if (empresa.isEmpty()) {
            mensaje = "Ingresa una empresa"
        }
        return mensaje
    }

    fun cargarInformacion(user: Usuario?) {
        if (user!!.ipOrDominioServidor!!.isNotEmpty() && user.empresa!!.isNotEmpty()) {
            edt_ipSv!!.setText(user.ipOrDominioServidor)
            edt_empresa!!.setText(user.empresa)
        }
    }

    fun obtenerDatosUsuario(): Usuario? {
        val usuarioBean = Usuario()
        usuarioBean.usuario_id = 1
        usuarioBean.ipOrDominioServidor = ""
        usuarioBean.empresa = ""
        return usuarioService!!.getBean(usuarioBean)
    }

    override fun onBackPressed() {
        val ipOrDominioConfigStr = edt_ipSv!!.text.toString()
        val empresa = edt_empresa!!.text.toString()

        val data = Intent()
        data.putExtra("ip_or_dominio_servidor", ipOrDominioConfigStr)
        data.putExtra("empresa", empresa)
        setResult(RESULT_OK, data)
        finish()
        super.onBackPressed()
    }
}


