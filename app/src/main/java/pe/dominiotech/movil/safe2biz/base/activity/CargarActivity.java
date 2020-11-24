package pe.dominiotech.movil.safe2biz.base.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pe.dominiotech.movil.safe2biz.MainApplication;
import pe.dominiotech.movil.safe2biz.R;
import pe.dominiotech.movil.safe2biz.model.FiltroDescargaModuloBean;
import pe.dominiotech.movil.safe2biz.model.Usuario;
import pe.dominiotech.movil.safe2biz.ops.dao.ListaVerificacionDao;
import pe.dominiotech.movil.safe2biz.ops.model.RegistroGenerales;
import pe.dominiotech.movil.safe2biz.ops.model.RegistroResultado;
import pe.dominiotech.movil.safe2biz.service.ListaVerificacionService;
import pe.dominiotech.movil.safe2biz.utils.AppConstants;
import pe.dominiotech.movil.safe2biz.utils.LogApp;
import pe.dominiotech.movil.safe2biz.utils.Util;

public class CargarActivity extends AppCompatActivity implements OnClickListener{

    Toolbar toolbar;
    FrameLayout statusBar;
    private LinearLayout pbEspera;
    private Button btnSubir;
    private CheckedTextView chkModuloListaVerificacion;
    private CheckedTextView chkModuloActosAndCondiciones;
    private CheckedTextView chkModuloEjecucionSac;
    private Usuario usuario;
    private MainApplication app;
    private FiltroDescargaModuloBean filtro;
    ProgressDialog progressDialog = null;
    private RelativeLayout lnlyPrincipal;
    private ListaVerificacionService listaVerificacionService;

    private boolean pvez;
    private int totalCount = 0;
    private ProgressDialog Dialog1;
    private int count = 0;
    private ListaVerificacionDao listaVerificacionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogApp.log(AppConstants.ruta_log_safe2biz, AppConstants.ARCHIVO_LOG_SAFE2BIZ,"[CargarActivity] entro ");
        setupTheme();
        super.onCreate(savedInstanceState);
        Dialog1 = new ProgressDialog(this);
        listaVerificacionDao = new ListaVerificacionDao(this, AppConstants.DB_NAME, null, AppConstants.DB_VERSION);

//		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//			@Override
//			public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
//				app.setUsuarioEnSesion(null);
//				Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(intent);
//				System.exit(2);
//			}
//		});

        setContentView(R.layout.enviar_modulos_activity);
        mostrarBarraAcciones();
        inicializarComponentes();

    }

    public void setupTheme() {
        setTheme(R.style.MyMaterialTheme);
    }

    private void mostrarBarraAcciones(){

        app = (MainApplication) getApplication();
        usuario = app.getUsuarioEnSesion();
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(R.string.lb_titulo_subida_modulos);
        }

    }

    private void inicializarComponentes() {
        listaVerificacionService = app.getListaVerificacionService();
        chkModuloListaVerificacion = (CheckedTextView)findViewById(R.id.chkModuloListaVerificacionSubida);
        chkModuloActosAndCondiciones = (CheckedTextView)findViewById(R.id.chkModuloActosAndCondicionesSubida);
        chkModuloEjecucionSac = (CheckedTextView)findViewById(R.id.chkModuloEjecucionSacSubida);

        chkModuloListaVerificacion.setOnClickListener(this);
        chkModuloActosAndCondiciones.setOnClickListener(this);
        chkModuloEjecucionSac.setOnClickListener(this);

        btnSubir = (Button)findViewById(R.id.btnSubir);
        btnSubir.setOnClickListener(this);

        pbEspera = (LinearLayout)findViewById(R.id.lnlyEspera);
        lnlyPrincipal = (RelativeLayout) findViewById(R.id.lnlySubirModulos);
        filtro = new FiltroDescargaModuloBean();

        chkModuloListaVerificacion.setChecked(false);
        chkModuloListaVerificacion.setEnabled(true);
        chkModuloActosAndCondiciones.setChecked(false);
        chkModuloActosAndCondiciones.setEnabled(true);
        chkModuloEjecucionSac.setChecked(false);
        chkModuloEjecucionSac.setEnabled(true);

        filtro.setListaVerificacion(true);
        filtro.setActosAndCondiciones(true);
        filtro.setEjecucionSac(true);
        filtro.setVerificacionSac(true);
    }

    @Override
    public void onClick(View v) {
        if(v == btnSubir){
            String URL_EXT = app.getUsuarioEnSesion().getIpOrDominioServidor();
//            String URL_EXT = "http://192.168.1.54:7777/safe2biz";
            Map<String,String> headers = new HashMap<>();
            headers.put("userLogin",usuario.getUser_login());
            headers.put("userPassword",usuario.getPassword());
            headers.put("systemRoot","safe2biz");
            Map <String,String> parameters = new HashMap<>();

            Dialog1.setMessage("Descargando datos del servidor...");
            Dialog1.show();

            if(chkModuloListaVerificacion.isChecked()){
                List<RegistroGenerales> registroGeneralesList = listaVerificacionDao.getRegistrosGeneralesList();
//				filtro.setListaVerificacion(false);
                if (registroGeneralesList != null){
                    int flag_count = 0;
                    for (final RegistroGenerales registroGenerales :
                            registroGeneralesList) {
                        if(registroGenerales.getFlag() != null && registroGenerales.getId_generado_syncronizacion() == null){
                            flag_count ++;
                            parameters.clear();
                            parameters.put("sc_user_id", "" + app.getUsuarioEnSesion().getSc_user_id());
                            parameters.put("fb_uea_pe_id",registroGenerales.getFbUeaPeId().toString());
                            parameters.put("codigo",registroGenerales.getCodigo());
                            parameters.put("g_tipo_origen_id", String.valueOf(registroGenerales.getgTipoOrigenId()));
                            parameters.put("fecha_ops", registroGenerales.getFechaOps());
                            parameters.put("hora_ops", registroGenerales.getHoraOps());
                            parameters.put("turno", registroGenerales.getTurno().getCode());
                            parameters.put("fb_area_id", String.valueOf(registroGenerales.getArea().getFb_area_id()));
                            parameters.put("g_rol_empresa_id", String.valueOf(registroGenerales.getEmpresaEspecializada().getG_rol_empresa_id()));
                            parameters.put("fb_empresa_especializada_id", String.valueOf(registroGenerales.getEmpresaEspecializada().getFb_empresa_especializada_id()));
                            parameters.put("fb_empleado_id", String.valueOf(registroGenerales.getFbEmpleadoId()));
                            parameters.put("ops_lista_verificacion_id", String.valueOf(registroGenerales.getOpsListaVerificacionId()));
                            parameters.put("latitud", String.valueOf(registroGenerales.getLatitud()));
                            parameters.put("longitud", String.valueOf(registroGenerales.getLongitud()));
                            parameters.put("ruta_foto", "movil.jpg");
                            parameters.put("id_interno", String.valueOf(registroGenerales.getOps_registro_generales_id()));
                            parameters.put("codigo_movil", app.getUsuarioEnSesion().getIdDispositivo());
                            AndroidNetworking.post(URL_EXT + getResources().getString(R.string.SERVICIO_OPS_REGISTRO_GENERALES_IN))
                                    .addHeaders(headers)
                                    .addBodyParameter(parameters)
                                    .setTag("OPS")
                                    .setPriority(Priority.HIGH)
                                    .build()
                                    .getAsJSONObject(new JSONObjectRequestListener() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                JSONArray data = response.getJSONArray("data");
                                                for (int i = 0; i < data.length(); i++) {
                                                    JSONObject object = data.getJSONObject(i);
                                                    registroGenerales.setId_generado_syncronizacion(object.getString("ops_registro_generales_id"));
                                                    listaVerificacionDao.guardarRegistroGenerales(registroGenerales);
                                                    uploadEvidencias(object.getLong("ops_registro_generales_id"), registroGenerales);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onError(ANError error) {
                                            cancelRequest(error);
                                        }
                                    });
                        }
                    }
                    if(flag_count == 0) {
                        Dialog1.dismiss();
                        Dialog1.hide();
                        Toast.makeText(app, "No hay registros Completos para subir.", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Dialog1.dismiss();
                    Dialog1.hide();
                    Toast.makeText(app, "No hay registros para subir.", Toast.LENGTH_SHORT).show();
                }


            }
            if(!chkModuloActosAndCondiciones.isChecked()){
                filtro.setActosAndCondiciones(false);
            }
            if(!chkModuloEjecucionSac.isChecked()){
                filtro.setEjecucionSac(false);
            }
            LogApp.log(AppConstants.ruta_log_safe2biz, AppConstants.ARCHIVO_LOG_SAFE2BIZ,"[CargarActivity] Verificando Checked... ");

//			try{
//				if (chkModuloListaVerificacion.isChecked() || chkModuloActosAndCondiciones.isChecked() || chkModuloEjecucionSac.isChecked()){
//					String res = "";
//					String msj = "";
//					if (chkModuloListaVerificacion.isChecked()){
//					}
//					if (chkModuloActosAndCondiciones.isChecked()){
//					}
//					if (chkModuloEjecucionSac.isChecked()){
//					}
//					LogApp.log(AppConstants.ruta_log_lista_verificacion, AppConstants.ARCHIVO_LOG_LISTA_VERIFICACION,"[CargarActivity] msj "+msj+" -");
//					if (msj.equals(AppConstants.cadena_vacia)){
//						if (Util.VerificarConexionInternet(getApplicationContext())){
//							mostrarPopupPrincipal(AppConstants.IS_LISTA_VERIFICACION);
//						}else{
//							Util.mostrarMensaje(getApplicationContext(), Mensajes.mensaje_no_hay_internet);
//						}
//					}else if (res.equals(AppConstants.estado_error)){
//						Util.mostrarMensaje(getApplicationContext(), Mensajes.error_procesar);
//					}else {
//						Util.mostrarMensaje(getApplicationContext(), msj);
//					}
//				}else{
//					Util.mostrarMensaje(getApplicationContext(), Mensajes.mensaje_seleccionar_modulo);
//				}
//			}catch(Exception e){
//				LogApp.log(AppConstants.ruta_log_lista_verificacion, AppConstants.ARCHIVO_LOG_LISTA_VERIFICACION,"[CargarActivity] error "+e.getLocalizedMessage());
//			}
        }else if (v == chkModuloListaVerificacion){
            if(chkModuloListaVerificacion.isChecked()){
                chkModuloListaVerificacion.setChecked(false);
                filtro.setListaVerificacion(false);
            } else{
                chkModuloListaVerificacion.setChecked(true);
                filtro.setListaVerificacion(true);
            }
            LogApp.log(AppConstants.ruta_log_lista_verificacion, AppConstants.ARCHIVO_LOG_LISTA_VERIFICACION,"[CargarActivity] chkModuloListaVerificacion "+chkModuloListaVerificacion.isChecked()+ " - " +filtro.isListaVerificacion());
        }else if (v == chkModuloActosAndCondiciones){
            if(chkModuloActosAndCondiciones.isChecked()){
                chkModuloActosAndCondiciones.setChecked(false);
                filtro.setActosAndCondiciones(false);
            } else{
                chkModuloActosAndCondiciones.setChecked(true);
                filtro.setActosAndCondiciones(true);
            }
            LogApp.log(AppConstants.ruta_log_actos_and_condiciones, AppConstants.ARCHIVO_LOG_ACTOS_Y_CONDICIONES,"[CargarActivity] chkModuloActosAndCondiciones "+chkModuloActosAndCondiciones.isChecked()+ " - " +filtro.isActosAndCondiciones());
        }else if (v == chkModuloEjecucionSac){
            if(chkModuloEjecucionSac.isChecked()){
                chkModuloEjecucionSac.setChecked(false);
                filtro.setEjecucionSac(false);
            } else{
                chkModuloEjecucionSac.setChecked(true);
                filtro.setEjecucionSac(true);
            }
            LogApp.log(AppConstants.ruta_log_ejecucion_sac, AppConstants.ARCHIVO_LOG_EJECUCION_SAC,"[CargarActivity] chkModuloEjecucionSac "+chkModuloEjecucionSac.isChecked()+ " - " +filtro.isEjecucionSac());
        }
        if(chkModuloListaVerificacion.isChecked() || chkModuloEjecucionSac.isChecked() || chkModuloActosAndCondiciones.isChecked()){
            btnSubir.setEnabled(true);

        }else{
            btnSubir.setEnabled(false);
        }
    }

    private void uploadEvidencias(Long identity, RegistroGenerales registroGenerales) {
        String URL_EXT = app.getUsuarioEnSesion().getIpOrDominioServidor();
//            String URL_EXT = "http://192.168.1.54:7777/safe2biz";
        Map<String,String> headers = new HashMap<>();
        headers.put("userLogin",usuario.getUser_login());
        headers.put("userPassword",usuario.getPassword());
        headers.put("systemRoot","safe2biz");

        Map <String,String> parameters = new HashMap<>();

        List<RegistroResultado> registroGeneralesList = listaVerificacionDao.getListaResultadosByRegistro(registroGenerales);
        if (registroGeneralesList != null) {
            totalCount += registroGeneralesList.size();
            for (RegistroResultado registroResultado :
                    registroGeneralesList) {
                parameters.clear();
                parameters.put("sc_user_id", String.valueOf(app.getUsuarioEnSesion().getSc_user_id()));
                parameters.put("id_interno", String.valueOf(registroResultado.getOpsRegistroResultadoId()));
                parameters.put("codigo_movil", app.getUsuarioEnSesion().getIdDispositivo());
                parameters.put("ops_registro_generales_id", identity.toString());
                parameters.put("fb_uea_pe_id", "" + app.getUsuarioEnSesion().getFb_uea_pe_id());
                parameters.put("ops_lista_verif_pregunta_id", String.valueOf(registroResultado.getOpsListaVerifPreguntaId()));
                parameters.put("ops_lista_verif_seccion_id", String.valueOf(registroResultado.getOpsListaVerifSeccionId()));
                parameters.put("ops_lista_verif_categoria_id", String.valueOf(registroResultado.getOpsListaVerifCategoriaId()));
                parameters.put("ops_lista_verif_resultado_id", String.valueOf(registroResultado.getOpsListaVerifResultadoId()));
                if (registroResultado.getObservacion() != null) {
                    parameters.put("observacion", String.valueOf(registroResultado.getObservacion()));
                }
                if (registroResultado.getRuta() != null){
                    Bitmap bitmap = BitmapFactory.decodeFile(registroResultado.getRuta());
                    String imageStream = Util.encodeToBase64(Util.resize(bitmap,800,800), Bitmap.CompressFormat.JPEG, 100);
                    parameters.put("imagen", registroResultado.getNombreImagen() + ";" + imageStream);
                }

                AndroidNetworking.post(URL_EXT + getResources().getString(R.string.SERVICIO_OPS_REGISTRO_RESULTADO_IN))
                        .addBodyParameter(parameters)
                        .addHeaders(headers)
                        .setTag("OPS")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("lol","entro");
                                addCount();
                            }
                            @Override
                            public void onError(ANError error) {
                                Log.e("Error",error.toString());
                                Toast.makeText(app, "No se completó la sincronización", Toast.LENGTH_SHORT).show();
                                cancelRequest(error);
                            }
                        });
            }
        }


    }


    private void addCount() {
        count++;
        if (count == totalCount){
            Dialog1.dismiss();
            Dialog1.hide();
            Toast.makeText(app, "Se completó la sincronización", Toast.LENGTH_SHORT).show();
            count = 0;
            totalCount = 0;
        }
    }

    private void cancelRequest(ANError error) {
//        AndroidNetworking.forceCancelAll();
        Log.e("Error", Integer.toString(error.getErrorCode()));
        Dialog1.dismiss();
        Dialog1.hide();
        Toast.makeText(app, "No se completó la sincronización", Toast.LENGTH_SHORT).show();
        count = 0;
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




}
