package pe.dominiotech.movil.safe2biz.sac.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pe.dominiotech.movil.safe2biz.R;
import pe.dominiotech.movil.safe2biz.sac.dao.AccionCorrectivaEvidenciaDao;
import pe.dominiotech.movil.safe2biz.sac.model.AccionCorrectiva;
import pe.dominiotech.movil.safe2biz.sac.model.AccionCorrectivaEvidencia;
import pe.dominiotech.movil.safe2biz.utils.AppConstants;

public class AccionCorrectivaAdapter extends RecyclerView.Adapter<AccionCorrectivaAdapter.ViewHolder>{

    private List<AccionCorrectiva> mItems;
    View.OnClickListener onClickListener;
    Context context;

    public AccionCorrectivaAdapter(List<AccionCorrectiva> drawerItems, View.OnClickListener onClickListener, Context contex) {
        this.mItems = drawerItems;
        this.onClickListener = onClickListener;
        context = contex;
    }


    public boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    @Override
    public AccionCorrectivaAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.sac_card_view, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(  ViewHolder viewHolder,   int i) {
        AccionCorrectiva carViewModel = mItems.get(i);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Date today = new Date();
        Date fecha_sac;
        viewHolder.tvCodigoCarViewSac.setText(carViewModel.getCodigo_accion_correctiva());
        if (StringUtils.hasText(carViewModel.getAccion_correctiva_detalle())) {
            viewHolder.tvDescripcionCarViewSac.setText(carViewModel.getAccion_correctiva_detalle());
        }
        if (StringUtils.hasText(carViewModel.getOrigen())) {
            viewHolder.tvOrigenCarViewSac.setText(carViewModel.getOrigen());
        }
//        if (StringUtils.hasText(carViewModel.getNombre_responsable_correccion())) {
//            viewHolder.tvPersonaReportanteCarViewSAC.setText(carViewModel.getNombre_responsable_correccion());
//        }
        if (StringUtils.hasText(carViewModel.getFecha_acordada_ejecucion())) {
            try {
                fecha_sac = dateFormat.parse(carViewModel.getFecha_acordada_ejecucion());
                long diff =( today.getTime() - fecha_sac.getTime())/ (1000 * 60 * 60 * 24);
                System.out.print(diff);
                viewHolder.tvFechaCarViewSac.setText(carViewModel.getFecha_acordada_ejecucion());
                if (Math.abs(diff) == 1){
                    viewHolder.tvDiasCarViewSac.setText(Math.abs(diff) + " día.");
                }else{
                    viewHolder.tvDiasCarViewSac.setText(Math.abs(diff) + " días.");
                }

                if (diff<1) {
                    viewHolder.tvFechaCarViewSac.setTextColor(ContextCompat.getColor(context, R.color.md_orange_400));
                    viewHolder.tvDiasCarViewSac.setBackgroundResource(R.drawable.border_sac_dias_blue);

                }else{
                    viewHolder.tvFechaCarViewSac.setTextColor(ContextCompat.getColor(context, R.color.md_red_A700));
                    viewHolder.tvDiasCarViewSac.setBackgroundResource(R.drawable.border_sac_dias_red);

                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            viewHolder.tvFechaCarViewSac.setVisibility(View.GONE);
            viewHolder.tvDiasCarViewSac.setVisibility(View.GONE);

        }

        if (carViewModel.getEstado().equals("Enviar")){
            viewHolder.checkbox.setChecked(true);
        }else{
            viewHolder.checkbox.setChecked(false);
        }
        CardView  cardView = viewHolder.itemView.findViewById(R.id.card_view);
        cardView.setTag(viewHolder);
        viewHolder.setCardViewModelSac(carViewModel);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        LinearLayout lnlyCarViewSac;
        TextView tvCodigoCarViewSac;
        TextView tvOrigenCarViewSac;
        TextView tvDescripcionCarViewSac;
        TextView tvPersonaReportanteCarViewSAC;
        TextView tvFechaCarViewSac;
        TextView tvDiasCarViewSac;
        CheckBox checkbox;

        private AccionCorrectiva cardViewModelSac;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            lnlyCarViewSac = itemView.findViewById(R.id.lnlyCarViewSac);
            tvCodigoCarViewSac = itemView.findViewById(R.id.tvCodigoCarViewSac);
            tvOrigenCarViewSac = itemView.findViewById(R.id.tvOrigenCarViewSac);
            tvDescripcionCarViewSac = itemView.findViewById(R.id.tvDescripcionCarViewSac);
            tvPersonaReportanteCarViewSAC = itemView.findViewById(R.id.tvPersonaReportanteCarViewSAC);
            tvFechaCarViewSac = itemView.findViewById(R.id.tvFechaCarViewSac);
            tvDiasCarViewSac = itemView.findViewById(R.id.tvDiasCarViewSac);
            checkbox = itemView.findViewById(R.id.checkBox);

            cardView.setOnClickListener(onClickListener);
        }

        void setCardViewModelSac(AccionCorrectiva cardViewModelSac) {
            this.cardViewModelSac = cardViewModelSac;
        }

        public AccionCorrectiva getCardViewModelSac() {
            return cardViewModelSac;
        }
    }

    public void setList(List<AccionCorrectiva> list){
        this.mItems = list;
        notifyDataSetChanged();
    }
}