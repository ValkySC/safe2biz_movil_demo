package pe.dominiotech.movil.safe2biz.inc.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import pe.dominiotech.movil.safe2biz.MainApplication;
import pe.dominiotech.movil.safe2biz.R;
import pe.dominiotech.movil.safe2biz.base.model.MenuPrincipalItem;
import pe.dominiotech.movil.safe2biz.model.Usuario;


public class DashboardIncidentesActivity extends AppCompatActivity {

    Toolbar toolbar;
    FrameLayout statusBar;
    private MainApplication app;
    private MenuPrincipalItem menuPrincipalItem;
    int onStartCount = 0;
    private Usuario usuario;

    private WebView webViewDashboard;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);

        onStartCount = 1;
        if (savedInstanceState == null){
            this.overridePendingTransition(R.anim.anim_slide_in_left,R.anim.anim_slide_out_left);
        } else {
            onStartCount = 2;
        }

        setContentView(R.layout.dashboard_fragment);
        menuPrincipalItem = (MenuPrincipalItem)getIntent().getExtras().getSerializable("menuPrincipalItem");
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        app = (MainApplication) getApplication();
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        usuario = app.getUsuarioEnSesion();
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Gráficos de Incidentes");
            actionBar.setHomeButtonEnabled(true);
        }

        webViewDashboard = (WebView) findViewById(R.id.webViewDashboard);
        cargarDashboard();
    }

    public void setupTheme() {
        setTheme(R.style.MyMaterialTheme);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
            this.overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void cargarDashboard(){
        webViewDashboard.loadUrl(usuario.getUrl_ext() + "/safe2biz_home/Home_Plantilla.asp?Id_Home=10030&Id_Unidad="+usuario.getFb_uea_pe_id()+"&Id_Usuario="+usuario.getSc_user_id()+"&EMPRESA="+usuario.getEnterprise());
        WebSettings webSettings = webViewDashboard.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webViewDashboard.setWebViewClient(new WebViewClient());
    }

}
