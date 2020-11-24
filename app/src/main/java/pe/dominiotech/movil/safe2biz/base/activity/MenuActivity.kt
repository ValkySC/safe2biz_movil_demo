package pe.dominiotech.movil.safe2biz.base.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.drawer.*
import kotlinx.android.synthetic.main.menu_activity.*
import pe.dominiotech.movil.safe2biz.MainApplication
import pe.dominiotech.movil.safe2biz.R
import pe.dominiotech.movil.safe2biz.ayc.activity.RegistroActivity
import pe.dominiotech.movil.safe2biz.ayc.activity.RegistroDetalleActivity
import pe.dominiotech.movil.safe2biz.base.adapter.MenuPrincipalAdapter
import pe.dominiotech.movil.safe2biz.base.model.MenuPrincipalItem
import pe.dominiotech.movil.safe2biz.inc.activity.IncidentesActivity
import pe.dominiotech.movil.safe2biz.model.Usuario
import pe.dominiotech.movil.safe2biz.sac.activity.AccionCorrectivaActivity
import pe.dominiotech.movil.safe2biz.utils.CircleTransform
import java.util.*


class MenuActivity : AppCompatActivity() {
    private var app: MainApplication? = null
    private var menuList: MutableList<MenuPrincipalItem> = ArrayList()
    internal var onStartCount = 0

    private val usuarioEnSesion: Usuario?
        get() = app!!.usuarioEnSesion

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

        onStartCount = 1
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else {
            onStartCount = 2
        }
        setContentView(R.layout.menu_activity)
        app = application as MainApplication
        setSupportActionBar(app_bar as Toolbar)
        setupNavigationDrawer()
        crearMenuPrincipal()
        val menuPrincipalAdapter = MenuPrincipalAdapter(menuList, OnClickListener { view ->
            val menuPrincipalItem = view.tag as MenuPrincipalItem
            var i = Intent()
            when (menuPrincipalItem.orden) {
                1 -> i = Intent(applicationContext, IncidentesActivity::class.java)
                2 -> i = Intent(applicationContext, RegistroActivity::class.java)
                3 -> i = Intent(applicationContext, DashboardActivity::class.java)
                4 -> i = Intent(applicationContext, RegistroDetalleActivity::class.java)
                5 -> i = Intent(applicationContext, MapaActivity::class.java)
                6 -> i = Intent(applicationContext, AccionCorrectivaActivity::class.java)
            }
            i.putExtra("menuPrincipalItem", menuPrincipalItem)
            startActivity(i)
        })
        menu_principal!!.setHasFixedSize(true)
        menu_principal!!.layoutManager = LinearLayoutManager(applicationContext)
        menu_principal!!.adapter = menuPrincipalAdapter

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Menú"
            actionBar.setHomeButtonEnabled(true)
        }
        inicializar()
        app!!.setActionBarPrincipal(actionBar)
    }

    private fun inicializar() {
        sincronizacion!!.setOnClickListener {
            if (descarga!!.visibility == View.GONE) {
                descarga!!.visibility = View.VISIBLE
                //                    envio.setVisibility(View.VISIBLE);
                sinc_arrow!!.setImageResource(R.drawable.ic_arrow_up_black_24dp)
            } else {
                descarga!!.visibility = View.GONE
                envio!!.visibility = View.GONE
                sinc_arrow!!.setImageResource(R.drawable.ic_arrow_down_black_24dp)
            }
        }
        descarga!!.setOnClickListener {
            val intent = Intent(applicationContext, DescargarActivity::class.java)
            startActivity(intent)
        }

        envio!!.setOnClickListener {
            val intent = Intent(applicationContext, CargarActivity::class.java)
            startActivity(intent)
        }

        cambiar_sede!!.setOnClickListener { super@MenuActivity.onBackPressed() }

        cerrar_sesion!!.setOnClickListener {
            val builder = AlertDialog.Builder(this@MenuActivity)
            builder.setTitle(R.string.confirmar)
            builder.setMessage(R.string.cerrarsesion2)
            builder.setPositiveButton("Aceptar") { dialogo1, id ->
                app!!.usuarioEnSesion = null
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            builder.setNegativeButton("Cancelar") { dialogo1, id -> dialogo1.cancel() }
            builder.create().show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun setupTheme() {
        setTheme(R.style.MyMaterialTheme)
    }

    private fun setupNavigationDrawer() {
        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, app_bar as Toolbar, R.string.guion, R.string.guion)
        drawerLayout.addDrawerListener(drawerToggle)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
        drawerToggle.syncState()

        textViewName.text = usuarioEnSesion?.nombre_empleado

        Picasso.with(applicationContext).load(R.mipmap.ic_launcher).transform(CircleTransform()).into(imageViewPictureMain)
        Picasso.with(applicationContext).load(R.drawable.img_cover_main).into(imageViewCover)

        val typedValue = TypedValue()
        this@MenuActivity.theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
        val color = typedValue.data
        drawerLayout.setStatusBarBackgroundColor(color)

    }

    private fun crearMenuPrincipal() {
        var menuPrincipalItem = MenuPrincipalItem()

        menuPrincipalItem.orden = 1
        menuPrincipalItem.titulo = "Incidentes"
        menuPrincipalItem.icono = R.drawable.ic_warning
        menuList.add(menuPrincipalItem)

        //        menuPrincipalItem.setOrden(1);
        //        menuPrincipalItem.setTitulo("Observaciones Preventivas");
        //        menuPrincipalItem.setIcono(R.drawable.ic_encuestas);
        //        menuList.add(menuPrincipalItem);

        menuPrincipalItem = MenuPrincipalItem()
        menuPrincipalItem.orden = 2
        menuPrincipalItem.titulo = "Actos y Condiciones"
        menuPrincipalItem.icono = R.drawable.ic_gestion_a_y_c
        menuList.add(menuPrincipalItem)
        //
        menuPrincipalItem = MenuPrincipalItem()
        menuPrincipalItem.orden = 6
        menuPrincipalItem.titulo = "Acciones Correctivas"
        menuPrincipalItem.icono = R.drawable.ic_encuestas
        menuList.add(menuPrincipalItem)
        //
        //        menuPrincipalItem = new MenuPrincipalItem();
        //        menuPrincipalItem.setOrden(5);
        //        menuPrincipalItem.setTitulo("Mapa");
        //        menuPrincipalItem.setIcono(R.drawable.ic_mapa_03);
        //        menuList.add(menuPrincipalItem);
        //
        //        menuPrincipalItem = new MenuPrincipalItem();
        //        menuPrincipalItem.setOrden(3);
        //        menuPrincipalItem.setTitulo("Tablero Gerencial");
        //        menuPrincipalItem.setIcono(R.drawable.ic_reportes_graficos);
        //        menuList.add(menuPrincipalItem);
    }
}


