package pe.dominiotech.movil.safe2biz.inc.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.springframework.util.StringUtils;

import java.util.List;

import pe.dominiotech.movil.safe2biz.R;
import pe.dominiotech.movil.safe2biz.inc.model.Incidente;

public class IncidenteAdapter extends RecyclerView.Adapter<IncidenteAdapter.ViewHolder>{

    List<Incidente> mItems;
    View.OnClickListener onClickListener;
    Context context;
    private ProgressBar progressBar;

    public IncidenteAdapter(List<Incidente> drawerItems, View.OnClickListener onClickListener, Context contex) {
        this.mItems = drawerItems;
        this.onClickListener = onClickListener;
        context = contex;
    }


    public boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    @Override
    public IncidenteAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.inc_card_view, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(  ViewHolder viewHolder,   int i) {
        Incidente incidente = mItems.get(i);
        viewHolder.tvFecha.setText(incidente.getFecha_evento());
        viewHolder.tvHora.setText(incidente.getHora());
        viewHolder.tvGerencia.setText(incidente.getFb_area_nombre());
        viewHolder.tvGerencia.setTypeface(null, Typeface.BOLD);
        viewHolder.tvDesc.setText(incidente.getDescripcion_evento());
        if (StringUtils.hasText(incidente.getInc_tipo_evento_nombre())) {
            viewHolder.tvTipo.setText(incidente.getInc_tipo_evento_nombre().substring(0, 5).toUpperCase());
            viewHolder.tvTipo.setTypeface(null, Typeface.BOLD);
        }
        if (StringUtils.hasText(incidente.getInc_potencial_perdida_nombre())) {
            viewHolder.tvNivelRiesgo.setText(incidente.getInc_potencial_perdida_nombre());
            switch (incidente.getInc_potencial_perdida_nombre()) {
                case "Alto": case "Extremo":
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

        if (incidente.getEstado().equals("Enviar")){
            viewHolder.checkBox.setChecked(true);
        }else{
            viewHolder.checkBox.setChecked(false);
        }

        LinearLayout lnlInc = viewHolder.itemView.findViewById(R.id.lnlInc);
        lnlInc.setTag(viewHolder);
        viewHolder.setIncidentes(incidente);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout lnlyCardViewCheckList;
        TextView tvFecha;
        TextView tvHora;
        TextView tvDesc;
        TextView tvGerencia;
        TextView tvAspecto;
        TextView tvTipo;
        TextView tvNivelRiesgo;
        CheckBox checkBox;

        private Incidente incidente;

        public ViewHolder(View itemView) {
            super(itemView);
            lnlyCardViewCheckList = itemView.findViewById(R.id.lnlInc);
            tvFecha = itemView.findViewById(R.id.incFecha);
            tvHora = itemView.findViewById(R.id.incHora);
            tvDesc = itemView.findViewById(R.id.incDesc);
            tvGerencia = itemView.findViewById(R.id.incGerencia);
            tvAspecto = itemView.findViewById(R.id.incAspecto);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvNivelRiesgo = itemView.findViewById(R.id.tvNivelRiesgo);
            checkBox = itemView.findViewById(R.id.checkBox);

            lnlyCardViewCheckList.setOnClickListener(onClickListener);
        }

        public void setIncidentes(Incidente incidente) {
            this.incidente = incidente;
        }

        public Incidente getIncidente() {
            return incidente;
        }
    }

    public void setList(List<Incidente> list){
        this.mItems = list;
        notifyDataSetChanged();
    }
}