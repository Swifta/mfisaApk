package com.swifta.mats.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.swifta.mats.R;
import com.swifta.mats.forms.ProcessServiceProviderActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moyinoluwa on 11/30/15.
 */
public class ServiceProviderDetailsAdapter extends RecyclerView.Adapter<ServiceProviderDetailsAdapter.ServiceViewHolder> {

    private final LayoutInflater mInflater;
    private List<ServiceProviderDetailsModel> modelList;
    private String vendorId;
    private Context context;

    public ServiceProviderDetailsAdapter(Context context, String vendorId, List<ServiceProviderDetailsModel> models) {
        mInflater = LayoutInflater.from(context);
        modelList = new ArrayList<>(models);
        this.vendorId = vendorId;
        this.context = context;
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = mInflater.inflate(R.layout.service_provider_details, parent, false);
        return new ServiceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ServiceViewHolder holder, int position) {
        final ServiceProviderDetailsModel model = modelList.get(position);
        holder.bind(model);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ProcessServiceProviderActivity.class);
                i.putExtra("type", "cable");
                i.putExtra("vendorid", vendorId);
                i.putExtra("servicename", model.getServiceName());
                i.putExtra("serviceamount", model.getServiceAmount());
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public void setModels(List<ServiceProviderDetailsModel> models) {
        modelList = new ArrayList<>(models);
    }

    public ServiceProviderDetailsModel removeItem(int position) {
        final ServiceProviderDetailsModel model = modelList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, ServiceProviderDetailsModel model) {
        modelList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ServiceProviderDetailsModel model = modelList.remove(fromPosition);
        modelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<ServiceProviderDetailsModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateMovedItems(List<ServiceProviderDetailsModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ServiceProviderDetailsModel model = newModels.get(toPosition);
            final int fromPosition = modelList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }


    private void applyAndAnimateAdditions(List<ServiceProviderDetailsModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ServiceProviderDetailsModel model = newModels.get(i);
            if (!modelList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateRemovals(List<ServiceProviderDetailsModel> newModels) {
        for (int i = modelList.size() - 1; i >= 0; i--) {
            final ServiceProviderDetailsModel model = modelList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    public class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView amount;
        private final LinearLayout layout;

        public ServiceViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.serviceName);
            amount = (TextView) view.findViewById(R.id.serviceAmount);
            layout = (LinearLayout) view.findViewById(R.id.serviceLayout);
        }

        public void bind(ServiceProviderDetailsModel model) {
            name.setText(model.getServiceName());
            amount.setText(model.getServiceAmount());
        }
    }
}
