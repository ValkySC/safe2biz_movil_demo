package pe.dominiotech.movil.safe2biz.ayc.adapter;

import android.content.Context;
import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.springframework.util.StringUtils;

import java.util.List;

import pe.dominiotech.movil.safe2biz.R;
import pe.dominiotech.movil.safe2biz.ayc.dao.RegistroDao;
import pe.dominiotech.movil.safe2biz.ayc.dao.RegistroEvidenciaDao;
import pe.dominiotech.movil.safe2biz.ayc.model.Registro;
import pe.dominiotech.movil.safe2biz.ayc.model.RegistroEvidencia;
import pe.dominiotech.movil.safe2biz.ops.dao.ListaVerificacionDao;
import pe.dominiotech.movil.safe2biz.utils.AppConstants;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.ViewHolder>{

    private List<Registro> mItems;
    private View.OnClickListener onClickListener;
    private Context context;

    public RegistroAdapter(List<Registro> drawerItems, View.OnClickListener onClickListener, Context contex) {
        this.mItems = drawerItems;
        this.onClickListener = onClickListener;
        context = contex;
    }


    public boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    @Override
    public RegistroAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ayc_registro_card, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(  ViewHolder viewHolder,   int i) {
        Registro carViewModel = mItems.get(i);
        if (StringUtils.hasText(carViewModel.getOrigen())) {

            switch (carViewModel.getOrigen()) {
                case "A":
                    viewHolder.tvOrigen.setText("ACTO");
                    break;
                case "C":
                    viewHolder.tvOrigen.setText("COND");
                    break;
            }
        }
        viewHolder.tvOrigen.setTypeface(null,Typeface.BOLD);
        if (StringUtils.hasText(carViewModel.getNivel_riesgo_nombre())) {

            viewHolder.tvNivelRiesgo.setText(carViewModel.getNivel_riesgo_nombre());
            switch (carViewModel.getNivel_riesgo_nombre()) {
                case "Alto":
                case "Extremo":
                    viewHolder.tvNivelRiesgo.setBackgroundResource(R.drawable.border_rojo);
                    break;
                case "Medio":
                    viewHolder.tvNivelRiesgo.setBackgroundResource(R.drawable.border_naranja);
                    break;
                case "Bajo":
                    viewHolder.tvNivelRiesgo.setBackgroundResource(R.drawable.border_verde);
                    break;
            }
        }
        if (StringUtils.hasText(carViewModel.getDescripcion())) {
            viewHolder.tvDescripcion.setText(carViewModel.getDescripcion());
        } else {
            viewHolder.tvDescripcion.setVisibility(View.GONE);
        }
        if (StringUtils.hasText(carViewModel.getFecha())) {
            viewHolder.tvFecha.setText(carViewModel.getFecha());
        } else {
            viewHolder.tvFecha.setVisibility(View.GONE);
        }
        if (StringUtils.hasText(carViewModel.getHora())) {
            viewHolder.tvHora.setText(carViewModel.getHora());
        } else {
            viewHolder.tvHora.setVisibility(View.GONE);
        }

        viewHolder.tvArea.setText(carViewModel.getArea_nombre());
        viewHolder.tvArea.setTypeface(null,Typeface.BOLD);
        viewHolder.tvEmpresaReportante.setText(carViewModel.getEmpresa_nombre());

        if (carViewModel.getEstado().equals("Enviar")){
            viewHolder.checkBox.setChecked(true);
        }else{
            viewHolder.checkBox.setChecked(false);
        }
        LinearLayout lnlyAycRegistro = viewHolder.itemView.findViewById(R.id.lnlyAycRegistro);
        lnlyAycRegistro.setTag(viewHolder);
        viewHolder.setRegistroModel(carViewModel);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout lnlyAycRegistro;
        TextView tvOrigen;
        TextView tvDescripcion;
        TextView tvFecha;
        TextView tvHora;
        TextView tvNivelRiesgo;
        TextView tvArea;
        TextView tvEmpresaReportante;
        CheckBox checkBox;

        private Registro RegistroModel;

        public ViewHolder(View itemView) {
            super(itemView);
            lnlyAycRegistro = itemView.findViewById(R.id.lnlyAycRegistro);
            tvOrigen = itemView.findViewById(R.id.tvOrigen);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvNivelRiesgo = itemView.findViewById(R.id.tvNivelRiesgo);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvEmpresaReportante = itemView.findViewById(R.id.tvEmpresaReportante);
            checkBox = itemView.findViewById(R.id.checkBox);

            lnlyAycRegistro.setOnClickListener(onClickListener);
        }

        public Registro getRegistroModel() {
            return RegistroModel;
        }

        void setRegistroModel(Registro registroModel) {
            RegistroModel = registroModel;
        }

    }

    public void setList(List<Registro> list){
        this.mItems = list;
        notifyDataSetChanged();
    }
}